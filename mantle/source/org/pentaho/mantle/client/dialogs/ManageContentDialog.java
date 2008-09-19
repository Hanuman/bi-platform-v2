package org.pentaho.mantle.client.dialogs;

import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ManageContentDialog extends PromptDialogBox {

  public enum STATE {
    EDIT, SHARE, SCHEDULE
  }

  private RadioButton editRadioButton = new RadioButton("manage");
  private RadioButton shareRadioButton = new RadioButton("manage");
  private RadioButton scheduleRadioButton = new RadioButton("manage");

  public ManageContentDialog() {
    super("Manage Content", "OK", "Cancel", false, true);

    editRadioButton.setText("Edit");
    shareRadioButton.setText("Share");
    scheduleRadioButton.setText("Schedule");

    VerticalPanel contentPanel = new VerticalPanel();
    contentPanel.add(new Label("Select the Manage function to perform"));
    contentPanel.add(new HTML("<BR>"));
    contentPanel.add(editRadioButton);
    contentPanel.add(shareRadioButton);
    contentPanel.add(scheduleRadioButton);

    setContent(contentPanel);
  }

  public STATE getState() {
    if (editRadioButton.isChecked()) {
      return STATE.EDIT;
    }
    if (shareRadioButton.isChecked()) {
      return STATE.SHARE;
    }
    return STATE.SCHEDULE;
  }

}
