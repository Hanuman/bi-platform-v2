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
 * Copyright 2009 Pentaho Corporation.  All rights reserved. 
 * 
 */

package org.pentaho.platform.engine.services.solution;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.ActionSequenceDocument;
import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.IActionResource;
import org.pentaho.actionsequence.dom.IActionSequenceOutput;
import org.pentaho.platform.api.action.ActionPreProcessingException;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IDefinitionAwareAction;
import org.pentaho.platform.api.action.ILoggingAction;
import org.pentaho.platform.api.action.IPreProcessingAction;
import org.pentaho.platform.api.action.ISessionAwareAction;
import org.pentaho.platform.api.action.IStreamingAction;
import org.pentaho.platform.api.action.IVarArgsAction;
import org.pentaho.platform.api.engine.ActionExecutionException;
import org.pentaho.platform.api.engine.ActionValidationException;
import org.pentaho.platform.api.engine.IComponent;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.SimpleContentItem;
import org.pentaho.platform.engine.services.messages.Messages;

/**
 * The purpose of the {@link ActionDelegate} is to represent an action object 
 * (which implements {@link IAction}) as an {@link IComponent}.
 */
@SuppressWarnings("serial")
public class ActionDelegate extends ComponentBase {

  //PropertyUtils are strictly typed and do not do type conversion
  private static PropertyUtilsBean beanUtil = new PropertyUtilsBean();

  //BeanUtils will make a best effort to convert to the requested type on copy or set
  private static BeanUtilsBean typeConvertingBeanUtil;

  private Object actionBean;

  private IActionInput[] actionDefintionInputs;

  private IActionOutput[] actionDefintionOutputs;

  {
    //
    //Configure a bean util that throws exceptions during type conversion
    //
    ConvertUtilsBean convertUtil = new ConvertUtilsBean();
    convertUtil.register(true, true, 0);

    typeConvertingBeanUtil = new BeanUtilsBean(convertUtil);
  }

  public ActionDelegate(Object actionBean) {
    this.actionBean = actionBean;
  }

  public Object getActionBean() {
    return actionBean;
  }

  /**
   * Clean-up should happen in the {@link IAction#execute()}
   **/
  @Override
  public void done() {
  }

  /**
   * This method will tell you if an output in the action definition should be treated like
   * and input and passed to the Action as an OutputStream.  An output should be assigned an OutputStream
   * if it has a counterpart of the same name in the action sequence outputs AND that output
   * is of type "content" AND it has a destination defined. 
   * @param privateOutputName the name of the action definition output to check
   * @return true if we should deal with this output as an OutputStream writable by the Action
   */
  protected boolean isStreamingOutput(String privateOutputName) {
    IActionSequenceOutput publicOutput = getActionDefinition().getDocument().getOutput(privateOutputName);
    if (publicOutput == null) {
      return false;
    }
    return (publicOutput.getType().equals(ActionSequenceDocument.CONTENT_TYPE) && publicOutput.getDestinations().length > 0);
  }

  /**
   * Wires up inputs outputs and resources to an Action and executes it.
   */
  @Override
  protected boolean executeAction() throws Throwable {
    //
    //Provide output stream for streaming actions.  We are going to look for an output where
    //type = "content", which is a required output for IStreamingActions.  If this output
    //is found, an OutputStream will be retrieved for this output and that OutputStream will
    //be handed to the IStreamingAction.
    //
    Map<String, IContentItem> outputContentItems = new HashMap<String, IContentItem>();
    StreamingOutputOps streamOutputOps = new StreamingOutputOps(outputContentItems);

    IActionOutput[] contentOutputs = getActionDefinition().getOutputs(ActionSequenceDocument.CONTENT_TYPE);
    if (contentOutputs.length > 0) {

      for (IActionOutput contentOutput : contentOutputs) {

        if (!isStreamingOutput(contentOutput.getPublicName())) {
          //we will deal with this output like normal non-streaming outputs post execution
          continue;
        }

        streamOutputOps.setOutputStream(contentOutput);
      }
    }

    //
    //Create a map for passing undeclared inputs if an IVarArgsAction
    //
    Map<String, Object> varArgsMap = null;
    if (actionBean instanceof IVarArgsAction) {
      varArgsMap = new HashMap<String, Object>();
      ((IVarArgsAction) actionBean).setVarArgs(varArgsMap);
    }

    //
    //Set inputs
    //
    InputOps inputOps = new InputOps(varArgsMap);
    for (IActionInput input : getActionDefinition().getInputs()) {
      //      inputOps.setValue(input.getName(), input.getValue());
      inputOps.setInput(input);
    }

    //
    //Set resources
    //
    ResourceOps resOps = new ResourceOps();
    for (IActionResource res : getActionDefinition().getResources()) {
      resOps.setResource(res);
    }

    //
    //Execute the Action if the bean is executable
    //
    if (actionBean instanceof IAction) {
      ((IAction) actionBean).execute();
    }

    //
    //Get and store outputs
    //
    for (IActionOutput output : actionDefintionOutputs) {
      String outputName = output.getName();
      outputName = compatibilityToCamelCase(outputName);

      //if streaming output, add it to the context and don't try to get it from the Action bean
      if (outputContentItems.containsKey(outputName)) {
        IContentItem contentItem = outputContentItems.get(outputName);

        if (!(contentItem instanceof SimpleContentItem)) {
          //this is a special output for streaming actions and does not require a bean accessor
          output.setValue(contentItem);
        } else {
          //          warn(SimpleContentItem.class.getSimpleName() + " is for testing purposes only and should not be used in production.");
        }
      } else if (beanUtil.isReadable(actionBean, outputName)) {
        Object outputVal = beanUtil.getSimpleProperty(actionBean, outputName);
        output.setValue(outputVal);
      } else {
        if (loggingLevel <= ILogger.WARN) {
          warn(Messages.getInstance().getString("ActionDelegate.WARN_OUTPUT_NOT_READABLE", //$NON-NLS-1$
              outputName, output.getType(), actionBean.getClass().getSimpleName()));
        }
      }
    }
    return true;
  }

