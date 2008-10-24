/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jul 18, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.engine.services.solution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.pentaho.platform.api.engine.IActionCompleteListener;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.ICreateFeedbackParameterCallback;
import org.pentaho.platform.api.engine.IExecutionListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.repository.IRuntimeElement;
import org.pentaho.platform.api.repository.IRuntimeRepository;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityParameterProvider;
import org.pentaho.platform.engine.services.PentahoMessenger;
import org.pentaho.platform.engine.services.actionsequence.SequenceDefinition;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.engine.services.runtime.RuntimeContext;
import org.pentaho.platform.engine.services.runtime.SimpleRuntimeElement;
import org.pentaho.platform.util.JVMParameterProvider;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

public class SolutionEngine extends PentahoMessenger implements ISolutionEngine, IPentahoInitializer {

  /**
   * 
   */
  private static final long serialVersionUID = -8957434833691831700L;

  private final boolean debug = PentahoSystem.debug;

  private Map parameterProviders;

  private boolean persisted;

  private IActionCompleteListener doneListener;

  private IExecutionListener execListener;

  private IPentahoSession session;

  protected IRuntimeContext runtime = null;

  private int status;

  private boolean forcePrompt = false;

  private static final String LOG_NAME = "SOLUTION-ENGINE"; //$NON-NLS-1$

  private static final String JVM_PARAMETER_PROVIDER = "jvm"; //$NON-NLS-1$

  private static final Log logger = LogFactory.getLog(SolutionEngine.class);

  private String parameterXsl = null;

  private ICreateFeedbackParameterCallback createFeedbackParameterCallback;
  
  @Override
  public Log getLogger() {
    return SolutionEngine.logger;
  }

  public SolutionEngine() {
    status = IRuntimeContext.RUNTIME_STATUS_NOT_STARTED;
  }

  public void init(final IPentahoSession pSession) {
    parameterProviders = new HashMap();
    this.session = pSession;
    this.setParameterProvider(SolutionEngine.JVM_PARAMETER_PROVIDER, new JVMParameterProvider());
    setForcePrompt(false);
    // Provide the security parameter provider to the parameter provider map in the super class
    SecurityParameterProvider provider = new SecurityParameterProvider(pSession);
    this.setParameterProvider(SecurityParameterProvider.SCOPE_SECURITY, provider);
  }

  public void setParameterProvider(final String name, final IParameterProvider parameterProvider) {
    parameterProviders.put(name, parameterProvider);
  }

  protected Map getParameterProviders() {
    return parameterProviders;
  }

  public void setlistener(final IActionCompleteListener doneListener) {
    this.doneListener = doneListener;
  }

  public void setlistener(final IExecutionListener execListener) {
    this.execListener = execListener;
  }

  public void setSession(final IPentahoSession session) {
    this.session = session;
  }

  protected boolean checkParameters(final String solutionName, final String sequencePath, final String sequenceName,
      final String processId) {
    if (processId == null) {
      // cannot allow this
      error(Messages.getErrorString("SolutionEngine.ERROR_0001_PROCESS_NOT_SPECIFIED")); //$NON-NLS-1$
      return false;
    }
    if (solutionName == null) {
      // cannot allow this
      error(Messages.getErrorString("SolutionEngine.ERROR_0002_SOLUTION_NOT_SPECIFIED")); //$NON-NLS-1$
      return false;
    }
    if (sequencePath == null) {
      // cannot allow this
      error(Messages.getErrorString("SolutionEngine.ERROR_0003_PATH_NOT_SPECIFIED")); //$NON-NLS-1$
      return false;
    }
    if (sequenceName == null) {
      // cannot allow this
      error(Messages.getErrorString("SolutionEngine.ERROR_0004_ACTION_NOT_SPECIFIED")); //$NON-NLS-1$
      return false;
    }
    return true;

  }

  public int getStatus() {
    return status;
  }

  public IRuntimeContext getExecutionContext() {
    return runtime;
  }

  protected IPentahoSession getSession() {
    return session;
  }

  protected IRuntimeContext getRuntime() {
    return runtime;
  }

  protected void setRuntime(final IRuntimeContext runtime) {
    this.runtime = runtime;
  }

  protected void auditStart(final String solutionName, final String sequencePath, final String sequenceName,
      final String instanceId) {
    if (debug) {
      debug(Messages.getString("SolutionEngine.DEBUG_STARTING_EXECUTION", solutionName, sequencePath, sequenceName)); //$NON-NLS-1$
    }
    genLogIdFromInfo(instanceId, SolutionEngine.LOG_NAME, sequenceName);
  }

