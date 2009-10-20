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
package org.pentaho.mantle.client.solutionbrowser.fileproperties;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.commands.AbstractCommand;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.solutionbrowser.FileItem;
import org.pentaho.mantle.client.solutionbrowser.FileTypeEnabledOptions;
import org.pentaho.mantle.client.solutionbrowser.TabWidget;
import org.pentaho.mantle.client.solutionbrowser.FileCommand.COMMAND;

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
  private FileTypeEnabledOptions options;
  private boolean isAdministrator = false;
  private Tabs defaultTab = Tabs.GENERAL;
  private int tabApplyCounter = 0;

  public FilePropertiesDialog(FileItem fileItem, FileTypeEnabledOptions options, final boolean isAdministrator, final TabPanel propertyTabs, final IDialogCallback callback, Tabs defaultTab) {
    super(Messages.getString("properties"), Messages.getString("ok"), Messages.getString("cancel"), false, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    setContent(propertyTabs);

    generalTab = new GeneralPanel();
    permissionsTab = new PermissionsPanel();
    subscriptionsTab = new SubscriptionsPanel();
    
    generalTab.getElement().setId("filePropertiesGeneralTab");
    permissionsTab.getElement().setId("filePropertiesPermissionsTab");
    subscriptionsTab.getElement().setId("filePropertiesSubscriptionsTab");
    okButton.getElement().setId("filePropertiesOKButton");
    cancelButton.getElement().setId("filePropertiesCancelButton");
    
    this.defaultTab = defaultTab;

    super.setCallback(new IDialogCallback() {

      public void cancelPressed() {
        if (callback != null) {
          callback.cancelPressed();
        }
      }

      public void okPressed() {
        applyPanel();
        if (callback != null) {
          callback.okPressed();
        }
      }
    });
    this.fileItem = fileItem;
    this.options = options;
    this.propertyTabs = propertyTabs;
    this.propertyTabs.setStyleName("gwt-Dialog-TabPanel"); //$NON-NLS-1$
    this.propertyTabs.getTabBar().setStyleName("gwt-Dialog-TabBar"); //$NON-NLS-1$
    this.isAdministrator = isAdministrator;
    propertyTabs.add(generalTab, new TabWidget(Messages.getString("general"), Messages.getString("general"), null, propertyTabs, generalTab)); //$NON-NLS-1$ //$NON-NLS-2$
    fetchFileInfoAndInitTabs();
    getWidget().setHeight("100%"); //$NON-NLS-1$
    getWidget().setWidth("100%"); //$NON-NLS-1$
    setPixelSize(390, 420);
  }

  private void applyPanel() {
    // this method will chain asynchronous requests
    if (tabApplyCounter < propertyTabs.getWidgetCount()) {
      Widget w = propertyTabs.getWidget(tabApplyCounter);
      if (w instanceof IFileModifier) {
        ((IFileModifier) w).apply(new IDialogCallback() {
          public void okPressed() {
            ++tabApplyCounter;
            // apply the next guy
            applyPanel();
          }

          public void cancelPressed() {
            // unused
          }
        });
      }
    }
  }

  public void fetchFileInfoAndInitTabs() {
    final AsyncCallback<SolutionFileInfo> callback = new AsyncCallback<SolutionFileInfo>() {

      public void onFailure(Throwable caught) {
        // we are already logged in, or something horrible happened
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetFileProperties"), false, false, //$NON-NLS-1$ //$NON-NLS-2$
          true);
        dialogBox.center();
      }

      public void onSuccess(SolutionFileInfo fileInfo) {
        if (isAdministrator && !fileInfo.isDirectory() && options != null && options.isCommandEnabled(COMMAND.SCHEDULE_NEW)) {
          propertyTabs.remove(subscriptionsTab);
          propertyTabs.add(subscriptionsTab, new TabWidget(Messages.getString("advanced"), Messages.getString("advanced"), null, propertyTabs, //$NON-NLS-1$ //$NON-NLS-2$
              subscriptionsTab));
        }
        if (fileInfo.supportsAccessControls && fileInfo.canEffectiveUserManage) {
          propertyTabs.remove(permissionsTab);
          propertyTabs.add(permissionsTab, new TabWidget(Messages.getString("share"), Messages.getString("share"), null, propertyTabs, permissionsTab)); //$NON-NLS-1$ //$NON-NLS-2$
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
    if ((fileItem.getPath() == null || "".equals(fileItem.getPath())) && (fileItem.getSolution().equals(fileItem.getName()))) {
      // no path, in this situation, we're probably looking at the solution itself
        AbstractCommand getSolutionFileCmd = new AbstractCommand() {

          private void getFileInfo() {
            MantleServiceCache.getService().getSolutionFileInfo(fileItem.getSolution(), "", "", callback);
          }
          
          protected void performOperation() {
            getFileInfo();
          }

          protected void performOperation(boolean feedback) {
            getFileInfo();
          }
          
        };
        getSolutionFileCmd.execute();
        

    } else {
      AbstractCommand getSolutionFileCmd = new AbstractCommand() {

        private void getFileInfo() {
          MantleServiceCache.getService().getSolutionFileInfo(fileItem.getSolution(), fileItem.getPath(), fileItem.getName(), callback);
        }
        
        protected void performOperation() {
          getFileInfo();
        }

        protected void performOperation(boolean feedback) {
          getFileInfo();
        }
        
      };
      getSolutionFileCmd.execute();      
    }
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
