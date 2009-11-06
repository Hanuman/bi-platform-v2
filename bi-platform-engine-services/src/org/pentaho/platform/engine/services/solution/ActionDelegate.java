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
    //Create a map for passing undeclared inputs if an IVarArgsAction
    //
    Map<String, Object> varArgsMap = null;
    if (actionBean instanceof IVarArgsAction) {
      varArgsMap = new HashMap<String, Object>();
      ((IVarArgsAction) actionBean).setVarArgs(varArgsMap);
    }

    //
    //Provide output stream for streaming actions.  We are going to look for an output where
    //type = "content", which is a required output for IStreamingActions.  If this output
    //is found, an OutputStream will be retrieved for this output and that OutputStream will
    //be handed to the IStreamingAction.
    //
    Map<String, IContentItem> outputContentItems = new HashMap<String, IContentItem>();

    IActionOutput[] contentOutputs = getActionDefinition().getOutputs(ActionSequenceDocument.CONTENT_TYPE);
    
    if (contentOutputs.length > 0) {
      
      boolean streamingCheckPerformed = false;

      for (IActionOutput contentOutput : contentOutputs) {
        
        if(!isStreamingOutput(contentOutput.getPublicName())) {
          //we will deal with this output like normal non-streaming outputs post execution
          continue;
        }

        String contentOutputName = compatibilityToCamelCase(contentOutput.getName());
        String contentOutputPropertyName = contentOutputName + "Stream"; //$NON-NLS-1$

        if (beanUtil.isWriteable(actionBean, contentOutputPropertyName)) {

          //fail early if we cannot handle stream outputs
          if (!streamingCheckPerformed && !(actionBean instanceof IStreamingAction)) {
            throw new ActionExecutionException(Messages.getInstance().getErrorString(
                "ActionDelegate.ERROR_0002_ACTION_CANNOT_ACCEPT_STREAM", //$NON-NLS-1$
                contentOutputName, actionBean.getClass().getSimpleName()));
          }
          streamingCheckPerformed = true;

          String mimeType = ((IStreamingAction) actionBean).getMimeType(contentOutputName);
          if (StringUtils.isEmpty(mimeType)) {
            throw new ActionValidationException(Messages.getInstance().getErrorString(
                "ActionDelegate.ERROR_0001_MIMETYPE_NOT_DECLARED")); //$NON-NLS-1$
          }

          //most output handlers will manage multiple destinations for us and hand us back a MultiContentItem
          IContentItem contentItem = getRuntimeContext().getOutputContentItem(contentOutput.getPublicName(), mimeType);

          if (contentItem == null) {
            //this is the best I can do here to point users to a tangible problem without unwrapping code in RuntimeEngine - AP
            throw new ActionValidationException(Messages.getInstance().getErrorString(
                "ActionDelegate.ERROR_0003_OUTPUT_STREAM_NOT_AVAILABLE_1", //$NON-NLS-1$
                contentOutput.getPublicName()));
          }

          //this will be a MultiOutputStream in the case where there is more than one destination for the content output
          OutputStream contentOutputStream = contentItem.getOutputStream(getActionName());
          if (contentOutputStream == null) {
            throw new ActionExecutionException(Messages.getInstance().getErrorString(
                "ActionDelegate.ERROR_0004_OUTPUT_STREAM_NOT_AVAILABLE_2", //$NON-NLS-1$
                actionBean.getClass().getSimpleName()));
          }

          //save this for later when we set the action outputs
          outputContentItems.put(contentOutput.getName(), contentItem);

          //
          // Now hand the Action a writeable output stream so it has something to write to during execution
          //

          try {
            //trying our best to set the input value to the type specified by the action bean
            beanUtil.setSimpleProperty(actionBean, contentOutputPropertyName, contentOutputStream);
          } catch (Exception e) {
            PropertyDescriptor desc = beanUtil.getPropertyDescriptor(actionBean, contentOutputName);
            //we could not convert to the type the action wants. this is a failure
            throw new ActionExecutionException(Messages.getInstance().getErrorString(
                "ActionDelegate.ERROR_0008_FAILED_TO_SET_STREAM", contentOutputName, OutputStream.class.getName(),
                actionBean.getClass().getSimpleName(), desc.getPropertyType().getName()), e);
          }
        } else {
          if (loggingLevel <= ILogger.WARN) {
            warn(Messages.getInstance().getString("ActionDelegate.WARN_INPUT_NOT_WRITABLE", actionBean //$NON-NLS-1$
                .getClass().getSimpleName(), contentOutputName, OutputStream.class.getName()));
          }
        }
      }
    }

    //
    //Set inputs
    //
    for (IActionInput input : actionDefintionInputs) {
      String inputName = input.getName();
      inputName = compatibilityToCamelCase(inputName);

      /* InputVal has a concrete type that may be determined by the type attribute 
       * of the action sequence input.  See type mappings in documentation. */
      Object inputVal = input.getValue();

      if (beanUtil.isWriteable(actionBean, inputName)) {

        try {
          //trying our best to set the input value to the type specified by the action bean
          typeConvertingBeanUtil.copyProperty(actionBean, inputName, inputVal);
        } catch (Exception e) {
          PropertyDescriptor desc = beanUtil.getPropertyDescriptor(actionBean, inputName);
          //we could not convert to the type the action wants. this is a failure
          throw new ActionExecutionException(Messages.getInstance().getErrorString(
              "ActionDelegate.ERROR_0005_FAILED_TO_SET_INPUT", //$NON-NLS-1$
              inputName, inputVal.getClass().getName(), actionBean.getClass().getSimpleName(),
              desc.getPropertyType().getName()), e);
        }
      } else {
        //the input is undeclared, put it in the varArgsMap if anyone cares
        if (varArgsMap != null) {
          varArgsMap.put(inputName, inputVal);
        } else if (loggingLevel <= ILogger.WARN) {
          //log a warning if there is no way to get this input to the Action
          String valueType = (inputVal == null) ? null : inputVal.getClass().getName();
          warn(Messages.getInstance().getString(
              "ActionDelegate.WARN_INPUT_NOT_WRITABLE", actionBean.getClass().getSimpleName(), //$NON-NLS-1$
              inputName, valueType));
        }
      }
    }

    //
    //Set resources
    //
    for (IActionResource res : getActionDefinition().getResources()) {
      String resName = res.getName();
      resName = compatibilityToCamelCase(resName);

      if (beanUtil.isWriteable(actionBean, resName)) {
        InputStream inputStream = res.getInputStream();

        try {
          //There is not a question of type here, it must be InputStream, so we use the non-converting
          //propertyUtil instance.
          beanUtil.setSimpleProperty(actionBean, resName, inputStream);
        } catch (Exception e) {
          PropertyDescriptor desc = beanUtil.getPropertyDescriptor(actionBean, resName);
          //we could not convert to the type the action wants. this is a failure
          throw new ActionExecutionException(Messages.getInstance().getErrorString(
              "ActionDelegate.ERROR_0006_FAILED_TO_SET_RESOURCE", //$NON-NLS-1$
              resName, inputStream.getClass().getName(), actionBean.getClass().getSimpleName(),
              desc.getPropertyType().getName()), e);
        }
      } else {
        if (loggingLevel <= ILogger.WARN) {
          warn(Messages.getInstance().getString("ActionDelegate.WARN_RESOURCE_NOT_WRITABLE", actionBean //$NON-NLS-1$
              .getClass().getSimpleName(), resName, InputStream.class.getName()));
        }
      }
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
