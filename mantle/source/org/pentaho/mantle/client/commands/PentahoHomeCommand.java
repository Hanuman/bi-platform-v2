package org.pentaho.mantle.client.commands;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class PentahoHomeCommand implements Command {

  public PentahoHomeCommand() {
  }

  public void execute() {
    Window.open("http://www.pentaho.com", "_blank", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

}
