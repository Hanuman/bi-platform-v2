package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CleanContentRepositoryCommand implements Command {

  int daysBack = 90;

  public CleanContentRepositoryCommand(int daysBack) {
    this.daysBack = daysBack;
  }

  public void execute() {
    AsyncCallback<Integer> callback = new AsyncCallback<Integer>() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(Integer numItemsCleaned) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().info(), Messages.getInstance().cleanContentRepositorySuccess(), false,
            false, true);
        dialogBox.center();
      }
    };
    MantleServiceCache.getService().cleanContentRepository(daysBack, callback);
  }

}
