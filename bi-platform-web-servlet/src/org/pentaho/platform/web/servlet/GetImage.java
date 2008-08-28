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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.web.servlet.messages.Messages;

public class GetImage extends ServletBase {
  private static final long serialVersionUID = 119698153917362988L;

  private static final Log logger = LogFactory.getLog(GetImage.class);

  public GetImage() {
  }

  @Override
  protected void doGet(final HttpServletRequest arg0, final HttpServletResponse arg1) throws ServletException, IOException {
    doPost(arg0, arg1);
  }

  @Override
  public Log getLogger() {
    return GetImage.logger;
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    try {
      PentahoSystem.systemEntryPoint();

      // TODO perform any authorization here...
      final IPentahoSession userSession = getPentahoSession(request);
      final String user = request.getRemoteUser();
      if ((user != null) && !userSession.isAuthenticated()) {
        // the user was not logged in before but is now....
        userSession.setAuthenticated(user);
      }

      final String image = request.getParameter("image"); //$NON-NLS-1$
      if (image != null) {
        if (ServletBase.debug) {
          debug(Messages.getString("IMAGE.DEBUG_IMAGE_PARAMETER") + image); //$NON-NLS-1$
        }
      } else {
        error(Messages.getErrorString("IMAGE.ERROR_0001_IMAGE_PARAMETER_EMPTY")); //$NON-NLS-1$
        return;
      }

      // some sanity checks ...
      if ( StringUtil.doesPathContainParentPathSegment( image ) ) {
        error(Messages.getErrorString("IMAGE.ERROR_0002_FILE_NOT_FOUND", image)); //$NON-NLS-1$
        // we don't give hints that we check the parameter. Just return not
        // found.
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      final String tempDirectory = "system/tmp/"; //$NON-NLS-1$

      String location = (image.charAt(0) != '/') && (image.charAt(0) != '\\') ? tempDirectory + image : tempDirectory
          + image.substring(1);
      //      if (image.charAt(0) != '/' && image.charAt(0) != '\\') {
      //        file = new File(tempDirectory, image);
      //      } else {
      //        file = new File(tempDirectory, image.substring(1));
      //      }

      // paranoia: Check whether the new file is contained in the temp
      // directory.
      // an evil user could simply use "//" as parameter and would therefore
      // circument the test above ...
      //      IOUtils ioUtils = IOUtils.getInstance();
      //      if (ioUtils.isSubDirectory(tempDirectory, file) == false) {
      //        error(Messages.getErrorString("IMAGE.ERROR_0002_FILE_NOT_FOUND", image)); //$NON-NLS-1$
      //        // we dont give hints that we check the parameter. Just return not
      //        // found.
      //        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      //        return;
      //      }
      ISolutionRepository repository = PentahoSystem.getSolutionRepository(userSession);

      //    Open the file and output streams
      InputStream in = repository.getResourceInputStream(location, true);

      if (in == null) {
        error(Messages.getErrorString("IMAGE.ERROR_0002_FILE_NOT_FOUND", image)); //$NON-NLS-1$
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      String mimeType = getServletContext().getMimeType(image);
      if ((null == mimeType) || (mimeType.length() <= 0)) {
        // Hard coded to PNG because BIRT does not give us a mime type at
        // all...
        response.setContentType("image/png"); //$NON-NLS-1$
      } else {
        response.setContentType(mimeType);
      }
      OutputStream out = response.getOutputStream();
      try {
        byte buffer[] = new byte[2048];
        int n, length = 0;
        while ((n = in.read(buffer)) > 0) {
          out.write(buffer, 0, n);
          length += n;
        }
        response.setContentLength(length);
      } finally {
        in.close();
        out.close();
      }
    } finally {
      PentahoSystem.systemExitPoint();
    }

  }

}
