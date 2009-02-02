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

import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.MasterReport;

/**
 * Creation-Date: 07.07.2006, 20:50:22
 * 
 * @author Thomas Morgner
 */
public abstract class AbstractGenerateStreamContentComponent extends AbstractGenerateContentComponent {

  private IContentItem contentItem = null;

  protected AbstractGenerateStreamContentComponent() {
  }

  @Override
  protected boolean validateAction() {
    if ( !(super.validateAction()) ) {
      return false;
    }

    if (isDefinedOutput(AbstractJFreeReportComponent.REPORTGENERATESTREAM_REPORT_OUTPUT)) {
      return true;
    }

    if (getOutputNames().size() == 1) {
      return true;
    }

    if (getOutputNames().size() == 0) {
      warn(Messages.getString("Base.WARN_NO_OUTPUT_STREAM")); //$NON-NLS-1$
      return true;
    }

    warn(Messages.getString("AbstractGenerateStreamContentComponent.JFreeReport.ERROR_0038_NO_OUTPUT_DEFINED")); //$NON-NLS-1$
    return false;
  }

  protected abstract String getMimeType();

  protected abstract String getExtension();

  @Override
  protected final boolean performExport(final MasterReport report) {
    OutputStream outputStream = createOutputStream();
    if (outputStream == null) {
      // We could not get an output stream for the content
      error(Messages.getErrorString("JFreeReport.ERROR_0008_INVALID_OUTPUT_STREAM")); //$NON-NLS-1$
      return false;
    }

    return performExport(report, outputStream);
  }

  protected final void close() {
    if (contentItem != null) {
      contentItem.closeOutputStream();
    }
  }

  protected abstract boolean performExport(final MasterReport report, final OutputStream outputStream);

  protected OutputStream createOutputStream() {
    // Try to get the output from the action-sequence document.
    final String mimeType = getMimeType();

    if (isDefinedOutput(AbstractJFreeReportComponent.REPORTGENERATESTREAM_REPORT_OUTPUT)) {
      contentItem = getOutputItem(AbstractJFreeReportComponent.REPORTGENERATESTREAM_REPORT_OUTPUT, mimeType,
          getExtension());
      try {
        contentItem.setMimeType(mimeType);
        return contentItem.getOutputStream(getActionName());
      } catch (Exception e) {
        return null;
      }
    } else if (getOutputNames().size() == 1) {
      String outputName = (String) getOutputNames().iterator().next();
      contentItem = getOutputContentItem(outputName, mimeType);
      try {
        contentItem.setMimeType(mimeType);
        return contentItem.getOutputStream(getActionName());
      } catch (Exception e) {
        return null;
      }
    }
    if (getOutputNames().size() == 0) {
      // There was no output in the action-sequence document, so make a
      // default
      // outputStream.
      final OutputStream outputStream = getDefaultOutputStream(mimeType);
      return outputStream;
    }

    return null;
  }

  protected IContentItem getContentItem() {
    return contentItem;
  }

}