  abstract class BeanOpsTemplate {
    abstract public void failedToSetValue(String name, Object value, String beanPropertyType, Throwable cause)
        throws ActionExecutionException;

    public String getPropertyNameSuffix() {
      return ""; //$NON-NLS-1$
    }

    abstract public void propertyNotWritable(String name) throws Exception;

    abstract public Object getValueToSet(String name) throws Exception;

    /**
     * Converts to a bean utils consumable expression and applies other customizations
     * as necessary, such as suffix additions.
     * @param name the property name to format
     * @return the formatted property name ready for use in bean utils
     */
    public String format(String name) {
      String formattedName = name;
      int indexDelimiter = name.lastIndexOf('_');
      if (indexDelimiter > 0) { //implies there is a name and an index like 'b_1' or 'a_b'
        String possibleIndex = name.substring(indexDelimiter + 1);
        try {
          int index = Integer.parseInt(possibleIndex);
          String propertyName = name.substring(0, indexDelimiter);
          formattedName = propertyName + getPropertyNameSuffix() + "[" + index + "]"; //$NON-NLS-1$ //$NON-NLS-2$
          return formattedName;
        } catch (NumberFormatException e) {
          //we don't have a numeric index, so just return the original expression
        }
      }
      return formattedName + getPropertyNameSuffix();
    }

    public void setValue(String name) throws Exception {
      name = compatibilityToCamelCase(name);
      name = format(name);

      /* InputVal has a concrete type that may be determined by the type attribute 
       * of the action sequence input.  See type mappings in documentation. */
      //    Object inputVal = input.getValue();
      //here we check if we can set the input value on the bean.  There are three ways that bean utils will go about this
      //1. use a simple property setter method
      //.. in the case of an indexed property there are two methods:
      //2. if there is an indexed setter method bean utils will that (note: a simple getter is required as well though it will not be invoked)
      //3. if there is an array-based getter like List<String> getNames(), bean utils will insert the new value into the array reference 
      //   it gets from the array getter.
      if (beanUtil.isWriteable(actionBean, name) || (beanUtil.getResolver().isIndexed(name))
          && beanUtil.isReadable(actionBean, name)) {

        //we get the value at the latest point possible
        Object val = getValueToSet(name);
        try {
          //trying our best to set the input value to the type specified by the action bean
          typeConvertingBeanUtil.copyProperty(actionBean, name, val);
        } catch (Exception e) {
          String propertyType = ""; //$NON-NLS-1$
          try {
            PropertyDescriptor desc = beanUtil.getPropertyDescriptor(actionBean, name);
            propertyType = desc.getPropertyType().getName();
          } catch (Throwable t) {
            //we are in a catch, we should never let an exception escape here
          }
          failedToSetValue(name, val, propertyType, e);
        }
      } else {
        propertyNotWritable(name);
      }
    }
  }

  class StreamingOutputOps extends BeanOpsTemplate {
    private Map<String, IContentItem> outputContentItems;

    private boolean streamingCheckPerformed = false;

    private IActionOutput curActionOutput;

    public StreamingOutputOps(Map<String, IContentItem> outputContentItems) {
      this.outputContentItems = outputContentItems;
    }
    
