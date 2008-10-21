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
