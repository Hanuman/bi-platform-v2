package org.pentaho.mantle.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.toolbars.MainToolbarController;
import org.pentaho.mantle.client.toolbars.MainToolbarModel;
import org.pentaho.platform.api.engine.IXulOverlay;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.tags.GwtToolbar;
import org.pentaho.ui.xul.gwt.tags.GwtToolbarbutton;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.EventHandlerWrapper;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SimplePanel;

public class XulMain extends SimplePanel implements IXulLoaderCallback{
  
  private Map<String, IXulOverlay> overlayMap = new HashMap<String, IXulOverlay>();  
  
  private MainToolbarModel model;
  
  private MainToolbarController controller;
  
  private GwtXulDomContainer container;
  
  private static XulMain _instance = null;

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
    
    //instantiate our Model and Controller
    model = new MainToolbarModel(solutionBrowser, this);
    controller = new MainToolbarController(model);
    //TODO: remove controller reference from model when Bindings in place
    model.setController(controller);
    
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
  
    // Get the toolbar from the XUL doc
    Toolbar bar = (Toolbar) container.getDocumentRoot().getElementById("mainToolbar").getManagedObject();    //$NON-NLS-1$
    bar.setStylePrimaryName("mainToolbar");    //$NON-NLS-1$
    this.add(bar);

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
        }
      }
    }
  }
  
  public void overlayLoaded(){
    
  } 
  
  public void loadOverlays(List<IXulOverlay> overlays) {
    for(IXulOverlay overlay: overlays) {
      overlayMap.put(overlay.getId(), overlay);
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
        IXulOverlay overlay = overlayMap.get(id); 
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
        IXulOverlay overlay = overlayMap.get(id); 
        AsyncXulLoader.removeOverlayFromSource(overlay.getOverlayXml(), overlay.getResourceBundleUri(), container, this);
      } else {
        // Should I log this or throw an exception here
      }
    }
  }

  public void overlayRemoved() {
    // TODO Auto-generated method stub
    
  }
  
}

  