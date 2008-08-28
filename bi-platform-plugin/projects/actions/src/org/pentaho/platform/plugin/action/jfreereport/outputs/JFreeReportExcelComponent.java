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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jfree.report.JFreeReport;
import org.jfree.report.ReportProcessingException;
import org.jfree.report.layout.output.YieldReportListener;
import org.jfree.report.modules.output.table.base.FlowReportProcessor;
import org.jfree.report.modules.output.table.xls.FlowExcelOutputProcessor;
import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.messages.Messages;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 * 
 * @author Thomas Morgner
 */
public class JFreeReportExcelComponent extends AbstractGenerateStreamContentComponent {
  private static final long serialVersionUID = -2130145967763406737L;

  public JFreeReportExcelComponent() {
  }

  @Override
  protected String getMimeType() {
    return "application/vnd.ms-excel"; //$NON-NLS-1$
  }

  @Override
  protected String getExtension() {
    return ".xls"; //$NON-NLS-1$
  }

  @Override
  protected boolean performExport(final JFreeReport report, final OutputStream outputStream) {
    try {
      final FlowExcelOutputProcessor target = new FlowExcelOutputProcessor(report.getConfiguration(), outputStream);
      final FlowReportProcessor reportProcessor = new FlowReportProcessor(report, target);

      if (isDefinedInput(AbstractJFreeReportComponent.WORKBOOK_PARAM)) {
        try {
          final InputStream inputStream = getInputStream(AbstractJFreeReportComponent.WORKBOOK_PARAM);
          target.setTemplateInputStream(inputStream);
        } catch (Exception e) {
          error(Messages.getString("JFreeReportExcelComponent.ERROR_0037_ERROR_READING_REPORT_INPUT"), e); //$NON-NLS-1$
          return false;
        }
      }

      final int yieldRate = getYieldRate();
      if (yieldRate > 0) {
        reportProcessor.addReportProgressListener(new YieldReportListener(yieldRate));
      }
      reportProcessor.processReport();
      reportProcessor.close();
      outputStream.flush();
      close();
      return true;
    } catch (ReportProcessingException e) {
      return false;
    } catch (IOException e) {
      return false;
    }
  }
}
