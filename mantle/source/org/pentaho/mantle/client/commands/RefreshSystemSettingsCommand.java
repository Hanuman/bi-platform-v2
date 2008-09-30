package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RefreshSystemSettingsCommand implements Command {

  public RefreshSystemSettingsCommand() {
  }

  public void execute() {
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(Object result) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().info(), Messages.getInstance().refreshSystemSettingsSuccess(), false, false, true);
        dialogBox.center();
      }
    };
    MantleServiceCache.getService().refreshSystemSettings(callback);
  }
}
