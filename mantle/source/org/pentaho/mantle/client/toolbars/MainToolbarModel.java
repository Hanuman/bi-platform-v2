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

import org.pentaho.mantle.client.XulMain;
import org.pentaho.mantle.client.commands.OpenFileCommand;
import org.pentaho.mantle.client.commands.PrintCommand;
import org.pentaho.mantle.client.commands.SaveCommand;
import org.pentaho.mantle.client.solutionbrowser.FileItem;
import org.pentaho.mantle.client.solutionbrowser.ReloadableIFrameTabPanel;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;

/**
 * State model for the main toolbar. Replace controller code calls with Bindings
 * when available.
 * 
 * @author NBaker
 */
public class MainToolbarModel extends XulEventSourceAdapter implements
    SolutionBrowserListener {

  private SolutionBrowserPerspective solutionBrowser;
  private XulMain main;
  private boolean saveEnabled;
  private boolean saveAsEnabled;
  private boolean printEnabled;
  private boolean newAnalysisEnabled;
  private boolean contentEditEnabled;
  private boolean contentEditSelected;
  private boolean showBrowserSelected;
  private boolean workspaceSelected;
  private JavaScriptObject callback;
  
  public MainToolbarModel(final SolutionBrowserPerspective solutionBrowser,
      XulMain main) {
    this.solutionBrowser = solutionBrowser;
    this.solutionBrowser.addSolutionBrowserListener(this);
    this.main = main;
  }

  @Bindable
  public void setSaveEnabled(Boolean enabled) {
    boolean prevVal = this.saveEnabled;
    saveEnabled = enabled;
    this.firePropertyChange("saveEnabled", prevVal, saveEnabled);
  }

  @Bindable
  public boolean isSaveEnabled() {
    return this.saveEnabled;
  }

  @Bindable
  public void setSaveAsEnabled(Boolean enabled) {
    boolean prevVal = this.saveAsEnabled;
    saveAsEnabled = enabled;

    this.firePropertyChange("saveAsEnabled", prevVal, saveAsEnabled);
  }

  @Bindable
  public void setPrintEnabled(Boolean enabled) {
    boolean prevVal = this.printEnabled;
    printEnabled = enabled;

    this.firePropertyChange("printEnabled", prevVal, enabled);
  }

  @Bindable
  public void setNewAnalysisEnabled(Boolean enabled) {
    boolean prevVal = this.newAnalysisEnabled;
    newAnalysisEnabled = enabled;

    this.firePropertyChange("newAnalysisEnabled", prevVal, newAnalysisEnabled);
  }

  @Bindable
  public void executeOpenFileCommand() {
    OpenFileCommand openFileCommand = new OpenFileCommand();
    openFileCommand.execute();
  }

  @Bindable
  public void executeAnalysisViewCommand() {
    Command analysisViewCommand = solutionBrowser.getNewAnalysisViewCommand();
    analysisViewCommand.execute();
  }

  @Bindable
  public void executePrintCommand() {
    PrintCommand printCommand = new PrintCommand();
    printCommand.execute();
  }

  @Bindable
  public void executeSaveCommand() {
    SaveCommand saveCommand = new SaveCommand(false);
    saveCommand.execute();
  }

  @Bindable
  public void executeSaveAsCommand() {
    SaveCommand saveCommand = new SaveCommand(true);
    saveCommand.execute();
  }

  @Bindable
  public void executeWAQRCommand() {
    Command wAQRCommand = solutionBrowser.getNewReportCommand();
    wAQRCommand.execute();
  }

  /**
   * Process incoming events from the SolutionBrowser here
   * 
   * @TODO Move this listener to a controller where it really belongs, models shouldn't do this.
   */
  public void solutionBrowserEvent(SolutionBrowserListener.EventType type,
      Widget panel, FileItem selectedFileItem) {
    String selectedTabURL = null;
    boolean saveEnabled = false;
    boolean editIsEnabled = false;
    boolean editSelected = false;
    JavaScriptObject callback = null;

    if (panel != null && panel instanceof ReloadableIFrameTabPanel) {
      ReloadableIFrameTabPanel tbp = (ReloadableIFrameTabPanel) panel;
      selectedTabURL = tbp.getUrl();
      saveEnabled = tbp.isSaveEnabled();
      editIsEnabled = tbp.isEditEnabled();
      editSelected = tbp.isEditSelected();
      callback = tbp.getContentCallback();
    }

    setPrintEnabled(selectedTabURL != null && !"".equals(selectedTabURL)); //$NON-NLS-1$
    setSaveEnabled(saveEnabled);
    setSaveAsEnabled(saveEnabled);
    setContentEditEnabled(editIsEnabled);
    setContentEditSelected(editSelected);
    setCallback(callback);

    setWorkspaceSelected(solutionBrowser.isWorkspaceShowing());
    setShowBrowserSelected(solutionBrowser.isNavigatorShowing());

    if (panel instanceof ReloadableIFrameTabPanel) {
      if (SolutionBrowserListener.EventType.OPEN.equals(type)
          || SolutionBrowserListener.EventType.SELECT.equals(type)) {
        if (panel != null) {
          main
              .applyOverlays(((ReloadableIFrameTabPanel) panel).getOverlayIds());
        }
      } else if (SolutionBrowserListener.EventType.CLOSE.equals(type)
          || SolutionBrowserListener.EventType.DESELECT.equals(type)) {
        if (panel != null) {
          main.removeOverlays(((ReloadableIFrameTabPanel) panel)
              .getOverlayIds());
        }
      }
    }
  }

  @Bindable
  public boolean isShowBrowserSelected() {
    return showBrowserSelected;
  }

  @Bindable
  public boolean isWorkspaceSelected() {
    return workspaceSelected;
  }

  @Bindable
  public void setShowBrowserSelected(boolean showBrowserSelected) {
    boolean prevVal = this.showBrowserSelected;

    this.showBrowserSelected = showBrowserSelected;
    this
        .firePropertyChange("showBrowserSelected", prevVal, showBrowserSelected);
  }

  @Bindable
  public void setWorkspaceSelected(boolean workspaceSelected) {
    boolean prevVal = this.workspaceSelected;

    this.workspaceSelected = workspaceSelected;
    this.firePropertyChange("workspaceSelected", prevVal, workspaceSelected);
  }

  @Bindable
  public void setContentEditEnabled(boolean enable) {
    boolean prevVal = this.contentEditEnabled;
    contentEditEnabled = enable;
    this.firePropertyChange("contentEditEnabled", prevVal, contentEditEnabled);
  }

  @Bindable
  public void setContentEditSelected(boolean selected) {
    boolean prevVal = this.contentEditSelected;
    contentEditSelected = selected;
    this
        .firePropertyChange("contentEditSelected", prevVal, contentEditSelected);
  }

  @Bindable
  public boolean isContentEditSelected() {
    return this.contentEditSelected;
  }

  @Bindable
  public void setContentEditToggled() {
    setContentEditSelected(!this.contentEditSelected);
  }

  @Bindable
  public boolean isContentEditEnabled() {
    return contentEditEnabled;
  }

  public JavaScriptObject getCallback() {
    return callback;
  }

  public void setCallback(JavaScriptObject callback) {
    this.callback = callback;
  }
  
}
