package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AboutCommand implements Command {

  public AboutCommand() {
  }

  public void execute() {
    AsyncCallback<String> callback = new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(String version) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().about(), version, false, false, true);
        dialogBox.center();
      }
    };
    MantleServiceCache.getService().getVersion(callback);
  }

}
