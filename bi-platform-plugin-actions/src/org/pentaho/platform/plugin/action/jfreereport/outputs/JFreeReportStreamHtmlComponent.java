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
import org.jfree.report.modules.output.table.html.AllItemsHtmlPrinter;
import org.jfree.report.modules.output.table.html.FileSystemURLRewriter;
import org.jfree.report.modules.output.table.html.HtmlOutputProcessor;
import org.jfree.report.modules.output.table.html.HtmlPrinter;
import org.jfree.report.modules.output.table.html.StreamHtmlOutputProcessor;
import org.jfree.repository.ContentLocation;
import org.jfree.repository.DefaultNameGenerator;
import org.jfree.repository.stream.StreamRepository;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 * 
 * @author Thomas Morgner
 */
public class JFreeReportStreamHtmlComponent extends AbstractGenerateStreamContentComponent {
  private static final long serialVersionUID = 7103996413736560262L;

  public JFreeReportStreamHtmlComponent() {
  }

  @Override
  protected String getMimeType() {
    return "text/html"; //$NON-NLS-1$
  }

  @Override
  protected String getExtension() {
    return ".html"; //$NON-NLS-1$
  }

  @Override
  protected boolean performExport(final JFreeReport report, final OutputStream outputStream) {
    try {

      final StreamRepository targetRepository = new StreamRepository(null, outputStream);
      final ContentLocation targetRoot = targetRepository.getRoot();

      final HtmlOutputProcessor outputProcessor = new StreamHtmlOutputProcessor(report.getConfiguration());
      final HtmlPrinter printer = new AllItemsHtmlPrinter(report.getResourceManager());
      printer.setContentWriter(targetRoot, new DefaultNameGenerator(targetRoot, "index", "html"));//$NON-NLS-1$//$NON-NLS-2$
      printer.setDataWriter(null, null);
      printer.setUrlRewriter(new FileSystemURLRewriter());
      outputProcessor.setPrinter(printer);

      final StreamReportProcessor sp = new StreamReportProcessor(report, outputProcessor);
      final int yieldRate = getYieldRate();
      if (yieldRate > 0) {
        sp.addReportProgressListener(new YieldReportListener(yieldRate));
      }
      sp.processReport();
      sp.close();

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