  public IRuntimeContext execute(final String solutionName, final String sequencePath, final String sequenceName,
      final String processId, final boolean async, final boolean instanceEnds, final String instanceId,
      final boolean isPersisted, final Map parameterProviderMap, final IOutputHandler outputHandler,
      final IActionCompleteListener pListener, final IPentahoUrlFactory urlFactory, final List messages) {
    applyParameterMappers(parameterProviderMap);
    return execute(solutionName, sequencePath, sequenceName, processId, async, instanceEnds, instanceId, isPersisted,
        parameterProviderMap, outputHandler, pListener, urlFactory, messages, null);
  }

  public IRuntimeContext execute(final String actionSequenceXML, final String sequenceName, final String processId,
      final boolean async, final boolean instanceEnds, final String instanceId, final boolean isPersisted,
      final Map parameterProviderMap, final IOutputHandler outputHandler, final IActionCompleteListener pListener,
      final IPentahoUrlFactory urlFactory, final List messages) {

    return execute("InMemorySolution", "", sequenceName, processId, async, instanceEnds, instanceId, //$NON-NLS-1$ //$NON-NLS-2$
        isPersisted, parameterProviderMap, outputHandler, pListener, urlFactory, messages, actionSequenceXML);
  }

  protected IRuntimeContext execute(final String solutionName, final String sequencePath, final String sequenceName,
      final String processId, final boolean async, final boolean instanceEnds, String instanceId,
      final boolean isPersisted, final Map parameterProviderMap, final IOutputHandler outputHandler,
      final IActionCompleteListener pListener, final IPentahoUrlFactory urlFactory, final List messages,
      final String actionSequenceXML) {

    this.persisted = isPersisted;
    setlistener(pListener);
    setSession(session);

    setMessages(messages);

    auditStart(solutionName, sequencePath, sequenceName, instanceId);

    if (!checkParameters(solutionName, sequencePath, sequenceName, processId)) {
      return null;
    }

    session.setProcessId(processId);
    session.setActionName(sequenceName);

    // create the runtime context object for this operation
    if (debug) {
      debug(Messages.getString("SolutionEngine.DEBUG_GETTING_RUNTIME_CONTEXT")); //$NON-NLS-1$
    }
    boolean newInstance = instanceId == null;
    IRuntimeRepository runtimeRepository = PentahoSystem.getRuntimeRepository(session);

    IRuntimeElement runtimeData;
    if (runtimeRepository == null) {
      String id = UUIDUtil.getUUIDAsString();
      runtimeData = new SimpleRuntimeElement(id, session.getId(), IParameterProvider.SCOPE_SESSION);
    } else {
      runtimeRepository.setLoggingLevel(loggingLevel);
      if (newInstance) {
        // we need to create runtime data for this execution
        try {
          runtimeData = runtimeRepository.newRuntimeElement(session.getId(), IParameterProvider.SCOPE_SESSION,
              !persisted);
        } catch (Throwable t) {
          error(Messages.getErrorString("SolutionEngine.ERROR_0008_INVALID_INSTANCE", instanceId), t); //$NON-NLS-1$
          status = IRuntimeContext.RUNTIME_STATUS_SETUP_FAIL;
          return null;
        }
      } else {
        try {
          runtimeData = runtimeRepository.loadElementById(instanceId, null);
        } catch (Throwable t) {
          error(Messages.getErrorString("SolutionEngine.ERROR_0008_INVALID_INSTANCE", instanceId), t); //$NON-NLS-1$
          status = IRuntimeContext.RUNTIME_STATUS_SETUP_FAIL;
          return null;
        }
      }
    }
    if (runtimeData == null) {
      error(Messages.getErrorString("SolutionEngine.ERROR_0008_INVALID_INSTANCE", instanceId)); //$NON-NLS-1$
      status = IRuntimeContext.RUNTIME_STATUS_SETUP_FAIL;
      return null;
    }
    createRuntime(runtimeData, solutionName, outputHandler, processId, urlFactory);
    runtime.setLoggingLevel(loggingLevel);
    instanceId = runtime.getInstanceId();
    genLogIdFromInfo(instanceId, SolutionEngine.LOG_NAME, sequenceName);

    if (newInstance) {
      // audit the creation of this against the session
      AuditHelper.audit(session.getId(), session.getName(), sequenceName, getObjectName(), processId,
          MessageTypes.INSTANCE_START, instanceId, "", 0, this); //$NON-NLS-1$
    }

    /*
     IRuntimeElement runtimeData;
     if (instanceId == null) {
     // we need to create runtime data for this execution
     try {
     runtimeRepository.setLoggingLevel(loggingLevel);
     runtimeData = runtimeRepository.newRuntimeElement(session.getId(), IParameterProvider.SCOPE_SESSION,
     !persisted);
     createRuntime(runtimeData, solutionName, outputHandler, processId, urlFactory);
     runtime.setLoggingLevel(loggingLevel);
     instanceId = runtime.getInstanceId();
     genLogIdFromInfo(instanceId, SolutionEngine.LOG_NAME, sequenceName);
     // audit the creation of this against the session
     AuditHelper.audit(session.getId(), session.getName(), sequenceName, getObjectName(), processId,
     MessageTypes.INSTANCE_START, instanceId, "", 0, this); //$NON-NLS-1$
     } catch (Throwable t) {
     error(Messages.getErrorString("SolutionEngine.ERROR_0008_INVALID_INSTANCE", instanceId), t); //$NON-NLS-1$
     status = IRuntimeContext.RUNTIME_STATUS_SETUP_FAIL;
     return null;
     }
     } else {
     try {
     runtimeRepository.setLoggingLevel(loggingLevel);
     runtimeData = runtimeRepository.loadElementById(instanceId, null);
     createRuntime(runtimeData, solutionName, outputHandler, processId, urlFactory);
     runtime.setLoggingLevel(loggingLevel);
     instanceId = runtime.getInstanceId();
     genLogIdFromInfo(instanceId, SolutionEngine.LOG_NAME, sequenceName);
     } catch (Throwable t) {
     error(Messages.getErrorString("SolutionEngine.ERROR_0008_INVALID_INSTANCE", instanceId), t); //$NON-NLS-1$
     status = IRuntimeContext.RUNTIME_STATUS_SETUP_FAIL;
     return null;
     }
     }
     */
    if (outputHandler != null) {
      outputHandler.setRuntimeContext(runtime);
    }
    return executeInternal(solutionName, sequencePath, sequenceName, processId, async, instanceEnds,
        parameterProviderMap, actionSequenceXML);
  }

