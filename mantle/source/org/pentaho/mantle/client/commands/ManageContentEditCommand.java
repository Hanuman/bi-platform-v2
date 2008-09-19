package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.user.client.Command;

public class ManageContentEditCommand implements Command {

  SolutionBrowserPerspective solutionBrowserPerspective;
  
  public ManageContentEditCommand(SolutionBrowserPerspective solutionBrowserPerspective) {
    this.solutionBrowserPerspective = solutionBrowserPerspective;
  }

  public void execute() {
    EditFileCommand cmd = new EditFileCommand(solutionBrowserPerspective);
    cmd.execute();
  }
}
