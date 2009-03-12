/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
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
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;
import org.pentaho.platform.web.servlet.messages.Messages;

public class GetMondrianModel extends ServletBase {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(final HttpServletRequest arg0, final HttpServletResponse arg1) throws ServletException, IOException {
    doPost(arg0, arg1);
  }

  private static final Log logger = LogFactory.getLog(GetMondrianModel.class);

  @Override
  public Log getLogger() {
    return GetMondrianModel.logger;
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

    PentahoSystem.systemEntryPoint();
    try {
    // TODO perform any authorization here...
    getPentahoSession(request);

    String model = null;
    if (request.getParameter("model") != null) { //$NON-NLS-1$
      model = request.getParameter("model"); //$NON-NLS-1$
    }

    if (model == null) {
      error(Messages.getErrorString("MondrianModel.ERROR_0001_MODEL_PARAMETER_MISSING")); //$NON-NLS-1$
      return;
    }

    if (!model.endsWith(".mondrian.xml")) { //$NON-NLS-1$
      error(Messages.getErrorString("MondrianModel.ERROR_0002_INVALID_FILE", model)); //$NON-NLS-1$
      return;
    }

    // Open the input and output streams
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, PentahoHttpSessionHelper.getPentahoSession(request));
    if (repository != null) {
      String mimeType = "text/xml"; //$NON-NLS-1$
      response.setContentType(mimeType);
      response.setCharacterEncoding(LocaleHelper.getSystemEncoding());

      InputStream in = repository.getResourceInputStream(model, true);
      OutputStream out = response.getOutputStream();

      try {
        // Copy the contents of the file to the output stream
        byte[] buf = new byte[2048];
        int count = 0;
        int length = 0;
        while ((count = in.read(buf)) >= 0) {
          out.write(buf, 0, count);
          length += count;
        }
        response.setContentLength(length);
      } finally {
        in.close();
        out.close();
      }
    } else {
      error(Messages.getErrorString("MondrianModel.ERROR_0004_INVALID_REPOSITORY")); //$NON-NLS-1$
    }
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

}