    public void setOutputStream(IActionOutput actionOutput) throws Exception {
      curActionOutput = actionOutput;
      super.setValue(actionOutput.getName());
    }

    @Override
    public String getPropertyNameSuffix() {
      return "Stream"; //$NON-NLS-1$
    }
    
    @Override
    public void failedToSetValue(String name, Object value, String destPropertyType, Throwable cause)
        throws ActionExecutionException {
      throw new ActionExecutionException(Messages.getInstance().getErrorString(
          "ActionDelegate.ERROR_0008_FAILED_TO_SET_STREAM", name, OutputStream.class.getName(), //$NON-NLS-1$
          actionBean.getClass().getSimpleName(), destPropertyType), cause);
    }

    @Override
    public void propertyNotWritable(String name) {
      if (loggingLevel <= ILogger.WARN) {
        warn(Messages.getInstance().getString("ActionDelegate.WARN_INPUT_NOT_WRITABLE", actionBean //$NON-NLS-1$
            .getClass().getSimpleName(), name, OutputStream.class.getName()));
      }
    }

    @Override
    public Object getValueToSet(String name) throws Exception {
      //fail early if we cannot handle stream outputs
      if (!streamingCheckPerformed && !(actionBean instanceof IStreamingAction)) {
        throw new ActionExecutionException(Messages.getInstance().getErrorString(
            "ActionDelegate.ERROR_0002_ACTION_CANNOT_ACCEPT_STREAM", //$NON-NLS-1$
            name, actionBean.getClass().getSimpleName()));
      }
      streamingCheckPerformed = true;

      String mimeType = ((IStreamingAction) actionBean).getMimeType(name);
      if (StringUtils.isEmpty(mimeType)) {
        throw new ActionValidationException(Messages.getInstance().getErrorString(
            "ActionDelegate.ERROR_0001_MIMETYPE_NOT_DECLARED")); //$NON-NLS-1$
      }

      //most output handlers will manage multiple destinations for us and hand us back a MultiContentItem
      IContentItem contentItem = getRuntimeContext().getOutputContentItem(curActionOutput.getPublicName(), mimeType);

      if (contentItem == null) {
        //this is the best I can do here to point users to a tangible problem without unwrapping code in RuntimeEngine - AP
        throw new ActionValidationException(Messages.getInstance().getErrorString(
            "ActionDelegate.ERROR_0003_OUTPUT_STREAM_NOT_AVAILABLE_1", //$NON-NLS-1$
            curActionOutput.getPublicName()));
      }

      //this will be a MultiOutputStream in the case where there is more than one destination for the content output
      OutputStream contentOutputStream = contentItem.getOutputStream(getActionName());
      if (contentOutputStream == null) {
        throw new ActionExecutionException(Messages.getInstance().getErrorString(
            "ActionDelegate.ERROR_0004_OUTPUT_STREAM_NOT_AVAILABLE_2", //$NON-NLS-1$
            actionBean.getClass().getSimpleName()));
      }

      //save this for later when we set the action outputs
      outputContentItems.put(curActionOutput.getName(), contentItem);

      return contentOutputStream;
    }
  };

  class ResourceOps extends BeanOpsTemplate {

    private IActionResource curResource;

    public void setResource(IActionResource resource) throws Exception {
      curResource = resource;
      super.setValue(resource.getName());
    }

    @Override
    public void failedToSetValue(String name, Object value, String destPropertyType, Throwable cause)
        throws ActionExecutionException {
      throw new ActionExecutionException(Messages.getInstance().getErrorString(
          "ActionDelegate.ERROR_0006_FAILED_TO_SET_RESOURCE", //$NON-NLS-1$
          name, value.getClass().getName(), actionBean.getClass().getSimpleName(), destPropertyType), cause);
    }

    @Override
    public void propertyNotWritable(String name) {
      if (loggingLevel <= ILogger.WARN) {
        warn(Messages.getInstance().getString("ActionDelegate.WARN_RESOURCE_NOT_WRITABLE", actionBean //$NON-NLS-1$
            .getClass().getSimpleName(), name, InputStream.class.getName()));
      }
    }

    @Override
    public Object getValueToSet(String name) throws Exception {
      return curResource.getInputStream();
    }
  }

  class InputOps extends BeanOpsTemplate {
    private Map<String, Object> varArgsMap;

    private IActionInput curInput;

    public InputOps(Map<String, Object> varArgsMap) {
      this.varArgsMap = varArgsMap;
    }

    public void setInput(IActionInput input) throws Exception {
      curInput = input;
      super.setValue(input.getName());
    }

