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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
/*
 * Created on Jun 17, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.pentaho.platform.engine.services.runtime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionCompleteListener;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IComponent;
import org.pentaho.platform.api.engine.IConditionalExecution;
import org.pentaho.platform.api.engine.IExecutionListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionActionDefinition;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.InvalidParameterException;
import org.pentaho.platform.api.repository.IRuntimeElement;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.actionsequence.ActionParameter;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.engine.services.solution.SolutionReposHelper;
import org.pentaho.platform.util.logging.Logger;

/**
 * @author James Dixon
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RuntimeContext_new extends RuntimeContextBase implements IRuntimeContext {

  /**
   * 
   */
  private static final long serialVersionUID = -1179016850860938879L;

  private IRuntimeElement runtimeData;

  private String currentComponent;

  private int promptStatus = IRuntimeContext.PROMPT_NO;

  private int contentSequenceNumber; // = 0

  /*
   * public RuntimeContext( IApplicationContext applicationContext, String
   * solutionName ) { this( null, solutionName, applicationContext, null,
   * null, null, null ); }
   */
  public RuntimeContext_new(final String instanceId, final ISolutionEngine solutionEngine, final String solutionName,
      final IRuntimeElement runtimeData, final IPentahoSession session, final IOutputHandler outputHandler,
      final String processId, final IPentahoUrlFactory urlFactory, final Map parameterProviders, final List messages) {
	  
	  super( instanceId, solutionEngine, solutionName,
		      runtimeData, session, outputHandler,
		      processId, urlFactory, parameterProviders, messages );
	  
  }

  public int executeSequence(final IActionSequence sequence, final IActionCompleteListener doneListener,
	      final IExecutionListener execListener, final boolean async) {
	    String loopParamName = sequence.getLoopParameter();
	    Object loopList;
	    IActionParameter loopParm = null;

	    if (loopParamName == null) {
	      loopList = new ArrayList();
	      ((ArrayList) loopList).add(new Integer(0));
	    } else {
	      loopParm = getLoopParameter(loopParamName);
	      loopList = loopParm.getValue();

	      // If the loop list is an array, convert it to an array list for processing 
	      if (loopList instanceof Object[]) {
	        loopList = Arrays.asList((Object[]) loopList);
	      }

	    }

	    if (loopList instanceof List) {
	      int result = executeLoop(loopParm, (List) loopList, sequence, doneListener, execListener, async);
	      if (loopParm != null) {
	        addInputParameter(loopParm.getName(), loopParm); // replace the loop param in case the last loop muggled it
	      }

	      if (result != IRuntimeContext.RUNTIME_STATUS_SUCCESS) {
	        return result;
	      }
	    } else if (loopList instanceof IPentahoResultSet) {
	      int result = executeLoop(loopParm, (IPentahoResultSet) loopList, sequence, doneListener, execListener, async);
	      if (result != IRuntimeContext.RUNTIME_STATUS_SUCCESS) {
	        return result;
	      }
	    }

	    return (IRuntimeContext.RUNTIME_STATUS_SUCCESS);
	  }

	  private int executeLoop(final IActionParameter loopParm, final IPentahoResultSet loopSet,
	      final IActionSequence sequence, final IActionCompleteListener doneListener,
	      final IExecutionListener execListener, final boolean async) {

	    // execute the actions
	    int loopCount = 0;

	    // TODO handle results sets directly instead of using Properties maps

	    Object row[] = loopSet.next();
	    Object headerSet[][] = loopSet.getMetaData().getColumnHeaders();
	    // TODO handle OLAP result sets
	    Object headers[] = headerSet[0];
	    while (row != null) {

	      if (RuntimeContextBase.debug) {
	        debug(Messages.getString("RuntimeContext.DEBUG_EXECUTING_ACTION", Integer.toString(loopCount++))); //$NON-NLS-1$
	      }

	      if (execListener != null) {
	        execListener.loop(this, loopCount);
	      }
	      if (loopParm != null) {
	        IActionParameter ap;
	        for (int columnNo = 0; columnNo < headers.length; columnNo++) {
	          String name = headers[columnNo].toString();
	          Object value = row[columnNo];
	          String type = null;
	          if (value instanceof String) {
	            type = IActionParameter.TYPE_STRING;
	          } else if (value instanceof Date) {
	            type = IActionParameter.TYPE_DATE;
	          } else if ((value instanceof Long) || (value instanceof Integer)) {
	            type = IActionParameter.TYPE_INTEGER;
	          } else if ((value instanceof BigDecimal) || (value instanceof Double) || (value instanceof Float)) {
	            type = IActionParameter.TYPE_DECIMAL;
	          } else if (value instanceof String[]) {
	            type = IActionParameter.TYPE_STRING;
	          } else if (value == null) {
	            warn(Messages.getString("RuntimeContext.WARN_VARIABLE_IN_LOOP_IS_NULL", name)); //$NON-NLS-1$
	          } else {
	            type = IActionParameter.TYPE_OBJECT;
	            warn(Messages.getString(
	                "RuntimeContext.WARN_VARIABLE_IN_LOOP_NOT_RECOGNIZED", name, value.getClass().toString())); //$NON-NLS-1$
	          }
	          // TODO make sure any previous loop values are removed
	          ap = paramManager.getInput(name);
	          if (ap == null) {
	            ap = new ActionParameter(name, type, value, null, null);
	            addInputParameter(name, ap);
	          } else {
	            ap.dispose();
	            ap.setValue(value);
	          }
	        }
	      }
	      int rtn = performActions(sequence, doneListener, execListener, async);
	      if (rtn != IRuntimeContext.RUNTIME_STATUS_SUCCESS) {
	        return rtn;
	      }
	      row = loopSet.next();
	    }

	    return IRuntimeContext.RUNTIME_STATUS_SUCCESS;
	  }

	  private int executeLoop(final IActionParameter loopParm, final List loopList, final IActionSequence sequence,
	      final IActionCompleteListener doneListener, final IExecutionListener execListener, final boolean async) {

	    // execute the actions
	    int loopCount = 0;
	    for (Iterator it = loopList.iterator(); it.hasNext();) {

	      if (RuntimeContextBase.debug) {
	        debug(Messages.getString("RuntimeContext.DEBUG_EXECUTING_ACTION", Integer.toString(loopCount++))); //$NON-NLS-1$
	      }

	      if (execListener != null) {
	        execListener.loop(this, loopCount);
	      }
	      Object loopVar = it.next();
	      if (loopParm != null) {
	        IActionParameter ap;
	        if (loopVar instanceof Map) {
	          ap = new ActionParameter(loopParm.getName(), "property-map", loopVar, null, null); //$NON-NLS-1$
	        } else {
	          ap = new ActionParameter(loopParm.getName(), "string", loopVar, null, null); //$NON-NLS-1$
	        }

	        addInputParameter(loopParm.getName(), ap);
	      }

	      int rtn = performActions(sequence, doneListener, execListener, async);
	      if ((rtn != IRuntimeContext.RUNTIME_STATUS_SUCCESS) || (promptStatus == IRuntimeContext.PROMPT_NOW)) {
	        return rtn;
	      }
	    }
	    return IRuntimeContext.RUNTIME_STATUS_SUCCESS;
	  }

	  private int performActions(final IActionSequence sequence, final IActionCompleteListener doneListener,
	      final IExecutionListener execListener, final boolean async) {
	    IConditionalExecution conditional = sequence.getConditionalExecution();
	    if (conditional != null) {
	      boolean shouldExecute = false;
	      try {
	        shouldExecute = conditional.shouldExecute(paramManager.getAllParameters(), getLogger());
	      } catch (Exception ex) {
	        error(Messages.getErrorString("RuntimeContext.ERROR_0032_CONDITIONAL_EXECUTION_FAILED"), ex); //$NON-NLS-1$
	        // return the runtime so the messages are available
	        currentComponent = ""; //$NON-NLS-1$
	        return IRuntimeContext.RUNTIME_STATUS_FAILURE;
	      }
	      if (!shouldExecute) {
	        //audit(MessageTypes.ACTION_SEQUENCE_EXECUTE_CONDITIONAL, MessageTypes.NOT_EXECUTED, "", 0); //$NON-NLS-1$ //$NON-NLS-2$
	        if (RuntimeContextBase.debug) {
	          this.debug(Messages.getString("RuntimeContext.INFO_ACTION_NOT_EXECUTED")); //$NON-NLS-1$
	        }
	        return IRuntimeContext.RUNTIME_STATUS_SUCCESS;
	      }
	    }

	    List defList = sequence.getActionDefinitionsAndSequences();

	    Object listItem;
	    SolutionReposHelper.setSolutionRepositoryThreadVariable(PentahoSystem.getSolutionRepository( getSession() ));
	    for (Iterator actIt = defList.iterator(); actIt.hasNext();) {
	      listItem = actIt.next();

	      if (listItem instanceof IActionSequence) {
	        int rtn = executeSequence((IActionSequence) listItem, doneListener, execListener, async);
	        if (rtn != IRuntimeContext.RUNTIME_STATUS_SUCCESS) {
	          return (rtn);
	        }
	      } else if (listItem instanceof ISolutionActionDefinition) {
	        ISolutionActionDefinition actionDef = (ISolutionActionDefinition) listItem;
	        currentComponent = actionDef.getComponentName();
	        paramManager.setCurrentParameters(actionDef);

	        int executeResult = executeAction(actionDef, parameterProviders, doneListener, execListener, async);
	        if (executeResult != IRuntimeContext.RUNTIME_STATUS_SUCCESS) {
	          error(Messages.getErrorString("RuntimeContext.ERROR_0012_EXECUTION_FAILED", currentComponent)); //$NON-NLS-1$
	          // return the runtime so the messages are available
	          currentComponent = ""; //$NON-NLS-1$
	          return IRuntimeContext.RUNTIME_STATUS_FAILURE;
	        }
	        paramManager.addOutputParameters(actionDef);
	      }
	      if (promptStatus == IRuntimeContext.PROMPT_NOW) {
	        // promptStatus = PROMPT_NO; // turn it off - just in case  DM - Turning off was causing problems
	        return (IRuntimeContext.RUNTIME_STATUS_SUCCESS);
	      }
	      currentComponent = ""; //$NON-NLS-1$
	    }
	    return IRuntimeContext.RUNTIME_STATUS_SUCCESS;
	  }

  protected int executeAction(final ISolutionActionDefinition actionDefinition, final Map pParameterProviders,
      final IActionCompleteListener doneListener, final IExecutionListener execListener, final boolean async) {

    this.parameterProviders = pParameterProviders;
    // TODO get audit setting from action definition

    long start = new Date().getTime();
    if (isAudit()) {
      audit(MessageTypes.COMPONENT_EXECUTE_START, MessageTypes.START, "", 0); //$NON-NLS-1$
    }

    int errorLevel = IRuntimeContext.RUNTIME_CONTEXT_RESOLVE_OK;

    // resolve the parameters
    errorLevel = resolveParameters();
    if (errorLevel != IRuntimeContext.RUNTIME_CONTEXT_RESOLVE_OK) {
      error(Messages.getErrorString("RuntimeContext.ERROR_0013_BAD_PARAMETERS")); //$NON-NLS-1$
      audit(MessageTypes.COMPONENT_EXECUTE_FAILED, MessageTypes.VALIDATION, Messages
          .getErrorString("RuntimeContext.ERROR_0013_BAD_PARAMETERS"), 0); //$NON-NLS-1$
      if (doneListener != null) {
        doneListener.actionComplete(this);
      }
      setErrorLevel( errorLevel );
      return errorLevel;
    }

    if (RuntimeContext.debug) {
      debug(Messages.getString("RuntimeContext.DEBUG_PRE-EXECUTE_AUDIT")); //$NON-NLS-1$
    }
    List auditPre = actionDefinition.getPreExecuteAuditList();
    audit(auditPre);

    // resolve the output parameters
    errorLevel = resolveOutputHandler();
    if (errorLevel != IRuntimeContext.RUNTIME_CONTEXT_RESOLVE_OK) {
      audit(MessageTypes.COMPONENT_EXECUTE_FAILED, MessageTypes.VALIDATION, Messages
          .getErrorString("RuntimeContext.ERROR_0014_NO_OUTPUT_HANDLER"), 0); //$NON-NLS-1$
      if (doneListener != null) {
        doneListener.actionComplete(this);
      }
      setErrorLevel( errorLevel );
      return errorLevel;
    }

    // resolve the resources
    // Param Manager resolves them at create time

    if (async) {
      // TODO handle threading
      // create the thread if necessary
    }

    // initialize the component
    IComponent component = actionDefinition.getComponent();

    if (RuntimeContext.debug) {
      debug(Messages.getString("RuntimeContext.DEBUG_SETTING_LOGGING", Logger.getLogLevelName(loggingLevel))); //$NON-NLS-1$
    }
    component.setLoggingLevel(loggingLevel);
    if (RuntimeContext.debug) {
      debug(Messages.getString("RuntimeContext.DEBUG_INITIALIZING_COMPONENT")); //$NON-NLS-1$
    }
    errorLevel = component.init() ? IRuntimeContext.RUNTIME_STATUS_INITIALIZE_OK
        : IRuntimeContext.RUNTIME_STATUS_INITIALIZE_FAIL;
    if (errorLevel != IRuntimeContext.RUNTIME_STATUS_INITIALIZE_OK) {
      audit(MessageTypes.COMPONENT_EXECUTE_FAILED, MessageTypes.VALIDATION, Messages
          .getErrorString("RuntimeContext.ERROR_0016_COMPONENT_INITIALIZE_FAILED"), 0); //$NON-NLS-1$
      error(Messages.getErrorString("RuntimeContext.ERROR_0016_COMPONENT_INITIALIZE_FAILED")); //$NON-NLS-1$
      if (doneListener != null) {
        doneListener.actionComplete(this);
      }
      setErrorLevel( errorLevel );
      return errorLevel;
    }

    // run the component
    errorLevel = executeComponent(actionDefinition);
    if (errorLevel != IRuntimeContext.RUNTIME_STATUS_SUCCESS) {
      if (doneListener != null) {
        doneListener.actionComplete(this);
      }
      setErrorLevel( errorLevel );
      return errorLevel;
    }

    if (RuntimeContext.debug) {
      debug(Messages.getString("RuntimeContext.DEBUG_POST-EXECUTE_AUDIT")); //$NON-NLS-1$
    }
    if (isAudit()) {
        List auditPost = actionDefinition.getPostExecuteAuditList();
        audit(auditPost);
      long end = new Date().getTime();
      audit(MessageTypes.COMPONENT_EXECUTE_END, MessageTypes.END, "", (int) (end - start)); //$NON-NLS-1$
    }

    if (doneListener != null) {
      doneListener.actionComplete(this);
    }
    if (execListener != null) {
      execListener.action(this, actionDefinition);
    }
    setErrorLevel( errorLevel );
    return errorLevel;
  }

  // TODO Add to Param Manager - Need spcial case to grab loop param only from sequence inputs
  private IActionParameter getLoopParameter(final String name) {
    IActionParameter actionParameter = paramManager.getLoopParameter(name);
    if (actionParameter == null) {
      // TODO need to know from the action definition if this is ok or not
      error(Messages.getErrorString(
          "RuntimeContext.ERROR_0020_INVALID_LOOP_PARAMETER", name, actionSequence.getSequenceName())); //$NON-NLS-1$
      throw new InvalidParameterException();
    }
    return actionParameter;
  }


}