  public IRuntimeContext execute(final IRuntimeContext pRuntime, final String solutionName, final String sequencePath,
      final String sequenceName, final String processId, final boolean async, final boolean instanceEnds,
      final Map parameterProviderMap, final IOutputHandler outputHandler) {
    applyParameterMappers(parameterProviderMap);

    runtime = pRuntime;
    runtime.setOutputHandler(outputHandler);
    auditStart(solutionName, sequencePath, sequenceName, runtime.getInstanceId());

    if (!checkParameters(solutionName, sequencePath, sequenceName, processId)) {
      status = IRuntimeContext.RUNTIME_STATUS_FAILURE;
      return null;
    }
    return executeInternal(solutionName, sequencePath, sequenceName, processId, async, instanceEnds,
        parameterProviderMap);

  }

  protected IRuntimeContext executeInternal(final String solutionName, final String sequencePath,
      final String sequenceName, final String processId, final boolean async, final boolean instanceEnds,
      final Map parameterProviderMap) {
    return executeInternal(solutionName, sequencePath, sequenceName, processId, async, instanceEnds,
        parameterProviderMap, null);
  }

  protected IRuntimeContext executeInternal(final String solutionName, final String sequencePath,
      final String sequenceName, final String processId, final boolean async, final boolean instanceEnds,
      final Map parameterProviderMap, final String actionSequenceXML) {

    long start = System.currentTimeMillis();

    parameterProviders.putAll(parameterProviderMap);
    parameterProviders.put(PentahoSystem.SCOPE_GLOBAL, PentahoSystem.getGlobalParameters());

    // load the solution action document
    if (debug) {
      debug(Messages.getString("SolutionEngine.DEBUG_LOADING_ACTION_DEFINITION")); //$NON-NLS-1$
    }

    IActionSequence actionSequence = null;
    if (actionSequenceXML != null) {
      actionSequence = createActionSequence(actionSequenceXML, solutionName);
    } else {
      actionSequence = createActionSequence(sequenceName, sequencePath, solutionName);
    }
    if (actionSequence == null) {
      error(Messages.getErrorString("SolutionEngine.ERROR_0007_ACTION_EXECUTION_FAILED")); //$NON-NLS-1$
      status = IRuntimeContext.RUNTIME_STATUS_FAILURE;
      long end = System.currentTimeMillis();
      AuditHelper
          .audit(
              session.getId(),
              session.getName(),
              sequenceName,
              getObjectName(),
              processId,
              MessageTypes.INSTANCE_FAILED,
              runtime.getInstanceId(),
              Messages.getErrorString("SolutionEngine.ERROR_0007_ACTION_EXECUTION_FAILED"), ((float) (end - start) / 1000), this); //$NON-NLS-1$
      return runtime;
    }

    runtime.setActionSequence(actionSequence);
    if (parameterXsl != null) {
      runtime.setParameterXsl(parameterXsl);
    }

    if (forcePrompt) {
      runtime.setPromptStatus(IRuntimeContext.PROMPT_WAITING);
    } else {
      runtime.setPromptStatus(IRuntimeContext.PROMPT_NO);
    }

    int validationStatus = runtime.validateSequence(sequenceName, execListener);
    if (validationStatus != IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK) {
      error(Messages.getErrorString("SolutionEngine.ERROR_0006_ACTION_SEQUENCE_INVALID")); //$NON-NLS-1$
      status = IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL;
      long end = System.currentTimeMillis();
      AuditHelper
          .audit(
              session.getId(),
              session.getName(),
              sequenceName,
              getObjectName(),
              processId,
              MessageTypes.INSTANCE_FAILED,
              runtime.getInstanceId(),
              Messages.getErrorString("SolutionEngine.ERROR_0006_ACTION_SEQUENCE_INVALID"), ((float) (end - start) / 1000), this); //$NON-NLS-1$
      return runtime;
    }

    try {
      int executionStatus = runtime.executeSequence(doneListener, execListener, async);
      if (executionStatus != IRuntimeContext.RUNTIME_STATUS_SUCCESS) {
        error(Messages.getErrorString("SolutionEngine.ERROR_0007_ACTION_EXECUTION_FAILED")); //$NON-NLS-1$
        status = IRuntimeContext.RUNTIME_STATUS_FAILURE;
        long end = System.currentTimeMillis();
        AuditHelper
            .audit(
                session.getId(),
                session.getName(),
                sequenceName,
                getObjectName(),
                processId,
                MessageTypes.INSTANCE_FAILED,
                runtime.getInstanceId(),
                Messages.getErrorString("SolutionEngine.ERROR_0007_ACTION_EXECUTION_FAILED"), ((float) (end - start) / 1000), this); //$NON-NLS-1$
        return runtime;
      }
    } finally {
      if (persisted) {
        // HibernateUtil.commitTransaction();
        // HibernateUtil.closeSession();
      }
    }
    // return the runtime context for the action
    if (instanceEnds) {
      long end = System.currentTimeMillis();
      AuditHelper.audit(session.getId(), session.getName(), sequenceName, getObjectName(), processId,
          MessageTypes.INSTANCE_END, runtime.getInstanceId(), "", ((float) (end - start) / 1000), this); //$NON-NLS-1$
    }
    status = runtime.getStatus();
    
    return runtime;
  }

