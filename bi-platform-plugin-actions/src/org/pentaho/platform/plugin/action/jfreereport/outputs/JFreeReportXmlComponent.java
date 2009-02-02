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
import java.io.OutputStreamWriter;

import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.modules.output.xml.XMLProcessor;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 * 
 * @author Thomas Morgner
 */
public class JFreeReportXmlComponent extends AbstractGenerateStreamContentComponent {
  private static final long serialVersionUID = 8323789322309175815L;

  public JFreeReportXmlComponent() {
  }

  @Override
  protected String getMimeType() {
    return "text/xml"; //$NON-NLS-1$
  }

  @Override
  protected String getExtension() {
    return ".xml"; //$NON-NLS-1$
  }

  @Override
  protected boolean performExport(final MasterReport report, final OutputStream outputStream) {
    try {
      final XMLProcessor processor = new XMLProcessor(report);
      final OutputStreamWriter writer = new OutputStreamWriter(outputStream);
      processor.setWriter(writer);
      processor.processReport();

      writer.close();
      close();
      return true;
    } catch (ReportProcessingException e) {
      error(Messages.getString("JFreeReportXmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT"), e); //$NON-NLS-1$
      return false;
    } catch (IOException e) {
      error(Messages.getString("JFreeReportXmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT"), e); //$NON-NLS-1$
      return false;
    }
  }
}
