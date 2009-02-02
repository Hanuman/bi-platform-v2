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

import java.io.OutputStream;

import org.jfree.util.Log;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.layout.output.YieldReportListener;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.base.PageableReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfOutputProcessor;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 * 
 * @author Thomas Morgner
 */
public class JFreeReportPdfComponent extends AbstractGenerateStreamContentComponent {
  private static final long serialVersionUID = 3209507821690330555L;

  public JFreeReportPdfComponent() {
  }

  @Override
  protected String getMimeType() {
    return "application/pdf"; //$NON-NLS-1$
  }

  @Override
  protected String getExtension() {
    return ".pdf"; //$NON-NLS-1$
  }

  @Override
  protected boolean performExport(final MasterReport report, final OutputStream outputStream) {
    PageableReportProcessor proc = null;
    try {

      final PdfOutputProcessor outputProcessor = new PdfOutputProcessor(report.getConfiguration(), outputStream);
      proc = new PageableReportProcessor(report, outputProcessor);
      final int yieldRate = getYieldRate();
      if (yieldRate > 0) {
        proc.addReportProgressListener(new YieldReportListener(yieldRate));
      }
      proc.processReport();
      proc.close();
      proc = null;
      close();
      return true;
    } catch (Exception e) {
      Log.error(Messages.getErrorString("JFreeReportPdfComponent.ERROR_0001_WRITING_PDF_FAILED", //$NON-NLS-1$
          e.getLocalizedMessage()), e);
      return false;
    } finally {
      if (proc != null) {
        proc.close();
      }
    }
  }
}
