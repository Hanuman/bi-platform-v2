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
package org.pentaho.mantle.client.toolbars;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.mantle.client.commands.ShowBrowserCommand;
import org.pentaho.mantle.client.commands.ToggleWorkspaceCommand;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.ui.xul.EventMethod;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * 
 * Warning: currently all methods must not take references. If  
 * 
 * @author NBaker
 */
public class MainToolbarController extends AbstractXulEventHandler{

  private MainToolbarModel model;
  private XulToolbarbutton openBtn;
  private XulToolbarbutton saveBtn;
  private XulToolbarbutton saveAsBtn;
  private XulToolbarbutton newAdhocBtn;
  private XulToolbarbutton newAnalysisBtn;
  private XulToolbarbutton printBtn;
  private XulToolbarbutton workspaceBtn;
  private XulToolbarbutton showBrowserBtn;
  private XulToolbarbutton contentEditBtn;
  private SolutionBrowserPerspective solutionBrowser;
  
  private List<JavaScriptObject> callbacks = new ArrayList<JavaScriptObject>();
  
  
  public MainToolbarController(SolutionBrowserPerspective solutionBrowser, MainToolbarModel model){
    this.solutionBrowser = solutionBrowser;
    this.model = model;
  }

  /** 
   * Called when the Xul Dom is ready, grab all Xul references here.
   */
  @EventMethod
  public void init(){
    openBtn = (XulToolbarbutton) document.getElementById("openButton");
    saveBtn = (XulToolbarbutton) document.getElementById("saveButton");
    saveAsBtn = (XulToolbarbutton) document.getElementById("saveAsButton");
    newAdhocBtn = (XulToolbarbutton) document.getElementById("newAdhocButton");
    newAnalysisBtn = (XulToolbarbutton) document.getElementById("newAnalysisButton");
    printBtn = (XulToolbarbutton) document.getElementById("printButton");
    workspaceBtn = (XulToolbarbutton) document.getElementById("workspaceButton");
    showBrowserBtn = (XulToolbarbutton) document.getElementById("showBrowserButton");
    contentEditBtn = (XulToolbarbutton) document.getElementById("editContentButton");
    
    BindingFactory bf = new GwtBindingFactory(this.document);
    bf.createBinding(model, "saveEnabled", saveBtn, "!disabled");
    bf.createBinding(model, "saveAsEnabled", saveAsBtn, "!disabled");
    bf.createBinding(model, "printEnabled", printBtn, "!disabled");
    bf.createBinding(model, "contentEditEnabled", contentEditBtn, "!disabled");
    bf.createBinding(model, "contentEditSelected", this, "editContentSelected");
    
    
  }
  
  public void setEditContentSelected(boolean selected){
    contentEditBtn.setSelected(selected, false);
  }

  @EventMethod
  public void openClicked(){
    model.executeOpenFileCommand();
  }

  @EventMethod
  public void newAnalysisClicked(){
    model.executeAnalysisViewCommand();
  }
  
  @EventMethod
  public void newAdhocClicked(){
    model.executeWAQRCommand();
  }

  @EventMethod
  public void printClicked(){
    model.executePrintCommand();
  }
  
  @EventMethod
  public void saveClicked(){
    model.executeSaveCommand();
  }
  
  @EventMethod
  public void saveAsClicked(){
    model.executeSaveAsCommand();
  }
  
  @EventMethod
  public void workspaceClicked(){

    ToggleWorkspaceCommand toggleWorkspaceCommand = new ToggleWorkspaceCommand(solutionBrowser);
    toggleWorkspaceCommand.execute();
    model.setWorkspaceSelected(solutionBrowser.isWorkspaceShowing());
  }  
  
  @EventMethod
  public void showBrowserClicked(){
    ShowBrowserCommand showBrowserCommand = new ShowBrowserCommand(solutionBrowser);
    showBrowserCommand.execute();
    model.setShowBrowserSelected(solutionBrowser.isExplorerViewShowing());
  }  
  
  public void setShowBrowserSelected(boolean flag) {
    //called by the MainToolbarModel to change state.
    showBrowserBtn.setSelected(flag);
  }
  
  public void setWorkspaceSelected(boolean flag) {
    //called by the MainToolbarModel to change state.
    workspaceBtn.setSelected(flag);
  }
  
  public void setSaveEnabled(boolean flag){
    //called by the MainToolbarModel to change state.
    saveBtn.setDisabled(!flag); 
  }

  public void setSaveAsEnabled(boolean flag){
    //called by the MainToolbarModel to change state.
    saveAsBtn.setDisabled(!flag); 
  }

  public void setPrintEnabled(boolean flag){
    //called by the MainToolbarModel to change state.
    printBtn.setDisabled(!flag); 
  }
  
  public void setNewAnalysisEnabled(boolean flag){
    //called by the MainToolbarModel to change state.
    newAnalysisBtn.setDisabled(!flag); 
  }
  @Override
  public String getName() {
    return "mainToolbarHandler";
  }
  
  public void executeCallback(String jsScript){
    for(JavaScriptObject callback : callbacks){
      executeJS(callback, jsScript);
    }
  }
  public void executeMantleFunc(String funct){
    executeMantleCall(funct);
  }
  

  private native void executeMantleCall(String js)/*-{
    try{
      $wnd.eval(js);
    } catch (e){
      $wnd.mantle_showMessage("Javascript Error",e.message+"\n\n"+js);
      
    }
  }-*/;
  
  
  private native void executeJS(JavaScriptObject obj, String js)/*-{
    try{
      var tempObj = obj;
      eval("tempObj."+js);
    } catch (e){
      $wnd.mantle_showMessage("Javascript Error",e.message+"          "+"tempObj."+js);
    }
  }-*/;
  
  public native void openUrl(String title, String name, String uri)/*-{
    try{
      $wnd.eval("openURL('"+name+"','"+title+"','"+uri+"')");
    } catch (e){
      $wnd.mantle_showMessage("Javascript Error",e.message);
    }
  }-*/;

  public void addJSCallback(JavaScriptObject obj){
    callbacks.add(obj);
  }
  
  public void setContentEditEnabled(boolean enable){
    contentEditBtn.setDisabled(!enable);
  }
  
  public void setContentEditSelected(boolean selected){
    contentEditBtn.setSelected(selected);  
  }
  
  public void editContentClicked(){
    for(JavaScriptObject callback : callbacks){
      model.setContentEditToggled();
      executeEditContentCallback(callback, model.isContentEditSelected());
    }
  }

  private native void executeEditContentCallback(JavaScriptObject obj, boolean selected)/*-{
    try{
      obj.editContentToggled(selected);
    } catch (e){}
  }-*/;

  public MainToolbarModel getModel() {
  
    return model;
  }

  public void setModel(MainToolbarModel model) {
  
    this.model = model;
  }

  public SolutionBrowserPerspective getSolutionBrowser() {
  
    return solutionBrowser;
  }

  public void setSolutionBrowser(SolutionBrowserPerspective solutionBrowser) {
  
    this.solutionBrowser = solutionBrowser;
  }
  
  
}

  