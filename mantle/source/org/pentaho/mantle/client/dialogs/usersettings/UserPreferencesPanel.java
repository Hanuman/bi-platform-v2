package org.pentaho.mantle.client.dialogs.usersettings;

import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class UserPreferencesPanel extends VerticalPanel {
  public abstract void onCancel();
  public abstract boolean onApply();
}
