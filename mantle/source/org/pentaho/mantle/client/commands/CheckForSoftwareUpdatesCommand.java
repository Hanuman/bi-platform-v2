package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.MantleApplicationConstants;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class CheckForSoftwareUpdatesCommand implements Command {

  public CheckForSoftwareUpdatesCommand() {
  }

  public void execute() {
    AsyncCallback<String> callback = new AsyncCallback<String>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().softwareUpdates(), Messages.getInstance().noUpdatesAvailable(), false, false, true);
        dialogBox.center();
      }

      public void onSuccess(String result) {
        Document doc = (Document) XMLParser.parse(result);
        NodeList updates = doc.getElementsByTagName("update"); //$NON-NLS-1$
        if (updates.getLength() > 0) {
          FlexTable updateTable = new FlexTable();
          updateTable.setStyleName("backgroundContentTable"); //$NON-NLS-1$
          updateTable.setWidget(0, 0, new Label(Messages.getInstance().version()));
          updateTable.setWidget(0, 1, new Label(Messages.getInstance().type()));
          updateTable.setWidget(0, 2, new Label(Messages.getInstance().os()));
          updateTable.setWidget(0, 3, new Label(Messages.getInstance().link()));
          updateTable.getCellFormatter().setStyleName(0, 0, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
          updateTable.getCellFormatter().setStyleName(0, 1, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
          updateTable.getCellFormatter().setStyleName(0, 2, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
          updateTable.getCellFormatter().setStyleName(0, 3, "backgroundContentHeaderTableCellRight"); //$NON-NLS-1$

          for (int i = 0; i < updates.getLength(); i++) {
            Element updateElement = (Element) updates.item(i);
            String version = updateElement.getAttribute("version"); //$NON-NLS-1$
            String type = updateElement.getAttribute("type"); //$NON-NLS-1$
            String os = updateElement.getAttribute("os"); //$NON-NLS-1$
            // String title = updateElement.getAttribute("title");
            String downloadURL = updateElement.getElementsByTagName("downloadurl").item(0).toString(); //$NON-NLS-1$
            downloadURL = downloadURL.substring(downloadURL.indexOf("http"), downloadURL.indexOf("]")); //$NON-NLS-1$ //$NON-NLS-2$
            updateTable.setWidget(i + 1, 0, new Label(version));
            updateTable.setWidget(i + 1, 1, new Label(type));
            updateTable.setWidget(i + 1, 2, new Label(os));
            updateTable.setWidget(i + 1, 3, new HTML("<A HREF=\"" + downloadURL + "\" target=\"_blank\" title=\"" + downloadURL + "\">" + Messages.getInstance().download() + "</A>")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            updateTable.getCellFormatter().setStyleName(i + 1, 0, "backgroundContentTableCell"); //$NON-NLS-1$
            updateTable.getCellFormatter().setStyleName(i + 1, 1, "backgroundContentTableCell"); //$NON-NLS-1$
            updateTable.getCellFormatter().setStyleName(i + 1, 2, "backgroundContentTableCell"); //$NON-NLS-1$
            updateTable.getCellFormatter().setStyleName(i + 1, 3, "backgroundContentTableCellRight"); //$NON-NLS-1$
            if (i == updates.getLength() - 1) {
              // last
              updateTable.getCellFormatter().setStyleName(i + 1, 0, "backgroundContentTableCellBottom"); //$NON-NLS-1$
              updateTable.getCellFormatter().setStyleName(i + 1, 1, "backgroundContentTableCellBottom"); //$NON-NLS-1$
              updateTable.getCellFormatter().setStyleName(i + 1, 2, "backgroundContentTableCellBottom"); //$NON-NLS-1$
              updateTable.getCellFormatter().setStyleName(i + 1, 3, "backgroundContentTableCellBottomRight"); //$NON-NLS-1$
            }
          }
          PromptDialogBox versionPromptDialog = new PromptDialogBox(Messages.getInstance().softwareUpdateAvailable(), Messages.getInstance().ok(), null, false, true, updateTable);
          versionPromptDialog.center();
        } else {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().softwareUpdates(), Messages.getInstance().noUpdatesAvailable(), false, false, true);
          dialogBox.center();
        }
      }
    };
    MantleServiceCache.getService().getSoftwareUpdatesDocument(callback);
  }

}
