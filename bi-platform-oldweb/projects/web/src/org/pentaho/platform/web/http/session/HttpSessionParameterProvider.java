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
 * @created Jul 11, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.web.http.session;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;

public class HttpSessionParameterProvider extends SimpleParameterProvider {

    private IPentahoSession session;

    public HttpSessionParameterProvider(final IPentahoSession session) {
        this.session = session;
    }

    @Override
    public Object getParameter(final String name) {
      if ("name".equals(name)) { //$NON-NLS-1$
          return session.getName();
      }
      return session.getAttribute(name);
    }

    @Override
    public String getStringParameter(final String name, final String defaultValue) {
      Object value = getParameter(name);
      if (value != null) {
        return value.toString();
      }
      return defaultValue;
    }

}
