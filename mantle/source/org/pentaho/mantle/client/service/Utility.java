/*
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
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
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.service;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

public class Utility {

  // if ($doc.onselectstart_default == null) {
  // $doc.onselectstart_default = $doc.onselectstart;
  // $doc.onmousedown_default = $doc.onmousedown;
  // }
  // $doc.onselectstart = function() {return false;} // ie
  // $doc.onmousedown = function() {return false;} // mozilla
  public static native void disableMouseSelection() /*-{
           }-*/;

  // $doc.onselectstart = $doc.onselectstart_default;
  // $doc.onmousedown = $doc.onmousedown_default;
  public static native void enableMouseSelection() /*-{
           }-*/;

  public static native void setBusyCursor() /*-{
                      $doc.body.style.cursor = "wait";
                      }-*/;

  public static native void setDefaultCursor() /*-{
                     $doc.body.style.cursor = "default";
                     }-*/;

  // redirect the browser to the given url
  public static native void redirect(String url)/*-{
                                               $wnd.location = url;
                                               }-*/;

  public static native String getWindowURL() /*-{
                         return $wnd.location;
                         }-*/;

  public static String getRequestPathAndParams() {
    String fullURL = Utility.getWindowURL();
    // 0123456
    // http://
    int start = 7;
    if (fullURL.startsWith("https")) {
      start = 8;
    }
    return fullURL.substring(fullURL.indexOf("/", start));
  }

  public static String getRequestParameter(String paramName) {
    String queryString = "" + getQueryString();
    int paramIndex = queryString.indexOf(paramName);
    if (paramIndex >= 0) {
      String paramString = queryString.substring(paramIndex);
      int ampIndex = paramString.indexOf('&');
      int eqIndex = paramString.indexOf('=');
      int poundIndex = paramString.indexOf("#");
      if (ampIndex >= 0 && eqIndex >= 0) {
        paramString = paramString.substring(eqIndex + 1, ampIndex);
        return paramString;
      } else if (eqIndex >= 0 && poundIndex == -1) {
        paramString = paramString.substring(eqIndex + 1);
        return paramString;
      } else if (eqIndex >= 0 && poundIndex >= 0) {
        paramString = paramString.substring(eqIndex + 1, poundIndex);
        return paramString;
      }
    }
    return "";
  }

  public static String getInternalLinkParameter() {
    String queryString = getWindowURL() + "";
    int poundIndex = queryString.indexOf("#");
    if (poundIndex >= 0) {
      return queryString.substring(poundIndex + 1);
    }
    return "";
  }

  public static String getQueryString() {
    try {
      String fullurl = "" + getWindowURL();
      if (fullurl.indexOf('?') >= 0) {
        String queryString = fullurl.substring(fullurl.indexOf('?') + 1);
        return "" + queryString;
      }
    } catch (Exception e) {
      // dead
    }
    return "";
  }

  public static String getBaseURL() {
    String url = getWindowURL() + "";
    String domain = getDomain();
    if (url.indexOf("https") == 0) {
      return "https://" + domain;
    }
    return "http://" + domain;
  }

  public static String getDomain() {
    // rip off http:// or https://
    String url = getWindowURL() + "";
    // 0123456
    // http://
    int start = 7;
    if (url.indexOf("https") == 0) {
      start = 8;
    }
    url = url.substring(start);
    // now rip off possible port server:port
    if (url.indexOf(":") != -1) {
      // we have a port
      url = url.substring(0, url.indexOf(":"));
    } else {
      url = url.substring(0, url.indexOf("/"));
    }
    return url;
  }

  public static HasHorizontalAlignment.HorizontalAlignmentConstant getHorizontalAlignment(String alignmentStr) {
    if ("left".equalsIgnoreCase(alignmentStr)) {
      return HasHorizontalAlignment.ALIGN_LEFT;
    } else if ("center".equalsIgnoreCase(alignmentStr)) {
      return HasHorizontalAlignment.ALIGN_CENTER;
    } else if ("right".equalsIgnoreCase(alignmentStr)) {
      return HasHorizontalAlignment.ALIGN_RIGHT;
    }
    return HasHorizontalAlignment.ALIGN_LEFT;
  }
  
  public static native void preventTextSelection(Element ele) /*-{
    ele.onselectstart=function() {return false};
  }-*/;

}
