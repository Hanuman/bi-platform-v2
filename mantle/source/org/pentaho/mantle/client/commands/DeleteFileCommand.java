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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

public class DeleteFileCommand extends AbstractCommand {

  private FileItem fileItem;

  public DeleteFileCommand() {
  }

  public DeleteFileCommand(FileItem fileItem) {
    this.fileItem = fileItem;
  }

  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    // delete file
    String url = ""; //$NON-NLS-1$
    if (GWT.isScript()) {
      String windowpath = Window.Location.getPath();
      if (!windowpath.endsWith("/")) { //$NON-NLS-1$
        windowpath = windowpath.substring(0, windowpath.lastIndexOf("/") + 1); //$NON-NLS-1$
      }
      url = windowpath + "SolutionRepositoryService?component=delete&solution=" + fileItem.getSolution() + "&path=" + fileItem.getPath() + "&name=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          + fileItem.getName();
    } else if (!GWT.isScript()) {
      url = "http://localhost:8080/pentaho/SolutionRepositoryService?component=delete&solution=" + fileItem.getSolution() + "&path=" //$NON-NLS-1$ //$NON-NLS-2$
          + fileItem.getPath() + "&name=" + fileItem.getName(); //$NON-NLS-1$
    }
    final String myurl = url;
    VerticalPanel vp = new VerticalPanel();
    vp.add(new Label(Messages.getString("deleteQuestion", fileItem.getLocalizedName()))); //$NON-NLS-1$
    final PromptDialogBox deleteConfirmDialog = new PromptDialogBox(
        Messages.getString("deleteConfirm"), Messages.getString("yes"), Messages.getString("no"), false, true, vp); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    final IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
        deleteConfirmDialog.hide();
      }

      public void okPressed() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, myurl);
        try {
          builder.sendRequest(null, new RequestCallback() {

            public void onError(Request request, Throwable exception) {
              MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotDelete", fileItem.getName()), //$NON-NLS-1$ //$NON-NLS-2$
                  false, false, true);
              dialogBox.center();
            }

            public void onResponseReceived(Request request, Response response) {
              Document resultDoc = (Document) XMLParser.parse((String) (String) response.getText());
              boolean result = "true".equals(resultDoc.getDocumentElement().getFirstChild().getNodeValue()); //$NON-NLS-1$
              if (result) {
                new RefreshRepositoryCommand().execute(false);
              } else {
                MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
                    Messages.getString("couldNotDelete", fileItem.getName()), false, false, true); //$NON-NLS-1$
                dialogBox.center();
              }
            }

          });
        } catch (RequestException e) {
        }
      }
    };
    deleteConfirmDialog.setCallback(callback);
    deleteConfirmDialog.center();
  }

  public FileItem getFileItem() {
    return fileItem;
  }

  public void setFileItem(FileItem fileItem) {
    this.fileItem = fileItem;
  }
}
