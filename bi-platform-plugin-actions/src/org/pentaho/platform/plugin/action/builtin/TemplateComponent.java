/*
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Aug 17, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.plugin.action.builtin;

import java.io.OutputStream;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.actions.TemplateMsgAction;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

public class TemplateComponent extends ComponentBase {

  private final static String TEMPLATE = "template"; //$NON-NLS-1$

  /**
   * 
   */
  private static final long serialVersionUID = 4383466190328580251L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog(TemplateComponent.class);
  }

  @Override
  protected boolean validateAction() {

    // see if we have a template defined
    TemplateMsgAction actionDefinition = (TemplateMsgAction) getActionDefinition();

    boolean templateOk = false;
    if (null != actionDefinition.getTemplate()) {
      templateOk = true;
    } else if (isDefinedResource(TemplateComponent.TEMPLATE)) {
      templateOk = true;
    }

    if (!templateOk) {
      error(Messages.getString("Template.ERROR_0001_TEMPLATE_NOT_DEFINED")); //$NON-NLS-1$
      return false;
    }
    Set outputs = getOutputNames();
    if ((outputs == null) || (outputs.size() == 0) || (outputs.size() > 1)) {
      error(Messages.getString("Template.ERROR_0002_OUTPUT_COUNT_WRONG")); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    // nothing to do here
    return true;
  }

  @Override
  public void done() {
    // nothing to do here
  }

  @Override
  protected boolean executeAction() {

    try {

      TemplateMsgAction actionDefinition = (TemplateMsgAction) getActionDefinition();
      String template = null;

      template = actionDefinition.getTemplate().getStringValue();
      if ((null == template) && isDefinedResource(TemplateComponent.TEMPLATE)) {
        IActionSequenceResource resource = getResource("template"); //$NON-NLS-1$
        template = getResourceAsString(resource);
      }

      String outputName = (String) getOutputNames().iterator().next();
      IActionParameter outputParam = getOutputItem(outputName);

      if (outputParam.getType().equals(IActionParameter.TYPE_CONTENT)) {

        String mimeType = actionDefinition.getMimeType().getStringValue();
        String extension = actionDefinition.getExtension().getStringValue();

        //This would prevent null values being passed as parameters to getOutputItem
        if (mimeType == null) {
          mimeType = ""; //$NON-NLS-1$
        }

        if (extension == null) {
          extension = ""; //$NON-NLS-1$
        }

        // Removing the null check here because if we avoid null exception it gives misleading hibernate 
        // stale data exception which has nothing to do with a report that simply reads data.        
        IContentItem outputItem = getOutputContentItem(outputName, mimeType);
        //        IContentItem outputItem = getOutputItem(outputName, mimeType, extension);
        OutputStream outputStream = outputItem.getOutputStream(getActionName());

        outputStream.write(applyInputsToFormat(template).getBytes(LocaleHelper.getSystemEncoding()));
        outputItem.closeOutputStream();
        return true;
      } else {
        setOutputValue(outputName, applyInputsToFormat(template));
      }

      return true;
    } catch (Exception e) {
      error(Messages.getString("Template.ERROR_0004_COULD_NOT_FORMAT_TEMPLATE"), e); //$NON-NLS-1$
      return false;
    }

  }

  @Override
  public boolean init() {
    // nothing to do here
    return true;
  }

}