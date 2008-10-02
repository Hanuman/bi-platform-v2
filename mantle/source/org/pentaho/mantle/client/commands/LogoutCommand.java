package org.pentaho.mantle.client.commands;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class LogoutCommand implements Command {

  public LogoutCommand() {
  }

  public void execute() {
    String location = Window.Location.getPath().substring(0, Window.Location.getPath().lastIndexOf('/')) + "/Logout"; //$NON-NLS-1$
    Window.open(location, "_top", ""); //$NON-NLS-1$ //$NON-NLS-2$
  }

}
