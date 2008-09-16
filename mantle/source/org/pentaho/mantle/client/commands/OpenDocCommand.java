package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.user.client.Command;

public class OpenDocCommand implements Command {

   SolutionBrowserPerspective navigatorPerspective;

   public OpenDocCommand(SolutionBrowserPerspective navigatorPerspective) {
     this.navigatorPerspective = navigatorPerspective;
   }

  public void execute() {
    navigatorPerspective.showNewURLTab(Messages.getInstance().documentation(), Messages.getInstance().documentation(), Messages.getInstance().documentationUrl());
  }

}

  