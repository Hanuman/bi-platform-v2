package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.dialogs.usersettings.UserPreferencesDialog;

import com.google.gwt.user.client.Command;

public class ShowPreferencesCommand implements Command {

  public ShowPreferencesCommand() {
  }

  public void execute() {
    // read solution engine interactivity service
    UserPreferencesDialog dialog = new UserPreferencesDialog(UserPreferencesDialog.PREFERENCE.STYLES);
    dialog.center();
  }

}
