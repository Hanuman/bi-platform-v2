package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
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
        MessageDialogBox dialogBox = new MessageDialogBox("Software Update", "No updates are available.", false, false, true);
        dialogBox.center();
      }

      public void onSuccess(String result) {
        Document doc = (Document) XMLParser.parse(result);
        NodeList updates = doc.getElementsByTagName("update");
        if (updates.getLength() > 0) {
          FlexTable updateTable = new FlexTable();
          updateTable.setStyleName("backgroundContentTable");
          updateTable.setWidget(0, 0, new Label("Version"));
          updateTable.setWidget(0, 1, new Label("Type"));
          updateTable.setWidget(0, 2, new Label("OS"));
          updateTable.setWidget(0, 3, new Label("Link"));
          updateTable.getCellFormatter().setStyleName(0, 0, "backgroundContentHeaderTableCell");
          updateTable.getCellFormatter().setStyleName(0, 1, "backgroundContentHeaderTableCell");
          updateTable.getCellFormatter().setStyleName(0, 2, "backgroundContentHeaderTableCell");
          updateTable.getCellFormatter().setStyleName(0, 3, "backgroundContentHeaderTableCellRight");

          for (int i = 0; i < updates.getLength(); i++) {
            Element updateElement = (Element) updates.item(i);
            String version = updateElement.getAttribute("version");
            String type = updateElement.getAttribute("type");
            String os = updateElement.getAttribute("os");
            // String title = updateElement.getAttribute("title");
            String downloadURL = updateElement.getElementsByTagName("downloadurl").item(0).toString();
            downloadURL = downloadURL.substring(downloadURL.indexOf("http"), downloadURL.indexOf("]"));
            updateTable.setWidget(i + 1, 0, new Label(version));
            updateTable.setWidget(i + 1, 1, new Label(type));
            updateTable.setWidget(i + 1, 2, new Label(os));
            updateTable.setWidget(i + 1, 3, new HTML("<A HREF=\"" + downloadURL + "\" target=\"_blank\" title=\"" + downloadURL + "\">Download</A>"));
            updateTable.getCellFormatter().setStyleName(i + 1, 0, "backgroundContentTableCell");
            updateTable.getCellFormatter().setStyleName(i + 1, 1, "backgroundContentTableCell");
            updateTable.getCellFormatter().setStyleName(i + 1, 2, "backgroundContentTableCell");
            updateTable.getCellFormatter().setStyleName(i + 1, 3, "backgroundContentTableCellRight");
            if (i == updates.getLength() - 1) {
              // last
              updateTable.getCellFormatter().setStyleName(i + 1, 0, "backgroundContentTableCellBottom");
              updateTable.getCellFormatter().setStyleName(i + 1, 1, "backgroundContentTableCellBottom");
              updateTable.getCellFormatter().setStyleName(i + 1, 2, "backgroundContentTableCellBottom");
              updateTable.getCellFormatter().setStyleName(i + 1, 3, "backgroundContentTableCellBottomRight");
            }
          }
          PromptDialogBox versionPromptDialog = new PromptDialogBox("Software Update Available", "OK", null, false, true, updateTable);
          versionPromptDialog.center();
        } else {
          MessageDialogBox dialogBox = new MessageDialogBox("Software Update", "No updates are available.", false, false, true);
          dialogBox.center();
        }
      }
    };
    MantleServiceCache.getService().getSoftwareUpdatesDocument(callback);
  }

}
