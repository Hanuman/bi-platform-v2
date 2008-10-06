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