  protected void createRuntime(final IRuntimeElement runtimeData, final String solutionName,
      final IOutputHandler outputHandler, final String processId, final IPentahoUrlFactory urlFactory) {
    runtime = new RuntimeContext(runtimeData.getInstanceId(), this, solutionName, runtimeData, session, outputHandler,
        processId, urlFactory, parameterProviders, getMessages(), createFeedbackParameterCallback);
  }

  private IActionSequence createActionSequence(final String actionName, final String actionPath,
      final String solutionName) {
    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, session);
    solutionRepository.setMessages(getMessages());
    return solutionRepository.getActionSequence(solutionName, actionPath, actionName, loggingLevel,
        ISolutionRepository.ACTION_EXECUTE);
  }

  private IActionSequence createActionSequence(final String actionDef, final String actionName) {
    try {
      Document actionSequenceDocument = XmlDom4JHelper.getDocFromString(actionDef, null);
      if (actionSequenceDocument == null) {
        return null;
      }

      IActionSequence actionSequence = SequenceDefinition.ActionSequenceFactory(actionSequenceDocument, actionName,
          "", ISolutionEngine.RUNTIME_SOLUTION_NAME, this, PentahoSystem.getApplicationContext(), loggingLevel); //$NON-NLS-1$
      return (actionSequence);
    } catch (Exception e) {

    }
    return null;
  }

  public void setForcePrompt(final boolean forcePrompt) {
    this.forcePrompt = forcePrompt;
  }

  public void setParameterXsl(final String xsl) {
    this.parameterXsl = xsl;
  }

  /**
   * FIXME: This is in dire need of fixing
   * @param parameterProviderMap
   */
  protected void applyParameterMappers(final Map parameterProviderMap) {
    IParameterProvider request = (IParameterProvider) parameterProviderMap.get(IParameterProvider.SCOPE_REQUEST);
    if (request != null) {
      IParameterProvider chartRequest = new FlashChartRequestMapper(request);
      parameterProviderMap.remove(IParameterProvider.SCOPE_REQUEST);
      parameterProviderMap.put(IParameterProvider.SCOPE_REQUEST, chartRequest);
    }
  }

  public void setCreateFeedbackParameterCallback(ICreateFeedbackParameterCallback callback) {
    this.createFeedbackParameterCallback = callback;
  } 

}
