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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.actionsequence.dom.actions.ActionFactory;
import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IActionCompleteListener;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IComponent;
import org.pentaho.platform.api.engine.IContentOutputHandler;
import org.pentaho.platform.api.engine.ICreateFeedbackParameterCallback;
import org.pentaho.platform.api.engine.IExecutionListener;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterManager;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IParameterResolver;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISelectionMapper;
import org.pentaho.platform.api.engine.ISolutionActionDefinition;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.InvalidParameterException;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.IContentRepository;
import org.pentaho.platform.api.repository.IRuntimeElement;
import org.pentaho.platform.api.repository.IRuntimeRepository;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.PentahoMessenger;
import org.pentaho.platform.engine.services.SolutionURIResolver;
import org.pentaho.platform.engine.services.actionsequence.ActionParameterSource;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceParameterMgr;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.XForm;
import org.pentaho.platform.util.xml.XmlHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

/**
 * @author James Dixon
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public abstract class RuntimeContextBase extends PentahoMessenger implements IRuntimeContext {

  /**
   * 
   */
  private static final long serialVersionUID = -1179016850860938879L;

  private IRuntimeElement runtimeData;

  // private int loggingLevel = UNKNOWN;
  private static final String LOG_NAME = "RUNTIME"; //$NON-NLS-1$

  private static final String PLUGIN_BUNDLE_NAME = "org.pentaho.platform.engine.services.runtime.plugins";//$NON-NLS-1$

  protected static String PARAMETER_FORM = "actionparam"; //$NON-NLS-1$

  private String logId;

  private IPentahoSession session;

  protected ISolutionEngine solutionEngine;

  private int errorLevel = IRuntimeContext.RUNTIME_CONTEXT_RESOLVE_OK;

  protected StringBuffer xformHeader;

  protected StringBuffer xformBody;

  protected Map xformFields;

  private static final String DEFAULT_PARAMETER_XSL = "DefaultParameterForm.xsl"; //$NON-NLS-1$

  protected String parameterXsl = RuntimeContextBase.DEFAULT_PARAMETER_XSL;

  protected String parameterTemplate = null;

  protected String parameterTarget;

  private String instanceId;

  private String processId;

  private String handle;

  private String solutionName;

  protected IPentahoUrlFactory urlFactory;

  protected Map parameterProviders;

  protected static Map componentClassMap;

  protected IActionSequence actionSequence;

  public static final boolean debug = PentahoSystem.debug;

  private boolean audit = true;

  private int status;

  protected IOutputHandler outputHandler;

  protected IParameterManager paramManager;

  private String currentComponent;

  private int promptStatus = IRuntimeContext.PROMPT_NO;

  private int contentSequenceNumber; // = 0

  // Normally shouldn't need to synchronize. But, a bug in
  // pattern compilation results in the need to synchronize
  // a small block of code. If/when this problem is fixed, we
  // can remove this synchronization lock.
  private static final byte[] PATTERN_COMPILE_LOCK = new byte[0];

  private static final Log logger = LogFactory.getLog(RuntimeContextBase.class);

  private ICreateFeedbackParameterCallback createFeedbackParameterCallback;
  
  static {
    RuntimeContextBase.getComponentClassMap();
  }

  @Override
  public Log getLogger() {
    return RuntimeContextBase.logger;
  }

  /*
   * public RuntimeContext( IApplicationContext applicationContext, String
   * solutionName ) { this( null, solutionName, applicationContext, null,
   * null, null, null ); }
   */
  public RuntimeContextBase(final String instanceId, final ISolutionEngine solutionEngine, final String solutionName,
      final IRuntimeElement runtimeData, final IPentahoSession session, final IOutputHandler outputHandler,
      final String processId, final IPentahoUrlFactory urlFactory, final Map parameterProviders, final List messages) {
    this.instanceId = instanceId;
    this.solutionEngine = solutionEngine;
    this.session = session;
    this.outputHandler = outputHandler;
    this.processId = processId;
    this.solutionName = solutionName;
    this.urlFactory = urlFactory;
    this.parameterProviders = parameterProviders;
    setMessages(messages);
    xformHeader = new StringBuffer();
    xformBody = new StringBuffer();
    xformFields = new HashMap();
    // TODO - Throw invalid parameter error if these babies are null

    this.currentComponent = ""; //$NON-NLS-1$
    status = IRuntimeContext.RUNTIME_STATUS_NOT_STARTED;

    this.runtimeData = runtimeData;
    if (runtimeData != null) {
      this.instanceId = runtimeData.getInstanceId();
    }

    handle = "context-" + this.hashCode() + "-" + new Date().getTime(); //$NON-NLS-1$ //$NON-NLS-2$

    logId = ((instanceId != null) ? instanceId : solutionName) + ":" + RuntimeContextBase.LOG_NAME + ":" + handle + " "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    paramManager = new ParameterManager();

    // Set the default XSL for parameter forms
    parameterXsl = RuntimeContextBase.DEFAULT_PARAMETER_XSL;

  }

  private IRuntimeElement createChild(boolean persisted) {
    try {
      IRuntimeRepository runtimeRepository = PentahoSystem.getRuntimeRepository(session);
      // the runtime repository is optional
      if (runtimeRepository == null) {
        return null;
      }
      runtimeRepository.setLoggingLevel(loggingLevel);
      IRuntimeElement childRuntimeData = runtimeRepository.newRuntimeElement(instanceId, "instance", !persisted); //$NON-NLS-1$
      String childInstanceId = childRuntimeData.getInstanceId();
      // audit the creation of this against the parent instance
      AuditHelper.audit(instanceId, session.getName(), getActionName(), getObjectName(), processId,
          MessageTypes.INSTANCE_START, childInstanceId, "", 0, this); //$NON-NLS-1$
      return childRuntimeData;
    } catch (Exception e) {
      error(Messages.getString("RuntimeContext.ERROR_0027_COULD_NOT_CREATE_CHILD"), e); //$NON-NLS-1$
      return null;
    }
  }

  public String createNewInstance(final boolean persisted) {
    try {
      IRuntimeElement childRuntimeData = createChild(persisted);
      if (childRuntimeData != null) {
        String childInstanceId = childRuntimeData.getInstanceId();
        return childInstanceId;
      }
    } catch (Exception e) {
      error(Messages.getString("RuntimeContext.ERROR_0027_COULD_NOT_CREATE_CHILD"), e); //$NON-NLS-1$
      return null;
    }
    return null;
  }

  public String createNewInstance(final boolean persisted, final Map parameters) {
    return createNewInstance(persisted, parameters, false);
  }

  public String createNewInstance(final boolean persisted, final Map parameters, final boolean forceImmediateWrite) {
    try {
      IRuntimeElement childRuntimeData = createChild(persisted);
      if (childRuntimeData != null) {

        if (parameters != null) {
          Iterator parameterIterator = parameters.keySet().iterator();
          while (parameterIterator.hasNext()) {
            String parameterName = (String) parameterIterator.next();
            Object parameterValue = parameters.get(parameterName);
            if (parameterValue instanceof String) {
              childRuntimeData.setStringProperty(parameterName, (String) parameterValue);
            } else if (parameterValue instanceof BigDecimal) {
              childRuntimeData.setBigDecimalProperty(parameterName, (BigDecimal) parameterValue);
            } else if (parameterValue instanceof Date) {
              childRuntimeData.setDateProperty(parameterName, (Date) parameterValue);
            } else if (parameterValue instanceof List) {
              childRuntimeData.setListProperty(parameterName, (List) parameterValue);
            } else if (parameterValue instanceof Long) {
              childRuntimeData.setLongProperty(parameterName, (Long) parameterValue);
            }
          }
        }
        String childInstanceId = childRuntimeData.getInstanceId();
        if (forceImmediateWrite) {
          childRuntimeData.forceSave();
        }
        return childInstanceId;
      }
    } catch (Exception e) {
      error(Messages.getString("RuntimeContext.ERROR_0027_COULD_NOT_CREATE_CHILD"), e); //$NON-NLS-1$
      return null;
    }
    return null;

  }

  public int getStatus() {
    return status;
  }

  public void promptNow() {
    promptStatus = IRuntimeContext.PROMPT_NOW;
  }

  /** Sets the prompt flag but continue processing Actions */
  public void promptNeeded() {
    if (promptStatus < IRuntimeContext.PROMPT_WAITING) { // Don't mask a Prompt_Now
      promptStatus = IRuntimeContext.PROMPT_WAITING;
    }
  }

  /**
   * Tells if a component is waiting for a prompt
   * 
   * @return true if a prompt is pending
   */
  public boolean isPromptPending() {
    return (promptStatus != IRuntimeContext.PROMPT_NO);
  }

  public IPentahoUrlFactory getUrlFactory() {
    return urlFactory;
  }

  public boolean feedbackAllowed() {
    return (outputHandler != null) && outputHandler.allowFeedback();
  }

  public IContentItem getFeedbackContentItem() {
    return outputHandler.getFeedbackContentItem();
  }

  private int getContentSequenceNumber() {
    return contentSequenceNumber++;
  }

  public IContentItem getOutputItem(final String outputName, final String mimeType, final String extension) {

    // TODO support content output versions in the action definition

    IActionParameter outputParameter = getOutputParameter(outputName);
    if (outputParameter == null) {
      error(Messages.getErrorString(
          "RuntimeContext.ERROR_0021_INVALID_OUTPUT_REQUEST", outputName, actionSequence.getSequenceName())); //$NON-NLS-1$
      throw new InvalidParameterException();
    }

    // If there is an output mapping, use that name to store the content
    // under.
    //        String contentName = outputName;
    //        if (currentActionDef != null) {
    //            contentName = currentActionDef.getMappedOutputName(outputName);
    //            contentName = (contentName != null) ? contentName : outputName;
    //        }

    // contentrepo : {solution}/{path}/{action}.{extension}
    int seqNum = getContentSequenceNumber();
    String contentName = "contentrepo:" + getSolutionName() + "/" + getSolutionPath() + "/" + getActionName() + ((seqNum > 0) ? Integer.toString(seqNum) : "") + extension; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    if (!IActionParameter.TYPE_CONTENT.equals(outputParameter.getType())) {
      error(Messages.getErrorString("RuntimeContext.ERROR_0023_INVALID_OUTPUT_STREAM", outputName)); //$NON-NLS-1$
      return null;
    }

    try {
      IContentOutputHandler output = PentahoSystem.getOutputDestinationFromContentRef(contentName, session);
      if (output != null) {
        // TODO get this info
        output.setActionName(getActionName());
        output.setInstanceId(instanceId);
        output.setMimeType(mimeType);
        output.setSession(session);
        output.setSolutionName(solutionName);
        output.setSolutionPath(getSolutionPath());
        IContentItem contentItem = output.getFileOutputContentItem();
        setOutputValue(outputName, contentItem);
        return contentItem;
      }
    } catch (Exception e) {

    }

    return null;

  }

  public IContentItem getOutputContentItem(final String mimeType) {
    // TODO check the sequence definition to see where this should come from
    return outputHandler.getOutputContentItem(IOutputHandler.RESPONSE, IOutputHandler.CONTENT, actionSequence
        .getTitle(), null, solutionName, instanceId, mimeType);
  }

  public IContentItem getOutputContentItem(final String outputName, final String mimeType) {

    IActionParameter parameter = (IActionParameter) actionSequence.getOutputDefinitions().get(outputName);
    if (parameter == null) {
      error(Messages.getErrorString(
          "RuntimeContext.ERROR_0021_INVALID_OUTPUT_REQUEST", outputName, actionSequence.getSequenceName())); //$NON-NLS-1$
      throw new InvalidParameterException();
    }

    List destinationsList = parameter.getVariables();
    Iterator destinationsIterator = destinationsList.iterator();
    // we can only handle one destination at the moment
    while (destinationsIterator.hasNext()) {
      ActionParameterSource destination = (ActionParameterSource) destinationsIterator.next();
      String objectName = destination.getSourceName();
      String contentName = destination.getValue();
      contentName = TemplateUtil.applyTemplate(contentName, this);
      IContentItem contentItem = outputHandler.getOutputContentItem(objectName, contentName, actionSequence.getTitle(),
          null, solutionName, instanceId, mimeType);
      return contentItem;
    }
    return null;
  }

  public String getHandle() {
    return handle;
  }

  public IPentahoSession getSession() {
    return session;
  }

  public String getSolutionName() {
    return (solutionName);
  }

  public String getSolutionPath() {
    return ((actionSequence != null) ? actionSequence.getSolutionPath() : null);
  }

  public String getCurrentComponentName() {
    if ("".equals(currentComponent)) { //$NON-NLS-1$
      return this.getClass().getName();
    }
    return currentComponent;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public int getErrorLevel() {
    return errorLevel;
  }

  public void setActionSequence(final IActionSequence sequence) {
    this.actionSequence = sequence;
    paramManager = new ParameterManager(sequence);
  }

  public int validateSequence(final String sequenceName, final IExecutionListener execListener) {
    paramManager.resetParameters();

    logId = ((instanceId != null) ? instanceId : solutionName)
        + ":" + RuntimeContextBase.LOG_NAME + ":" + handle + ":" + sequenceName + " "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    if (audit) {
      audit(MessageTypes.ACTION_SEQUENCE_START, MessageTypes.START, "", 0); //$NON-NLS-1$
    }

    if (status != IRuntimeContext.RUNTIME_STATUS_NOT_STARTED) {
      error(Messages.getErrorString("RuntimeContext.ERROR_0001_RUNTIME_RUNNING")); //$NON-NLS-1$
      return (status);
    }

    errorLevel = IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK;

    // validate action header
    errorLevel = validateHeader(sequenceName);
    if (errorLevel != IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK) {
      error(Messages.getErrorString("RuntimeContext.ERROR_0002_ACTION_NOT_VALIDATED", sequenceName)); //$NON-NLS-1$
      status = IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL;
      return errorLevel;
    }

    // TODO deep validation of sequence and action inputs and outputs

    // validate resources
    errorLevel = validateResources();
    if (errorLevel != IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK) {
      error(Messages.getErrorString("RuntimeContext.ERROR_0005_ACTION_RESOURCES_NOT_VALID", sequenceName)); //$NON-NLS-1$
      status = IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL;
      return errorLevel;
    }

    // validate component
    errorLevel = validateComponents(execListener);
    if (errorLevel != IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK) {
      error(Messages.getErrorString("RuntimeContext.ERROR_0006_ACTION_COMPONENT_NOT_VALID", sequenceName)); //$NON-NLS-1$
      status = IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL;
      return errorLevel;
    }

    status = IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK;

    return IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK;
  }

  private int validateHeader(final String sequenceName) {

    /*        if (!actionSequence.getSequenceName().equals(sequenceName)) {
     error(Messages.getErrorString("RuntimeContext.ERROR_0007_NAMES_DO_NOT_MATCH", actionSequence.getSequenceName(), sequenceName)); //$NON-NLS-1$ 
     return RUNTIME_CONTEXT_VALIDATE_FAIL;
     }*/

    // setup auditing and logging etc
    errorLevel = initFromActionSequenceDefinition();
    if (errorLevel != IRuntimeContext.RUNTIME_CONTEXT_RESOLVE_OK) {
      error(Messages.getErrorString("RuntimeContext.ERROR_0008_ACTION_INITIALIZATION_FAILED", sequenceName)); //$NON-NLS-1$
      return errorLevel;
    }

    return IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK;
  }

  private int validateResources() {

    // allResources = actionSequence.getResourceDefinitions();
    return IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK;
  }

  private int validateComponents(final IExecutionListener execListener) {
    return validateComponents(actionSequence, execListener);
  }

  private int validateComponents(final IActionSequence sequence, final IExecutionListener execListener) {

    List defList = sequence.getActionDefinitionsAndSequences();

    Object listItem;
    for (Iterator it = defList.iterator(); it.hasNext();) {
      listItem = it.next();

      if (listItem instanceof IActionSequence) {
        int rtn = validateComponents((IActionSequence) listItem, execListener);
        if (rtn != IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK) {
          return (rtn);
        }
      } else if (listItem instanceof ISolutionActionDefinition) {
        ISolutionActionDefinition actionDef = (ISolutionActionDefinition) listItem;

        if (RuntimeContextBase.debug) {
          debug(Messages.getString("RuntimeContext.DEBUG_VALIDATING_COMPONENT", actionDef.getComponentName())); //$NON-NLS-1$
        }

        IComponent component = resolveComponent(actionDef, instanceId, processId, session);
        if (component != null) {
          component.setLoggingLevel(loggingLevel);

          // allow the ActionDefinition to cache the component
          actionDef.setComponent(component);
          paramManager.setCurrentParameters(actionDef);
          // int stat = component.validate( instanceId,
          // actionSequence.getSequenceName(), processId,
          // actionDef.getComponentSection(), this, session, this,
          // loggingLevel );
          int stat = component.validate();
          if (stat != IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK) {
            return (stat);
          }
          paramManager.addOutputParameters(actionDef);
        } else {
          return IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL;
        }
        setCurrentComponent(""); //$NON-NLS-1$
        setCurrentActionDef(null);
      }
    }
    if (execListener != null) {
      execListener.validated(this);
    }
    return (IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK);
  }

  protected static Map getComponentClassMap() {
    if (RuntimeContextBase.componentClassMap == null) {
      RuntimeContextBase.componentClassMap = Collections.synchronizedMap(RuntimeContextBase.createComponentClassMap());
    }
    return RuntimeContextBase.componentClassMap;
  }

  protected static void createPluginComponentMap( Properties knownComponents ) {
	  try {
		  // see if we have any pentaho_action_plugin.xml files in the root of the classpath
		 
		  Enumeration<URL> enumer =  RuntimeContextBase.class.getClassLoader().getResources( ActionFactory.PLUGIN_XML_FILENAME );
		  
		  // we might have multiple documents
		  while( enumer.hasMoreElements() ) {
			  URL url = enumer.nextElement();
			  // make sure a failure for one resource does not affect any other ones
			  try {
				  Object obj = url.getContent();
				  if( obj instanceof InputStream ) {
					  // we have an input stream, it should give us an xml document
					  InputStream in = (InputStream) obj;
					  StringBuilder str = new StringBuilder();
					  byte buffer[] = new byte[4096];
					  int n = in.read( buffer );
					  while( n != -1 ) {
						  str.append( new String( buffer, 0, n, "UTF-8" ) );
						  n = in.read( buffer );
					  }
					  // we have the text now generate a DOM
					  Document doc = DocumentHelper.parseText( str.toString() );
					  if( doc != null ) {
						  // look for nodes 
						  List nodes = doc.selectNodes( ActionFactory.PLUGIN_ROOT_NODE+"/"+"bi-component" );
						  Iterator it = nodes.iterator();
						  while( it.hasNext() ) {
							  // make sure that one failed class will not affect any others
							  try {
								  // pull the class name from each node
								  Element node = (Element) it.next();
								  String className = node.getText();
								  String id = node.attributeValue( "id" );
								  if( id != null ) {
									  knownComponents.put( id , className );
								  }
							  } catch (Exception e) {
								  // TODO log this
								  e.printStackTrace();
							  }
						  }
					  }
				  }
			  } catch (Exception e) {
				  // TODO log this
				  e.printStackTrace();
			  }
		  }
	  } catch (Exception e) {
		  // TODO log this
		  e.printStackTrace();
	  }
  }

  
  private static Map createComponentClassMap() {
    Properties knownComponents = new Properties();
    // First, get known plugin names...
    try {
      ResourceBundle pluginBundle = ResourceBundle.getBundle(RuntimeContextBase.PLUGIN_BUNDLE_NAME);
      if (pluginBundle != null) { // Copy the bundle here...
        Enumeration keyEnum = pluginBundle.getKeys();
        String bundleKey = null;
        while (keyEnum.hasMoreElements()) {
          bundleKey = (String) keyEnum.nextElement();
          knownComponents.put(bundleKey, pluginBundle.getString(bundleKey));
        }
      }
    } catch (Exception ex) {
      RuntimeContextBase.logger.warn("Could not read plugin.properties from the runtime package.");
    }
    
    // next load from resources, this lets plugin jars get in there
    createPluginComponentMap( knownComponents );
    
    // Get overrides...
    //
    // Note - If the override wants to remove an existing "known" plugin, 
    // simply adding an empty value will cause the "known" plugin to be removed.
    //
    ISolutionRepository solutionRepository = PentahoSystem.getSolutionRepository(new StandaloneSession("system"));
    if (solutionRepository == null) {
      // this is ok
      return knownComponents;
    }
    try {
      InputStream is = solutionRepository.getResourceInputStream("system/plugin.properties", false);
      Properties overrideComponents = new Properties();
      overrideComponents.load(is);
      knownComponents.putAll(overrideComponents); // load over the top of the known properties
    } catch (FileNotFoundException ignored) {
      RuntimeContextBase.logger.warn("No override plugin properties found in the system solution");
    } catch (IOException ignored) {
      RuntimeContextBase.logger.warn("Exception reading override properties");
    }

    return knownComponents;
  }

  /*
   public static Map createComponentClassMap() {
   HashMap ccm = new HashMap();
   // map the short names
   ccm.put("ContentOutputComponent", "org.pentaho.plugin.core.ContentOutputComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("ContentRepositoryCleaner", "org.pentaho.plugin.core.ContentRepositoryCleaner"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("HelloWorldComponent", "org.pentaho.plugin.core.HelloWorldComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("ResultSetCompareComponent", "org.pentaho.plugin.core.ResultSetCompareComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("ResultSetExportComponent", "org.pentaho.plugin.core.ResultSetExportComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("ResultSetFlattenerComponent", "org.pentaho.plugin.core.ResultSetFlattenerComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("SecureFilterComponent", "org.pentaho.plugin.core.SecureFilterComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("SubActionComponent", "org.pentaho.plugin.core.SubActionComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("TemplateComponent", "org.pentaho.plugin.core.TemplateComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("BIRTReportComponent", "org.pentaho.plugin.eclipsebirt.BIRTReportComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("EmailComponent", "org.pentaho.plugin.email.EmailComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("JasperReportsComponent", "org.pentaho.plugin.jasperreports.JasperReportsComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("JavascriptRule", "org.pentaho.plugin.javascript.JavascriptRule"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("ChartComponent", "org.pentaho.plugin.jfreechart.ChartComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("JFreeReportComponent", "org.pentaho.plugin.jfreereport.JFreeReportComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("JFreeReportGeneratorComponent", "org.pentaho.plugin.jfreereport.JFreeReportGeneratorComponent");//$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("ReportWizardSpecComponent", "org.pentaho.plugin.jfreereport.ReportWizardSpecComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("KettleComponent", "org.pentaho.plugin.kettle.KettleComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("MDXDataComponent", "org.pentaho.plugin.mdx.MDXDataComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("MDXLookupRule", "org.pentaho.plugin.mdx.MDXLookupRule"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("XMLALookupRule", "org.pentaho.plugin.xmla.XMLALookupRule"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("HQLLookupRule", "org.pentaho.plugin.hql.HQLLookupRule"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("ReceiptAuditComponent", "org.pentaho.plugin.misc.ReceiptAuditComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("TestComponent", "org.pentaho.plugin.misc.TestComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("UtilityComponent", "org.pentaho.plugin.misc.UtilityComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("MondrianModelComponent", "org.pentaho.plugin.olap.MondrianModelComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("PivotViewComponent", "org.pentaho.plugin.olap.PivotViewComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("PrintComponent", "org.pentaho.plugin.print.PrintComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("JobSchedulerComponent", "org.pentaho.plugin.quartz.JobSchedulerComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("SchedulerAdminComponent", "org.pentaho.plugin.quartz.SchedulerAdminComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("SharkWorkflowComponent", "org.pentaho.plugin.shark.SharkWorkflowComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("SQLDataComponent", "org.pentaho.plugin.sql.SQLDataComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("SQLLookupRule", "org.pentaho.plugin.sql.SQLLookupRule"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("SQLExecute", "org.pentaho.plugin.sql.SQLExecute"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("SubscriptionBurstComponent", "org.pentaho.plugin.SubscriptionBurstComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("XQueryLookupRule", "org.pentaho.plugin.xquery.XQueryLookupRule"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("ResultSetCrosstabComponent", "org.pentaho.plugin.core.ResultSetCrosstabComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("MQLRelationalDataComponent", "org.pentaho.plugin.mql.MQLRelationalDataComponent"); //$NON-NLS-1$ //$NON-NLS-2$

   // map the old names
   ccm.put("org.pentaho.component.ContentOutputComponent", "!org.pentaho.plugin.core.ContentOutputComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.ContentRepositoryCleaner", "!org.pentaho.plugin.core.ContentRepositoryCleaner"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.HelloWorldComponent", "!org.pentaho.plugin.core.HelloWorldComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.ResultSetCompareComponent", "!org.pentaho.plugin.core.ResultSetCompareComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.ResultSetExportComponent", "!org.pentaho.plugin.core.ResultSetExportComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm
   .put(
   "org.pentaho.component.ResultSetFlattenerComponent", "!org.pentaho.plugin.core.ResultSetFlattenerComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.SecureFilterComponent", "!org.pentaho.plugin.core.SecureFilterComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.SubActionComponent", "!org.pentaho.plugin.core.SubActionComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.TemplateComponent", "!org.pentaho.plugin.core.TemplateComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.birt.BIRTReportComponent", "!org.pentaho.plugin.eclipsebirt.BIRTReportComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.EmailComponent", "!org.pentaho.plugin.email.EmailComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.jasper.JasperReportsComponent", "!org.pentaho.plugin.jasperreports.JasperReportsComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.JavascriptRule", "!org.pentaho.plugin.javascript.JavascriptRule"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.ChartComponent", "!org.pentaho.plugin.jfreechart.ChartComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.jfree.JFreeReportComponent", "!org.pentaho.plugin.jfreereport.JFreeReportComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.kettle.KettleComponent", "!org.pentaho.plugin.kettle.KettleComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.MDXDataComponent", "!org.pentaho.plugin.mdx.MDXDataComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.MDXLookupRule", "!org.pentaho.plugin.mdx.MDXLookupRule"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.ReceiptAuditComponent", "!org.pentaho.plugin.misc.ReceiptAuditComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.TestComponent", "!org.pentaho.plugin.misc.TestComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.UtilityComponent", "!org.pentaho.plugin.misc.UtilityComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.MondrianModelComponent", "!org.pentaho.plugin.olap.MondrianModelComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.PivotViewComponent", "!org.pentaho.plugin.olap.PivotViewComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.PrintComponent", "!org.pentaho.plugin.print.PrintComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.JobSchedulerComponent", "!org.pentaho.plugin.quartz.JobSchedulerComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.SchedulerAdminComponent", "!org.pentaho.plugin.quartz.SchedulerAdminComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.SharkWorkflowComponent", "!org.pentaho.plugin.shark.SharkWorkflowComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.SQLDataComponent", "!org.pentaho.plugin.sql.SQLDataComponent"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.SQLLookupRule", "!org.pentaho.plugin.sql.SQLLookupRule"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm.put("org.pentaho.component.XQueryLookupRule", "!org.pentaho.plugin.xquery.XQueryLookupRule"); //$NON-NLS-1$ //$NON-NLS-2$
   ccm
   .put(
   "com.pentaho.component.JFreeReportGeneratorComponent", "!org.pentaho.plugin.jfreereport.JFreeReportGeneratorComponent"); //$NON-NLS-1$ //$NON-NLS-2$

   return ccm;
   }
   */

  protected void setCurrentComponent(final String componentClassName) {
    currentComponent = componentClassName;
  }

  protected void setCurrentActionDef(final ISolutionActionDefinition actionDefinition) {
  }

  protected static String getComponentClassName(final String rawClassName, final IRuntimeContext runtime) {
    String mappedClassName = (String) RuntimeContextBase.getComponentClassMap().get(rawClassName);
    if (mappedClassName != null) {
      if (mappedClassName.charAt(0) == '!') {
        // this is deprecated, log a warning
        mappedClassName = mappedClassName.substring(1);
        runtime.warn(Messages
            .getString("RuntimeContext.WARN_DEPRECATED_COMPONENT_CLASS", rawClassName, mappedClassName)); //$NON-NLS-1$
        runtime.audit(MessageTypes.DEPRECATION_WARNING, rawClassName, mappedClassName, 0);
      }
      return mappedClassName;
    }
    return rawClassName;

  }

  protected IComponent resolveComponent(final ISolutionActionDefinition actionDefinition,
      final String currentInstanceId, final String currentProcessId, final IPentahoSession currentSession) {

    // try to create an instance of the component class specified in the
    // action document
    String componentClassName = actionDefinition.getComponentName().trim();

    String mappedClassName = (String) RuntimeContextBase.getComponentClassMap().get(componentClassName);
    if (mappedClassName != null) {
      if (mappedClassName.charAt(0) == '!') {
        // this is deprecated, log a warning
        mappedClassName = mappedClassName.substring(1);
        warn(Messages.getString("RuntimeContext.WARN_DEPRECATED_COMPONENT_CLASS", componentClassName, mappedClassName)); //$NON-NLS-1$
        audit(MessageTypes.DEPRECATION_WARNING, componentClassName, mappedClassName, 0);
      }
      componentClassName = mappedClassName;
    }

    Element componentDefinition = (Element) actionDefinition.getComponentSection();
    setCurrentComponent(componentClassName);
    setCurrentActionDef(actionDefinition);
    try {

      /*
       * String instanceId, String actionName, String processId, Node
       * componentDefinition, IRuntimeContext runtimeContext,
       * IPentahoSession sessionContext, int loggingLevel
       */

      IComponent component = null;
      Class componentClass;
      /*
       Class[] paramClasses = new Class[] { String.class, String.class, String.class, Node.class, IRuntimeContext.class, IPentahoSession.class, int.class, List.class };
       Integer logLevel = new Integer(getLoggingLevel());
       Object[] paramArgs = new Object[] { instanceId, getActionName(), processId, componentDefinition, this, session, logLevel, getMessages() };
       Constructor componentConstructor;
       componentClass = Class.forName(componentClassName);
       componentConstructor = componentClass.getConstructor(paramClasses);
       component = (IComponent) componentConstructor.newInstance(paramArgs);
       */

      componentClass = Class.forName(componentClassName);
      component = (IComponent) componentClass.newInstance();
      component.setInstanceId(currentInstanceId);
      component.setActionName(getActionName());
      component.setProcessId(currentProcessId);

      // This next conditional is used to allow components to use the new action sequence dom commons project. The ActionFactory
      // should return an object that wraps the action definition element to be processed by the component. The component can
      // then use the wrappers API to access the action definition rather than make explicit references to the dom nodes.
      if (component instanceof IParameterResolver) {
        component.setActionDefinition(ActionFactory.getActionDefinition((Element) actionDefinition.getNode(),
            new ActionSequenceParameterMgr(this, currentSession, (IParameterResolver) component)));
      } else {
        component.setActionDefinition(ActionFactory.getActionDefinition((Element) actionDefinition.getNode(),
            new ActionSequenceParameterMgr(this, currentSession)));
      }

      // create a map of the top level component definition nodes and their text
      Map<String, String> componentDefinitionMap = new HashMap<String, String>();
      List elements = componentDefinition.elements();
      Element element;
      String name;
      String value;
      for (int idx = 0; idx < elements.size(); idx++) {
        element = (Element) elements.get(idx);
        name = element.getName();
        value = element.getText();
        // see if we have a target window for the output
        if ("target".equals(name)) {
          setParameterTarget(value);
        } else {
          // see if we have a custom XSL for the parameter page, if required
          if ("xsl".equals(name)) {
            setParameterXsl(value);
          } else {
            //Proposed fix for bug BISERVER-97 by Ezequiel Cuellar
            //If the component-definition's action-definition does not have an xsl element it reuses the one already
            //set by its previous component-definition's action-definition peer. 
            //If the xsl element is not present for the component-definition then reset to the default xsl value 
            //specified in the Pentaho.xml tag "default-parameter-xsl"

            //Proposed fix for bug BISERVER-238 by Ezequiel Cuellar
            //Added a default value of DefaultParameterForm.xsl when getting the value of default-parameter-xsl
            ISystemSettings systemSettings = PentahoSystem.getSystemSettings();
            String defaultParameterXsl = systemSettings.getSystemSetting("default-parameter-xsl", null); //$NON-NLS-1$
            if ((defaultParameterXsl != null) && (defaultParameterXsl.length() > 0)) {
              setParameterXsl(defaultParameterXsl);
            }
          }

        }

        componentDefinitionMap.put(element.getName(), element.getText());
      }

      component.setComponentDefinitionMap(componentDefinitionMap);
      component.setComponentDefinition(componentDefinition);
      component.setRuntimeContext(this);
      component.setSession(currentSession);
      component.setLoggingLevel(getLoggingLevel());
      component.setMessages(getMessages());
      return component;
    } catch (Exception e) {
      error(Messages.getErrorString("RuntimeContext.ERROR_0009_COULD_NOT_CREATE_COMPONENT", componentClassName), e); //$NON-NLS-1$
    }

    // we were not successful
    return null;
  }

  public int executeSequence(final IActionCompleteListener doneListener, final IExecutionListener execListener,
      final boolean async) {
    paramManager.resetParameters();

    long start = new Date().getTime();
    if (status != IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK) {
      audit(MessageTypes.ACTION_SEQUENCE_FAILED, MessageTypes.VALIDATION, Messages
          .getErrorString("RuntimeContext.ERROR_0010_RUNTIME_DID_NOT_VALIDATE"), 0); //$NON-NLS-1$
      error(Messages.getErrorString("RuntimeContext.ERROR_0010_RUNTIME_DID_NOT_VALIDATE")); //$NON-NLS-1$
      return (status);
    }
    status = IRuntimeContext.RUNTIME_STATUS_RUNNING;

    // create an IActionDef object
    List actionDefinitions = actionSequence.getActionDefinitionsAndSequences();
    if (actionDefinitions == null) {
      audit(MessageTypes.ACTION_SEQUENCE_FAILED, MessageTypes.VALIDATION, Messages
          .getErrorString("RuntimeContext.ERROR_0011_NO_VALID_ACTIONS"), 0); //$NON-NLS-1$
      error(Messages.getErrorString("RuntimeContext.ERROR_0011_NO_VALID_ACTIONS")); //$NON-NLS-1$
      return IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL;
    }

    setLoggingLevel(loggingLevel);

    if (RuntimeContextBase.debug) {
      debug(Messages.getString("RuntimeContext.DEBUG_EXECUTING_ACTIONS")); //$NON-NLS-1$
    }

    paramManager.setCurrentParameters(null);
    errorLevel = resolveParameters();
    if (errorLevel != IRuntimeContext.RUNTIME_CONTEXT_RESOLVE_OK) {
      audit(MessageTypes.ACTION_SEQUENCE_FAILED, MessageTypes.VALIDATION, Messages
          .getErrorString("RuntimeContext.ERROR_0013_BAD_PARAMETERS"), 0); //$NON-NLS-1$
      error(Messages.getErrorString("RuntimeContext.ERROR_0013_BAD_PARAMETERS")); //$NON-NLS-1$
      return errorLevel;
    }
    if (execListener != null) {
      execListener.loaded(this);
    }
    int rtnStat = executeSequence(actionSequence, doneListener, execListener, async);

    if (this.feedbackAllowed()
        && ((promptStatus != IRuntimeContext.PROMPT_NO) || (xformBody.length() > 0) || (parameterTemplate != null))) {
      try {
        sendFeedbackForm();
      } catch (Exception e) {
        // TODO log an error
        error(Messages.getString("RuntimeContext.ERROR_0030_SEND_FEEDBACKFORM"), e); //$NON-NLS-1$
        try {
          outputHandler.getFeedbackContentItem().setMimeType("text/html"); //$NON-NLS-1$ 
          IContentItem contentItem = outputHandler.getFeedbackContentItem();
          OutputStream os = contentItem.getOutputStream(getActionName());
          if (os != null) {
            os.write(Messages
                .getString("RuntimeContext.USER_BAD_PARAMETER_PAGE").getBytes(LocaleHelper.getSystemEncoding())); //$NON-NLS-1$
          }
          contentItem.closeOutputStream();
        } catch (Throwable t) {
          return IRuntimeContext.RUNTIME_STATUS_FAILURE;
        }
      }
    }

    paramManager.setCurrentParameters(null);

    long end = new Date().getTime();
    if ((rtnStat == IRuntimeContext.RUNTIME_STATUS_SUCCESS) && audit) {
      audit(MessageTypes.ACTION_SEQUENCE_END, MessageTypes.END, "", (int) (end - start)); //$NON-NLS-1$
    } else {
      audit(MessageTypes.ACTION_SEQUENCE_FAILED, MessageTypes.EXECUTION, "", (int) (end - start)); //$NON-NLS-1$
    }

    status = rtnStat;

    if ((rtnStat == IRuntimeContext.RUNTIME_STATUS_SUCCESS) && !isPromptPending()) {
      Map returnParamMap = paramManager.getReturnParameters();

      for (Iterator it = returnParamMap.entrySet().iterator(); it.hasNext();) {
        Map.Entry mapEntry = (Map.Entry) it.next();

        String paramName = (String) mapEntry.getKey();
        ParameterManager.ReturnParameter returnParam = (ParameterManager.ReturnParameter) mapEntry.getValue();

        if (returnParam == null) {
          error(Messages.getErrorString("RuntimeContext.ERROR_0029_SAVE_PARAM_NOT_FOUND", paramName)); //$NON-NLS-1$
        } else {
          if (IParameterProvider.SCOPE_SESSION.equals(returnParam.destinationName)) {
            session.setAttribute(returnParam.destinationParameter, returnParam.value);
            if (RuntimeContextBase.debug) {
              debug(paramName + " - session - " + returnParam.destinationParameter); //$NON-NLS-1$
            }
          } else if ("response".equals(returnParam.destinationName)) { //$NON-NLS-1$
            if (outputHandler != null) {
              outputHandler.setOutput(returnParam.destinationParameter, returnParam.value);
            } else {
              info(Messages.getString("RuntimeContext.INFO_NO_OUTPUT_HANDLER")); //$NON-NLS-1$
            }
            if (RuntimeContextBase.debug) {
              debug(paramName + " - response - " + returnParam.destinationParameter); //$NON-NLS-1$
            }
          } else if (PentahoSystem.SCOPE_GLOBAL.equals(returnParam.destinationName)) {
            PentahoSystem.putInGlobalAttributesMap(returnParam.destinationParameter, returnParam.value);
            if (RuntimeContextBase.debug) {
              debug(paramName + " - global - " + returnParam.destinationParameter); //$NON-NLS-1$
            }
          } else { // Unrecognized scope
            warn(Messages
                .getString(
                    "RuntimeContext.WARN_UNRECOGNIZED_SCOPE", returnParam.destinationName, returnParam.destinationParameter)); //$NON-NLS-1$
          }
        }
      }
    }

    // return the status for the action
    return (rtnStat);
  }

  public void setPromptStatus(final int status) {
    promptStatus = status;
  }

  protected abstract int executeSequence(final IActionSequence sequence, final IActionCompleteListener doneListener,
      final IExecutionListener execListener, final boolean async);

  protected abstract int executeAction(final ISolutionActionDefinition actionDefinition, final Map pParameterProviders,
      final IActionCompleteListener doneListener, final IExecutionListener execListener, final boolean async);

  private int initFromActionSequenceDefinition() {

    // TODO get audit setting from action sequence

    int actionLogLevel = actionSequence.getLoggingLevel();
    int instanceLogLevel = runtimeData.getLoggingLevel();
    int actionSequenceLoggingLevel = (instanceLogLevel != ILogger.UNKNOWN) ? instanceLogLevel
        : ((actionLogLevel != ILogger.UNKNOWN) ? actionLogLevel : solutionEngine.getLoggingLevel());

    setLoggingLevel(actionSequenceLoggingLevel);

    return IRuntimeContext.RUNTIME_CONTEXT_RESOLVE_OK;
  }

  protected int executeComponent(final ISolutionActionDefinition actionDefinition) {
	    if (RuntimeContext.debug) {
	      debug(Messages.getString("RuntimeContext.DEBUG_STARTING_COMPONENT_EXECUTE")); //$NON-NLS-1$
	    }
	    int executeStatus = IRuntimeContext.RUNTIME_STATUS_FAILURE;
	    try {
	      executeStatus = actionDefinition.getComponent().execute();
	      actionDefinition.getComponent().done();
	    } catch (Exception e) {
	      audit(MessageTypes.COMPONENT_EXECUTE_FAILED, MessageTypes.FAILED, e.getLocalizedMessage(), 0);
	      error(Messages.getErrorString("RuntimeContext.ERROR_0017_COMPONENT_EXECUTE_FAILED"), e); //$NON-NLS-1$
	    }
	    if (RuntimeContext.debug) {
	      debug(Messages.getString("RuntimeContext.DEBUG_FINISHED_COMPONENT_EXECUTE")); //$NON-NLS-1$
	    }
	    return executeStatus;
	  }

  protected int resolveParameters() {

    Set inputNames = getInputNames();
    Iterator inputNamesIterator = inputNames.iterator();
    IActionParameter actionParameter;
    List variables;
    Iterator variablesIterator;
    ActionParameterSource variable;
    String sourceName;
    String sourceValue;
    Object variableValue = null;
    IParameterProvider parameterProvider;
    while (inputNamesIterator.hasNext()) {
      variableValue = null;

      String inputName = (String) inputNamesIterator.next();
      actionParameter = paramManager.getCurrentInput(inputName);
      if (actionParameter == null) {
        error(Messages.getErrorString("RuntimeContext.ERROR_0031_INPUT_NOT_FOUND", inputName)); //$NON-NLS-1$
        return IRuntimeContext.RUNTIME_CONTEXT_RESOLVE_FAIL;
      }

      variables = actionParameter.getVariables();
      variablesIterator = variables.iterator();
      while (variablesIterator.hasNext()) {
        variable = (ActionParameterSource) variablesIterator.next();
        sourceName = variable.getSourceName();
        sourceValue = variable.getValue();
        variableValue = null;
        // TODO support accessing the ancestors of the current instance,
        // e.g. runtme.parent
        if ("runtime".equals(sourceName)) { //$NON-NLS-1$
          // first check the standard variables
          variableValue = getStringParameter(sourceValue, null);
          if (variableValue == null) {
            // now check the runtime data
            variableValue = runtimeData.getStringProperty(sourceValue, null);
          }
          if (variableValue != null) {
            break;
          }
        } else {
          parameterProvider = (IParameterProvider) parameterProviders.get(sourceName);
          if (parameterProvider == null) {
            warn(Messages.getString(
                "RuntimeContext.WARN_REQUESTED_PARAMETER_SOURCE_NOT_AVAILABLE", sourceName, inputName)); //$NON-NLS-1$
          } else {
            variableValue = parameterProvider.getParameter(sourceValue);
            // variableValue = parameterProvider.getStringParameter(
            // sourceValue, null );
            if (variableValue != null) {
              break;
            }
          }
        }
      } //while

      if (variableValue == null) {

        if (actionParameter.getValue() != null) {
          if (actionParameter.hasDefaultValue()) {
            if (PentahoSystem.trace) {
              trace(Messages.getString("RuntimeContext.TRACE_USING_DEFAULT_PARAMETER_VALUE", inputName)); //$NON-NLS-1$
            }
          } else {
            if (PentahoSystem.trace) {
              trace(Messages.getString("RuntimeContext.TRACE_INFO_USING_CURRENT_PARAMETER_VALUE" + inputName)); //$NON-NLS-1$
            }
          }
        } else if ("content".equals(actionParameter.getType())) { //$NON-NLS-1$
          // store a dummy value in the map
          variableValue = ""; //$NON-NLS-1$
        } else {
          error(Messages.getErrorString("RuntimeContext.ERROR_0018_PARAMETER_NOT_FULFILLED", inputName)); //$NON-NLS-1$
          return IRuntimeContext.RUNTIME_CONTEXT_RESOLVE_FAIL;
        }
      } else {
        actionParameter.setValue(variableValue);
      }
    } // while

    return IRuntimeContext.RUNTIME_CONTEXT_RESOLVE_OK;
  }

  public void dispose() {
    paramManager.dispose();
  }

  public void dispose(final List actionParameters) {
    paramManager.dispose(actionParameters);
  }

  protected int resolveOutputHandler() {

    // TODO
    return IRuntimeContext.RUNTIME_CONTEXT_RESOLVE_OK;
  }

  // IParameterProvider methods
  public String getStringParameter(final String name, final String defaultValue) {
    if ("instance-id".equals(name)) { //$NON-NLS-1$
      return instanceId;
    } else if ("solution-id".equals(name)) { //$NON-NLS-1$
      return solutionName;
    }
    return defaultValue;
    // return runtimeData.getStringProperty( name, defaultValue );
  }

  // IRuntimeContext input and output methods

  public Object getInputParameterValue(final String name) {
    IActionParameter actionParameter = paramManager.getCurrentInput(name);
    if (actionParameter == null) {
      // TODO need to know from the action definition if this is ok or not
      error(Messages.getErrorString(
          "RuntimeContext.ERROR_0019_INVALID_INPUT_REQUEST", name, actionSequence.getSequenceName())); //$NON-NLS-1$
      throw new InvalidParameterException();
    }
    return actionParameter.getValue();
  }

  public String getInputParameterStringValue(final String name) {
    IActionParameter actionParameter = paramManager.getCurrentInput(name);
    if (actionParameter == null) {
      // TODO need to know from the action definition if this is ok or not
      error(Messages.getErrorString(
          "RuntimeContext.ERROR_0019_INVALID_INPUT_REQUEST", name, actionSequence.getSequenceName())); //$NON-NLS-1$
      throw new InvalidParameterException();
    }
    return actionParameter.getStringValue();
  }

  public IActionParameter getInputParameter(final String name) {
    IActionParameter actionParameter = paramManager.getCurrentInput(name);
    if (actionParameter == null) {
      // TODO need to know from the action definition if this is ok or not
      error(Messages.getErrorString(
          "RuntimeContext.ERROR_0019_INVALID_INPUT_REQUEST", name, actionSequence.getSequenceName())); //$NON-NLS-1$
      throw new InvalidParameterException();
    }
    return actionParameter;
  }

  public IActionParameter getOutputParameter(final String name) {
    IActionParameter actionParameter = paramManager.getCurrentOutput(name);
    if (actionParameter == null) {
      // TODO need to know from the action definition if this is ok or not
      error(Messages.getErrorString(
          "RuntimeContext.ERROR_0021_INVALID_OUTPUT_REQUEST", name, actionSequence.getSequenceName())); //$NON-NLS-1$
      throw new InvalidParameterException();
    }
    return actionParameter;
  }

  public IActionSequenceResource getResourceDefintion(final String name) {
    IActionSequenceResource actionResource = paramManager.getCurrentResource(name);

    if (actionResource == null) {
      // TODO need to know from the action definition if this is ok or not
      error(Messages.getErrorString(
          "RuntimeContext.ERROR_0022_INVALID_RESOURCE_REQUEST", name, actionSequence.getSequenceName())); //$NON-NLS-1$
      throw new InvalidParameterException();
    }
    return actionResource;
  }

  public Set getInputNames() {
    return paramManager.getCurrentInputNames();
  }

  public void addTempParameter(final String name, final IActionParameter param) {
    paramManager.addToCurrentInputs(name, param);
  }

  /*
   public IContentItem getOutputItem_old(String outputName, String mimeType, String extension) {

   // TODO support content output versions in the action definition

   IActionParameter outputParameter = getOutputParameter(outputName);
   if (outputParameter == null) {
   error(Messages.getErrorString(
   "RuntimeContext.ERROR_0021_INVALID_OUTPUT_REQUEST", outputName, actionSequence.getSequenceName())); //$NON-NLS-1$
   throw new InvalidParameterException();
   }

   // If there is an output mapping, use that name to store the content
   // under.
   String contentName = outputName;
   if (currentActionDef != null) {
   contentName = currentActionDef.getMappedOutputName(outputName);
   contentName = (contentName != null) ? contentName : outputName;
   }

   if (!IActionParameter.TYPE_CONTENT.equals(outputParameter.getType())) {
   error(Messages.getErrorString("RuntimeContext.ERROR_0023_INVALID_OUTPUT_STREAM", outputName)); //$NON-NLS-1$
   return null;
   }

   if (this.outputHandler instanceof ContentRepositoryOutputHandler) {
   // Handling for outputting to the content repository already provided
   // by the ContentRepositoryOutputHander. If it's the current output
   // handler, use it.
   IContentItem outputItem = this.outputHandler.getOutputContentItem(null, contentName, actionSequence.getTitle(),
   null, solutionName, instanceId, mimeType);
   outputItem.setMimeType(mimeType);
   try {
   setOutputValue(outputName, outputItem);
   return outputItem;
   } catch (Exception e) {

   }
   return null;
   }

   // get an output stream to hand to the caller
   IContentRepository contentRepository = PentahoSystem.getContentRepository(session);
   if (contentRepository == null) {
   error(Messages.getErrorString("RuntimeContext.ERROR_0024_NO_CONTENT_REPOSITORY")); //$NON-NLS-1$
   return null;
   }
   String extensionFolder = extension;
   if (extensionFolder.startsWith(".")) { //$NON-NLS-1$
   extensionFolder = extensionFolder.substring(1);
   }
   String outputFolder = actionSequence.getSequenceName().substring(0,
   actionSequence.getSequenceName().lastIndexOf('.'));
   String contentPath = getSolutionName()
   + "/" + getSolutionPath() + "/" + outputFolder + "/" + contentName + "/" + extensionFolder; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
   // Find the location if it's already there.
   IContentLocation contentLocation = null;
   try {
   contentLocation = contentRepository.getContentLocationByPath(contentPath);
   } catch (Exception ex) {
   // ignored
   }
   if (contentLocation == null) {
   contentLocation = contentRepository.newContentLocation(contentPath, contentName, contentName, getSolutionName(),
   true);
   }
   if (contentLocation == null) {
   error(Messages.getErrorString("RuntimeContext.ERROR_0025_INVALID_CONTENT_LOCATION")); //$NON-NLS-1$
   return null;
   }
   // TODO support content expiration

   // TODO make the write mode based on the output definition

   // Get the content item from the location - if it's there.
   IContentItem contentItem = null;
   try {
   contentItem = contentLocation.getContentItemByName(instanceId);
   } catch (Exception ex) {
   // Ignored
   }
   if (contentItem == null) { // DM - Need to keep versions so each report
   // in a burst gets saved
   contentItem = contentLocation.newContentItem(instanceId, contentName, extension, mimeType, null,
   IContentItem.WRITEMODE_KEEPVERSIONS);
   }

   try {
   setOutputValue(outputName, contentItem);
   return contentItem;
   } catch (Exception e) {

   }
   return null;

   }
   */
  public void setOutputValue(final String name, final Object output) {
    IActionParameter actionParameter = paramManager.getCurrentOutput(name);
    if (actionParameter == null) {
      // TODO need to know from the action definition if this is ok or not
      error(Messages.getErrorString(
          "RuntimeContext.ERROR_0021_INVALID_OUTPUT_REQUEST", name, actionSequence.getSequenceName())); //$NON-NLS-1$
      throw new InvalidParameterException();
    }
    actionParameter.setValue(output);

    if (output instanceof String) {
      runtimeData.setStringProperty(name, (String) output);
    } else if (output instanceof Date) {
      runtimeData.setDateProperty(name, (Date) output);
    } else if (output instanceof Long) {
      runtimeData.setLongProperty(name, (Long) output);
    } else if (output instanceof List) {
      runtimeData.setListProperty(name, (List) output);
    } else if (output instanceof Map) {
      runtimeData.setMapProperty(name, (Map) output);
    } else if (output instanceof IContentItem) {
      runtimeData.setStringProperty(name, ((IContentItem) output).getPath());
    }

  }

  public IPentahoStreamSource getDataSource(final String parameterName) {

    // TODO Temp workaround for content repos bug
    IActionParameter actionParameter = paramManager.getCurrentInput(parameterName);
    if (actionParameter == null) {
      error(Messages.getErrorString(
          "RuntimeContext.ERROR_0019_INVALID_INPUT_REQUEST", parameterName, actionSequence.getSequenceName())); //$NON-NLS-1$
      throw new InvalidParameterException();
    }

    Object locObj = actionParameter.getValue();
    if (locObj instanceof IContentItem) { // At this point we have an IContentItem so why do anything else?
      return ((IContentItem) locObj).getDataSource();
    }

    String location = locObj.toString();

    // get an output stream to hand to the caller
    IContentRepository contentRepository = PentahoSystem.getContentRepository(session);
    if (contentRepository == null) {
      error(Messages.getErrorString("RuntimeContext.ERROR_0024_NO_CONTENT_REPOSITORY")); //$NON-NLS-1$
      return null;
    }

    IContentItem contentItem = contentRepository.getContentItemByPath(location);
    if (contentItem == null) {
      return null;
    }

    return contentItem.getDataSource();
  }

  public String getContentUrl(final IContentItem contentItem) {
    if (contentItem == null) {
      return (null);
    }
    String url = PentahoSystem.getApplicationContext().getBaseUrl();
    return (url + "GetContent?id=" + contentItem.getId()); //$NON-NLS-1$
  }

  public InputStream getInputStream(final String parameterName) {

    IActionParameter inputParameter = getInputParameter(parameterName);

    if (inputParameter == null) {
      error(Messages.getErrorString(
          "RuntimeContext.ERROR_0019_INVALID_INPUT_REQUEST", parameterName, actionSequence.getSequenceName())); //$NON-NLS-1$
      throw new InvalidParameterException();
    }
    Object value = inputParameter.getValue();
    if (value instanceof IContentItem) {
      IContentItem contentItem = (IContentItem) value;
      return contentItem.getInputStream();
    } else {
      return null;
    }

    /*
     * // get an output stream to hand to the caller IContentRepository
     * contentRepository = PentahoSystem.getContentRepository( session );
     * if( contentRepository == null ) { error(
     * Messages.getErrorString("RuntimeContext.ERROR_0024_NO_CONTENT_REPOSITORY") );
     * //$NON-NLS-1$ return null; } String outputFolder =
     * actionSequence.getSequenceName().substring( 0,
     * actionSequence.getSequenceName().lastIndexOf('.') ); String location =
     * getSolutionName()+"/"+getSolutionPath()+"/"+outputFolder+"/"+parameterName+"/"+instanceId;
     * //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ IContentItem
     * contentItem = contentRepository.getContentItemByPath( location );
     * 
     * if( contentItem == null ) { return null; }
     * 
     * return contentItem.getInputStream();
     */
  }

  public Set getOutputNames() {
    return paramManager.getCurrentOutputNames();
  }

  public Set getResourceNames() {
    return paramManager.getCurrentResourceNames();
  }

  public InputStream getResourceInputStream(final IActionSequenceResource actionResource) throws FileNotFoundException {
    if (isEmbeddedResource(actionResource)) {
      return (new ByteArrayInputStream(getEmbeddedResource(actionResource).getBytes()));
    }
    return PentahoSystem.getSolutionRepository(session).getResourceInputStream(actionResource, true);
  }

  public Reader getResourceReader(final IActionSequenceResource actionResource) throws IOException {
    if (isEmbeddedResource(actionResource)) {
      String s = actionResource.getAddress();
      if (s == null) {
        s = ""; //$NON-NLS-1$
      }
      return (new InputStreamReader(new ByteArrayInputStream(getEmbeddedResource(actionResource).getBytes())));
    }
    return PentahoSystem.getSolutionRepository(session).getResourceReader(actionResource);
  }

  public String getResourceAsString(final IActionSequenceResource actionResource) throws IOException {
    if (isEmbeddedResource(actionResource)) {
      return (getEmbeddedResource(actionResource));
    }
    return PentahoSystem.getSolutionRepository(session).getResourceAsString(actionResource);
  }

  public Document getResourceAsDocument(final IActionSequenceResource actionResource) throws IOException {
    if (isEmbeddedResource(actionResource)) {
      try {
        return XmlDom4JHelper.getDocFromString(getEmbeddedResource(actionResource), null);  
      } catch(XmlParseException e) {
        error(Messages.getString("RuntimeContext.ERROR_UNABLE_TO_GET_RESOURCE_AS_DOCUMENT"), e); //$NON-NLS-1$
        return null;
      }
    }
    return PentahoSystem.getSolutionRepository(session).getResourceAsDocument(actionResource);
  }

  public IPentahoStreamSource getResourceDataSource(final IActionSequenceResource actionResource)
      throws FileNotFoundException {
    //TODO Provide a datasource wrapper for string and xml
    return PentahoSystem.getSolutionRepository(session).getResourceDataSource(actionResource);
  }

  private boolean isEmbeddedResource(final IActionSequenceResource actionResource) {
    int type = actionResource.getSourceType();
    return ((type == IActionSequenceResource.STRING) || (type == IActionSequenceResource.XML));
  }

  private String getEmbeddedResource(final IActionSequenceResource actionResource) {
    String s = actionResource.getAddress();
    return ((s == null) ? "" : s); //$NON-NLS-1$
  }

  // IAuditable methods

  public String getId() {
    return handle;
  }

  public String getProcessId() {
    return processId;
  }

  public String getActionName() {
    return ((actionSequence != null) ? actionSequence.getSequenceName() : Messages
        .getString("RuntimeContext.DEBUG_NO_ACTION")); //$NON-NLS-1$
  }

  public String getActionTitle() {
    return ((actionSequence != null) ? actionSequence.getTitle() : Messages.getString("RuntimeContext.DEBUG_NO_ACTION")); //$NON-NLS-1$
  }

  // Audit methods

  public void audit(final List auditList) {

    if ((auditList == null) || (auditList.size() == 0)) {
      return;
    }

    // TODO pass in a list of parameter objects instead of parameter names
    Iterator it = auditList.iterator();
    while (it.hasNext()) {
      Element auditNode = (Element) it.next();
      String name = auditNode.getText();
      String value = getStringParameter(name, ""); //$NON-NLS-1$
      AuditHelper.audit(this, session, MessageTypes.INSTANCE_ATTRIBUTE, name, value, 0, this);
    }

  }

  public void audit(final String messageType, final String message, final String value, final long duration) {
    if (!audit) {
      return;
    }

    if (RuntimeContextBase.debug) {
      debug(Messages.getString("RuntimeContext.DEBUG_AUDIT", instanceId, getCurrentComponentName(), messageType)); //$NON-NLS-1$
    }
    AuditHelper.audit(this, session, messageType, message, value, (float) duration / 1000, this);
  }

  public void addInputParameter(final String name, final IActionParameter param) {
    paramManager.addToAllInputs(name, param);
  }

  public String applyInputsToFormat(final String format) {
    return TemplateUtil.applyTemplate(format, this);
  }

  public String applyInputsToFormat(final String format, final IParameterResolver resolver) {
    return TemplateUtil.applyTemplate(format, this, resolver);
  }

  // Feebdack form handling

  public void sendFeedbackForm() throws IOException {
    try {
      if (!feedbackAllowed()) {
        return;
      }
      // add the standard parameters that we need
      createFeedbackParameter("solution", "solution", "", getSolutionName(), false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      createFeedbackParameter("action", "action", "", getActionName(), false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      createFeedbackParameter("path", "path", "", getSolutionPath(), false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      // ProSolutionEngine proSolutionEngine = (ProSolutionEngine) solutionEngine;
      IParameterProvider parameterProvider = (IParameterProvider) parameterProviders.get("PRO_EDIT_SUBSCRIPTION"); //$NON-NLS-1$
      String editId = null;
      if (parameterProvider == null) { // Then we are not editing subscriptions
        parameterProvider = (IParameterProvider) parameterProviders.get(IParameterProvider.SCOPE_REQUEST);
      } else {
        editId = parameterProvider.getStringParameter("subscribe-id", null); //$NON-NLS-1$
      }
      Iterator parameterNameIterator = parameterProvider.getParameterNames();
      while (parameterNameIterator.hasNext()) {
        String name = (String) parameterNameIterator.next();
        if (!"solution".equals(name) && !"action".equals(name) && !"path".equals(name) && (xformFields.get(name) == null)) {//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$                   
          // TODO we need to check to see if this has been handled as
          // a control before adding a hidden field
          Object value = parameterProvider.getParameter(name);
          if (value != null) {
            createFeedbackParameter(name, name, "", value, false); //$NON-NLS-1$
          }
        }
      }
      SolutionURIResolver resolver = new SolutionURIResolver(getSession());
      if (parameterXsl == null) {
        // Generate XForm for the parameters needed, transform into
        // HTML, and float it down the feedback stream
        xformBody.append("<tr><td>"); //$NON-NLS-1$
        XForm.createXFormSubmit(RuntimeContextBase.PARAMETER_FORM, xformBody, Messages
            .getString("RuntimeContext.USER_PARAMETER_FORM_SUBMIT")); //$NON-NLS-1$
        xformBody.append("</td></tr></table></body>"); //$NON-NLS-1$
        String html = XForm.completeXForm(XForm.OUTPUT_HTML_PAGE, RuntimeContextBase.PARAMETER_FORM, xformHeader,
            xformBody, getSession(), resolver);
        if (RuntimeContextBase.debug) {
          debug(Messages.getString("RuntimeContext.DEBUG_PARAMETER_HTML", html)); //$NON-NLS-1$
        }
        outputHandler.getFeedbackContentItem().setMimeType("text/html"); //$NON-NLS-1$ 
        OutputStream os = outputHandler.getFeedbackContentItem().getOutputStream(getActionName());
        os.write(html.getBytes());
      } else if (parameterTemplate != null) {
        String html = XForm.completeXForm(XForm.OUTPUT_HTML_PAGE, RuntimeContextBase.PARAMETER_FORM, xformHeader,
            new StringBuffer(parameterTemplate), getSession(), resolver);
        if (RuntimeContextBase.debug) {
          debug(Messages.getString("RuntimeContext.DEBUG_PARAMETER_HTML", html)); //$NON-NLS-1$
        }
        IContentItem contentItem = outputHandler.getFeedbackContentItem();
        contentItem.setMimeType("text/html"); //$NON-NLS-1$ 
        OutputStream os = contentItem.getOutputStream(getActionName());
        os.write(html.getBytes(LocaleHelper.getSystemEncoding()));
        contentItem.closeOutputStream();
      } else if (parameterXsl.endsWith(".xsl")) { //$NON-NLS-1$
        String id = actionSequence.getSequenceName();
        int pos = id.indexOf('.');
        if (pos > -1) {
          id = id.substring(0, pos);
        }
        // make sure the id can form a valid javascript variable or
        // function name
        id = id.replace('-', '_');
        id = id.replace(' ', '_');
        String actionUrl = urlFactory.getActionUrlBuilder().getUrl();
        String displayUrl = urlFactory.getDisplayUrlBuilder().getUrl();
        // String target = (parameterTarget == null) ? "" : parameterTarget; //$NON-NLS-1$
        XForm.completeXFormHeader(RuntimeContextBase.PARAMETER_FORM, xformHeader);
        Document document = XmlDom4JHelper
            .getDocFromString(
                "<?xml version=\"1.0\" encoding=\"" + LocaleHelper.getSystemEncoding() + "\" ?><filters xmlns:xf=\"http://www.w3.org/2002/xforms\">" + //$NON-NLS-1$ //$NON-NLS-2$
                    xformHeader + "<id><![CDATA[" + //$NON-NLS-1$
                    id + "]]></id><title><![CDATA[" + //$NON-NLS-1$
                    Messages.getEncodedString(actionSequence.getTitle()) + "]]></title><description><![CDATA[" + //$NON-NLS-1$
                    Messages.getEncodedString(actionSequence.getDescription()) + "]]></description><icon><![CDATA[" + //$NON-NLS-1$
                    actionSequence.getIcon() + "]]></icon><help><![CDATA[" + //$NON-NLS-1$
                    Messages.getEncodedString(actionSequence.getHelp()) + "]]></help>" + //$NON-NLS-1$
                    "<action><![CDATA[" + actionUrl + "]]></action>" + //$NON-NLS-1$ //$NON-NLS-2$
                    "<display><![CDATA[" + displayUrl + "]]></display>" + //$NON-NLS-1$ //$NON-NLS-2$
                    ((parameterTarget != null) ? "<target>" + parameterTarget + "</target>" : "") + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    xformBody.toString() + "</filters>", null); //$NON-NLS-1$ 
        // add any subscription information here
        Element root = document.getRootElement();
        Element subscriptionsNode = root.addElement("subscriptions"); //$NON-NLS-1$
        Element schedulesNode = root.addElement("schedules"); //$NON-NLS-1$

        /*
         * FIX FOR BISERVER-238 Ezequiel Cuellar:
         * Add "doSubscribe" attribute to the "subscriptions" element.
         * The "doSubscribe" attribute will define what logic should be performed
         * by the DefaultParameterForm.xsl 
         * */
        boolean isSubscription = (parameterProvider.getStringParameter("subscribepage", "no").equalsIgnoreCase("yes")) || (editId != null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        subscriptionsNode.addAttribute("doSubscribe", isSubscription ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        if (getSession().getName() == null) {
          subscriptionsNode.addAttribute("valid-session", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
          subscriptionsNode.addAttribute("valid-session", "true"); //$NON-NLS-1$//$NON-NLS-2$
          String contentId = getSolutionName() + "/" + getSolutionPath() + "/" + getActionName(); //$NON-NLS-1$//$NON-NLS-2$
          ISubscriptionRepository subscriptionRepository = PentahoSystem.getSubscriptionRepository(getSession());
          if (subscriptionRepository != null) {
            try {
              subscriptionRepository.addSubscriptionsToDocument(getSession().getName(), contentId, subscriptionsNode,
                  editId, getSession());
              subscriptionRepository.addSchedulesToDocument(getSession().getName(), contentId, schedulesNode, editId);
            } catch (Throwable t) {
              error(Messages.getErrorString("PRO_SUBSCRIPTREP.ERROR_0005_GENERAL_ERROR"), t); //$NON-NLS-1$
            }
          }
          // TODO...
        }
        Map parameters = new HashMap();
        parameters.put("baseUrl", PentahoSystem.getApplicationContext().getBaseUrl()); //$NON-NLS-1$
        parameters.put("actionUrl", this.getUrlFactory().getActionUrlBuilder().getUrl()); //$NON-NLS-1$
        parameters.put("displayUrl", this.getUrlFactory().getDisplayUrlBuilder().getUrl()); //$NON-NLS-1$
        // Uncomment this line for troubleshooting the XSL.
        // System .out.println( document.asXML() );
        StringBuffer content = XmlHelper.transformXml(parameterXsl, getSolutionName() + File.separator
            + getSolutionPath(), document.asXML(), parameters, resolver);

        IContentItem contentItem = outputHandler.getFeedbackContentItem();
        contentItem.setMimeType("text/html"); //$NON-NLS-1$ 
        OutputStream os = contentItem.getOutputStream(getActionName());
        os.write(content.toString().getBytes(LocaleHelper.getSystemEncoding()));
        contentItem.closeOutputStream();
      }
    } catch (Exception e) {
      throw new IOException(
          Messages.getErrorString("RuntimeContext.ERROR_0030_SEND_FEEDBACKFORM") + e.getLocalizedMessage()); //$NON-NLS-1$
    }
  }

  private void addXFormHeader() {

    XForm.createXFormHeader(RuntimeContextBase.PARAMETER_FORM, xformHeader);

    IActionSequenceResource resource = paramManager.getCurrentResource(parameterXsl);

    if (!parameterXsl.endsWith(".xsl") && (resource != null)) { //$NON-NLS-1$
      // load the parameter page template
      try {
        parameterTemplate = getResourceAsString(resource);
      } catch (Exception e) {
        // TODO log this
      }
    }

  }

  /**
   * @deprecated
   * Unused
   */
  @Deprecated
  public void createFeedbackParameter(final IActionParameter actionParam) {
    if (actionParam.hasSelections()) {
      // TODO support display styles
      // TODO support help hints
      createFeedbackParameter(actionParam.getName(), actionParam.getSelectionDisplayName(),
          "", actionParam.getStringValue(), actionParam.getSelectionValues(), actionParam.getSelectionNameMap(), null); //$NON-NLS-1$
    }
  }

  public void createFeedbackParameter(final ISelectionMapper selMap, final String fieldName, final Object defaultValues) {
    createFeedbackParameter(selMap, fieldName, defaultValues, false);
  }

  public void createFeedbackParameter(final ISelectionMapper selMap, final String fieldName,
      final Object defaultValues, final boolean optional) {
    if (selMap != null) {
      // TODO support help hints
      createFeedbackParameter(
          fieldName,
          selMap.getSelectionDisplayName(),
          "", defaultValues, selMap.getSelectionValues(), selMap.getSelectionNameMap(), selMap.getDisplayStyle(), optional); //$NON-NLS-1$
    }
  }

  public void createFeedbackParameter(final String fieldName, final String displayName, final String hint,
      final Object defaultValues, final List values, final Map dispNames, final String displayStyle) {
    createFeedbackParameter(fieldName, displayName, hint, defaultValues, values, dispNames, displayStyle, false);
  }

  public void createFeedbackParameter(String fieldName, final String displayName, String hint, Object defaultValues,
      final List values, final Map dispNames, final String displayStyle, final boolean optional) {

    // If there is a "PRO_EDIT_SUBSCRIPTION" param provider, then we must be editing a subscription so use its values
    IParameterProvider parameterProvider = (IParameterProvider) parameterProviders.get("PRO_EDIT_SUBSCRIPTION"); //$NON-NLS-1$
    if (parameterProvider != null) {
      defaultValues = parameterProvider.getParameter(paramManager.getActualRequestParameterName(fieldName));
    }

    if (values == null) {
      return;
    }
    if ((xformHeader == null) || (xformHeader.length() == 0)) {
      // this is the first parameter, need to create the header...
      addXFormHeader();
    }

    // See if the parameter is defined in the template. If so, then
    // don't add it to the XForm.
    if (checkForFieldInTemplate(fieldName)) {
      return;
    }

    int type = (values.size() < 6) ? XForm.TYPE_RADIO : XForm.TYPE_SELECT;
    if (displayStyle != null) {
      if ("text-box".equals(displayStyle)) { //$NON-NLS-1$
        type = XForm.TYPE_TEXT;
      } else if ("radio".equals(displayStyle)) { //$NON-NLS-1$
        type = XForm.TYPE_RADIO;
      } else if ("select".equals(displayStyle)) { //$NON-NLS-1$
        type = XForm.TYPE_SELECT;
      } else if ("list".equals(displayStyle)) { //$NON-NLS-1$
        type = XForm.TYPE_LIST;
      } else if ("list-multi".equals(displayStyle)) { //$NON-NLS-1$
        type = XForm.TYPE_LIST_MULTI;
      } else if ("check-multi".equals(displayStyle)) { //$NON-NLS-1$
        type = XForm.TYPE_CHECK_MULTI;
      } else if ("check-multi-scroll".equals(displayStyle)) { //$NON-NLS-1$
        type = XForm.TYPE_CHECK_MULTI_SCROLL;
      } else if ("check-multi-scroll-2-column".equals(displayStyle)) { //$NON-NLS-1$
        type = XForm.TYPE_CHECK_MULTI_SCROLL_2_COLUMN;
      } else if ("check-multi-scroll-3-column".equals(displayStyle)) { //$NON-NLS-1$
        type = XForm.TYPE_CHECK_MULTI_SCROLL_3_COLUMN;
      } else if ("check-multi-scroll-4-column".equals(displayStyle)) { //$NON-NLS-1$
        type = XForm.TYPE_CHECK_MULTI_SCROLL_4_COLUMN;
      }

    }
    fieldName = paramManager.getActualRequestParameterName(fieldName);
    if (hint == null) {
      hint = ""; //$NON-NLS-1$
    }
    if (parameterXsl == null) {
      // create some xform to represent this parameter...
      xformBody.append(Messages.getString("RuntimeContext.CODE_XFORM_CONTROL_LABEL_START", displayName)); //$NON-NLS-1$
      XForm.createXFormControl(type, fieldName, defaultValues, values, dispNames, RuntimeContextBase.PARAMETER_FORM,
          xformHeader, xformBody);
      xformBody.append(Messages.getString("RuntimeContext.CODE_XFORM_CONTROL_LABEL_END")); //$NON-NLS-1$
    } else if (parameterTemplate != null) {
      StringBuffer body = new StringBuffer();
      XForm.createXFormControl(type, fieldName, defaultValues, values, dispNames, RuntimeContextBase.PARAMETER_FORM,
          xformHeader, body);
      parameterTemplate = parameterTemplate.replaceAll("\\{" + fieldName + "\\}", body.toString()); //$NON-NLS-1$ //$NON-NLS-2$
    } else if (parameterXsl.endsWith(".xsl")) { //$NON-NLS-1$
      StringBuffer body = new StringBuffer();
      XForm.createXFormControl(type, fieldName, defaultValues, values, dispNames, RuntimeContextBase.PARAMETER_FORM,
          xformHeader, body);
      xformBody.append("<filter"); //$NON-NLS-1$
      if (optional) {
        xformBody.append(" optional=\"true\""); //$NON-NLS-1$
      }
      xformBody.append("><id><![CDATA[" + fieldName + "]]></id>") //$NON-NLS-1$ //$NON-NLS-2$
          .append("<title><![CDATA[" + displayName + "]]></title>") //$NON-NLS-1$ //$NON-NLS-2$
          .append("<help><![CDATA[" + hint + "]]></help><control>") //$NON-NLS-1$ //$NON-NLS-2$
          .append(body).append("</control></filter>"); //$NON-NLS-1$
    }

    xformFields.put(fieldName, fieldName);

  }

  public boolean checkForFieldInTemplate(final String fieldName) {
    //
    // This pattern looks for:
    //
    // id="fieldname"
    // iD="fieldname"
    // Id="fieldname"
    // ID="fieldname"
    // id='fieldname'
    // iD='fieldname'
    // Id='fieldname'
    // ID='fieldname'
    //
    // TODO: This is actually optimistic searching as it's not looking for the
    // string within the form portion of the template. IMO, to be more robust,
    // this needs to at least look for something only within a form and only
    // within a control on a form.
    if ((parameterTemplate == null) || (parameterTemplate.length() == 0)) {
      return false;
    }
    String regex = "[iI][dD]=[\'\"]" + fieldName + "[\'\"]"; //$NON-NLS-1$ //$NON-NLS-2$
    Pattern pattern = null;
    // Normally shouldn't need to synchronize. But, a Java bug in
    // pattern compilation on multi-processor machines results in the 
    // need to synchronize a small block of code. If/when this problem 
    // is fixed, we can remove this synchronization lock.
    // See: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6238699
    synchronized (RuntimeContextBase.PATTERN_COMPILE_LOCK) {
      pattern = Pattern.compile(regex);
    }
    Matcher matcher = pattern.matcher(parameterTemplate);
    if (matcher.find()) {
      return true;
    }
    return false;
  }

  public void createFeedbackParameter(final String fieldName, final String displayName, final String hint,
      final Object defaultValue, final boolean visible) {
    createFeedbackParameter(fieldName, displayName, hint, defaultValue, visible, false);
  }

  public void createFeedbackParameter(String fieldName, final String displayName, String hint, Object defaultValue,
      final boolean visible, final boolean optional) {

    // If there is a "PRO_EDIT_SUBSCRIPTION" param provider, then we must be editing a subscription so use its values
    IParameterProvider parameterProvider = (IParameterProvider) parameterProviders.get("PRO_EDIT_SUBSCRIPTION"); //$NON-NLS-1$
    if (parameterProvider != null) {
      Object newValue = parameterProvider.getParameter(paramManager.getActualRequestParameterName(fieldName));
      defaultValue = newValue == null ? defaultValue : newValue;
    }

    if ((xformHeader == null) || (xformHeader.length() == 0)) {
      // this is the first parameter, need to create the header...
      addXFormHeader();
    }
    if (parameterTemplate != null) {
      // see if the parameter is defined in the HTML template
      if (checkForFieldInTemplate(fieldName)) {
        return;
      }
    }
    if (hint == null) {
      hint = ""; //$NON-NLS-1$
    }
    fieldName = paramManager.getActualRequestParameterName(fieldName);
    if (parameterXsl == null) {
      // create some xform to represent this parameter...

      if (visible) {
        xformBody.append(Messages.getString("RuntimeContext.CODE_XFORM_CONTROL_LABEL_START", displayName)); //$NON-NLS-1$
        // xformBody.append( "<tr><td class=\"portlet-font\">").append(
        // displayName ).append("</td><td class=\"portlet-font\">"
        // );//$NON-NLS-1$ //$NON-NLS-2$
      }
      XForm.createXFormControl(fieldName, defaultValue, RuntimeContextBase.PARAMETER_FORM, xformHeader, xformBody, visible);
      if (visible) {
        xformBody.append(Messages.getString("RuntimeContext.CODE_XFORM_CONTROL_LABEL_END")); //$NON-NLS-1$
        // xformBody.append( "</td></tr>" ); //$NON-NLS-1$
      }
    } else if (parameterTemplate != null) {
      StringBuffer body = new StringBuffer();
      if (visible) {
        XForm.createXFormControl(fieldName, defaultValue, RuntimeContextBase.PARAMETER_FORM, xformHeader, body, visible);
      } else {
        try {
          if (defaultValue instanceof Object[]) {
            setObjectArrayParameters(fieldName, (Object[]) defaultValue);
          }
          String value = defaultValue.toString().replaceAll("&", "&amp;"); //$NON-NLS-1$//$NON-NLS-2$
          value = value.replaceAll("\"", "''"); //$NON-NLS-1$ //$NON-NLS-2$
          body.append("<input type=\"hidden\" name=\"" + fieldName + "\" value=\"" + value + "\"></input>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } catch (Exception e) {
          body.append("<input type=\"hidden\" name=\"" + fieldName + "\" value=\"" + defaultValue + "\"></input>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
      }
      parameterTemplate = parameterTemplate.replaceAll("\\{" + fieldName + "\\}", body.toString()); //$NON-NLS-1$ //$NON-NLS-2$
    } else {
      if (visible) {
        StringBuffer body = new StringBuffer();
        XForm.createXFormControl(fieldName, defaultValue, RuntimeContextBase.PARAMETER_FORM, xformHeader, body, visible);
        xformBody.append("<filter"); //$NON-NLS-1$
        if (optional) {
          xformBody.append(" optional=\"true\""); //$NON-NLS-1$
        }
        xformBody.append("><id><![CDATA[" + fieldName + "]]></id>") //$NON-NLS-1$ //$NON-NLS-2$
            .append("<title><![CDATA[" + displayName + "]]></title>") //$NON-NLS-1$ //$NON-NLS-2$
            .append("<help><![CDATA[" + hint + "]]></help><control>") //$NON-NLS-1$ //$NON-NLS-2$
            .append(body).append("</control></filter>"); //$NON-NLS-1$

      } else {
        try {
          if (defaultValue instanceof Object[]) {
            setObjectArrayParameters(fieldName, (Object[]) defaultValue);
          } else {
            // String value = URLEncoder.encode(defaultValue, "UTF-8" );
            // //$NON-NLS-1$
            String value = defaultValue.toString().replaceAll("&", "&amp;"); //$NON-NLS-1$//$NON-NLS-2$
            value = value.replaceAll("\"", "''"); //$NON-NLS-1$ //$NON-NLS-2$
            xformBody.append("<input type=\"hidden\" name=\"" + fieldName + "\" value=\"" + value + "\"></input>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          }
        } catch (Exception e) {
          xformBody.append("<input type=\"hidden\" name=\"" + fieldName + "\" value=\"" + defaultValue + "\"></input>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
      }
    }
    xformFields.put(fieldName, fieldName);
  }

  private void setObjectArrayParameters(final String fieldName, final Object[] values) {
    for (Object element : values) {
      String value = element.toString().replaceAll("&", "&amp;"); //$NON-NLS-1$//$NON-NLS-2$
      value = value.replaceAll("\"", "''"); //$NON-NLS-1$ //$NON-NLS-2$
      xformBody.append("<input type=\"hidden\" name=\"" + fieldName + "\" value=\"" + value + "\"></input>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
  }

  public void setParameterXsl(final String xsl) {
    this.parameterXsl = xsl;
  }

  public void setParameterTarget(final String target) {
    this.parameterTarget = target;
  }

  @Override
  public String getLogId() {
    return logId;
  }

  /**
   * Forces the immediate write of runtime data to underlying persistence
   * mechanism. In the case of using Hibernate for the runtime data
   * persistence, this works out to a call to HibernateUtil.flush().
   */
  public void forceSaveRuntimeData() {
    if (runtimeData != null) {
      runtimeData.forceSave();
    }
  }

  /**
   * Gets the output type preferred by the handler. Values are defined in
   * org.pentaho.core.solution.IOutputHander and are OUTPUT_TYPE_PARAMETERS,
   * OUTPUT_TYPE_CONTENT, or OUTPUT_TYPE_DEFAULT
   * 
   * @return Output type
   */
  public int getOutputPreference() {
    return outputHandler.getOutputPreference();
  }

  public void setOutputHandler(final IOutputHandler outputHandler) {
    this.outputHandler = outputHandler;
  }

  public IActionSequence getActionSequence() {
    return actionSequence;
  }

  public IParameterManager getParameterManager() {
    return paramManager;
  }

protected boolean isAudit() {
	return audit;
}

protected void setAudit(boolean audit) {
	this.audit = audit;
}

protected void setErrorLevel(int errorLevel) {
	this.errorLevel = errorLevel;
}

protected void setInstanceId(String instanceId) {
	this.instanceId = instanceId;
}

protected void setProcessId(String processId) {
	this.processId = processId;
}

public Map getParameterProviders() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setCreateFeedbackParameterCallback(ICreateFeedbackParameterCallback callback) {
    createFeedbackParameterCallback = callback;    
  }
  
}
