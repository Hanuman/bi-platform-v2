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

import java.util.HashMap;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.IDialogValidatorCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class UserPreferencesDialog extends PromptDialogBox implements ChangeListener, IDialogValidatorCallback, IDialogCallback {

  public static enum PREFERENCE { STYLES, REPOSITORY, FAVORITES };
  
  VerticalPanel preferencesContent = new VerticalPanel();
  ListBox preferencesList = new ListBox();
  HashMap<String, UserPreferencesPanel> preferencesPanelMap = new HashMap<String, UserPreferencesPanel>();
  PREFERENCE initialSelectedPreference = PREFERENCE.STYLES;

  public UserPreferencesDialog(PREFERENCE initialSelectedPreference) {
    super(Messages.getString("userPreferences"), Messages.getString("ok"), Messages.getString("cancel"), false, true, new HorizontalPanel());
    setCallback(this);
    setValidatorCallback(this);
    this.initialSelectedPreference = initialSelectedPreference;
    init();
  }

  public void init() {
    preferencesPanelMap.put(Messages.getString("repository"), new RepositoryPanel());
    preferencesPanelMap.put(Messages.getString("favorites"), new FavoritesPanel());

    HorizontalPanel content = (HorizontalPanel) getContent();
    content.setSpacing(10);
    content.add(preferencesList);
    content.add(preferencesContent);

    preferencesList.setVisibleItemCount(10);
    preferencesList.setWidth("120px"); //$NON-NLS-1$

    for (String key : preferencesPanelMap.keySet()) {
      preferencesList.addItem(key);
    }

    // preferencesList.addItem("Favorites");
    preferencesList.addChangeListener(this);
    for (int i = 0; i < preferencesList.getItemCount(); i++) {
      String item = preferencesList.getItemText(i);
      if (initialSelectedPreference.equals(PREFERENCE.STYLES) && item.equalsIgnoreCase(Messages.getString("styles"))) {
        preferencesList.setSelectedIndex(i);
      } else if (initialSelectedPreference.equals(PREFERENCE.REPOSITORY) && item.equalsIgnoreCase(Messages.getString("repository"))) {
        preferencesList.setSelectedIndex(i);
      } else if (initialSelectedPreference.equals(PREFERENCE.FAVORITES) && item.equalsIgnoreCase(Messages.getString("favorites"))) {
        preferencesList.setSelectedIndex(i);
      }
    }
    onChange(preferencesList);
  }

  public void onChange(Widget sender) {
    String preferenceName = preferencesList.getItemText(preferencesList.getSelectedIndex());
    Widget content = preferencesPanelMap.get(preferenceName);

    preferencesContent.clear();
    preferencesContent.add(content);
  }

  public void cancelPressed() {
    for (UserPreferencesPanel preferencesPanel : preferencesPanelMap.values()) {
      preferencesPanel.onCancel();
    }
  }

  public void okPressed() {
    // the validator will have already been run, upon which settings will have been applied
  }

  public boolean validate() {
    for (UserPreferencesPanel preferencesPanel : preferencesPanelMap.values()) {
      if (!preferencesPanel.onApply()) {
        return false;
      }
    }
    return true;
  }
}
