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
import org.pentaho.mantle.client.commands.AnalysisViewCommand;
import org.pentaho.mantle.client.commands.OpenFileCommand;
import org.pentaho.mantle.client.commands.PrintCommand;
import org.pentaho.mantle.client.commands.SaveCommand;
import org.pentaho.mantle.client.commands.WAQRCommand;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.perspective.solutionbrowser.ReloadableIFrameTabPanel;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

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
  private boolean saveEnabled = false;
  private boolean saveAsEnabled = false;
  private boolean printEnabled = false;
  private boolean newAnalysisEnabled = false;
  private boolean contentEditEnabled = false;
  private boolean contentEditSelected = false;
  private boolean showBrowserSelected = false;
  private boolean workspaceSelected = false;

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
    OpenFileCommand openFileCommand = new OpenFileCommand(solutionBrowser);
    openFileCommand.execute();
  }

  @Bindable
  public void executeAnalysisViewCommand() {
    AnalysisViewCommand analysisViewCommand = new AnalysisViewCommand(
        solutionBrowser);
    analysisViewCommand.execute();
  }

  @Bindable
  public void executePrintCommand() {
    PrintCommand printCommand = new PrintCommand(solutionBrowser);
    printCommand.execute();
  }

  @Bindable
  public void executeSaveCommand() {
    SaveCommand saveCommand = new SaveCommand(solutionBrowser, false);
    saveCommand.execute();
  }

  @Bindable
  public void executeSaveAsCommand() {
    SaveCommand saveCommand = new SaveCommand(solutionBrowser, true);
    saveCommand.execute();
  }

  @Bindable
  public void executeWAQRCommand() {
    WAQRCommand wAQRCommand = new WAQRCommand(solutionBrowser);
    wAQRCommand.execute();
  }

  /**
   * Process incoming events from the SolutionBrowser here
   */
  public void solutionBrowserEvent(SolutionBrowserListener.EventType type,
      Widget panel, FileItem selectedFileItem) {
    String selectedTabURL = null;
    boolean saveEnabled = false;
    boolean editIsEnabled = false;
    boolean editSelected = false;

    if (panel != null && panel instanceof ReloadableIFrameTabPanel) {
      selectedTabURL = ((ReloadableIFrameTabPanel) panel).getUrl();
      saveEnabled = ((ReloadableIFrameTabPanel) panel).isSaveEnabled();
      editIsEnabled = ((ReloadableIFrameTabPanel) panel).isEditEnabled();
      editSelected = ((ReloadableIFrameTabPanel) panel).isEditSelected();
    }

    setPrintEnabled(selectedTabURL != null && !"".equals(selectedTabURL)); //$NON-NLS-1$
    setSaveEnabled(saveEnabled);
    setSaveAsEnabled(saveEnabled);
    setContentEditEnabled(editIsEnabled);
    setContentEditSelected(editSelected);

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

}
