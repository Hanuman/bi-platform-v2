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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.perspective.solutionbrowser;

import java.util.Stack;

import org.pentaho.mantle.client.objects.SolutionFileInfo;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ReloadableIFrameTabPanel extends VerticalPanel implements IReloadableTabPanel {

  String url;
  CustomFrame frame;
  protected SolutionFileInfo fileInfo;
  protected FormPanel form;
  protected boolean saveEnabled;
  
  public ReloadableIFrameTabPanel(String url) {
    this.url = url;
    frame = new CustomFrame(""+System.currentTimeMillis(), url); //$NON-NLS-1$
    add(frame);
  }  
  
  public ReloadableIFrameTabPanel(String name, String url) {
    this.url = url;
    
    setSaveEnabled(url.endsWith("analysisview.xaction")); //$NON-NLS-1$
    
    frame = new CustomFrame(name, url);
    add(frame);
  }

  public void reload() {

    if(form != null){
      form.submit();
    } else {
      frame.setUrl(
          getCurrentUrl()
      );
    }
  }

  public void back(){
    frame.back();
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
    Window.open(getCurrentUrl(), "_blank", ""); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public NamedFrame getFrame() {
    return frame;
  }

  public void setFrame(CustomFrame frame) {
    this.frame = frame;
  }
  
  public FormPanel getForm() {
    return form;
  }

  public void setForm(FormPanel form) {
    this.form = form;
  }
  
  public class CustomFrame extends NamedFrame{
    private boolean ignoreNextHistoryAdd = false;
    private Stack<String> history = new Stack<String>();
    
    private CustomFrame(String name){
      super(name);
    }
    
    private CustomFrame(String name, String url){
      super(name);
      setUrl(url);
    }
    
    public void back(){
      if(!history.empty()){
        ignoreNextHistoryAdd = true;
        frame.setUrl(history.pop());
      }
    }
    
    
    
    public void addHistory(String url){
      if(ignoreNextHistoryAdd || url.equals("about:blank")){  //$NON-NLS-1$
        ignoreNextHistoryAdd = false;
        return;
      }
      history.add(url);
    }
    
    
    
    @Override
    protected void onAttach() {
      super.onAttach();
      attachEventListeners(frame.getElement(), this);
    }

    public native void attachEventListeners(Element ele, CustomFrame frame)/*-{
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
        frame.@org.pentaho.mantle.client.perspective.solutionbrowser.ReloadableIFrameTabPanel$CustomFrame::addHistory(Ljava/lang/String;)(iwind.location.href);
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

  public boolean isSaveEnabled() {
    return saveEnabled;
  }

  public void setSaveEnabled(boolean enabled) {
    saveEnabled = enabled;  
  }
}
