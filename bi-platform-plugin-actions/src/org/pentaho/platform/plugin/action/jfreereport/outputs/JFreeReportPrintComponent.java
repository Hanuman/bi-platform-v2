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

import javax.print.DocFlavor;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.modules.gui.print.PrintUtil;
import org.pentaho.reporting.engine.classic.extensions.modules.java14print.Java14PrintUtil;

/**
 * Creation-Date: 07.07.2006, 20:06:56
 * 
 * @author Thomas Morgner
 */
public class JFreeReportPrintComponent extends AbstractGenerateContentComponent {
  private static final long serialVersionUID = 3365941892457480119L;

  public JFreeReportPrintComponent() {
  }

  private PrintService findPrintService(final String name) {
    final PrintService[] services = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
    for (final PrintService service : services) {
      if (service.getName().equals(name)) {
        return service;
      }
    }

    if (services.length == 0) {
      return null;
    }
    return services[0];
  }

  @Override
  protected boolean performExport(final MasterReport report) {
    final String printerName = getInputStringValue(StandardSettings.PRINTER_NAME);
    final Object jobName = getActionTitle();

    if (jobName instanceof String) {
      report.getReportConfiguration().setConfigProperty(PrintUtil.PRINTER_JOB_NAME_KEY, String.valueOf(jobName));
    }

    final PrintService printer = findPrintService(printerName);
    try {
      Java14PrintUtil.printDirectly(report, printer);
    } catch (PrintException e) {
      return false;
    } catch (ReportProcessingException e) {
      return false;
    }
    return true;
  }
}
