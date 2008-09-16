package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.user.client.Command;

public class ShowBrowserCommand implements Command {

  SolutionBrowserPerspective solutionBrowserPerspective;

  public ShowBrowserCommand(SolutionBrowserPerspective solutionBrowserPerspective) {
    this.solutionBrowserPerspective = solutionBrowserPerspective;
  }

  public void execute() {
    solutionBrowserPerspective.toggleShowSolutionBrowser();
  }

}
