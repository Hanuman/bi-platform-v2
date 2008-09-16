package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RefreshRepositoryCommand implements Command {

  SolutionBrowserPerspective navigatorPerspective;

  public RefreshRepositoryCommand(SolutionBrowserPerspective navigatorPerspective) {
    this.navigatorPerspective = navigatorPerspective;
  }

  public void execute(final boolean feedback) {
    AsyncCallback<Void> callback = new AsyncCallback<Void>() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(Void nothing) {
        if (feedback) {
          MessageDialogBox dialogBox = new MessageDialogBox("Info", "Repository refreshed successfully.", false, false, true);
          dialogBox.center();
        }
        navigatorPerspective.refreshPerspective(false);
      }
    };
    MantleServiceCache.getService().refreshRepository(callback);
  }

  public void execute() {
    execute(true);
  }

}
