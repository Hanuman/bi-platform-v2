package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.dialogs.WaitPopup;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RefreshRepositoryCommand implements Command {

  SolutionBrowserPerspective navigatorPerspective;

  public RefreshRepositoryCommand(SolutionBrowserPerspective navigatorPerspective) {
    this.navigatorPerspective = navigatorPerspective;
  }

  public void execute(final boolean feedback) {
    AsyncCallback<Void> callback = new AsyncCallback<Void>() {

      public void onFailure(Throwable caught) {
        WaitPopup.getInstance().setVisible(false);
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().info(), Messages.getInstance().refreshRepositoryFailed(), false, false, true);
        dialogBox.center();
      }

      public void onSuccess(Void nothing) {
        WaitPopup.getInstance().setVisible(false);
        if (feedback) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().info(), Messages.getInstance().refreshRepositorySuccess(), false, false, true);
          dialogBox.center();
        }
        navigatorPerspective.refreshPerspective(false);
      }
    };
    WaitPopup.getInstance().setVisible(true);
    MantleServiceCache.getService().refreshRepository(callback);
  }

  public void execute() {
    execute(true);
  }

}
