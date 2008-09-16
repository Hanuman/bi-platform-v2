/*
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.perspective.solutionbrowser.fileproperties;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

public class GeneralPanel extends FlexTable implements IFileModifier {

  Label nameLabel = new Label();
  Label locationLabel = new Label();
  Label solutionLabel = new Label();
  Label typeLabel = new Label();
  Label sizeLabel = new Label();
  Label lastModifiedDateLabel = new Label();
  FileItem fileItem;

  public GeneralPanel() {
    setWidget(0, 0, new Label("Name:"));
    setWidget(0, 1, nameLabel);
    setWidget(1, 0, new Label("Solution:"));
    setWidget(1, 1, solutionLabel);
    setWidget(2, 0, new Label("Location:"));
    setWidget(2, 1, locationLabel);
    setWidget(3, 0, new Label("Type:"));
    setWidget(3, 1, typeLabel);
    setWidget(4, 0, new Label("Size:"));
    setWidget(4, 1, sizeLabel);
    setWidget(5, 0, new Label("Last Modified:"));
    setWidget(5, 1, lastModifiedDateLabel);
    setStyleName("filePropertyTabContent");
  }

  public void apply() {
    // hit server with new settings
  }

  public void init(FileItem file, SolutionFileInfo fileInfo) {
    fileItem = file;
    // possibly hit server to pull all this data
    if (fileInfo == null) {
      populateUIFromServer();
    } else {
      nameLabel.setText(fileInfo.name);
      locationLabel.setText(fileInfo.path);
      lastModifiedDateLabel.setText(fileInfo.lastModifiedDate.toString());
      solutionLabel.setText(fileInfo.solution);
      sizeLabel.setText("" + fileInfo.size);
    }
  }

  public void populateUIFromServer() {
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox("Error", caught.toString(), false, false, true);
        dialogBox.center();
      }

      public void onSuccess(Object result) {
        SolutionFileInfo fileInfo = (SolutionFileInfo) result;
        init(fileItem, fileInfo);
      }
    };
    MantleServiceCache.getService().getSolutionFileInfo(fileItem.getSolution(), fileItem.getPath(), fileItem.getName(), callback);
  }

}
