package org.pentaho.mantle.client.toolbars;

import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.utils.IMessageBundleLoadCallback;
import org.pentaho.gwt.widgets.client.utils.MessageBundle;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.perspective.solutionbrowser.IReloadableTabPanel;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.GwtXulLoader;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.tags.GwtToolbar;
import org.pentaho.ui.xul.gwt.tags.GwtToolbarbutton;
import org.pentaho.ui.xul.gwt.util.EventHandlerWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.xml.client.XMLParser;

public class XulMainToolbar extends SimplePanel implements IMessageBundleLoadCallback, SolutionBrowserListener{
  
  private MessageBundle bundle;
  
  private SolutionBrowserPerspective solutionBrowser;
  
  private MainToolbarModel model;
  
  private MainToolbarController controller;
  
  public XulMainToolbar(final SolutionBrowserPerspective solutionBrowser){
    this.solutionBrowser = solutionBrowser;
    model = new MainToolbarModel(solutionBrowser);
    controller = new MainToolbarController(model);
    
    //TODO: remove controller reference from model when Bindings in place
    model.setController(controller);
    
    
    //Load the message bundle for the xul file.
    try {
      bundle = new MessageBundle("messages/","messages", this );    //$NON-NLS-1$   //$NON-NLS-2$
    } catch (Exception e) {
      Window.alert("Error loading message bundle: "+e.getMessage());    //$NON-NLS-1$
      e.printStackTrace();
    }
  }
  
  /**
   * Called when the message bundle successfully loads. Here we request the XUL file from the sever
   * and then hand it off for loading into the app.
   */
  public void bundleLoaded(String bundleName) {
    try {
      RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "xul/main_toolbar.xul");    //$NON-NLS-1$

      try {
        Request response = builder.sendRequest(null, new RequestCallback() {
          public void onError(Request request, Throwable exception) {
            Window.alert("Error loading XUL: "+exception.getMessage());   //$NON-NLS-1$
          }

          public void onResponseReceived(Request request, Response response) {
            generateXul(response.getText());
          }
        });
      } catch (RequestException e) {
        Window.alert("error loading bundle: "+e.getMessage());    //$NON-NLS-1$
      }
    } catch (Exception e) {
      Window.alert("error loading bundle: "+e.getMessage());    //$NON-NLS-1$
      e.printStackTrace();
    }
  }

  /**
   * Takes the given XUL file, parses it and then grabs the Toolbar element for insertion into the 
   * application.
   */
  private void generateXul(String xul) {
    try {
      
      GwtXulLoader loader = new GwtXulLoader();
      GwtXulRunner runner = new GwtXulRunner();

      com.google.gwt.xml.client.Document gwtDoc = XMLParser.parse(xul);
      GwtXulDomContainer container;

      if(bundle != null){
        container = loader.loadXul(gwtDoc, bundle);
      } else {
        container = loader.loadXul(gwtDoc);
      }

      
      //handlers need to be wrapped genertically in GWT, create one and pass it our reference.
      EventHandlerWrapper wrapper = GWT.create(MainToolbarController.class);
      wrapper.setHandler(controller);

      //
      container.addEventHandler(wrapper);

      runner.addContainer(container);
      runner.initialize();

      Toolbar bar = (Toolbar) container.getDocumentRoot().getElementById("mainToolbar").getManagedObject();    //$NON-NLS-1$
      this.add(bar);
      bar.setStylePrimaryName("mainToolbar");    //$NON-NLS-1$
      
      //Fix for image locations in hosted mode. This really needs to be addressed
      if (!GWT.isScript()) {
        
        GwtToolbar toolbar = (GwtToolbar) container.getDocumentRoot().getElementById("mainToolbar");
        for(XulComponent c : toolbar.getChildNodes()){
          if(c instanceof XulToolbarbutton){
            GwtToolbarbutton btn = (GwtToolbarbutton) c;
            
            String curSrc = btn.getImage();
            btn.setImage(curSrc.replace("mantle/", ""));
            
            curSrc = btn.getDisabledImage();
            if(curSrc != null ){
              btn.setDisabledImage(curSrc.replace("mantle/", ""));
            }
          }
        }
      }

      RootPanel.get().add(runner.getRootPanel());
    } catch (Exception e) {
      Window.alert("Error generating XUL: "+e.getMessage());    //$NON-NLS-1$
      e.printStackTrace();
    }
  }
  public void solutionBrowserEvent(IReloadableTabPanel panel, FileItem selectedFileItem) {
      
  }
  
}

  