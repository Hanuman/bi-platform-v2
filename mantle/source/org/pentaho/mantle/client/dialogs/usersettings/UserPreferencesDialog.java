package org.pentaho.mantle.client.dialogs.usersettings;

import java.util.HashMap;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.IDialogValidatorCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class UserPreferencesDialog extends PromptDialogBox implements ChangeListener, IDialogValidatorCallback, IDialogCallback {

  public static final int STYLES = 0;
  public static final int REPOSITORY = 1;
  public static final int FAVORITES = 2;

  VerticalPanel preferencesContent = new VerticalPanel();
  ListBox preferencesList = new ListBox();
  HashMap<String, UserPreferencesPanel> preferencesPanelMap = new HashMap<String, UserPreferencesPanel>();
  int initialSelectedPreference = STYLES;

  public UserPreferencesDialog(int initialSelectedPreference) {
    super("User Preferences", "OK", "Cancel", false, true, new HorizontalPanel());
    setCallback(this);
    setValidatorCallback(this);
    this.initialSelectedPreference = initialSelectedPreference;
    init();
  }

  public void init() {
    preferencesPanelMap.put("Styles", new StyleEditorPanel());
    preferencesPanelMap.put("Repository", new RepositoryPanel());
    preferencesPanelMap.put("Favorites", new FavoritesPanel());

    HorizontalPanel content = (HorizontalPanel) getContent();
    content.setSpacing(10);
    content.add(preferencesList);
    content.add(preferencesContent);

    preferencesList.setVisibleItemCount(10);
    preferencesList.setWidth("120px");

    for (String key : preferencesPanelMap.keySet()) {
      preferencesList.addItem(key);
    }

    // preferencesList.addItem("Favorites");
    preferencesList.addChangeListener(this);
    for (int i = 0; i < preferencesList.getItemCount(); i++) {
      String item = preferencesList.getItemText(i);
      if (initialSelectedPreference == STYLES && item.equalsIgnoreCase("Styles")) {
        preferencesList.setSelectedIndex(i);
      } else if (initialSelectedPreference == REPOSITORY && item.equalsIgnoreCase("Repository")) {
        preferencesList.setSelectedIndex(i);
      } else if (initialSelectedPreference == FAVORITES && item.equalsIgnoreCase("Favorites")) {
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
