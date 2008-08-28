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
import org.jfree.report.modules.output.table.base.FlowReportProcessor;
import org.jfree.report.modules.output.table.html.AllItemsHtmlPrinter;
import org.jfree.report.modules.output.table.html.FlowHtmlOutputProcessor;
import org.jfree.report.modules.output.table.html.HtmlPrinter;
import org.jfree.report.modules.output.table.html.SingleRepositoryURLRewriter;
import org.jfree.repository.ContentIOException;
import org.jfree.repository.ContentLocation;
import org.jfree.repository.DefaultNameGenerator;
import org.jfree.repository.RepositoryUtilities;
import org.jfree.repository.zipwriter.ZipRepository;
import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.messages.Messages;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 * 
 * @author Thomas Morgner
 */
public class JFreeReportZipHtmlComponent extends AbstractGenerateStreamContentComponent {
  private static final long serialVersionUID = -3904516365257691828L;

  public JFreeReportZipHtmlComponent() {
  }

  @Override
  protected String getMimeType() {
    return "application/zip"; //$NON-NLS-1$
  }

  @Override
  protected String getExtension() {
    return ".zip"; //$NON-NLS-1$
  }

  @Override
  protected boolean performExport(final JFreeReport report, final OutputStream outputStream) {
    try {
      String dataDirectory = getInputStringValue(AbstractJFreeReportComponent.REPORTDIRECTORYHTML_DATADIR);
      if (dataDirectory == null) {
        dataDirectory = "data"; //$NON-NLS-1$
      }

      final ZipRepository zipRepository = new ZipRepository(outputStream);
      final ContentLocation root = zipRepository.getRoot();
      final ContentLocation data = RepositoryUtilities.createLocation(zipRepository, RepositoryUtilities.split(
          dataDirectory, "/"));//$NON-NLS-1$

      final FlowHtmlOutputProcessor outputProcessor = new FlowHtmlOutputProcessor(report.getConfiguration());

      final HtmlPrinter printer = new AllItemsHtmlPrinter(report.getResourceManager());
      printer.setContentWriter(root, new DefaultNameGenerator(root, "report.html"));//$NON-NLS-1$
      printer.setDataWriter(data, new DefaultNameGenerator(data, "content"));//$NON-NLS-1$
      printer.setUrlRewriter(new SingleRepositoryURLRewriter());
      outputProcessor.setPrinter(printer);

      final FlowReportProcessor sp = new FlowReportProcessor(report, outputProcessor);
      final int yieldRate = getYieldRate();
      if (yieldRate > 0) {
        sp.addReportProgressListener(new YieldReportListener(yieldRate));
      }
      sp.processReport();
      zipRepository.close();
      close();
      return true;
    } catch (ReportProcessingException e) {
      error(Messages.getString("JFreeReportZipHtmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT"), e); //$NON-NLS-1$
      return false;
    } catch (IOException e) {
      error(Messages.getString("JFreeReportZipHtmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT"), e); //$NON-NLS-1$
      return false;
    } catch (ContentIOException e) {
      error(Messages.getString("JFreeReportZipHtmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT"), e); //$NON-NLS-1$
      return false;
    }
  }
}
