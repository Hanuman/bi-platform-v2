package org.pentaho.mantle.client.perspective;

import com.google.gwt.user.client.Command;

public class RefreshPerspectiveCommand implements Command {

  IPerspective perspective;

  public RefreshPerspectiveCommand(IPerspective perspective) {
    this.perspective = perspective;
  }

  public void execute() {
    perspective.refreshPerspective(true);
  }

}
