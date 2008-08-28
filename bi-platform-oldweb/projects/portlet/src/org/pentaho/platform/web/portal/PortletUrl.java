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
 * @created Aug 3, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.web.portal;

import javax.portlet.PortletURL;

import org.pentaho.platform.api.engine.IPentahoUrl;

public class PortletUrl implements IPentahoUrl {

  private PortletURL portletURL;

  public PortletUrl(final PortletURL portletURL) {
    this.portletURL = portletURL;
  }

  public void setParameter(final String name, final String value) {
    portletURL.setParameter(name, value);
  }

  public String getUrl() {
    String url = portletURL.toString();
    if (url.indexOf('?') == -1) {
      return url + '?';
    } else {
      return url;
    }
  }

}
