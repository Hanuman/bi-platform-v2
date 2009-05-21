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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 *
 * Created May 20, 2009
 * @author Aaron Phillips
 */

package org.pentaho.samples.gecho;

import java.io.OutputStream;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.pentaho.platform.engine.services.solution.SimpleContentGenerator;
import org.pentaho.platform.util.messages.LocaleHelper;

public class GEchoContentGenerator extends SimpleContentGenerator {

  @Override
  public void createContent(OutputStream out) throws Exception {
    try {
      StringBuilder html = new StringBuilder();
      html.append("<html>");
      html.append("<head>");
      html.append("<meta name='gwt:property' content='locale=en_UK'/>");
      html.append("<title>GWT Echo sample plugin client</title>");
      html.append("<script language=\"javascript\" src=\"/pentaho/content/gecho-res/gecho/gecho.nocache.js\"></script>");
      html.append("</head>");
      
      html.append("<body>");
      html.append("<img src=\"/pentaho/content/gecho-res/images/pentaho_logo.png\" />");
      html.append("<h1>GWT Echo sample plugin</h1>");
      html.append("<div id=\"gechodiv\"></div>");
      html.append("<iframe src=\"javascript:''\" id=\"__gwt_historyFrame\" style=\"width:0;height:0;border:0\"></iframe>");
      html.append("</body>");
      
      html.append("</html>");
      out.write(html.toString().getBytes(LocaleHelper.getSystemEncoding()));
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getMimeType() {
    return "text/html";
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog(GEchoContentGenerator.class);
  }

}
