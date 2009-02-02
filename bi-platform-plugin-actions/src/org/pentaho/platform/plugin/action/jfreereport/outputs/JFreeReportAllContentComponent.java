/*
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved.
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.platform.plugin.action.jfreereport.outputs;

import java.awt.GraphicsEnvironment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IComponent;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.MasterReport;

/**
 * Creation-Date: 07.07.2006, 21:01:58
 * 
 * @author Thomas Morgner
 */
public class JFreeReportAllContentComponent extends AbstractJFreeReportComponent {
  private static final long serialVersionUID = -8233725514054165666L;

  private AbstractGenerateContentComponent component;

  public JFreeReportAllContentComponent() {
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog(getClass());
  }

  @Override
  protected boolean executeAction() throws Throwable {
    if (component != null) {
      debug(Messages.getString("JFreeReportAllContentComponent.DEBUG_EXECUTING_COMPONENT", component.toString())); //$NON-NLS-1$
      return (component.execute() == IRuntimeContext.RUNTIME_STATUS_SUCCESS);
    }

    debug(Messages.getString("JFreeReportAllContentComponent.DEBUG_NO_COMPONENT")); //$NON-NLS-1$
    return false;
  }

  @Override
  public boolean init() {
    if (component != null) {
      return component.init();
    }
    return true;
  }

  @Override
  public void done() {
    if (component != null) {
      component.done();
    }
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  protected boolean initAndValidate(final IComponent componentToValidate) {
    componentToValidate.setInstanceId(getInstanceId());
    componentToValidate.setActionName(getActionName());
    componentToValidate.setProcessId(getProcessId());
    componentToValidate.setComponentDefinition(getComponentDefinition());
    componentToValidate.setSession(getSession());
    componentToValidate.setRuntimeContext(getRuntimeContext());
    componentToValidate.setLoggingLevel(getLoggingLevel());
    componentToValidate.setMessages(getMessages());
    return (componentToValidate.validate() == IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK);
  }

  @Override
  protected boolean validateAction() {

    if (isDefinedInput(StandardSettings.PRINTER_NAME)) {
      component = new JFreeReportPrintComponent();
      return initAndValidate(component);
    }

    if (isDefinedInput(AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE)) {

      String reportOutputType = getInputStringValue(AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE);
      if (getLogger().isDebugEnabled()) {
        debug(Messages.getString("JFreeReport.DEBUG_OUTPUT_TYPE", reportOutputType)); //$NON-NLS-1$
      }
      if (AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_HTML.equals(reportOutputType)) {
        component = new JFreeReportHtmlComponent();
        return initAndValidate(component);
      } else if (AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_PDF.equals(reportOutputType)) {
        component = new JFreeReportPdfComponent();
        return initAndValidate(component);
      } else if (AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_XLS.equals(reportOutputType)) {
        component = new JFreeReportExcelComponent();
        return initAndValidate(component);
      } else if (AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_CSV.equals(reportOutputType)) {
        component = new JFreeReportCSVComponent();
        return initAndValidate(component);
      } else if (AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_RTF.equals(reportOutputType)) {
        component = new JFreeReportRTFComponent();
        return initAndValidate(component);
      } else if (AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_XML.equals(reportOutputType)) {
        component = new JFreeReportXmlComponent();
        return initAndValidate(component);
      } else if (AbstractJFreeReportComponent.REPORTALLCONTENT_OUTPUTTYPE_SWING.equals(reportOutputType)) {
        if (GraphicsEnvironment.isHeadless()) {
          component = new JFreeReportPreviewSwingComponent();
          return initAndValidate(component);
        }
        warn(Messages.getString("JFreeReportAllContentComponent.WARN_HEADLESSMODE_ACTIVE")); //$NON-NLS-1$
        return false;
      } else {
        return false;
      }
    }
    warn(Messages.getString("JFreeReportAllContentComponent.WARN_NO_PRINTER_GIVEN")); //$NON-NLS-1$
    return false;
  }

  protected boolean performExport(final MasterReport report) {
    return false;
  }
}
