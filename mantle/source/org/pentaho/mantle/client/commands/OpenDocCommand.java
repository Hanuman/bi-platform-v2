package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.user.client.Command;

/**
 * Executes the Open Document command.
 * 
 * @author nbaker / dkincade
 */
public class OpenDocCommand implements Command {
  private SolutionBrowserPerspective navigatorPerspective;
  private String documentationURL;

  public OpenDocCommand(String documentationURL, SolutionBrowserPerspective navigatorPerspective) {
    this.documentationURL = documentationURL;
    this.navigatorPerspective = navigatorPerspective;
  }

  /**
   * Executes the command to open the help documentation. Based on the subscription setting, the document being opened will be the CE version of the document or
   * the EE version of the document.
   */
  public void execute() {
    navigatorPerspective.showNewURLTab(Messages.getInstance().documentation(), documentationURL, documentationURL);
  }
}
