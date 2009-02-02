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

import java.io.File;

import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.layout.output.YieldReportListener;
import org.pentaho.reporting.engine.classic.core.modules.output.table.base.FlowReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.AllItemsHtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.FileSystemURLRewriter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.FlowHtmlOutputProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlPrinter;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.repository.ContentLocation;
import org.pentaho.reporting.libraries.repository.DefaultNameGenerator;
import org.pentaho.reporting.libraries.repository.file.FileRepository;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 * 
 * @author Thomas Morgner
 */
public class JFreeReportDirectoryHtmlComponent extends AbstractGenerateContentComponent {
  private static final long serialVersionUID = -7511578647689368225L;

  public JFreeReportDirectoryHtmlComponent() {
  }

  private File getInputFileValue(final String inputName) {
    final Object input = getInputValue(inputName);
    if (input == null) {
      return null;
    }
    if (input instanceof File) {
      return (File) input;
    }
    if (input instanceof String) {
      return new File((String) input);
    }
    return null;
  }

  @Override
  protected boolean performExport(final MasterReport report) {
    try {
      final File targetFile = getInputFileValue(AbstractJFreeReportComponent.REPORTDIRECTORYHTML_TARGETFILE);
      if (targetFile == null) {
        return false;
      }

      File dataDirectory = getInputFileValue(AbstractJFreeReportComponent.REPORTDIRECTORYHTML_DATADIR);
      if (dataDirectory == null) {
        dataDirectory = new File(targetFile, "data/"); //$NON-NLS-1$
      }

      final File targetDirectory = targetFile.getParentFile();
      if (dataDirectory.exists() && (dataDirectory.isDirectory() == false)) {
        dataDirectory = dataDirectory.getParentFile();
        if (dataDirectory.isDirectory() == false) {
          String msg = Messages.getErrorString("JFreeReportDirectoryComponent.ERROR_0001_INVALID_DIR", //$NON-NLS-1$
              dataDirectory.getPath());
          throw new ReportProcessingException(msg);
        }
      } else if (dataDirectory.exists() == false) {
        dataDirectory.mkdirs();
      }

      final FileRepository targetRepository = new FileRepository(targetDirectory);
      final ContentLocation targetRoot = targetRepository.getRoot();

      final FileRepository dataRepository = new FileRepository(dataDirectory);
      final ContentLocation dataRoot = dataRepository.getRoot();

      final FlowHtmlOutputProcessor outputProcessor = new FlowHtmlOutputProcessor(report.getConfiguration());

      final HtmlPrinter printer = new AllItemsHtmlPrinter(report.getResourceManager());
      printer.setContentWriter(targetRoot, new DefaultNameGenerator(targetRoot, targetFile.getName()));
      printer.setDataWriter(dataRoot, new DefaultNameGenerator(targetRoot, "content")); //$NON-NLS-1$
      printer.setUrlRewriter(new FileSystemURLRewriter());
      outputProcessor.setPrinter(printer);

      final FlowReportProcessor sp = new FlowReportProcessor(report, outputProcessor);
      final int yieldRate = getYieldRate();
      if (yieldRate > 0) {
        sp.addReportProgressListener(new YieldReportListener(yieldRate));
      }
      sp.processReport();
      sp.close();
      return true;
    } catch (ReportProcessingException e) {
      return false;
    } catch (ContentIOException e) {
      return false;
    }
  }
}
