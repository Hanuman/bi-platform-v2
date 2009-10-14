package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

public interface CommandExec {
  public void setSolutionBrowserPerspective(SolutionBrowserPerspective perspective);
  public void execute(String commandName);
}
