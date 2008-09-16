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
package org.pentaho.mantle.client.perspective.solutionbrowser;

import java.util.Date;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class FileCommand implements Command {
  public static final int RUN = 0;
  public static final int EDIT = 1;
  public static final int DELETE = 2;
  public static final int PROPERTIES = 4;
  public static final int BACKGROUND = 5;
  public static final int NEWWINDOW = 6;

  public static final int SCHEDULE_DAILY = 7;
  public static final int SCHEDULE_WEEKDAYS = 8;
  public static final int SCHEDULE_MWF = 9;
  public static final int SCHEDULE_TUTH = 10;

  public static final int SCHEDULE_WEEKLY_MON = 11;
  public static final int SCHEDULE_WEEKLY_TUE = 12;
  public static final int SCHEDULE_WEEKLY_WED = 13;
  public static final int SCHEDULE_WEEKLY_THU = 14;
  public static final int SCHEDULE_WEEKLY_FRI = 15;
  public static final int SCHEDULE_WEEKLY_SAT = 16;
  public static final int SCHEDULE_WEEKLY_SUN = 17;

  public static final int SCHEDULE_MONTHLY_1ST = 18;
  public static final int SCHEDULE_MONTHLY_15TH = 19;
  public static final int SCHEDULE_MONTHLY_FIRST_SUN = 20;
  public static final int SCHEDULE_MONTHLY_FIRST_MON = 21;
  public static final int SCHEDULE_MONTHLY_LAST_FRI = 22;
  public static final int SCHEDULE_MONTHLY_LAST_SUN = 23;
  public static final int SCHEDULE_MONTHLY_LAST_DAY = 24;
  public static final int SCHEDULE_ANUALLY = 25;
  public static final int SCHEDULE_CUSTOM = 26;
  public static final int SCHEDULE_NEW = 28;
  
  public static final int SUBSCRIBE = 27;
  public static final int SHARE = 28;
  
  

  int mode = RUN;
  PopupPanel popupMenu;
  IFileItemCallback fileItemCallback;

  public FileCommand(int inMode, PopupPanel popupMenu, IFileItemCallback fileItemCallback) {
    this.mode = inMode;
    this.popupMenu = popupMenu;
    this.fileItemCallback = fileItemCallback;
  }

  public void execute() {
    if (popupMenu != null) {
      popupMenu.hide();
    }
    if (mode == RUN || mode == BACKGROUND || mode == NEWWINDOW) {
      fileItemCallback.openFile(mode);
    } else if (mode == PROPERTIES) {
      fileItemCallback.loadPropertiesDialog();
    } else if (mode == EDIT) {
      fileItemCallback.editFile();
    } else if (mode == SCHEDULE_NEW) {
      fileItemCallback.createSchedule();
    } else if (mode == SCHEDULE_CUSTOM) {
      Date now = new Date();
      int second = now.getSeconds();
      int minute = now.getMinutes();
      int hour = now.getHours();
      final TextBox cronTextBox = new TextBox();
      cronTextBox.setText(second + " " + minute + " " + hour + " * * ?");
      IDialogCallback callback = new IDialogCallback() {

        public void cancelPressed() {
        }

        public void okPressed() {
          TextBox cronTextBox = new TextBox();
          fileItemCallback.createSchedule(cronTextBox.getText());
        }
        
      };
      PromptDialogBox inputDialog = new PromptDialogBox("Custom CRON Schedule", "Schedule", "Cancel", false, true);
      inputDialog.setContent(cronTextBox);
      inputDialog.setCallback(callback);
      inputDialog.center();
    } else {
      Date now = new Date();
      // need to build cron expressions for each possible type
      // Seconds 0-59 , - * /
      // Minutes 0-59 , - * /
      // Hours 0-23 , - * /
      // Day-of-month 1-31 , - * ? / L W C
      // Month 1-12 or JAN-DEC , - * /
      // Day-of-Week 1-7 or SUN-SAT , - * ? / L C #
      // Year (Optional) empty, 1970-2099 , - * /
      int second = now.getSeconds();
      int minute = now.getMinutes();
      int hour = now.getHours();
      int day = now.getDate();
      int month = now.getMonth() + 1;
      if (mode == SCHEDULE_DAILY) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " * * ?");
      } else if (mode == SCHEDULE_WEEKDAYS) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * MON-FRI");
      } else if (mode == SCHEDULE_MWF) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * MON,WED,FRI");
      } else if (mode == SCHEDULE_TUTH) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * TUE,THU");
      } else if (mode == SCHEDULE_WEEKLY_MON) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * MON");
      } else if (mode == SCHEDULE_WEEKLY_TUE) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * TUE");
      } else if (mode == SCHEDULE_WEEKLY_WED) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * WED");
      } else if (mode == SCHEDULE_WEEKLY_THU) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * THU");
      } else if (mode == SCHEDULE_WEEKLY_FRI) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * FRI");
      } else if (mode == SCHEDULE_WEEKLY_SAT) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * SAT");
      } else if (mode == SCHEDULE_WEEKLY_SUN) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * SUN");
      } else if (mode == SCHEDULE_MONTHLY_1ST) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " 1 * ?");
      } else if (mode == SCHEDULE_MONTHLY_15TH) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " 15 * ?");
      } else if (mode == SCHEDULE_MONTHLY_FIRST_SUN) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * 1#1");
      } else if (mode == SCHEDULE_MONTHLY_FIRST_MON) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * 2#1");
      } else if (mode == SCHEDULE_MONTHLY_LAST_FRI) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * 6L");
      } else if (mode == SCHEDULE_MONTHLY_LAST_SUN) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " ? * 1L");
      } else if (mode == SCHEDULE_MONTHLY_LAST_DAY) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " L * ?");
      } else if (mode == SCHEDULE_ANUALLY) {
        fileItemCallback.createSchedule(second + " " + minute + " " + hour + " " + day + " " + month + " ?");
      } else if (mode == SUBSCRIBE) {
        fileItemCallback.openFile(mode);
      } else if (mode == SHARE) {
        fileItemCallback.shareFile();
      }
    }
  }

  public void setFileItemCallback(IFileItemCallback fileItemCallback) {
    this.fileItemCallback = fileItemCallback;
  }

}