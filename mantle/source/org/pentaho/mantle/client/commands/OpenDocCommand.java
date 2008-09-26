package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.login.client.MantleLoginService;
import org.pentaho.mantle.login.client.MantleLoginServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * Executes the Open Document command. 
 * @author nbaker / dkincade
 */
public class OpenDocCommand implements Command {
  // Indicates if the instance running is a subscription instance 
  private static boolean isSubscription = false;

  static {
    // Setup the service used to check if we are running in a subscription instance
    final MantleLoginServiceAsync SERVICE = (MantleLoginServiceAsync) GWT.create(MantleLoginService.class);
    ServiceDefTarget endpoint = (ServiceDefTarget) SERVICE;
    String moduleRelativeURL = GWT.getModuleBaseURL() + "MantleLoginService"; //$NON-NLS-1$
    endpoint.setServiceEntryPoint(moduleRelativeURL);

    // Check to see if the running instance is a subscription instance
    final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
      public void onFailure(Throwable caught) {
        // Use the default - isSubscription = false;
      }

      public void onSuccess(Boolean result) {
        isSubscription = result;
      }
    };
    SERVICE.isSubscription(callback);
  }

  SolutionBrowserPerspective navigatorPerspective;

  public OpenDocCommand(SolutionBrowserPerspective navigatorPerspective) {
    this.navigatorPerspective = navigatorPerspective;
  }

  /**
   * Executes the command to open the help documentation. Based on the subscription setting,
   * the document being opened will be the CE version of the document or the EE version 
   * of the document.
   */
  public void execute() {
    String documentation = Messages.getInstance().documentation();
    String documentationUrl = (isSubscription ? Messages.getInstance().documentationEEUrl() : Messages.getInstance()
        .documentationUrl());
    navigatorPerspective.showNewURLTab(documentation, documentationUrl, documentationUrl);
  }
}
