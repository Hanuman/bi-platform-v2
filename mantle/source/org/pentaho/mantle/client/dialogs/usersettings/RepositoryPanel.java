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
package org.pentaho.mantle.client.dialogs.usersettings;

import java.util.List;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.IMantleUserSettingsConstants;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class RepositoryPanel extends UserPreferencesPanel {

  VerticalPanel content = new VerticalPanel();
  CheckBox showNavigatorCB = new CheckBox();
  CheckBox showLocalizedFileNamesCB = new CheckBox();
  CheckBox showHiddenFilesCB = new CheckBox();
  // for storing the initial default value
  boolean showNavigator = true;
  boolean showLocalizedFileNames = true;
  boolean showHiddenFiles = false;

  public RepositoryPanel() {
    init();
  }

  public void init() {
    content.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    content.setWidth("100%"); //$NON-NLS-1$
    ScrollPanel scroller = new ScrollPanel();
    scroller.add(content);
    add(scroller);
    scroller.setHeight("400px"); //$NON-NLS-1$
    scroller.setWidth("400px"); //$NON-NLS-1$
    showNavigatorCB.setText(Messages.getInstance().showSolutionBrowser());
    showLocalizedFileNamesCB.setText(Messages.getInstance().showLocalizedFileNames());
    showHiddenFilesCB.setText(Messages.getInstance().showHiddenFiles());
    loadAndApplyUserSettings();
  }

  public void loadAndApplyUserSettings() {
    AsyncCallback<List<IUserSetting>> callback = new AsyncCallback<List<IUserSetting>>() {

      public void onFailure(Throwable caught) {
        caught.printStackTrace();
        // Window.alert(caught.toString());
      }

      public void onSuccess(List<IUserSetting> settings) {

        content.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        content.add(showNavigatorCB);
        content.add(showLocalizedFileNamesCB);
        content.add(showHiddenFilesCB);

        for (IUserSetting setting : settings) {
          if (IMantleUserSettingsConstants.MANTLE_SHOW_NAVIGATOR.equals(setting.getSettingName())) {
            showNavigator = "true".equals(setting.getSettingValue()); //$NON-NLS-1$
          } else if (IMantleUserSettingsConstants.MANTLE_SHOW_LOCALIZED_FILENAMES.equals(setting.getSettingName())) {
            showLocalizedFileNames = "true".equals(setting.getSettingValue()); //$NON-NLS-1$
          } else if (IMantleUserSettingsConstants.MANTLE_SHOW_HIDDEN_FILES.equals(setting.getSettingName())) {
            showHiddenFiles = "true".equals(setting.getSettingValue()); //$NON-NLS-1$
          }
          showLocalizedFileNamesCB.setChecked(showLocalizedFileNames);
          showHiddenFilesCB.setChecked(showHiddenFiles);
          showNavigatorCB.setChecked(showNavigator);
        }
      }
    };
    MantleServiceCache.getService().getUserSettings(callback);
  }

  public void setShowHiddenFiles(boolean show) {
    // update setting
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
      }

      public void onSuccess(Object result) {
      }

    };
    MantleServiceCache.getService().setShowHiddenFiles(show, callback);
  }

  public void setShowLocalizedFileNames(boolean show) {
    // update setting
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
      }

      public void onSuccess(Object result) {
      }

    };
    MantleServiceCache.getService().setShowLocalizedFileNames(show, callback);
  }

  public void setShowNavigator(boolean show) {
    // update setting
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
      }

      public void onSuccess(Object result) {
      }

    };
    MantleServiceCache.getService().setShowNavigator(show, callback);
  }

  public boolean onApply() {
    boolean anythingSet = false;
    if (showHiddenFilesCB.isChecked() != showHiddenFiles) {
      setShowHiddenFiles(showHiddenFilesCB.isChecked());
      anythingSet = true;
    }
    if (showNavigatorCB.isChecked() != showNavigator) {
      setShowNavigator(showNavigatorCB.isChecked());
      anythingSet = true;
    }
    if (showLocalizedFileNamesCB.isChecked() != showLocalizedFileNames) {
      setShowLocalizedFileNames(showLocalizedFileNamesCB.isChecked());
      anythingSet = true;
    }
    if (anythingSet) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().info(), Messages.getInstance().preferencesSetSuccess(), true, false, true);
      dialogBox.center();
    }
    return true;
  }

  public void onCancel() {
  }

}
