package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import com.google.gwt.user.client.Command;

public class UrlCommand implements Command {

  SolutionBrowserPerspective navigatorPerspective;
  String url;
  String title;
  
  public UrlCommand(SolutionBrowserPerspective navigatorPerspective, String url, String title ) {
    this.navigatorPerspective = navigatorPerspective;
    this.url = url;
    this.title = title;
  }

  public void execute() {
    navigatorPerspective.showNewURLTab( title, "", url); //$NON-NLS-1$
  }
}
