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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 * 
*/
package org.pentaho.mantle.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.toolbars.MainToolbarController;
import org.pentaho.mantle.client.toolbars.MainToolbarModel;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.tags.GwtToolbar;
import org.pentaho.ui.xul.gwt.tags.GwtToolbarbutton;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.EventHandlerWrapper;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

public class XulMain extends SimplePanel implements IXulLoaderCallback{
  
  private Map<String, MantleXulOverlay> overlayMap = new HashMap<String, MantleXulOverlay>();  
  
  private MainToolbarModel model;
  
  private MainToolbarController controller;
  
  private GwtXulDomContainer container;
  
  private static XulMain _instance = null;
  
  private SolutionBrowserPerspective solutionBrowser;

  public static synchronized XulMain instance(final SolutionBrowserPerspective solutionBrowser) {
    if (null == _instance) {
      _instance = new XulMain(solutionBrowser);
    }
    return _instance;
  }
  
  public static XulMain getInstance() {
    return _instance;
  }
  
  protected XulMain(final SolutionBrowserPerspective solutionBrowser){
    this.solutionBrowser = solutionBrowser;
    //instantiate our Model and Controller
    controller = new MainToolbarController(solutionBrowser, new MainToolbarModel(solutionBrowser, this));
    
    // Invoke the async loading of the XUL DOM.
    AsyncXulLoader.loadXulFromUrl("xul/main_toolbar.xul", "messages/messages", this);
    
  }
  
  /**
   * Callback method for the MantleXulLoader. This is called when the Xul file has been processed.
   * 
   * @param runner GwtXulRunner instance ready for event handlers and initializing.
   */
  public void xulLoaded(GwtXulRunner runner)  {    
    
    // handlers need to be wrapped generically in GWT, create one and pass it our reference.
    EventHandlerWrapper wrapper = GWT.create(MainToolbarController.class);
    wrapper.setHandler(controller);
  
    // Add handler to container
    container = (GwtXulDomContainer) runner.getXulDomContainers().get(0);
    container.addEventHandler(wrapper);
  
    try{
      runner.initialize();
    } catch(XulException e){
      Window.alert("Error initializing XUL runner: "+e.getMessage());    //$NON-NLS-1$
      e.printStackTrace();
      return;
    }

    //TODO: remove controller reference from model when Bindings in place
    model = new MainToolbarModel(solutionBrowser, this);
    controller.setModel(model);
    controller.setSolutionBrowser(solutionBrowser);
    
  
    // Get the toolbar from the XUL doc
    Toolbar bar = (Toolbar) container.getDocumentRoot().getElementById("mainToolbar").getManagedObject();    //$NON-NLS-1$
    bar.setStylePrimaryName("mainToolbar");    //$NON-NLS-1$
    this.add(bar);

    //unfortunately hosted mode won't resolve the image with 'mantle/' in it
    cleanImageUrlsForHostedMode();
    
    
    AsyncCallback<List<MantleXulOverlay>> callback = new AsyncCallback<List<MantleXulOverlay>>() {

      public void onFailure(Throwable caught) {
        Window.alert("Error fetching XulOverlay list\n "+caught.toString());
      }

      public void onSuccess(List<MantleXulOverlay> overlays) {
        
        XulMain.getInstance().loadOverlays(overlays);
      }
    };
    MantleServiceCache.getService().getOverlays(callback);    
    

    
  }
  
  
  private void cleanImageUrlsForHostedMode(){
    if (!GWT.isScript()) {
      
      GwtToolbar toolbar = (GwtToolbar) container.getDocumentRoot().getElementById("mainToolbar");  //$NON-NLS-1$
      for(XulComponent c : toolbar.getChildNodes()){
        if(c instanceof XulToolbarbutton){
          GwtToolbarbutton btn = (GwtToolbarbutton) c;
          
          String curSrc = btn.getImage();
          btn.setImage(curSrc.replace("mantle/", ""));    //$NON-NLS-1$ //$NON-NLS-2$
          
          curSrc = btn.getDisabledImage();
          if(curSrc != null ){
            btn.setDisabledImage(curSrc.replace("mantle/", ""));    //$NON-NLS-1$ //$NON-NLS-2$
          }
          curSrc = btn.getDownimage();
          if(curSrc != null ){
            btn.setDownimage(curSrc.replace("mantle/", ""));    //$NON-NLS-1$ //$NON-NLS-2$
          }
          curSrc = btn.getDownimagedisabled();
          if(curSrc != null ){
            btn.setDownimagedisabled(curSrc.replace("mantle/", ""));    //$NON-NLS-1$ //$NON-NLS-2$
          }
        }
      }
    }
  }
  
  public void overlayLoaded(){
    cleanImageUrlsForHostedMode();
  } 
  
  public void loadOverlays(List<MantleXulOverlay> overlays) {
    for(MantleXulOverlay overlay: overlays) {
      overlayMap.put(overlay.getId(), overlay);
      if(overlay.getId().startsWith("startup")){
        AsyncXulLoader.loadOverlayFromSource(overlay.getSource(), overlay.getResourceBundleUri(), container, this);
      }
    }
  }

  public void applyOverlays(Set<String> overlayIds) {
    if(overlayIds != null && !overlayIds.isEmpty()) {
      for (String overlayId : overlayIds) {
        applyOverlay(overlayId);
      }
    }
  }
  public void applyOverlay(String id) {
    if(overlayMap != null && !overlayMap.isEmpty()) {
      if(overlayMap.containsKey(id)) {
        MantleXulOverlay overlay = overlayMap.get(id); 
        AsyncXulLoader.loadOverlayFromSource(overlay.getOverlayXml(), overlay.getResourceBundleUri(), container, this);
      } else {
        // Should I log this or throw an exception here
      }
    }
  }

  public void removeOverlays(Set<String> overlayIds) {
    if(overlayIds != null && !overlayIds.isEmpty()) {
      for (String overlayId : overlayIds) {
        removeOverlay(overlayId);
      }
    }
  }
  public void removeOverlay(String id) {
    if(overlayMap != null && !overlayMap.isEmpty()) {    
      if(overlayMap.containsKey(id)) {
        MantleXulOverlay overlay = overlayMap.get(id); 
        AsyncXulLoader.removeOverlayFromSource(overlay.getOverlayXml(), overlay.getResourceBundleUri(), container, this);
      } else {
        // Should I log this or throw an exception here
      }
    }
  }

  public void overlayRemoved() {
    // TODO Auto-generated method stub
    
  }
  
  public void registerContentCallback(JavaScriptObject obj){
    this.controller.addJSCallback(obj);
  }
  
}

  