    @Override
    public void failedToSetValue(String name, Object value, String destPropertyType, Throwable cause)
        throws ActionExecutionException {
      throw new ActionExecutionException(Messages.getInstance().getErrorString(
          "ActionDelegate.ERROR_0005_FAILED_TO_SET_INPUT", //$NON-NLS-1$
          name, value.getClass().getName(), actionBean.getClass().getSimpleName(), destPropertyType), cause);
    }

    @Override
    public void propertyNotWritable(String name) throws Exception {
      //the input is undeclared, put it in the varArgsMap if anyone cares
      Object value = getValueToSet(name);
      if (varArgsMap != null) {
        varArgsMap.put(name, value);
      } else if (loggingLevel <= ILogger.WARN) {
        //log a warning if there is no way to get this input to the Action
        String valueType = (value == null) ? null : value.getClass().getName();
        warn(Messages.getInstance().getString(
            "ActionDelegate.WARN_INPUT_NOT_WRITABLE", actionBean.getClass().getSimpleName(), //$NON-NLS-1$
            name, valueType));
      }
    }

    @Override
    public Object getValueToSet(String name) throws Exception {
      return curInput.getValue();
    }
  }

  /**
   * Any initialization can be done in the {@link IPreProcessingAction#doPreExecution()}
   */
  @Override
  public boolean init() {
    return true;
  }

  /**
   * Validation of Action input values should happen in the {@link IAction#execute()}
   * This method is used as a pre execution hook where we setup as much runtime information as
   * possible prior to the actual execute call.  
   **/
  @Override
  protected boolean validateAction() {
    if (actionBean == null) {
      throw new IllegalArgumentException(Messages.getInstance().getErrorString(
          "ActionDelegate.ERROR_0007_NO_ACTION_BEAN_SPECIFIED")); //$NON-NLS-1$
    }

    //
    //Provide a commons logging logger for logging actions
    //The log name will be the name of the Action class
    //
    if (actionBean instanceof ILoggingAction) {
      ((ILoggingAction) actionBean).setLogger(LogFactory.getLog(actionBean.getClass()));
    }

    //
    //Provide a session to the Action if an ISessionAwareAction
    //
    if (actionBean instanceof ISessionAwareAction) {
      ((ISessionAwareAction) actionBean).setSession(getSession());
    }

    actionDefintionInputs = getActionDefinition().getInputs();
    actionDefintionOutputs = getActionDefinition().getOutputs();

    //
    // If an Action is action-definition aware, then here is the place (prior to
    // execution) to tell it about the action definition.
    //
    List<String> inputNames = new ArrayList<String>();
    for (IActionInput input : actionDefintionInputs) {
      inputNames.add(input.getName());
    }
    List<String> outputNames = new ArrayList<String>();
    for (IActionOutput output : actionDefintionOutputs) {
      outputNames.add(output.getName());
    }
    if (actionBean instanceof IDefinitionAwareAction) {
      IDefinitionAwareAction definitionAwareAction = (IDefinitionAwareAction) actionBean;
      definitionAwareAction.setInputNames(inputNames);
      definitionAwareAction.setOutputNames(outputNames);
    }

    //
    // Invoke any pre-execution processing if the Action requires it.
    //
    if (actionBean instanceof IPreProcessingAction) {
      try {
        ((IPreProcessingAction) actionBean).doPreExecution();
      } catch (ActionPreProcessingException e) {
        throw new RuntimeException(e);
      }
    }
    //we do not use the return value to indicate failure.
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog(ActionDelegate.class);
  }

  /**
   * This method exists to make old-style action sequence inputs,
   * outputs and resource names work bean utils which adheres
   * to the Java bean spec.  All action definition inputs outputs
   * and resources should be named in camel case and dash characters
   * "-" should be avoided.  This method will convert a dash-style
   * arg name into camel case and print a warning, or just return the
   * original name if there are no dashes found.
   * @param name argument name to convert, if needed.
   * @return camel case representation of name
   */
  protected String compatibilityToCamelCase(String name) {
    String[] parts = name.split("-", 0); //$NON-NLS-1$
    if (parts.length > 1) {
      String camelCaseName = ""; //$NON-NLS-1$
      for (int i = 0; i < parts.length; i++) {
        if (i > 0) {
          camelCaseName += StringUtils.capitalize(parts[i]);
        } else {
          camelCaseName += parts[i];
        }
      }
      getLogger().warn(
          Messages.getInstance().getString("ActionDelegate.WARN_USING_IO_COMPATIBILITY_MODE", camelCaseName, name)); //$NON-NLS-1$
      return camelCaseName;
    }
    return name;
  }
}
