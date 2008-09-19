package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.user.client.Command;

public class ToggleWorkspaceCommand implements Command {

  SolutionBrowserPerspective solutionBrowserPerspective;
  
  public ToggleWorkspaceCommand(SolutionBrowserPerspective solutionBrowserPerspective) {
    this.solutionBrowserPerspective = solutionBrowserPerspective;
  }

  public void execute() {
    if (solutionBrowserPerspective.isWorkspaceShowing()) {
      solutionBrowserPerspective.showLaunchOrContent();
    } else {
      solutionBrowserPerspective.showWorkspace();
    }
  }

}
