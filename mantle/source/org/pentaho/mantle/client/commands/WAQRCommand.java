package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;

public class WAQRCommand implements Command {

  SolutionBrowserPerspective navigatorPerspective;

  public WAQRCommand(SolutionBrowserPerspective navigatorPerspective) {
    this.navigatorPerspective = navigatorPerspective;
  }

  public void execute() {
    String waqrURL = "adhoc/waqr.html"; //$NON-NLS-1$
    if (!GWT.isScript()) {
      waqrURL = "http://localhost:8080/pentaho/adhoc/waqr.html?userid=joe&password=password"; //$NON-NLS-1$
    }
    navigatorPerspective.showNewURLTab(Messages.getInstance().untitled(), Messages.getInstance().newAdhocReport(), waqrURL);
  }
}
