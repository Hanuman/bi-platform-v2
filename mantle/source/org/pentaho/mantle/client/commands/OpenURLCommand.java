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
