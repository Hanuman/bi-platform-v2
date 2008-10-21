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
 */
package org.pentaho.mantle.client.perspective.solutionbrowser;

import org.pentaho.mantle.client.commands.AnalysisViewCommand;
import org.pentaho.mantle.client.commands.ManageContentCommand;
import org.pentaho.mantle.client.commands.WAQRCommand;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class LaunchPanel extends Frame {
  Image launchWaqrImage;
  Image launchAnalysisViewImage;
  Image manageContentImage;
  SolutionBrowserPerspective perspective;

  public LaunchPanel(SolutionBrowserPerspective perspective) {

    
    this.perspective = perspective;

    String url = "mantle/launch/launch.jsp"; //$NON-NLS-1$
    if (GWT.isScript()) {
      
      String mypath = Window.Location.getPath();
      if (!mypath.endsWith("/")) { //$NON-NLS-1$
        mypath = mypath.substring(0, mypath.lastIndexOf("/") + 1); //$NON-NLS-1$
      }
      mypath = mypath.replaceAll("/mantle/", "/"); //$NON-NLS-1$ //$NON-NLS-2$
      if (!mypath.endsWith("/")) { //$NON-NLS-1$
        mypath = "/" + mypath; //$NON-NLS-1$
      }    
      url = mypath + url;
    } else {
      url = "http://localhost:8080/pentaho/mantle/launch/launch.jsp?userid=joe&password=password"; //$NON-NLS-1$
    }
    this.setUrl(url);

  }
  
  @Override
  protected void onLoad() {
    hookNativeEvents(this, this.getElement());
  }
  
  private native void hookNativeEvents(LaunchPanel panel, Element ele)/*-{
    $wnd.openWAQR = function(){
      panel.@org.pentaho.mantle.client.perspective.solutionbrowser.LaunchPanel::openWAQR()();
    }
    $wnd.openAnalysis = function(){
      panel.@org.pentaho.mantle.client.perspective.solutionbrowser.LaunchPanel::openAnalysis()();
    }
    $wnd.openManage = function(){
      panel.@org.pentaho.mantle.client.perspective.solutionbrowser.LaunchPanel::openManage()();
    }
    var iwind = ele.contentWindow;
    
  
    var funct = function(event){
      event = iwind.parent.translateInnerMouseEvent(ele, event);
      iwind.parent.sendMouseEvent(event);
    }  
    
    // Hooks up mouse and unload events
    try{
      iwind.onmouseup = funct;
      iwind.onmousedown = funct;
      iwind.onmousemove = funct;
      
    } catch(e){
      //You're most likely here because of Cross-site scripting permissions... consuming
    }
  }-*/;
  
  public void openWAQR(){
    new WAQRCommand(perspective).execute();
  }

  public void openAnalysis(){
    new AnalysisViewCommand(perspective).execute();
  }

  public void openManage(){
    new ManageContentCommand(perspective).execute();
  }

}
