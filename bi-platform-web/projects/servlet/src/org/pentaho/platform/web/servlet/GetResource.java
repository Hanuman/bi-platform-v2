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
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.servlet.messages.Messages;

public class GetResource extends ServletBase {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(final HttpServletRequest arg0, final HttpServletResponse arg1) throws ServletException, IOException {
    doPost(arg0, arg1);
  }

  private static final Log logger = LogFactory.getLog(GetResource.class);

  @Override
  public Log getLogger() {
    return GetResource.logger;
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    // TODO perform any authorization here...
    // TODO support caching
    IPentahoSession session = getPentahoSession(request);
    String resource = request.getParameter("resource"); //$NON-NLS-1$
    //String expires = request.getParameter("expires"); //$NON-NLS-1$
    //if ( null == expires )
    //{
    //	expires = "0";//$NON-NLS-1$
    //}
    if ((resource == null) || StringUtil.doesPathContainParentPathSegment( resource )) {
      error(Messages.getErrorString("GetResource.ERROR_0001_RESOURCE_PARAMETER_MISSING")); //$NON-NLS-1$
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      return;
    }
    String resLower = resource.toLowerCase();

    String resourcePath;
    if (resLower.endsWith(".xsl")) { //$NON-NLS-1$
      resourcePath = "system/custom/xsl/" + resource; //$NON-NLS-1$
    }
    else if (resLower.endsWith(".mondrian.xml")) { //$NON-NLS-1$
      resourcePath = resource;
    }
    else if (resLower.endsWith(".jpg") || resLower.endsWith(".jpeg") || resLower.endsWith(".gif") || resLower.endsWith(".png") || resLower.endsWith(".bmp")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
      resourcePath = resource;
    }
    else if (resLower.endsWith(".properties")) { //$NON-NLS-1$
      resourcePath = resource;
    }
    else if (resLower.endsWith(".jar")) { //$NON-NLS-1$
      resourcePath = resource;
    }
    else {
      error(Messages.getErrorString("GetResource.ERROR_0002_INVALID_FILE", resource)); //$NON-NLS-1$
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      return;
    }
    
    ISolutionRepository repository = PentahoSystem.getSolutionRepository(session);
    InputStream in = repository.getResourceInputStream(resourcePath, true);
    if (in == null) {
      error(Messages.getErrorString("GetResource.ERROR_0003_RESOURCE_MISSING", resourcePath)); //$NON-NLS-1$
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      return;
    }
    String mimeType = getServletContext().getMimeType(resourcePath);
    String resourceName = resourcePath;
    if (resourcePath.indexOf("/") != -1) { //$NON-NLS-1$
      resourceName = resourcePath.substring(resourcePath.lastIndexOf("/") + 1); //$NON-NLS-1$
    }
    response.setHeader("content-disposition", "attachment;filename=" + resourceName); //$NON-NLS-1$ //$NON-NLS-2$
    if ((null == mimeType) || (mimeType.length() <= 0)) {
      // Hard coded to PNG because BIRT does not give us a mime type at
      // all...
      response.setContentType("image/png"); //$NON-NLS-1$
    } else {
      response.setContentType(mimeType);
    }
    response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
    response.setHeader("expires", "0"); //$NON-NLS-1$ //$NON-NLS-2$
    // Open the input and output streams
    OutputStream out = response.getOutputStream();
    try {
      // Copy the contents of the file to the output stream
      byte[] buf = new byte[1024];
      int count = 0;
      int totalBytes = 0;
      while ((count = in.read(buf)) >= 0) {
        out.write(buf, 0, count);
        totalBytes += count;
      }
      response.setContentLength(totalBytes);
    } finally {
      in.close();
      out.close();
    }
  }
}
