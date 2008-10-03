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
package org.pentaho.mantle.client.perspective.solutionbrowser;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.pentaho.mantle.client.objects.SolutionFileInfo;

public class ReloadableIFrameTabPanel extends VerticalPanel implements IReloadableTabPanel {

  String url;
  NamedFrame frame;
  protected SolutionFileInfo fileInfo;
  
  public ReloadableIFrameTabPanel(String url) {
    this.url = url;
    frame = new CustomFrame(""+System.currentTimeMillis(), url);
    add(frame);
  }  
  
  public ReloadableIFrameTabPanel(String name, String url) {
    this.url = url;
    frame = new CustomFrame(name, url);
    add(frame);
  }

  public void reload() {
    this.frame.setUrl(
        getCurrentUrl()
    );
  }
  
  public void setFileInfo(SolutionFileInfo info){
    fileInfo = info;
  }
  
  public SolutionFileInfo getFileInfo(){
    return fileInfo;
  }
  
  /*
   * frame.getUrl returns the original URL, but not the current one. This method accesses the
   * DOM directly to get that URL
   */
  private String getCurrentUrl(){
    return IFrameElement.as(this.frame.getElement()).getContentDocument().getURL();
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void openTabInNewWindow() {
    Window.open(getCurrentUrl(), "_blank", "");
  }

  public NamedFrame getFrame() {
    return frame;
  }

  public void setFrame(NamedFrame frame) {
    this.frame = frame;
  }
  
  public class CustomFrame extends NamedFrame{
    private CustomFrame(String name){
      super(name);
    }
    
    private CustomFrame(String name, String url){
      super(name);
      setUrl(url);
    }
    
    public native void attachEventListeners(Element ele)/*-{
      var iwind = ele.contentWindow; //IFrame's window instance
      
      var funct = function(event){
        event = iwind.parent.translateInnerMouseEvent(ele, event);
        iwind.parent.sendMouseEvent(event);
      }  
      
      // Hooks up mouse and unload events
      $wnd.hookEvents = function(wind){
        try{
          if(wind == null){
            wind = $wnd.watchWindow
          }
          wind.onmouseup = funct;
          wind.onmousedown = funct;
          wind.onmousemove = funct;
          
          wind.onunload = unloader;
          wind.mantleEventsIn = true;
          $wnd.watchWindow = null;
        } catch(e){
          //You're most likely here because of Cross-site scripting permissions... consuming
        }
      }
      
      // IFrame URL watching code
      
      // Called on iFrame unload, calls containing Window to start monitoring it for Url change
      var unloader = function(event){
        $wnd.startIFrameWatcher(iwind);
      }
      
      // Starts the watching loop.
      $wnd.startIFrameWatcher = function(wind){
        $wnd.watchWindow = wind;
        $wnd.setTimeout("rehookEventsTimer()", 300);
      }
    
      // loop that's started when an iFrame unloads, when the url changes it adds back in the hooks
      $wnd.rehookEventsTimer = function(){
        if($wnd.watchWindow.mantleEventsIn == undefined){
          //location changed hook back up event interceptors
          $wnd.setTimeout("hookEvents()", 300);
        } else {
          $wnd.setTimeout("rehookEventsTimer()", 300);
        }
      }
      
      // Scope helper funct.
      function rehookEventsTimer(){
        $wnd.rehookEventsTimer();
      }
      
      //Hook up the mouse and unload event handlers for iFrame being created
      $wnd.hookEvents(iwind);
      
      
      
    }-*/;
  }
}
