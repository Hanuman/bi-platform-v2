package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;

public class WAQRCommand implements Command {

  SolutionBrowserPerspective navigatorPerspective;

  public WAQRCommand(SolutionBrowserPerspective navigatorPerspective) {
    this.navigatorPerspective = navigatorPerspective;
  }

  public void execute() {
    String waqrURL = "adhoc/waqr.html";
    if (!GWT.isScript()) {
      waqrURL = "http://localhost:8080/pentaho/adhoc/waqr.html?userid=joe&password=password";
    }
    navigatorPerspective.showNewURLTab("Untitled", "New Ad Hoc Report", waqrURL);
  }
}
