package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.mantle.client.dialogs.ManageContentDialog;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.user.client.Command;

public class ManageContentCommand implements Command {

  private SolutionBrowserPerspective solutionBrowserPerspective;
  
  public ManageContentCommand(SolutionBrowserPerspective solutionBrowserPerspective) {
    this.solutionBrowserPerspective = solutionBrowserPerspective;
  }

  public void execute() {
    final ManageContentDialog dialog = new ManageContentDialog();
    dialog.setCallback(new IDialogCallback() {
      public void okPressed() {
        if (dialog.getState() == ManageContentDialog.STATE.EDIT) {
          ManageContentEditCommand cmd = new ManageContentEditCommand(solutionBrowserPerspective);
          cmd.execute();
        } else if (dialog.getState() == ManageContentDialog.STATE.SHARE) {
          ManageContentShareCommand cmd = new ManageContentShareCommand(solutionBrowserPerspective);
          cmd.execute();
        } else if (dialog.getState() == ManageContentDialog.STATE.SCHEDULE) {
          ManageContentScheduleCommand cmd = new ManageContentScheduleCommand(solutionBrowserPerspective);
          cmd.execute();
        }
      }

      public void cancelPressed() {
      }
    });
    dialog.center();
  }
}
