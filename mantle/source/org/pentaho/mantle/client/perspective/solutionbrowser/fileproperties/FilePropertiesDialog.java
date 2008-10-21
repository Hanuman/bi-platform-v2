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
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.perspective.solutionbrowser.fileproperties;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.perspective.solutionbrowser.TabWidget;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.login.client.MantleLoginDialog;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

public class FilePropertiesDialog extends PromptDialogBox {
  public enum Tabs {
    GENERAL, PERMISSION, SUBSCRIBE
  };

  private TabPanel propertyTabs;
  private GeneralPanel generalTab;
  private PermissionsPanel permissionsTab;
  private SubscriptionsPanel subscriptionsTab;

  private FileItem fileItem;
  private boolean isAdministrator = false;
  private Tabs defaultTab = Tabs.GENERAL;

  public FilePropertiesDialog(FileItem fileItem, final boolean isAdministrator, final TabPanel propertyTabs, final IDialogCallback callback, Tabs defaultTab) {
    super(
        Messages.getInstance().properties(), Messages.getInstance().ok(), Messages.getInstance().cancel(), false, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
    this.propertyTabs.setStyleName("gwt-Dialog-TabPanel"); //$NON-NLS-1$
    this.propertyTabs.getTabBar().setStyleName("gwt-Dialog-TabBar"); //$NON-NLS-1$
    this.isAdministrator = isAdministrator;
    propertyTabs.add(generalTab, new TabWidget(Messages.getInstance().general(), Messages.getInstance().general(), null, propertyTabs, generalTab));
    fetchFileInfoAndInitTabs();
    getWidget().setHeight("100%"); //$NON-NLS-1$
    getWidget().setWidth("100%"); //$NON-NLS-1$
    setPixelSize(390, 420);
  }

  public void fetchFileInfoAndInitTabs() {
    AsyncCallback<SolutionFileInfo> callback = new AsyncCallback<SolutionFileInfo>() {

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback() {

          public void onFailure(Throwable caughtLogin) {
            // we are already logged in, or something horrible happened
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().error(), Messages.getInstance().couldNotGetFileProperties(), false, false,
                true);
            dialogBox.center();
          }

          public void onSuccess(Object result) {
            fetchFileInfoAndInitTabs();
          }
        });
      }

      public void onSuccess(SolutionFileInfo fileInfo) {
        if (isAdministrator && !fileInfo.isDirectory()) {
          propertyTabs.remove(subscriptionsTab);
          propertyTabs.add(subscriptionsTab, new TabWidget(Messages.getInstance().advanced(), Messages.getInstance().advanced(), null, propertyTabs,
              subscriptionsTab));
        }
        if (fileInfo.supportsAccessControls && fileInfo.canEffectiveUserManage) {
          propertyTabs.remove(permissionsTab);
          propertyTabs.add(permissionsTab, new TabWidget(Messages.getInstance().share(), Messages.getInstance().share(), null, propertyTabs, permissionsTab));
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
    if (fileItem.getName() == null || fileItem.getSolution() == null) {
      // No propertes to show
      return;
    }
    MantleServiceCache.getService().getSolutionFileInfo(fileItem.getSolution(), fileItem.getPath(), fileItem.getName(), callback);
  }

  public void showTab(Tabs tab) {
    this.defaultTab = tab;
    if (tab == Tabs.GENERAL && propertyTabs.getWidgetIndex(generalTab) > -1) {
      propertyTabs.selectTab(propertyTabs.getWidgetIndex(generalTab));
    } else if (tab == Tabs.PERMISSION && propertyTabs.getWidgetIndex(permissionsTab) > -1) {
      propertyTabs.selectTab(propertyTabs.getWidgetIndex(permissionsTab));
    } else if (tab == Tabs.SUBSCRIBE && propertyTabs.getWidgetIndex(subscriptionsTab) > -1) {
      propertyTabs.selectTab(propertyTabs.getWidgetIndex(subscriptionsTab));
    }
  }
}
