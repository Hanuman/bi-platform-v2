/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2009 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.engine.services.solution;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.MimeTypeListener;

public class ContentGeneratorUtil {
  /**
   * Convenience method for executing a content generator and getting back it's output as a string.
   * Useful for testing.
   * @param cg the content generator to execute
   * @return the output of the content generator
   * @throws Exception if there was a problem creating the content
   */
  public static String getContentAsString(IContentGenerator cg) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler(out, false);

    String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    parameterProviders.put(IParameterProvider.SCOPE_REQUEST, requestParams);
    SimpleUrlFactory urlFactory = new SimpleUrlFactory(baseUrl + "?"); //$NON-NLS-1$
    List<String> messages = new ArrayList<String>();
    cg.setOutputHandler(outputHandler);
    MimeTypeListener mimeTypeListener = new MimeTypeListener();
    outputHandler.setMimeTypeListener(mimeTypeListener);
    cg.setMessagesList(messages);
    cg.setParameterProviders(parameterProviders);
    cg.setSession(PentahoSessionHolder.getSession());
    cg.setUrlFactory(urlFactory);
    cg.createContent();
    String content = new String(out.toByteArray());
    return content;
  }
}
