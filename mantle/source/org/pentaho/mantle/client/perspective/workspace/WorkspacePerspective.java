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
package org.pentaho.mantle.client.perspective.workspace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pentaho.gwt.widgets.client.dialogs.DialogBox;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.JobDetail;
import org.pentaho.mantle.client.objects.JobSchedule;
import org.pentaho.mantle.client.objects.SubscriptionBean;
import org.pentaho.mantle.client.perspective.IPerspective;
import org.pentaho.mantle.client.perspective.IPerspectiveCallback;
import org.pentaho.mantle.client.perspective.RefreshPerspectiveCommand;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.login.client.MantleLoginDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class WorkspacePerspective extends ScrollPanel implements IPerspective {

  private static final int MINUTE = 60000;
  private static final int BACKGROUND_CHECKER_INTERVAL = 1 * MINUTE;
  private static final int WAITING = 0;
  private static final int COMPLETE = 1;
  private static final int MYSCHEDULES = 2;
  private static final int ALLSCHEDULES = 3;
  private boolean backgroundAlertRaised = false;
  DisclosurePanel allScheduledContentDisclosure = new DisclosurePanel("All Schedules (Admin Only)", false);
  DisclosurePanel subscriptionsContentDisclosure = new DisclosurePanel("My Subscriptions", false);
  DisclosurePanel myScheduledContentDisclosure = new DisclosurePanel("My Schedules", false);
  DisclosurePanel waitingContentDisclosure = new DisclosurePanel("Waiting", false);
  DisclosurePanel completedContentDisclosure = new DisclosurePanel("Complete", false);
  private FlexTable allScheduledContentTable;
  private FlexTable subscriptionsContentTable;
  private FlexTable myScheduledContentTable;
  private FlexTable waitingContentTable;
  private FlexTable completedContentTable;
  private FlexTable workspaceTable = new FlexTable();

  IPerspectiveCallback perspectiveCallback;

  public WorkspacePerspective(final IWorkspaceCallback workspaceCallback, final IPerspectiveCallback perspectiveCallback) {
    this.perspectiveCallback = perspectiveCallback;

    Timer backgroundContentAlerter = new Timer() {

      public void run() {
        AsyncCallback callback = new AsyncCallback() {

          public void onFailure(Throwable caught) {
          }

          public void onSuccess(Object result) {
            Boolean backgroundAlert = (Boolean) result;
            if (!backgroundAlertRaised && backgroundAlert) {
              // raise alert
              IDialogCallback callback = new IDialogCallback() {

                public void cancelPressed() {
                  resetBackgroundExecutionAlert();
                }

                public void okPressed() {
                  workspaceCallback.backgroundExecutionCompleted();
                }

              };
              PromptDialogBox dialogBox = new PromptDialogBox("Info", "Yes", "No", false, true);
              dialogBox.setContent(new Label("New content is ready!  Refresh your workspace now?"));
              dialogBox.setCallback(callback);
              dialogBox.center();

              // we will clear this alert when the user views their content
              backgroundAlertRaised = true;
            }
          }
        };
        MantleServiceCache.getService().getBackgroundExecutionAlert(callback);
      }
    };
    backgroundContentAlerter.scheduleRepeating(BACKGROUND_CHECKER_INTERVAL);

    buildScheduledAndCompletedContentPanel();
  }

  public FlexTable buildEmptyBackgroundItemTable(int tableType) {
    FlexTable table = new FlexTable();
    table.setWidth("100%");
    table.setStyleName("backgroundContentTable");
    table.setWidget(0, 0, new Label("Name"));
    table.setWidget(0, 1, new Label("Date"));
    if (tableType == COMPLETE) {
      table.setWidget(0, 2, new Label("Size"));
      table.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);
      table.setWidget(0, 3, new Label("Type"));
      table.setWidget(0, 4, new Label("Actions"));
      table.getCellFormatter().setStyleName(0, 0, "backgroundContentHeaderTableCell");
      table.getCellFormatter().setStyleName(0, 1, "backgroundContentHeaderTableCell");
      table.getCellFormatter().setStyleName(0, 2, "backgroundContentHeaderTableCell");
      table.getCellFormatter().setStyleName(0, 3, "backgroundContentHeaderTableCell");
      table.getCellFormatter().setStyleName(0, 4, "backgroundContentHeaderTableCellRight");
    } else {
      table.setWidget(0, 2, new Label("Actions"));
      table.getCellFormatter().setStyleName(0, 0, "backgroundContentHeaderTableCell");
      table.getCellFormatter().setStyleName(0, 1, "backgroundContentHeaderTableCell");
      table.getCellFormatter().setStyleName(0, 2, "backgroundContentHeaderTableCellRight");
    }
    return table;
  }

  public FlexTable buildEmptyScheduleTable() {
    FlexTable table = new FlexTable();
    table.setWidth("100%");
    table.setStyleName("backgroundContentTable");
    table.setWidget(0, 0, new Label("Job Name"));
    table.setWidget(0, 1, new Label("Job Group"));
    table.setWidget(0, 2, new Label("Description"));
    table.setWidget(0, 3, new Label("Last Run / Next Run"));
    table.setWidget(0, 4, new Label("State"));
    table.setWidget(0, 5, new Label("Actions"));
    table.getCellFormatter().setStyleName(0, 0, "backgroundContentHeaderTableCell");
    table.getCellFormatter().setStyleName(0, 1, "backgroundContentHeaderTableCell");
    table.getCellFormatter().setStyleName(0, 2, "backgroundContentHeaderTableCell");
    table.getCellFormatter().setStyleName(0, 3, "backgroundContentHeaderTableCell");
    table.getCellFormatter().setStyleName(0, 4, "backgroundContentHeaderTableCell");
    table.getCellFormatter().setStyleName(0, 5, "backgroundContentHeaderTableCellRight");
    return table;
  }

  private FlexTable buildEmptySubscriptionsTable() {
    FlexTable table = new FlexTable();
    table.setWidth("100%");
    table.setStyleName("backgroundContentTable");
    table.setWidget(0, 0, new Label(Messages.getInstance().name()));
    table.setWidget(0, 1, new Label(Messages.getInstance().scheduleDate()));
    table.setWidget(0, 2, new Label(Messages.getInstance().type()));
    table.setWidget(0, 3, new Label(Messages.getInstance().size()));
    table.setWidget(0, 4, new Label(Messages.getInstance().actions()));
    table.getFlexCellFormatter().setWidth(1, 0, "200em");
    table.getFlexCellFormatter().setWidth(1, 1, "200em");
    table.getFlexCellFormatter().setWidth(1, 2, "100em");
    table.getFlexCellFormatter().setWidth(1, 3, "100em");
    table.getFlexCellFormatter().setWidth(1, 4, "450em");

    table.getCellFormatter().setStyleName(0, 0, "backgroundContentHeaderTableCell");
    table.getCellFormatter().setStyleName(0, 1, "backgroundContentHeaderTableCell");
    table.getCellFormatter().setStyleName(0, 2, "backgroundContentHeaderTableCell");
    table.getCellFormatter().setStyleName(0, 3, "backgroundContentHeaderTableCell");
    table.getCellFormatter().setStyleName(0, 4, "backgroundContentHeaderTableCell");
    return table;
  }

  public void buildScheduledAndCompletedContentPanel() {
    AsyncCallback callback = new AsyncCallback() {

      public void onSuccess(Object result) {
        boolean isAdministrator = (Boolean) result;
        workspaceTable = new FlexTable();
        workspaceTable
            .setWidget(
                0,
                0,
                new HTML(
                    "<BR><H3>&nbsp;&nbsp;My Workspace</H3><BR>&nbsp;&nbsp;This page shows reports that you have submitted to run in background on the server.<BR>&nbsp;&nbsp;You can cancel ones that have not run yet, and you can view or delete ones that have.<BR><BR>"));
        workspaceTable.setWidget(1, 0, waitingContentDisclosure);
        workspaceTable.setWidget(2, 0, completedContentDisclosure);
        workspaceTable.setWidget(3, 0, myScheduledContentDisclosure);
        if (isAdministrator) {
          workspaceTable.setWidget(4, 0, allScheduledContentDisclosure);
        }

        workspaceTable.setWidget(5, 0, subscriptionsContentDisclosure);
        setWidget(workspaceTable);
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
              
          }

          public void onSuccess(Boolean result) {
            buildScheduledAndCompletedContentPanel();
          }

        });
      }
    };
    MantleServiceCache.getService().isAdministrator(callback);
  }

  public void buildJobTable(List<JobDetail> jobDetails, FlexTable jobTable, DisclosurePanel disclosurePanel, int tableType) {
    disclosurePanel.setOpen(jobDetails != null && jobDetails.size() > 0);
    for (int row = 0; row < jobDetails.size(); row++) {
      final JobDetail jobDetail = jobDetails.get(row);

      HorizontalPanel actionPanel = new HorizontalPanel();
      if (tableType == COMPLETE) {
        Label viewLabel = new Label("View");
        viewLabel.addClickListener(new ClickListener() {

          public void onClick(Widget sender) {
            DialogBox viewDialog = new DialogBox(true, true);
            viewDialog.setStyleName("viewContentDialogBox");
            viewDialog.setText(jobDetail.name + "  (Click outside this frame to close)");
            Frame iframe = new Frame("GetContent?action=view&id=" + jobDetail.id);
            iframe.setPixelSize(1024, 600);
            viewDialog.setWidget(iframe);
            viewDialog.setPixelSize(1024, 600);
            viewDialog.center();
          }

        });
        viewLabel.setStyleName("backgroundContentAction");
        viewLabel.setTitle("View Content");
        Label deleteLabel = new Label("Delete");
        deleteLabel.addClickListener(new ClickListener() {

          public void onClick(Widget sender) {
            deleteContentItem(jobDetail.id);
          }

        });
        deleteLabel.setStyleName("backgroundContentAction");
        deleteLabel.setTitle("Delete Content");

        actionPanel.add(viewLabel);
        actionPanel.add(new HTML("&nbsp;|&nbsp;"));
        actionPanel.add(deleteLabel);
      } else if (tableType == WAITING) {
        Label cancelLabel = new Label("Cancel");
        cancelLabel.addClickListener(new ClickListener() {

          public void onClick(Widget sender) {
            cancelBackgroundJob(jobDetail.id, jobDetail.group);
          }

        });
        cancelLabel.setStyleName("backgroundContentAction");
        cancelLabel.setTitle("Cancel Execution");
        actionPanel.add(cancelLabel);
      }

      jobTable.setWidget(row + 1, 0, new Label(jobDetail.name == null ? (jobDetail.id == null ? "-" : jobDetail.id) : jobDetail.name));
      jobTable.setWidget(row + 1, 1, new Label(jobDetail.timestamp == null ? "-" : jobDetail.timestamp));
      if (tableType == COMPLETE) {
        jobTable.setWidget(row + 1, 2, new Label("" + jobDetail.size));
        jobTable.setWidget(row + 1, 3, new Label(jobDetail.type));
        jobTable.setWidget(row + 1, 4, actionPanel);
        jobTable.getCellFormatter().setStyleName(row + 1, 0, "backgroundContentTableCell");
        jobTable.getCellFormatter().setStyleName(row + 1, 1, "backgroundContentTableCell");
        jobTable.getCellFormatter().setStyleName(row + 1, 2, "backgroundContentTableCell");
        jobTable.getCellFormatter().setHorizontalAlignment(row + 1, 2, HasHorizontalAlignment.ALIGN_RIGHT);
        jobTable.getCellFormatter().setStyleName(row + 1, 3, "backgroundContentTableCell");
        jobTable.getCellFormatter().setStyleName(row + 1, 4, "backgroundContentTableCellRight");
        if (row == jobDetails.size() - 1) {
          // last
          jobTable.getCellFormatter().setStyleName(row + 1, 0, "backgroundContentTableCellBottom");
          jobTable.getCellFormatter().setStyleName(row + 1, 1, "backgroundContentTableCellBottom");
          jobTable.getCellFormatter().setStyleName(row + 1, 2, "backgroundContentTableCellBottom");
          jobTable.getCellFormatter().setHorizontalAlignment(row + 1, 2, HasHorizontalAlignment.ALIGN_RIGHT);
          jobTable.getCellFormatter().setStyleName(row + 1, 3, "backgroundContentTableCellBottom");
          jobTable.getCellFormatter().setStyleName(row + 1, 4, "backgroundContentTableCellBottomRight");
        }
      } else {
        jobTable.setWidget(row + 1, 2, actionPanel);
        jobTable.getCellFormatter().setStyleName(row + 1, 0, "backgroundContentTableCell");
        jobTable.getCellFormatter().setStyleName(row + 1, 1, "backgroundContentTableCell");
        jobTable.getCellFormatter().setStyleName(row + 1, 2, "backgroundContentTableCellRight");
        if (row == jobDetails.size() - 1) {
          // last
          jobTable.getCellFormatter().setStyleName(row + 1, 0, "backgroundContentTableCellBottom");
          jobTable.getCellFormatter().setStyleName(row + 1, 1, "backgroundContentTableCellBottom");
          jobTable.getCellFormatter().setStyleName(row + 1, 2, "backgroundContentTableCellBottomRight");
        }
      }
    }
  }

  public void buildSubscriptionsTable(final List<SubscriptionBean> subscriptionsInfo, final FlexTable subscrTable, final DisclosurePanel disclosurePanel) {
    disclosurePanel.setOpen(subscriptionsInfo != null && subscriptionsInfo.size() > 0);
    subscrTable.setCellSpacing(2);

    int row = 0;
    Iterator<SubscriptionBean> subscrIter = subscriptionsInfo.iterator();
    while (subscrIter.hasNext()) {
      row++;
      final SubscriptionBean currentSubscr = subscrIter.next();
      VerticalPanel namePanel = new VerticalPanel();
      namePanel.add(new Label(currentSubscr.getName()));
      namePanel.add(new Label(currentSubscr.getXactionName()));

      Label scheduleDate = new Label(currentSubscr.getScheduleDate());
      Label size = new Label(currentSubscr.getSize());
      Label type = new Label(currentSubscr.getType());

      HorizontalPanel buttonsPanel = new HorizontalPanel();
      final String subscrName = currentSubscr.getId();

      Button btnRunNow = new Button(Messages.getInstance().run());
      btnRunNow.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          performActionOnSubscription("run", subscrName);
        }
      });

      Button btnArchive = new Button(Messages.getInstance().archive());
      btnArchive.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          performActionOnSubscription("archive", subscrName);
        }
      });

      Button btnEdit = new Button(Messages.getInstance().edit());
      btnEdit.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          performActionOnSubscription("edit", subscrName);
        }
      });
      Button btnDelete = new Button(Messages.getInstance().delete());
      btnDelete.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          performActionOnSubscription("delete", subscrName);
        }
      });

      buttonsPanel.add(btnRunNow);
      buttonsPanel.add(btnArchive);
      buttonsPanel.add(btnEdit);
      buttonsPanel.add(btnDelete);

      subscrTable.setWidget(row, 0, namePanel);
      subscrTable.setWidget(row, 1, scheduleDate);
      subscrTable.setWidget(row, 2, size);
      subscrTable.setWidget(row, 3, type);
      subscrTable.setWidget(row, 4, buttonsPanel);

      List<String[]> scheduleList = currentSubscr.getContent();
      if (scheduleList != null) {
        int scheduleSize = scheduleList.size();

        for (int j = 0; j < scheduleSize; j++) {
          row++;
          final String[] currSchedule = scheduleList.get(j);
          subscrTable.setWidget(row, 1, new Label(currSchedule[0]));
          subscrTable.setWidget(row, 2, new Label(currSchedule[1]));
          subscrTable.setWidget(row, 3, new Label(currSchedule[2]));

          HorizontalPanel actionButtonsPanel = new HorizontalPanel();

          final Button btnViewContent = new Button(Messages.getInstance().view());
          btnViewContent.addClickListener(new ClickListener() {

            public void onClick(Widget sender) {
              final String fileId = currSchedule[3];
              final String name = subscrName;
              performActionOnSubscriptionContent("archived", name, fileId);
            }
          });
          actionButtonsPanel.add(btnViewContent);

          final Button btnDeleteContent = new Button(Messages.getInstance().delete());
          btnDeleteContent.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
              VerticalPanel vp = new VerticalPanel();
              vp.add(new Label(Messages.getInstance().deleteContentConfirm()));
              final PromptDialogBox deleteConfirmDialog = new PromptDialogBox(Messages.getInstance().deleteConfirm(), Messages.getInstance().yes(),
                  Messages.getInstance().no(), false, true, vp);

              final IDialogCallback callback = new IDialogCallback() {

                public void cancelPressed() {
                  deleteConfirmDialog.hide();
                }

                public void okPressed() {
                  final String fileId = currSchedule[3];
                  final String name = subscrName;
                  // performActionOnSubscriptionContent("delete-archived", name, fileId);
                  deleteContentItem(name, fileId);
                }
              };
              deleteConfirmDialog.setCallback(callback);
              deleteConfirmDialog.center();
            }
          });

          actionButtonsPanel.add(btnDeleteContent);
          subscrTable.setWidget(row, 4, actionButtonsPanel);
        }
      }
    }
  }

  private void performActionOnSubscriptionContent(final String action, final String subscrName, final String contentID) {
    performActionOnSubscription(action, subscrName + ":" + contentID);
  }

  private void performActionOnSubscription(final String action, final String subscrName) {
    final PromptDialogBox viewDialog = new PromptDialogBox(Messages.getInstance().view(), Messages.getInstance().close(), null, false, false);
    viewDialog.setContent(new VerticalPanel());
    
    final String url;
    if (GWT.isScript()) {
      url = "/pentaho/ViewAction?subscribe=" + action + "&subscribe-name=" + subscrName;
    } else {
      url = "http://localhost:8080/pentaho/ViewAction?subscribe=" + action + "&subscribe-name=" + subscrName;
    }
    final Frame iframe = new Frame(url);
    iframe.setPixelSize(800, 600);

    final Button btnOk = new Button(Messages.getInstance().close());
    btnOk.setWidth("100");
    btnOk.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        viewDialog.hide();
        // Refresh the view only if user pressed Run and Archive
        if (action.equals("archive")) {
          final Command cmd = new RefreshPerspectiveCommand(WorkspacePerspective.this);
          cmd.execute();
          // fetchSubscriptions();
        }
      }
    });
    ((VerticalPanel)viewDialog.getContent()).add(iframe);
    viewDialog.center();
  }

  public void buildScheduleTable(List<JobSchedule> scheduleDetails, FlexTable scheduleTable, DisclosurePanel disclosurePanel, final int jobSource) {
    disclosurePanel.setOpen(scheduleDetails != null && scheduleDetails.size() > 0);
    for (int row = 0; row < scheduleDetails.size(); row++) {
      final JobSchedule jobSchedule = scheduleDetails.get(row);
      HorizontalPanel actionPanel = new HorizontalPanel();
      Label suspendJobLabel = new Label("Suspend");
      suspendJobLabel.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          suspendJob(jobSchedule.jobName, jobSchedule.jobGroup, jobSource);
        }

      });
      suspendJobLabel.setStyleName("backgroundContentAction");
      suspendJobLabel.setTitle("Suspend this job");

      Label resumeJobLabel = new Label("Resume");
      resumeJobLabel.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          resumeJob(jobSchedule.jobName, jobSchedule.jobGroup, jobSource);
        }

      });
      resumeJobLabel.setStyleName("backgroundContentAction");
      resumeJobLabel.setTitle("Resume this job");

      Label runJobLabel = new Label("Run");
      runJobLabel.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          runJob(jobSchedule.jobName, jobSchedule.jobGroup, jobSource);
        }

      });
      runJobLabel.setStyleName("backgroundContentAction");
      runJobLabel.setTitle("Run this job");

      Label deleteJobLabel = new Label("Delete");
      deleteJobLabel.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          deleteJob(jobSchedule.jobName, jobSchedule.jobGroup, jobSource);
        }

      });
      deleteJobLabel.setStyleName("backgroundContentAction");
      deleteJobLabel.setTitle("Delete this job");

      if (jobSchedule.triggerState == 0) {
        actionPanel.add(suspendJobLabel);
        actionPanel.add(new HTML("&nbsp;|&nbsp;"));
      }
      if (jobSchedule.triggerState == 1) {
        actionPanel.add(resumeJobLabel);
        actionPanel.add(new HTML("&nbsp;|&nbsp;"));
      }
      if (jobSchedule.triggerState != 2) {
        actionPanel.add(runJobLabel);
        actionPanel.add(new HTML("&nbsp;|&nbsp;"));
      }
      actionPanel.add(deleteJobLabel);

      scheduleTable.setWidget(row + 1, 0, new HTML(jobSchedule.jobName));
      scheduleTable.setWidget(row + 1, 1, new HTML(jobSchedule.jobGroup));
      scheduleTable.setWidget(row + 1, 2, new HTML(jobSchedule.jobDescription == null ? "&nbsp;" : jobSchedule.jobDescription));
      scheduleTable.setWidget(row + 1, 3, new HTML((jobSchedule.previousFireTime == null ? "Never" : jobSchedule.previousFireTime.toString()) + "<BR>"
          + (jobSchedule.nextFireTime == null ? "-" : jobSchedule.nextFireTime.toString())));
      scheduleTable.setWidget(row + 1, 4, new HTML(getTriggerStateName(jobSchedule.triggerState)));
      scheduleTable.setWidget(row + 1, 5, actionPanel);
      scheduleTable.getCellFormatter().setStyleName(row + 1, 0, "backgroundContentTableCell");
      scheduleTable.getCellFormatter().setStyleName(row + 1, 1, "backgroundContentTableCell");
      scheduleTable.getCellFormatter().setStyleName(row + 1, 2, "backgroundContentTableCell");
      scheduleTable.getCellFormatter().setStyleName(row + 1, 3, "backgroundContentTableCell");
      scheduleTable.getCellFormatter().setStyleName(row + 1, 4, "backgroundContentTableCell");
      scheduleTable.getCellFormatter().setStyleName(row + 1, 5, "backgroundContentTableCellRight");
      if (row == scheduleDetails.size() - 1) {
        // last
        scheduleTable.getCellFormatter().setStyleName(row + 1, 0, "backgroundContentTableCellBottom");
        scheduleTable.getCellFormatter().setStyleName(row + 1, 1, "backgroundContentTableCellBottom");
        scheduleTable.getCellFormatter().setStyleName(row + 1, 2, "backgroundContentTableCellBottom");
        scheduleTable.getCellFormatter().setStyleName(row + 1, 3, "backgroundContentTableCellBottom");
        scheduleTable.getCellFormatter().setStyleName(row + 1, 4, "backgroundContentTableCellBottom");
        scheduleTable.getCellFormatter().setStyleName(row + 1, 5, "backgroundContentTableCellBottomRight");
      }
    }
  }

  public String getTriggerStateName(int state) {
    if (state == 0) {
      return "Normal";
    } else if (state == 1) {
      return "Paused";
    } else if (state == 2) {
      return "Running";
    }
    return "Unknown";
  }

  public void deleteContentItem(final String subscriptionName, final String fileId) {
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(Object result) {
        String message = (String) result;
        Window.alert(message);
        final Command cmd = new RefreshPerspectiveCommand(WorkspacePerspective.this);
        cmd.execute();
      }
    };
    MantleServiceCache.getService().deleteSubscriptionArchive(subscriptionName, fileId, callback);
  }

  public void fetchWaitingBackgroundItems() {
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(Object result) {
        backgroundAlertRaised = false;
        // result is List<JobDetail>
        List<JobDetail> scheduledJobs = (List<JobDetail>) result;
        waitingContentTable = buildEmptyBackgroundItemTable(WAITING);
        buildJobTable(scheduledJobs, waitingContentTable, waitingContentDisclosure, WAITING);
        waitingContentDisclosure.setContent(waitingContentTable);

      }
    };
    MantleServiceCache.getService().getScheduledBackgroundContent(callback);
  }

  public void fetchCompletedBackgroundItems() {
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(Object result) {
        backgroundAlertRaised = false;
        // result is List<JobDetail>
        List<JobDetail> completedJobs = (List<JobDetail>) result;
        completedContentTable = buildEmptyBackgroundItemTable(COMPLETE);
        buildJobTable(completedJobs, completedContentTable, completedContentDisclosure, COMPLETE);
        completedContentDisclosure.setContent(completedContentTable);
      }
    };
    MantleServiceCache.getService().getCompletedBackgroundContent(callback);
  }

  public void fetchBackgroundItems() {
    AsyncCallback callback = new AsyncCallback() {

      public void onSuccess(Object result) {
        AsyncCallback callback = new AsyncCallback() {

          public void onFailure(Throwable caught) {
            Window.alert(caught.toString());
          }

          public void onSuccess(Object result) {
            fetchWaitingBackgroundItems();
            fetchCompletedBackgroundItems();
            fetchMySchedules();
            fetchAllSchedules();
            fetchSubscriptions();
          }
        };
        MantleServiceCache.getService().getCompletedBackgroundContent(callback);
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
              
          }

          public void onSuccess(Boolean result) {
            fetchBackgroundItems();
          }

        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  public void cancelBackgroundJob(final String jobName, final String jobGroup) {
    AsyncCallback callback = new AsyncCallback() {

      public void onSuccess(Object result) {
        AsyncCallback callback = new AsyncCallback() {

          public void onFailure(Throwable caught) {
            Window.alert(caught.toString());
          }

          public void onSuccess(Object result) {
            fetchWaitingBackgroundItems();
          }
        };
        MantleServiceCache.getService().cancelBackgroundJob(jobName, jobGroup, callback);
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
              
          }

          public void onSuccess(Boolean result) {
            cancelBackgroundJob(jobName, jobGroup);
          }

        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  public void deleteContentItem(final String contentId) {
    AsyncCallback callback = new AsyncCallback() {

      public void onSuccess(Object result) {
        AsyncCallback callback = new AsyncCallback() {

          public void onFailure(Throwable caught) {
            Window.alert(caught.toString());
          }

          public void onSuccess(Object result) {
            fetchCompletedBackgroundItems();
          }
        };
        MantleServiceCache.getService().deleteContentItem(contentId, callback);
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
              
          }

          public void onSuccess(Boolean result) {
            deleteContentItem(contentId);
          }

        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  public void suspendJob(final String jobName, final String jobGroup, final int jobSource) {
    AsyncCallback callback = new AsyncCallback() {

      public void onSuccess(Object result) {
        AsyncCallback callback = new AsyncCallback() {

          public void onFailure(Throwable caught) {
            Window.alert(caught.toString());
          }

          public void onSuccess(Object result) {
            refreshPerspective(false);
          }
        };
        MantleServiceCache.getService().suspendJob(jobName, jobGroup, callback);
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
              
          }

          public void onSuccess(Boolean result) {
            suspendJob(jobName, jobGroup, jobSource);
          }

        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  public void resumeJob(final String jobName, final String jobGroup, final int jobSource) {
    AsyncCallback callback = new AsyncCallback() {

      public void onSuccess(Object result) {
        AsyncCallback callback = new AsyncCallback() {

          public void onFailure(Throwable caught) {
            Window.alert(caught.toString());
          }

          public void onSuccess(Object result) {
            refreshPerspective(false);
          }
        };
        MantleServiceCache.getService().resumeJob(jobName, jobGroup, callback);
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
              
          }

          public void onSuccess(Boolean result) {
            resumeJob(jobName, jobGroup, jobSource);
          }
        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  public void deleteJob(final String jobName, final String jobGroup, final int jobSource) {
    AsyncCallback callback = new AsyncCallback() {

      public void onSuccess(Object result) {
        AsyncCallback callback = new AsyncCallback() {

          public void onFailure(Throwable caught) {
            Window.alert(caught.toString());
          }

          public void onSuccess(Object result) {
            refreshPerspective(false);
          }
        };
        MantleServiceCache.getService().deleteJob(jobName, jobGroup, callback);
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
              
          }

          public void onSuccess(Boolean result) {
            deleteJob(jobName, jobGroup, jobSource);
          }

        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  public void runJob(final String jobName, final String jobGroup, final int jobSource) {
    AsyncCallback callback = new AsyncCallback() {

      public void onSuccess(Object result) {
        AsyncCallback callback = new AsyncCallback() {

          public void onFailure(Throwable caught) {
            Window.alert(caught.toString());
          }

          public void onSuccess(Object result) {
            refreshPerspective(false);
          }
        };
        MantleServiceCache.getService().runJob(jobName, jobGroup, callback);
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
              
          }

          public void onSuccess(Boolean result) {
            runJob(jobName, jobGroup, jobSource);
          }

        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  public void resetBackgroundExecutionAlert() {
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(Object result) {
        backgroundAlertRaised = false;
      }
    };
    MantleServiceCache.getService().resetBackgroundExecutionAlert(callback);
  }

  public void fetchMySchedules() {
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(Object result) {
        // result is List<JobSchedule>
        List<JobSchedule> scheduledJobs = (List<JobSchedule>) result;
        myScheduledContentTable = buildEmptyScheduleTable();
        buildScheduleTable(scheduledJobs, myScheduledContentTable, myScheduledContentDisclosure, MYSCHEDULES);
        myScheduledContentDisclosure.setContent(myScheduledContentTable);
      }
    };
    MantleServiceCache.getService().getMySchedules(callback);
  }

  public void fetchSubscriptions() {
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(Object result) {
        // result is List<>
        List<SubscriptionBean> subscriptionsInfo = (List<SubscriptionBean>) result;
        subscriptionsContentTable = buildEmptySubscriptionsTable();
        buildSubscriptionsTable(subscriptionsInfo, subscriptionsContentTable, subscriptionsContentDisclosure);
        subscriptionsContentDisclosure.setContent(subscriptionsContentTable);
      }
    };
    MantleServiceCache.getService().getSubscriptionsForMyWorkspace(callback);
  }

  public void fetchAllSchedules() {
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(Object result) {
        // result is List<JobSchedule>
        List<JobSchedule> scheduledJobs = (List<JobSchedule>) result;
        allScheduledContentTable = buildEmptyScheduleTable();
        buildScheduleTable(scheduledJobs, allScheduledContentTable, allScheduledContentDisclosure, ALLSCHEDULES);
        allScheduledContentDisclosure.setContent(allScheduledContentTable);
      }
    };
    MantleServiceCache.getService().getAllSchedules(callback);
  }

  public boolean isBackgroundAlertRaised() {
    return backgroundAlertRaised;
  }

  public void setBackgroundAlertRaised(boolean backgroundAlertRaised) {
    this.backgroundAlertRaised = backgroundAlertRaised;
  }

  public void loadPerspective(boolean force, boolean showStatus) {
	// BISERVER-1838: when switching to Workspace, force refresh (always)
    fetchBackgroundItems();
    installViewMenu(perspectiveCallback);
  }

  public void unloadPerspective() {
  }

  public void refreshPerspective(boolean showStatus) {
    loadPerspective(true, showStatus);
  }

  public void installViewMenu(IPerspectiveCallback perspectiveCallback) {
//    List<UIObject> viewMenuItems = new ArrayList<UIObject>();
//    viewMenuItems.add(new MenuItem(Messages.getInstance().refresh(), new RefreshPerspectiveCommand(this)));
//    perspectiveCallback.installViewMenu(viewMenuItems);
  }
}
