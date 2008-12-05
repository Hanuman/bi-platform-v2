package org.pentaho.mantle.client.toolbars;

import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.ui.xul.EventMethod;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

import com.google.gwt.user.client.Window;

/**
 * 
 * Warning: currently all methods must not take references. If  
 * 
 * @author NBaker
 */
public class MainToolbarController extends AbstractXulEventHandler{

  private MainToolbarModel model;
  private XulButton openBtn;
  private XulButton saveBtn;
  
  public MainToolbarController(MainToolbarModel model){
    this.model = model;
  }

  /** 
   * Called when the Xul Dom is ready, grab all Xul references here.
   */
  @EventMethod
  public void init(){
    Window.alert("onLoad");
    openBtn = (XulButton) document.getElementById("openButton");
    saveBtn = (XulButton) document.getElementById("saveButton");
    
  }

  @EventMethod
  public void openClicked(){
    Window.alert("hello");
  }
  
  
  
  public void setSaveEnabled(boolean flag){
    //called by the MainToolbarModel to change state.
    
  }

  @Override
  public String getName() {
    return "mainToolbarHandler";
  }

}

  