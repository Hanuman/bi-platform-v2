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
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.perspective.solutionbrowser.fileproperties;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.gwt.widgets.client.buttons.RoundedButton;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.RolePermission;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.objects.UserPermission;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class PermissionsPanel extends FlexTable implements IFileModifier {

  public static final int PERM_ALL = -1;
  public static final int PERM_NOTHING = 0;
  public static final int PERM_EXECUTE = 0x01;
  public static final int PERM_SUBSCRIBE = 0x02;
  public static final int PERM_CREATE = 0x04;
  public static final int PERM_UPDATE = 0x08;
  public static final int PERM_DELETE = 0x10;
  public static final int PERM_UPDATE_PERMS = 0x20;

  private boolean dirty = false;
  
  List<String> existingUsersAndRoles = new ArrayList<String>();

  FileItem fileItem;
  SolutionFileInfo fileInfo;

  ListBox usersAndRolesList = new ListBox(false);
  Label permissionsLabel = new Label(Messages.getString("permissionsColon")); //$NON-NLS-1$
  FlexTable permissionsTable = new FlexTable();
  RoundedButton removeButton = new RoundedButton(Messages.getString("remove")); //$NON-NLS-1$
  RoundedButton addButton = new RoundedButton(Messages.getString("addPeriods")); //$NON-NLS-1$

  public PermissionsPanel() {
    removeButton.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        // find list to remove
        if (usersAndRolesList.getItemCount() == 0) {
          return;
        }
        dirty = true;
        final String userOrRoleString = usersAndRolesList.getValue(usersAndRolesList.getSelectedIndex());
        for (UserPermission userPermission : fileInfo.userPermissions) {
          if (userOrRoleString.equals(userPermission.name)) {
            existingUsersAndRoles.remove(userPermission.name);
            fileInfo.userPermissions.remove(userPermission);
            break;
          }
        }
        for (RolePermission rolePermission : fileInfo.rolePermissions) {
          if (userOrRoleString.equals(rolePermission.name)) {
            existingUsersAndRoles.remove(rolePermission.name);
            fileInfo.rolePermissions.remove(rolePermission);
            break;
          }
        }
        usersAndRolesList.removeItem(usersAndRolesList.getSelectedIndex());
      }

    });

    addButton.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        dirty = true;
        final SelectUserOrRoleDialog pickUserRoleDialog = new SelectUserOrRoleDialog(existingUsersAndRoles, new IUserRoleSelectedCallback() {

          public void roleSelected(String role) {
            fileInfo.rolePermissions.add(new RolePermission(role, PERM_NOTHING));
            usersAndRolesList.addItem(role + Messages.getString("role", role), role); //$NON-NLS-1$
            existingUsersAndRoles.add(role);
            usersAndRolesList.setSelectedIndex(usersAndRolesList.getItemCount() - 1);
            buildPermissionsTable();
          }

          public void userSelected(String user) {
            fileInfo.userPermissions.add(new UserPermission(user, PERM_NOTHING));
            usersAndRolesList.addItem(Messages.getString("user", user), user); //$NON-NLS-1$
            existingUsersAndRoles.add(user);
            usersAndRolesList.setSelectedIndex(usersAndRolesList.getItemCount() - 1);
            buildPermissionsTable();
          }
        });
        pickUserRoleDialog.center();
      }

    });

    FlowPanel buttonPanel = new FlowPanel();
    buttonPanel.add(addButton);
    buttonPanel.add(removeButton);
    usersAndRolesList.setVisibleItemCount(7);
    usersAndRolesList.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        // update permissions list and permission label (put username in it)
        // rebuild permissionsTable settings based on selected mask
        buildPermissionsTable();
      }

    });
    usersAndRolesList.setWidth("100%"); //$NON-NLS-1$
    buttonPanel.setWidth("100%"); //$NON-NLS-1$

    permissionsTable.setStyleName("permissionsTable"); //$NON-NLS-1$
    permissionsTable.setWidth("100%"); //$NON-NLS-1$
    permissionsTable.setHeight("100%"); //$NON-NLS-1$

    int row = 0;
    setWidget(row++, 0, new Label(Messages.getString("usersAndRoles"))); //$NON-NLS-1$
    setWidget(row++, 0, usersAndRolesList);
    setWidget(row++, 0, buttonPanel);
    setWidget(row++, 0, permissionsLabel);
    setWidget(row++, 0, permissionsTable);
    setWidth("100%"); //$NON-NLS-1$
  }

  public void buildPermissionsTable() {
    String userOrRoleString = ""; //$NON-NLS-1$
    permissionsTable.clear();
    if (usersAndRolesList.getItemCount() == 0) {
      permissionsLabel.setText(Messages.getString("permissionsColon")); //$NON-NLS-1$
    } else {
      userOrRoleString = usersAndRolesList.getValue(usersAndRolesList.getSelectedIndex());
      permissionsLabel.setText(Messages.getString("permissionsFor", userOrRoleString)); //$NON-NLS-1$
    }
    int mask = PERM_NOTHING;
    for (UserPermission userPermission : fileInfo.userPermissions) {
      if (userOrRoleString.equals(userPermission.name)) {
        mask = userPermission.mask;
        break;
      }
    }
    for (RolePermission rolePermission : fileInfo.rolePermissions) {
      if (userOrRoleString.equals(rolePermission.name)) {
        mask = rolePermission.mask;
        break;
      }
    }
    // create checkboxes, with listeners who update the fileInfo lists
    final CheckBox allPermissionCheckBox = new CheckBox(Messages.getString("allPermissions")); //$NON-NLS-1$
    final CheckBox createPermissionCheckBox = new CheckBox(Messages.getString("create")); //$NON-NLS-1$
    final CheckBox updatePermissionCheckBox = new CheckBox(Messages.getString("update")); //$NON-NLS-1$
    final CheckBox executePermissionCheckBox = new CheckBox(Messages.getString("execute")); //$NON-NLS-1$
    final CheckBox deletePermissionCheckBox = new CheckBox(Messages.getString("delete")); //$NON-NLS-1$
    final CheckBox grantPermissionCheckBox = new CheckBox(Messages.getString("grantPermissions")); //$NON-NLS-1$
    final CheckBox subscribePermissionCheckBox = new CheckBox(Messages.getString("schedule")); //$NON-NLS-1$

    if ("".equals(userOrRoleString)) { //$NON-NLS-1$
      allPermissionCheckBox.setEnabled(false);
      createPermissionCheckBox.setEnabled(false);
      updatePermissionCheckBox.setEnabled(false);
      executePermissionCheckBox.setEnabled(false);
      deletePermissionCheckBox.setEnabled(false);
      grantPermissionCheckBox.setEnabled(false);
      subscribePermissionCheckBox.setEnabled(false);
    }

    if ((mask & PERM_ALL) == PERM_ALL) {
      allPermissionCheckBox.setChecked(true);
      createPermissionCheckBox.setEnabled(false);
      updatePermissionCheckBox.setEnabled(false);
      executePermissionCheckBox.setEnabled(false);
      deletePermissionCheckBox.setEnabled(false);
      grantPermissionCheckBox.setEnabled(false);
      subscribePermissionCheckBox.setEnabled(false);
    }

    createPermissionCheckBox.setChecked((mask & PERM_CREATE) == PERM_CREATE);
    updatePermissionCheckBox.setChecked((mask & PERM_UPDATE) == PERM_UPDATE);
    executePermissionCheckBox.setChecked((mask & PERM_EXECUTE) == PERM_EXECUTE);
    deletePermissionCheckBox.setChecked((mask & PERM_DELETE) == PERM_DELETE);
    grantPermissionCheckBox.setChecked((mask & PERM_UPDATE_PERMS) == PERM_UPDATE_PERMS);
    subscribePermissionCheckBox.setChecked((mask & PERM_SUBSCRIBE) == PERM_SUBSCRIBE);
    
    allPermissionCheckBox.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        updatePermissionMask(allPermissionCheckBox.isChecked(), PERM_ALL);
        createPermissionCheckBox.setChecked(allPermissionCheckBox.isChecked());
        updatePermissionCheckBox.setChecked(allPermissionCheckBox.isChecked());
        executePermissionCheckBox.setChecked(allPermissionCheckBox.isChecked());
        deletePermissionCheckBox.setChecked(allPermissionCheckBox.isChecked());
        grantPermissionCheckBox.setChecked(allPermissionCheckBox.isChecked());
        subscribePermissionCheckBox.setChecked(allPermissionCheckBox.isChecked());

        createPermissionCheckBox.setEnabled(!allPermissionCheckBox.isChecked());
        updatePermissionCheckBox.setEnabled(!allPermissionCheckBox.isChecked());
        executePermissionCheckBox.setEnabled(!allPermissionCheckBox.isChecked());
        deletePermissionCheckBox.setEnabled(!allPermissionCheckBox.isChecked());
        grantPermissionCheckBox.setEnabled(!allPermissionCheckBox.isChecked());
        subscribePermissionCheckBox.setEnabled(!allPermissionCheckBox.isChecked());
      }
    });
    createPermissionCheckBox.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        updatePermissionMask(createPermissionCheckBox.isChecked(), PERM_CREATE);
      }
    });
    updatePermissionCheckBox.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        updatePermissionMask(updatePermissionCheckBox.isChecked(), PERM_UPDATE);
      }
    });
    executePermissionCheckBox.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        updatePermissionMask(executePermissionCheckBox.isChecked(), PERM_EXECUTE);
      }
    });
    deletePermissionCheckBox.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        updatePermissionMask(deletePermissionCheckBox.isChecked(), PERM_DELETE);
      }
    });
    grantPermissionCheckBox.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        updatePermissionMask(grantPermissionCheckBox.isChecked(), PERM_UPDATE_PERMS);
      }
    });
    subscribePermissionCheckBox.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        updatePermissionMask(subscribePermissionCheckBox.isChecked(), PERM_SUBSCRIBE);
      }
    });
    permissionsTable.setWidget(0, 0, allPermissionCheckBox);
    permissionsTable.setWidget(1, 0, createPermissionCheckBox);
    permissionsTable.setWidget(2, 0, updatePermissionCheckBox);
    permissionsTable.setWidget(3, 0, executePermissionCheckBox);
    permissionsTable.setWidget(4, 0, deletePermissionCheckBox);
    permissionsTable.setWidget(5, 0, grantPermissionCheckBox);
    permissionsTable.setWidget(6, 0, subscribePermissionCheckBox);
  }

  public void updatePermissionMask(boolean grant, int mask) {
    if (usersAndRolesList.getSelectedIndex() >= 0) {
      dirty = true;
      final String userOrRoleString = usersAndRolesList.getValue(usersAndRolesList.getSelectedIndex());
      for (UserPermission userPermission : fileInfo.userPermissions) {
        if (userOrRoleString.equals(userPermission.name)) {
          if (grant) {
            userPermission.mask = userPermission.mask | mask;
          } else {
            userPermission.mask &= ~mask;
          }
          break;
        }
      }
      for (RolePermission rolePermission : fileInfo.rolePermissions) {
        if (userOrRoleString.equals(rolePermission.name)) {
          if (grant) {
            rolePermission.mask = rolePermission.mask | mask;
          } else {
            rolePermission.mask &= ~mask;
          }
          break;
        }
      }
    }
  }

  public void apply(final IDialogCallback applyCallback) {
    if (!dirty) {
      // do nothing if we're not dirty (make sure to invoke callback)
      applyCallback.okPressed();
      return;
    }
    // send the fileInfo back to the server, we've updated it
    AsyncCallback<Void> callback = new AsyncCallback<Void>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), caught.toString(), false, false, true); //$NON-NLS-1$
        dialogBox.center();
        // invoke the next
        applyCallback.okPressed();
      }

      public void onSuccess(Void nothing) {
        // MessageDialogBox dialogBox = new MessageDialogBox("Info", "Permissions set.", false, null, false, true);
        // dialogBox.center();
        // invoke the next
        dirty = false;
        applyCallback.okPressed();
      }
    };

    MantleServiceCache.getService().setSolutionFileInfo(fileInfo, callback);
  }

  public void init(FileItem fileItem, SolutionFileInfo fileInfo) {
    this.fileItem = fileItem;
    this.fileInfo = fileInfo;
    usersAndRolesList.clear();
    existingUsersAndRoles.clear();
    for (UserPermission userPermission : fileInfo.userPermissions) {
      usersAndRolesList.addItem(Messages.getString("user", userPermission.name), userPermission.name); //$NON-NLS-1$
      existingUsersAndRoles.add(userPermission.name);
    }
    for (RolePermission rolePermission : fileInfo.rolePermissions) {
      usersAndRolesList.addItem(Messages.getString("role", rolePermission.name), rolePermission.name); //$NON-NLS-1$
      existingUsersAndRoles.add(rolePermission.name);
    }
    if (usersAndRolesList.getItemCount() > 0) {
      usersAndRolesList.setSelectedIndex(0);
    }

    buildPermissionsTable();
  }
}
