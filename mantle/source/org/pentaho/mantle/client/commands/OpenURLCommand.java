package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.IDialogValidatorCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.TextBox;

public class OpenURLCommand implements Command {

  SolutionBrowserPerspective navigatorPerspective;

  public OpenURLCommand(SolutionBrowserPerspective navigatorPerspective) {
    this.navigatorPerspective = navigatorPerspective;
  }

  public void execute() {
    final TextBox textBox = new TextBox();
    textBox.setText("http://"); //$NON-NLS-1$
    textBox.setWidth("500px"); //$NON-NLS-1$
    textBox.setVisibleLength(72);
    IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
      }

      public void okPressed() {
        navigatorPerspective.showNewURLTab(textBox.getText(), textBox.getText(), textBox.getText());
      }

    };
    IDialogValidatorCallback validatorCallback = new IDialogValidatorCallback() {
      public boolean validate() {
        boolean isValid = !"".equals(textBox.getText()) && textBox.getText() != null; //$NON-NLS-1$
        if (!isValid) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().error(), Messages.getInstance().urlNotSpecified(), false, false, true);
          dialogBox.center();
        }
        return isValid;
      }
    };
    PromptDialogBox promptDialog = new PromptDialogBox(Messages.getInstance().enterURL(), Messages.getInstance().ok(), Messages.getInstance().cancel(), false, true, textBox);
    promptDialog.setValidatorCallback(validatorCallback);
    promptDialog.setCallback(callback);
    promptDialog.setWidth("500px"); //$NON-NLS-1$
    promptDialog.center();
  }

}
