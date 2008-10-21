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
 *
 * @created Jun 30, 2008 
 * @author wseyler
 */
package org.pentaho.mantle.client.perspective.solutionbrowser.fileproperties;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.gwt.widgets.client.buttons.RoundedButton;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.objects.SubscriptionSchedule;
import org.pentaho.mantle.client.objects.SubscriptionState;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author wseyler
 *
 */
public class SubscriptionsPanel extends VerticalPanel implements IFileModifier {
  boolean dirty = false;

  boolean wasEnabled = false;

  CheckBox enableSubscriptions = new CheckBox(Messages.getInstance().enableSubscription());

  ListBox availableLB = new ListBox();

  ListBox appliedLB = new ListBox();

  RoundedButton moveRightBtn = new RoundedButton();

  RoundedButton moveLeftBtn = new RoundedButton();

  RoundedButton moveAllRightBtn = new RoundedButton();

  RoundedButton moveAllLeftBtn = new RoundedButton();

  FileItem fileItem = null;

  public SubscriptionsPanel() {
    layout();
    dirty = false;
  }

  public void layout() {
    this.setSize("100%", "100%"); //$NON-NLS-1$//$NON-NLS-2$
    enableSubscriptions.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        dirty = true;
        updateControls();
      }
    });
    this.add(enableSubscriptions);
    this.setSpacing(10);

    CaptionPanel scheduleCaptionPanel = new CaptionPanel(Messages.getInstance().schedule());

    FlexTable schedulePanel = new FlexTable();
    schedulePanel.setSize("100%", "100%"); //$NON-NLS-1$//$NON-NLS-2$

    VerticalPanel availablePanel = new VerticalPanel();
    availablePanel.add(new Label(Messages.getInstance().available()));
    availablePanel.add(availableLB);
    availableLB.setVisibleItemCount(9);
    availableLB.setMultipleSelect(true);
    availableLB.setWidth("100%"); //$NON-NLS-1$

    VerticalPanel appliedPanel = new VerticalPanel();
    appliedPanel.add(new Label(Messages.getInstance().current()));
    appliedPanel.add(appliedLB);
    appliedLB.setVisibleItemCount(9);
    appliedLB.setMultipleSelect(true);
    appliedLB.setWidth("100%"); //$NON-NLS-1$

    // Add the buttons
    VerticalPanel buttonGrid = new VerticalPanel();
    buttonGrid.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    buttonGrid.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
    buttonGrid.setSpacing(2);
    moveRightBtn.setText(">"); //$NON-NLS-1$
    moveRightBtn.setTitle(Messages.getInstance().add());
    moveRightBtn.setWidth("30px"); //$NON-NLS-1$
    moveRightBtn.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        moveSelectedToRight();
      }
    });
    buttonGrid.add(moveRightBtn);
    moveAllRightBtn.setText(">>"); //$NON-NLS-1$
    moveAllRightBtn.setTitle(Messages.getInstance().addAll());
    moveAllRightBtn.setWidth("30px"); //$NON-NLS-1$
    moveAllRightBtn.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        moveAllToRight();
      }
    });
    buttonGrid.add(moveAllRightBtn);
    moveLeftBtn.setText("<"); //$NON-NLS-1$
    moveLeftBtn.setTitle(Messages.getInstance().remove());
    moveLeftBtn.setWidth("30px"); //$NON-NLS-1$
    moveLeftBtn.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        moveSelectedToLeft();
      }
    });
    buttonGrid.add(moveLeftBtn);
    moveAllLeftBtn.setText("<<"); //$NON-NLS-1$
    moveAllLeftBtn.setTitle(Messages.getInstance().removeAll());
    moveAllLeftBtn.setWidth("30px"); //$NON-NLS-1$
    moveAllLeftBtn.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        moveAllToLeft();
      }
    });
    buttonGrid.add(moveAllLeftBtn);

    // Add the list boxes
    schedulePanel.setWidget(0, 0, availablePanel);
    schedulePanel.setWidget(0, 1, buttonGrid);
    schedulePanel.setWidget(0, 2, appliedPanel);

    scheduleCaptionPanel.add(schedulePanel);
    this.add(scheduleCaptionPanel);
    this.setCellHorizontalAlignment(schedulePanel, HasHorizontalAlignment.ALIGN_CENTER);

    availablePanel.setWidth("100%"); //$NON-NLS-1$
    buttonGrid.setWidth("100%"); //$NON-NLS-1$
    appliedPanel.setWidth("100%"); //$NON-NLS-1$
    schedulePanel.getCellFormatter().setWidth(0, 0, "50%"); //$NON-NLS-1$
    schedulePanel.getCellFormatter().setWidth(0, 2, "50%"); //$NON-NLS-1$

  }

  protected void moveSelectedToRight() {
    moveItems(availableLB, appliedLB, false);
  }

  protected void moveAllToRight() {
    moveItems(availableLB, appliedLB, true);
  }

  protected void moveSelectedToLeft() {
    moveItems(appliedLB, availableLB, false);
  }

  protected void moveAllToLeft() {
    moveItems(appliedLB, availableLB, true);
  }

  protected void moveItems(ListBox srcLB, ListBox destLB, boolean moveAll) {
    dirty = true;
    int itemCount = srcLB.getItemCount();
    int srcSelectionIndex = srcLB.getSelectedIndex();

    deselectAll(destLB);
    for (int i = 0; i < itemCount; i++) {
      if (moveAll || srcLB.isItemSelected(i)) {
        String value = srcLB.getValue(i);
        String name = srcLB.getItemText(i);
        destLB.addItem(name, value); // adds it to the bottom of the destination List
        destLB.setItemSelected(destLB.getItemCount() - 1, true);
      }
    }
    removeItems(srcLB, moveAll);
    if (srcLB.getItemCount() > 0) {
      if ( (srcLB.getItemCount() - 1) < srcSelectionIndex) {
        srcLB.setSelectedIndex(srcLB.getItemCount() - 1);
      } else {
        srcLB.setSelectedIndex(srcSelectionIndex);
      }
    }
  }

  private void removeItems(ListBox targetListBox, boolean removeAll) {
    int itemCount = targetListBox.getItemCount();
    for (int i = itemCount - 1; i >= 0; i--) {
      if (removeAll || targetListBox.isItemSelected(i)) {
        targetListBox.removeItem(i);
      }
    }
  }

  /**
   * @param destLB
   */
  private void deselectAll(ListBox targetListBox) {
    for (int i = 0; i < targetListBox.getItemCount(); i++) {
      targetListBox.setItemSelected(i, false);
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.mantle.client.perspective.solutionbrowser.fileproperties.IFileModifier#apply()
   */
  public void apply() {
    if (dirty) {
      if ((wasEnabled && !enableSubscriptions.isChecked() && Window.confirm(Messages.getInstance().appliedSchedulesWillBeLost())) || (!wasEnabled)) { // We're turning off this subscription... alert the user

        AsyncCallback<Void> callback = new AsyncCallback<Void>() {

          public void onFailure(Throwable caught) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().error(), caught.toString(), false, false, true);
            dialogBox.center();
          }

          public void onSuccess(Void nothing) {
            dirty = false; // I don't know if this even gets back here    
          }

        };
        List<SubscriptionSchedule> currentSchedules = new ArrayList<SubscriptionSchedule>();
        for (int i = 0; i < appliedLB.getItemCount(); i++) {
          SubscriptionSchedule subSchedule = new SubscriptionSchedule();
          subSchedule.title = appliedLB.getItemText(i);
          subSchedule.id = appliedLB.getValue(i);
          currentSchedules.add(subSchedule);
        }
        MantleServiceCache.getService().setSubscriptions(fileItem.getSolution() + fileItem.getPath() + "/" + fileItem.getName(), enableSubscriptions.isChecked(), currentSchedules, callback); //$NON-NLS-1$
      }
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.mantle.client.perspective.solutionbrowser.fileproperties.IFileModifier#init(org.pentaho.mantle.client.perspective.solutionbrowser.FileItem, org.pentaho.mantle.client.objects.SolutionFileInfo)
   */
  public void init(FileItem fileItem, SolutionFileInfo fileInfo) {
    this.fileItem = fileItem;

    updateState();
    wasEnabled = enableSubscriptions.isChecked();
  }

  protected void updateControls() {
    availableLB.setEnabled(enableSubscriptions.isChecked());
    appliedLB.setEnabled(enableSubscriptions.isChecked());
    moveAllLeftBtn.setEnabled(enableSubscriptions.isChecked());
    moveAllRightBtn.setEnabled(enableSubscriptions.isChecked());
    moveLeftBtn.setEnabled(enableSubscriptions.isChecked());
    moveRightBtn.setEnabled(enableSubscriptions.isChecked());
  }

  /**
   * 
   */
  private void updateState() {
    AsyncCallback<SubscriptionState> callBack = new AsyncCallback<SubscriptionState>() {

      public void onSuccess(SubscriptionState state) {
        enableSubscriptions.setChecked(state.subscriptionsEnabled);
        for (SubscriptionSchedule schedule : state.availableSchedules) {
          availableLB.addItem(schedule.title, schedule.id);
        }
        for (SubscriptionSchedule schedule : state.appliedSchedules) {
          appliedLB.addItem(schedule.title, schedule.id);
        }
        updateControls();
      }

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().error(), caught.toString(), false, false, true);
        dialogBox.center();
      }

    };
    MantleServiceCache.getService().getSubscriptionState(fileItem.getSolution() + fileItem.getPath() + "/" + fileItem.getName(), callBack); //$NON-NLS-1$
  }

}
