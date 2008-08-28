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
import java.io.OutputStream;

import org.jfree.report.JFreeReport;
import org.jfree.report.ReportProcessingException;
import org.jfree.report.layout.output.YieldReportListener;
import org.jfree.report.modules.output.table.base.StreamReportProcessor;
import org.jfree.report.modules.output.table.rtf.StreamRTFOutputProcessor;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 *
 * @author Thomas Morgner
 */
public class JFreeReportRTFComponent extends AbstractGenerateStreamContentComponent {
  private static final long serialVersionUID = -4095237855917616138L;

  public JFreeReportRTFComponent() {
  }

  @Override
  protected String getMimeType() {
    return "application/rtf"; //$NON-NLS-1$
  }

  @Override
  protected String getExtension() {
    return ".rtf"; //$NON-NLS-1$
  }

  @Override
  protected boolean performExport(final JFreeReport report, final OutputStream outputStream) {
    try {
      final StreamRTFOutputProcessor target = new StreamRTFOutputProcessor(report.getConfiguration(), outputStream);
      final StreamReportProcessor proc = new StreamReportProcessor(report, target);
      final int yieldRate = getYieldRate();
      if (yieldRate > 0) {
        proc.addReportProgressListener(new YieldReportListener(yieldRate));
      }
      proc.processReport();
      proc.close();
      outputStream.close();
      close();
      return true;
    } catch (ReportProcessingException e) {
      return false;
    } catch (IOException e) {
      return false;
    }
  }
}
