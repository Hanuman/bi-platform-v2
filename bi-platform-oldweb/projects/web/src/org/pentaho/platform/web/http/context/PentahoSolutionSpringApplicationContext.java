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
 * @created June 27, 2008
 * @author Angelo Rodriguez
 * 
 */
package org.pentaho.platform.web.http.context;

import java.io.File;

import org.pentaho.platform.web.http.PentahoHttpSessionHelper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class PentahoSolutionSpringApplicationContext extends XmlWebApplicationContext {

  protected Resource getResourceByPath(String path) {
    Resource resource = null;
    String solutionPath = PentahoHttpSessionHelper.getSolutionPath(getServletContext());
    if (solutionPath != null) {
      File file = new File(solutionPath + File.separator + "system" + File.separator + path);
      if (file.exists()) {
        resource = new FileSystemResource(file);
      }
    }
    if (resource == null) {
      resource = super.getResourceByPath(path);
    }
    return resource;
  }

}
