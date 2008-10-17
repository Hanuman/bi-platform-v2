package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RefreshMetaDataCommand implements Command {

  public RefreshMetaDataCommand() {
  }

  public void execute() {
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().error(), Messages.getInstance().refreshReportingMetadataFailed(), false, false, true);
        dialogBox.center();
      }

      public void onSuccess(Object result) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().info(), Messages.getInstance().refreshReportingMetadataSuccess(), false, false, true);
        dialogBox.center();
      }
    };
    MantleServiceCache.getService().refreshMetadata(callback);
  }

}
