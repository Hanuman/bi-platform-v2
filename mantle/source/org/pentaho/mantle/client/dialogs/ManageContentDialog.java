package org.pentaho.mantle.client.dialogs;

import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ManageContentDialog extends PromptDialogBox {

  public enum STATE {
    EDIT, SHARE, SCHEDULE
  }

  private RadioButton editRadioButton = new RadioButton("manage"); //$NON-NLS-1$
  private RadioButton shareRadioButton = new RadioButton("manage"); //$NON-NLS-1$
  private RadioButton scheduleRadioButton = new RadioButton("manage"); //$NON-NLS-1$

  public ManageContentDialog() {
    super(Messages.getInstance().manageContent(), Messages.getInstance().ok(), Messages.getInstance().cancel(), false, true);

    editRadioButton.setText(Messages.getInstance().edit());
    shareRadioButton.setText(Messages.getInstance().share());
    scheduleRadioButton.setText(Messages.getInstance().schedule());

    VerticalPanel contentPanel = new VerticalPanel();
    contentPanel.add(new Label(Messages.getInstance().manageContentSelectFunction()));
    contentPanel.add(new HTML("<BR>")); //$NON-NLS-1$
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
