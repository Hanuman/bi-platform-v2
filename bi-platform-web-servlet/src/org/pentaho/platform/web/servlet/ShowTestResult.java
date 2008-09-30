/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Jul 26, 2005 
 * @author Gretchen Moran 
 * 
 */

package org.pentaho.platform.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.servlet.messages.Messages;

public class ShowTestResult extends ServletBase {

  /**
   * 
   */
  private static final long serialVersionUID = -360244121499172556L;

  @Override
  protected void doGet(final HttpServletRequest arg0, final HttpServletResponse arg1) throws ServletException, IOException {
    doPost(arg0, arg1);
  }

  private static final Log logger = LogFactory.getLog(ShowTestResult.class);

  @Override
  public Log getLogger() {
    return ShowTestResult.logger;
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
    PentahoSystem.systemEntryPoint();
    try {
    getPentahoSession(req);
    String fileName = req.getParameter("file"); //$NON-NLS-1$
    String extension = req.getParameter("ext"); //$NON-NLS-1$

    if (fileName == null) {
      error(Messages.getErrorString("TESTRESULT.ERROR_0001_FILE_PARAMETER_EMPTY")); //$NON-NLS-1$
      return;
    }
    fileName += extension;
    String filePath;
    if ((fileName.charAt(0) != '/') && (fileName.charAt(0) != '\\')) {
      filePath = PentahoSystem.getApplicationContext().getFileOutputPath("test/tmp/") + fileName; //$NON-NLS-1$
    } else {
      filePath = PentahoSystem.getApplicationContext().getFileOutputPath("test/tmp") + fileName; //$NON-NLS-1$
    }

    File file = new File(filePath);
    if ((!file.exists()) || (!file.isFile())) {
      error(Messages.getErrorString("IMAGE.ERROR_0002_FILE_NOT_FOUND", fileName)); //$NON-NLS-1$
      return;
    }

    res.setContentLength((int) file.length());

    String mimeType = getServletContext().getMimeType(fileName);
    if ((null != mimeType) && (mimeType.length() > 0)) {
      res.setContentType(mimeType);
    }

    // Open the file and output streams
    FileInputStream in = new FileInputStream(file);
    OutputStream out = res.getOutputStream();

    try {
      // Copy the contents of the file to the output stream
      byte[] buf = new byte[1024];
      int count = 0;
      while ((count = in.read(buf)) >= 0) {
        out.write(buf, 0, count);
      }
    } finally {
      in.close();
      out.close();
    }
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

}
