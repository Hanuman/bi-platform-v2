/*
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.perspective.solutionbrowser.fileproperties;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.gwt.widgets.client.buttons.RoundedButton;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
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

  List<String> existingUsersAndRoles = new ArrayList<String>();

  FileItem fileItem;
  SolutionFileInfo fileInfo;

  ListBox usersAndRolesList = new ListBox(false);
  Label permissionsLabel = new Label("Permissions:");
  FlexTable permissionsTable = new FlexTable();
  RoundedButton removeButton = new RoundedButton("Remove");
  RoundedButton addButton = new RoundedButton("Add...");

  public PermissionsPanel() {
    removeButton.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        // find list to remove
        if (usersAndRolesList.getItemCount() == 0) {
          return;
        }
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
        final SelectUserOrRoleDialog pickUserRoleDialog = new SelectUserOrRoleDialog(existingUsersAndRoles, new IUserRoleSelectedCallback() {

          public void roleSelected(String role) {
            fileInfo.rolePermissions.add(new RolePermission(role, PERM_NOTHING));
            usersAndRolesList.addItem(role + " (Role)", role);
            existingUsersAndRoles.add(role);
            usersAndRolesList.setSelectedIndex(usersAndRolesList.getItemCount() - 1);
            buildPermissionsTable();
          }

          public void userSelected(String user) {
            fileInfo.userPermissions.add(new UserPermission(user, PERM_NOTHING));
            usersAndRolesList.addItem(user + " (User)", user);
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
    usersAndRolesList.setWidth("100%");
    buttonPanel.setWidth("100%");

    permissionsTable.setStyleName("permissionsTable");
    permissionsTable.setWidth("100%");
    permissionsTable.setHeight("100%");

    int row = 0;
    setWidget(row++, 0, new Label("Users and Roles:"));
    setWidget(row++, 0, usersAndRolesList);
    setWidget(row++, 0, buttonPanel);
    setWidget(row++, 0, permissionsLabel);
    setWidget(row++, 0, permissionsTable);
    setWidth("100%");
  }

  public void buildPermissionsTable() {
    String userOrRoleString = "";
    permissionsTable.clear();
    if (usersAndRolesList.getItemCount() == 0) {
      permissionsLabel.setText("Permissions");
    } else {
      userOrRoleString = usersAndRolesList.getValue(usersAndRolesList.getSelectedIndex());
      permissionsLabel.setText("Permissions for " + userOrRoleString + ":");
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
    final CheckBox allPermissionCheckBox = new CheckBox("All Permissions");
    final CheckBox createPermissionCheckBox = new CheckBox("Create");
    final CheckBox updatePermissionCheckBox = new CheckBox("Update");
    final CheckBox executePermissionCheckBox = new CheckBox("Execute");
    final CheckBox deletePermissionCheckBox = new CheckBox("Delete");
    final CheckBox grantPermissionCheckBox = new CheckBox("Grant Permissions");
    final CheckBox subscribePermissionCheckBox = new CheckBox("Subscribe");

    if ("".equals(userOrRoleString)) {
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

  public void apply() {
    // send the fileInfo back to the server, we've updated it
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox("Error", caught.toString(), false, false, true);
        dialogBox.center();
      }

      public void onSuccess(Object result) {
        // MessageDialogBox dialogBox = new MessageDialogBox("Info", "Permissions set.", false, null, false, true);
        // dialogBox.center();
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
      usersAndRolesList.addItem(userPermission.name + " (User)", userPermission.name);
      existingUsersAndRoles.add(userPermission.name);
    }
    for (RolePermission rolePermission : fileInfo.rolePermissions) {
      usersAndRolesList.addItem(rolePermission.name + " (Role)", rolePermission.name);
      existingUsersAndRoles.add(rolePermission.name);
    }
    if (usersAndRolesList.getItemCount() > 0) {
      usersAndRolesList.setSelectedIndex(0);
    }

    buildPermissionsTable();
  }
}
