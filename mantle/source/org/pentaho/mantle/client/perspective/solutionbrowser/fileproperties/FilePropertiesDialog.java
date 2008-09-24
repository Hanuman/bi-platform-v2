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

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.perspective.solutionbrowser.TabWidget;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

public class FilePropertiesDialog extends PromptDialogBox {
  public enum Tabs { GENERAL, PERMISSION, SUBSCRIBE };
  private TabPanel propertyTabs;
  private GeneralPanel generalTab;
  private PermissionsPanel permissionsTab;
  private SubscriptionsPanel subscriptionsTab;

  private FileItem fileItem;
  private boolean isAdministrator = false;
  private Tabs defaultTab = Tabs.GENERAL;
  
  public FilePropertiesDialog(FileItem fileItem, final boolean isAdministrator, final TabPanel propertyTabs, final IDialogCallback callback, Tabs defaultTab) {
    super("Properties" + (fileItem == null ? "" : " (" + fileItem.getLocalizedName() + ")"), "OK", "Cancel", false, true);
    setContent(propertyTabs);
    
    generalTab = new GeneralPanel();
    permissionsTab = new PermissionsPanel();
    subscriptionsTab = new SubscriptionsPanel();
    this.defaultTab = defaultTab;
    
    super.setCallback(new IDialogCallback() {

      public void cancelPressed() {
        if (callback != null) {
          callback.cancelPressed();
        }
      }

      public void okPressed() {
        for (int i = 0; i < propertyTabs.getTabBar().getTabCount(); i++) {
          Widget w = propertyTabs.getWidget(i);
          if (w instanceof IFileModifier) {
            ((IFileModifier) w).apply();
          }
        }
        if (callback != null) {
          callback.okPressed();
        }
      }
    });
    this.fileItem = fileItem;
    this.propertyTabs = propertyTabs;
    this.propertyTabs.setStyleName("gwt-Dialog-TabPanel");
    this.propertyTabs.getTabBar().setStyleName("gwt-Dialog-TabBar");
    this.isAdministrator = isAdministrator;
    propertyTabs.add(generalTab, new TabWidget("General", "General", null, propertyTabs, generalTab));
    fetchFileInfoAndInitTabs();
    getWidget().setHeight("100%");
    getWidget().setWidth("100%");
    setPixelSize(360, 420);
  }

  public void fetchFileInfoAndInitTabs() {
    AsyncCallback<SolutionFileInfo> callback = new AsyncCallback<SolutionFileInfo>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox("Error", caught.toString(), false, false, true);
        dialogBox.center();
      }

      public void onSuccess(SolutionFileInfo fileInfo) {
        if (isAdministrator && !fileInfo.isDirectory()) {
          propertyTabs.remove(subscriptionsTab);
          propertyTabs.add(subscriptionsTab, new TabWidget("Subscribe", "Subscribe", null, propertyTabs, subscriptionsTab));
        }
        if (fileInfo.supportsAccessControls) {
          propertyTabs.remove(permissionsTab);
          propertyTabs.add(permissionsTab, new TabWidget("Share", "Share", null, propertyTabs, permissionsTab));
        }
        // init all tabs
        for (int i = 0; i < propertyTabs.getTabBar().getTabCount(); i++) {
          Widget w = propertyTabs.getWidget(i);
          if (w instanceof IFileModifier) {
            ((IFileModifier) w).init(fileItem, fileInfo);
          }
        }
        showTab(defaultTab);
      }
    };
    MantleServiceCache.getService().getSolutionFileInfo(fileItem.getSolution(), fileItem.getPath(), fileItem.getName(), callback);
  }

  public void showTab(Tabs tab) {
    this.defaultTab = tab;
    if (tab == Tabs.GENERAL  && propertyTabs.getWidgetIndex(generalTab) > -1) {
      propertyTabs.selectTab(propertyTabs.getWidgetIndex(generalTab));
    } else if (tab == Tabs.PERMISSION && propertyTabs.getWidgetIndex(permissionsTab) > -1) {
      propertyTabs.selectTab(propertyTabs.getWidgetIndex(permissionsTab));
    } else if (tab == Tabs.SUBSCRIBE && propertyTabs.getWidgetIndex(subscriptionsTab) > -1) {
      propertyTabs.selectTab(propertyTabs.getWidgetIndex(subscriptionsTab));
    }
  }
}
