/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.solutionbrowser.tree.FileTreeItem;
import org.pentaho.mantle.client.solutionbrowser.tree.SolutionTree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

public class NewFolderCommand implements Command {

  private String repoPath = "";
  private String solution = "";
  
  public NewFolderCommand() {
  }

  public void execute() {
    SolutionTree solutionTree = SolutionBrowserPerspective.getInstance().getSolutionTree();
    if (solutionTree != null) {
      FileTreeItem selectedTreeItem = (FileTreeItem) solutionTree.getSelectedItem();
      final FileItem selectedItem = new FileItem(selectedTreeItem.getFileName(), selectedTreeItem.getText(), selectedTreeItem.getText(),
          solutionTree.getSolution(), solutionTree.getPath(), null, null, null, null, false, null);
      
      
//      path = solutionTree.getPath().substring(0, solutionTree.getPath().lastIndexOf("/")); //$NON-NLS-1$
      
      
      repoPath = selectedItem.getPath();
      // if a solution folder is selected then the solution-name/path are the same, we can't allow that
      // but we need them to be in the tree like this for building the tree paths correctly (other code)
      if (repoPath.equals("/" + selectedItem.getSolution())) { //$NON-NLS-1$
        repoPath = ""; //$NON-NLS-1$
      }
      solution = selectedItem.getSolution();
    }
    final TextBox folderNameTextBox = new TextBox();
    folderNameTextBox.setTabIndex(1);
    folderNameTextBox.setVisibleLength(40);

    VerticalPanel vp = new VerticalPanel();
    vp.add(new Label(Messages.getString("newFolderName"))); //$NON-NLS-1$
    vp.add(folderNameTextBox);
    final PromptDialogBox newFolderDialog = new PromptDialogBox(
        Messages.getString("newFolder"), Messages.getString("ok"), Messages.getString("cancel"), false, true, vp); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    newFolderDialog.setFocusWidget(folderNameTextBox);
    folderNameTextBox.setFocus(true);

    final IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
        newFolderDialog.hide();
      }

      public void okPressed() {
        String url = ""; //$NON-NLS-1$
        if (GWT.isScript()) {
          String windowpath = Window.Location.getPath();
          if (!windowpath.endsWith("/")) { //$NON-NLS-1$
            windowpath = windowpath.substring(0, windowpath.lastIndexOf("/") + 1); //$NON-NLS-1$
          }
          url = windowpath + "SolutionRepositoryService?component=createNewFolder&solution=" + solution + "&path=" + repoPath + "&name=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              + folderNameTextBox.getText(); //$NON-NLS-1$
        } else if (!GWT.isScript()) {
          url = "http://localhost:8080/pentaho/SolutionRepositoryService?component=createNewFolder&solution=" + solution + "&path=" //$NON-NLS-1$ //$NON-NLS-2$
              + repoPath + "&name=" + folderNameTextBox.getText(); //$NON-NLS-1$ //$NON-NLS-2$
        }
        final String myurl = url;
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, myurl);
        try {
          builder.sendRequest(null, new RequestCallback() {

            public void onError(Request request, Throwable exception) {
              MessageDialogBox dialogBox = new MessageDialogBox(
                  Messages.getString("error"), Messages.getString("couldNotCreateFolder", folderNameTextBox.getText()), //$NON-NLS-1$ //$NON-NLS-2$
                  false, false, true);
              dialogBox.center();
            }

            public void onResponseReceived(Request request, Response response) {
              Document resultDoc = (Document) XMLParser.parse((String) (String) response.getText());
              boolean result = "true".equals(resultDoc.getDocumentElement().getFirstChild().getNodeValue()); //$NON-NLS-1$
              if (result) {
                RefreshRepositoryCommand cmd = new RefreshRepositoryCommand();
                cmd.execute(false);
              } else {
                MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
                    Messages.getString("couldNotCreateFolder", folderNameTextBox.getText()), false, false, true); //$NON-NLS-1$
                dialogBox.center();
              }
            }

          });
        } catch (RequestException e) {
        }
      }
    };
    newFolderDialog.setCallback(callback);
    newFolderDialog.center();
  }

}
