package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.login.client.MantleLoginService;
import org.pentaho.mantle.login.client.MantleLoginServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class OpenDocCommand implements Command {

  private static boolean isSubscription = false;

  private static MantleLoginServiceAsync SERVICE;
  static {
    SERVICE = (MantleLoginServiceAsync) GWT.create(MantleLoginService.class);
    ServiceDefTarget endpoint = (ServiceDefTarget) SERVICE;
    String moduleRelativeURL = GWT.getModuleBaseURL() + "MantleLoginService"; //$NON-NLS-1$
    endpoint.setServiceEntryPoint(moduleRelativeURL);
  }

  SolutionBrowserPerspective navigatorPerspective;

  public OpenDocCommand() {
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

  public OpenDocCommand(SolutionBrowserPerspective navigatorPerspective) {
    this.navigatorPerspective = navigatorPerspective;
  }

  public void execute() {
    String documentation = Messages.getInstance().documentation();
    String documentationUrl = (isSubscription ? Messages.getInstance().documentationEEUrl() : Messages.getInstance()
        .documentationUrl());
    if (!GWT.isScript()) {
      documentationUrl = "http://localhost:8080/pentaho/" + documentationUrl;
    }
    navigatorPerspective.showNewURLTab(documentation, documentationUrl, documentationUrl);
  }

}
