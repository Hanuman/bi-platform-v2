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
package org.pentaho.mantle.client.perspective.workspace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.JobDetail;
import org.pentaho.mantle.client.objects.JobSchedule;
import org.pentaho.mantle.client.objects.SubscriptionBean;
import org.pentaho.mantle.client.perspective.IPerspectiveCallback;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.login.client.MantleLoginDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class WorkspacePerspective extends ScrollPanel {

  private static final int WAITING = 0;
  private static final int COMPLETE = 1;
  private static final int MYSCHEDULES = 2;
  private static final int ALLSCHEDULES = 3;
  private static final String DELETE = "delete"; //$NON-NLS-1$

  private boolean backgroundAlertRaised = false;
  private DisclosurePanel allScheduledContentDisclosure = new DisclosurePanel(Messages.getInstance().allSchedulesAdminOnly(), false);
  private DisclosurePanel subscriptionsContentDisclosure = new DisclosurePanel(Messages.getInstance().publicSchedules(), false);
  private DisclosurePanel myScheduledContentDisclosure = new DisclosurePanel(Messages.getInstance().mySchedules(), false);
  private DisclosurePanel waitingContentDisclosure = new DisclosurePanel(Messages.getInstance().waiting(), false);
  private DisclosurePanel completedContentDisclosure = new DisclosurePanel(Messages.getInstance().complete(), false);
  private FlexTable allScheduledContentTable;
  private FlexTable subscriptionsContentTable;
  private FlexTable myScheduledContentTable;
  private FlexTable waitingContentTable;
  private FlexTable completedContentTable;
  private FlexTable workspaceTable = new FlexTable();

  private SolutionBrowserPerspective solutionBrowserPerspective;

  public WorkspacePerspective(final SolutionBrowserPerspective solutionBrowserPerspective, final IPerspectiveCallback perspectiveCallback) {
    this.solutionBrowserPerspective = solutionBrowserPerspective;
    DOM.setStyleAttribute(getElement(), "backgroundColor", "white"); //$NON-NLS-1$ //$NON-NLS-2$
    buildScheduledAndCompletedContentPanel();
  }

  public FlexTable buildEmptyBackgroundItemTable(int tableType) {
    FlexTable table = new FlexTable();
    table.setWidth("100%"); //$NON-NLS-1$
    table.setStyleName("backgroundContentTable"); //$NON-NLS-1$
    table.setWidget(0, 0, new Label(Messages.getInstance().name()));
    table.setWidget(0, 1, new Label(Messages.getInstance().date()));
    if (tableType == COMPLETE) {
      table.setWidget(0, 2, new Label(Messages.getInstance().size()));
      table.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);
      table.setWidget(0, 3, new Label(Messages.getInstance().type()));
      table.setWidget(0, 4, new Label(Messages.getInstance().actions()));
      table.getCellFormatter().setStyleName(0, 0, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 1, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 2, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 3, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 4, "backgroundContentHeaderTableCellRight"); //$NON-NLS-1$
    } else {
      table.setWidget(0, 2, new Label(Messages.getInstance().actions()));
      table.getCellFormatter().setStyleName(0, 0, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 1, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 2, "backgroundContentHeaderTableCellRight"); //$NON-NLS-1$
    }
    return table;
  }

  public FlexTable buildEmptyScheduleTable() {
    FlexTable table = new FlexTable();
    table.setWidth("100%"); //$NON-NLS-1$
    table.setStyleName("backgroundContentTable"); //$NON-NLS-1$
    table.setWidget(0, 0, new Label(Messages.getInstance().jobName()));
    table.setWidget(0, 1, new Label(Messages.getInstance().jobGroup()));
    table.setWidget(0, 2, new Label(Messages.getInstance().description()));
    table.setWidget(0, 3, new Label(Messages.getInstance().lastRunNextRun()));
    table.setWidget(0, 4, new Label(Messages.getInstance().state()));
    table.setWidget(0, 5, new Label(Messages.getInstance().actions()));
    table.getCellFormatter().setStyleName(0, 0, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 1, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 2, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 3, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 4, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 5, "backgroundContentHeaderTableCellRight"); //$NON-NLS-1$
    return table;
  }

  private FlexTable buildEmptySubscriptionsTable() {
    FlexTable table = new FlexTable();
    table.setWidth("100%"); //$NON-NLS-1$
    table.setStyleName("backgroundContentTable"); //$NON-NLS-1$
    table.setWidget(0, 0, new Label(Messages.getInstance().name()));
    table.setWidget(0, 1, new Label(Messages.getInstance().scheduleDate()));
    table.setWidget(0, 2, new Label(Messages.getInstance().type()));
    table.setWidget(0, 3, new Label(Messages.getInstance().size()));
    table.setWidget(0, 4, new Label(Messages.getInstance().actions()));
    table.getFlexCellFormatter().setWidth(1, 0, "200em"); //$NON-NLS-1$
    table.getFlexCellFormatter().setWidth(1, 1, "200em"); //$NON-NLS-1$
    table.getFlexCellFormatter().setWidth(1, 2, "100em"); //$NON-NLS-1$
    table.getFlexCellFormatter().setWidth(1, 3, "100em"); //$NON-NLS-1$
    table.getFlexCellFormatter().setWidth(1, 4, "450em"); //$NON-NLS-1$

    table.getCellFormatter().setStyleName(0, 0, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 1, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 2, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 3, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 4, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    return table;
  }

  public void buildScheduledAndCompletedContentPanel() {
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean isAdministrator) {
        workspaceTable = new FlexTable();
        workspaceTable.setWidget(0, 0, new HTML(Messages.getInstance().workspaceMessage()));
        workspaceTable.setWidget(1, 0, waitingContentDisclosure);
        workspaceTable.setWidget(2, 0, completedContentDisclosure);
        workspaceTable.setWidget(3, 0, myScheduledContentDisclosure);
        if (isAdministrator) {
          workspaceTable.setWidget(4, 0, allScheduledContentDisclosure);
        }

        workspaceTable.setWidget(5, 0, subscriptionsContentDisclosure);
        DOM.setStyleAttribute(workspaceTable.getElement(), "margin", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
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
        Label viewLabel = new Label(Messages.getInstance().view());
        viewLabel.addClickListener(new ClickListener() {

          public void onClick(Widget sender) {
            // PromptDialogBox viewDialog = new PromptDialogBox(jobDetail.name, "Close", null, true, true);
            // viewDialog.setPixelSize(1024, 600);
            // viewDialog.center();
            // // if this iframe is placed above the show/center of the dialog, the browser will
            // // end up making 2 requests for the url in the iframe (one of which will be terminated and
            // // we'll see an error on the server about a broken pipe).
            // Frame iframe = new Frame("GetContent?action=view&id=" + jobDetail.id);
            // viewDialog.setContent(iframe);
            // iframe.setPixelSize(1024, 600);
            solutionBrowserPerspective.showNewURLTab(jobDetail.name, jobDetail.name, "GetContent?action=view&id=" + jobDetail.id); //$NON-NLS-1$
          }

        });
        viewLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
        viewLabel.setTitle(Messages.getInstance().viewContent());
        Label deleteLabel = new Label(Messages.getInstance().delete());
        deleteLabel.addClickListener(new ClickListener() {

          public void onClick(Widget sender) {
            deleteContentItem(jobDetail.id);
          }

        });
        deleteLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
        deleteLabel.setTitle(Messages.getInstance().deleteContent());

        actionPanel.add(viewLabel);
        actionPanel.add(new HTML("&nbsp;|&nbsp;")); //$NON-NLS-1$
        actionPanel.add(deleteLabel);
      } else if (tableType == WAITING) {
        Label cancelLabel = new Label(Messages.getInstance().cancel());
        cancelLabel.addClickListener(new ClickListener() {

          public void onClick(Widget sender) {
            cancelBackgroundJob(jobDetail.id, jobDetail.group);
          }

        });
        cancelLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
        cancelLabel.setTitle(Messages.getInstance().cancelExecution());
        actionPanel.add(cancelLabel);
      }

      jobTable.setWidget(row + 1, 0, new Label(jobDetail.name == null ? (jobDetail.id == null ? "-" : jobDetail.id) : jobDetail.name)); //$NON-NLS-1$
      jobTable.setWidget(row + 1, 1, new Label(jobDetail.timestamp == null ? "-" : jobDetail.timestamp)); //$NON-NLS-1$
      if (tableType == COMPLETE) {
        jobTable.setWidget(row + 1, 2, new Label("" + jobDetail.size)); //$NON-NLS-1$
        jobTable.setWidget(row + 1, 3, new Label(jobDetail.type));
        jobTable.setWidget(row + 1, 4, actionPanel);
        jobTable.getCellFormatter().setStyleName(row + 1, 0, "backgroundContentTableCell"); //$NON-NLS-1$
        jobTable.getCellFormatter().setStyleName(row + 1, 1, "backgroundContentTableCell"); //$NON-NLS-1$
        jobTable.getCellFormatter().setStyleName(row + 1, 2, "backgroundContentTableCell"); //$NON-NLS-1$
        jobTable.getCellFormatter().setHorizontalAlignment(row + 1, 2, HasHorizontalAlignment.ALIGN_RIGHT);
        jobTable.getCellFormatter().setStyleName(row + 1, 3, "backgroundContentTableCell"); //$NON-NLS-1$
        jobTable.getCellFormatter().setStyleName(row + 1, 4, "backgroundContentTableCellRight"); //$NON-NLS-1$
        if (row == jobDetails.size() - 1) {
          // last
          jobTable.getCellFormatter().setStyleName(row + 1, 0, "backgroundContentTableCellBottom"); //$NON-NLS-1$
          jobTable.getCellFormatter().setStyleName(row + 1, 1, "backgroundContentTableCellBottom"); //$NON-NLS-1$
          jobTable.getCellFormatter().setStyleName(row + 1, 2, "backgroundContentTableCellBottom"); //$NON-NLS-1$
          jobTable.getCellFormatter().setHorizontalAlignment(row + 1, 2, HasHorizontalAlignment.ALIGN_RIGHT);
          jobTable.getCellFormatter().setStyleName(row + 1, 3, "backgroundContentTableCellBottom"); //$NON-NLS-1$
          jobTable.getCellFormatter().setStyleName(row + 1, 4, "backgroundContentTableCellBottomRight"); //$NON-NLS-1$
        }
      } else {
        jobTable.setWidget(row + 1, 2, actionPanel);
        jobTable.getCellFormatter().setStyleName(row + 1, 0, "backgroundContentTableCell"); //$NON-NLS-1$
        jobTable.getCellFormatter().setStyleName(row + 1, 1, "backgroundContentTableCell"); //$NON-NLS-1$
        jobTable.getCellFormatter().setStyleName(row + 1, 2, "backgroundContentTableCellRight"); //$NON-NLS-1$
        if (row == jobDetails.size() - 1) {
          // last
          jobTable.getCellFormatter().setStyleName(row + 1, 0, "backgroundContentTableCellBottom"); //$NON-NLS-1$
          jobTable.getCellFormatter().setStyleName(row + 1, 1, "backgroundContentTableCellBottom"); //$NON-NLS-1$
          jobTable.getCellFormatter().setStyleName(row + 1, 2, "backgroundContentTableCellBottomRight"); //$NON-NLS-1$
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

      Label lblRunNow = new Label(Messages.getInstance().run());
      lblRunNow.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      lblRunNow.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          performActionOnSubscription("run", subscrName); //$NON-NLS-1$
        }
      });

      Label lblArchive = new Label(Messages.getInstance().archive());
      lblArchive.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      lblArchive.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          performActionOnSubscription("archive", subscrName); //$NON-NLS-1$
        }
      });

      Label lblEdit = new Label(Messages.getInstance().edit());
      lblEdit.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      lblEdit.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          performActionOnSubscription("edit", subscrName); //$NON-NLS-1$
        }
      });
      Label lblDelete = new Label(Messages.getInstance().delete());
      lblDelete.setStyleName("backgroundContentAction"); //$NON-NLS-1$

      lblDelete.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          doDelete(true, currentSubscr, ""); //$NON-NLS-1$
        }
      });

      buttonsPanel.add(lblRunNow);
      buttonsPanel.add(new HTML("&nbsp;|&nbsp;")); //$NON-NLS-1$
      buttonsPanel.add(lblArchive);
      buttonsPanel.add(new HTML("&nbsp;|&nbsp;")); //$NON-NLS-1$
      buttonsPanel.add(lblEdit);
      buttonsPanel.add(new HTML("&nbsp;|&nbsp;")); //$NON-NLS-1$
      buttonsPanel.add(lblDelete);

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

          final Label lblViewContent = new Label(Messages.getInstance().view());
          lblViewContent.setStyleName("backgroundContentAction"); //$NON-NLS-1$
          lblViewContent.addClickListener(new ClickListener() {

            public void onClick(Widget sender) {
              final String fileId = currSchedule[3];
              final String name = subscrName;
              performActionOnSubscriptionContent("archived", name, fileId); //$NON-NLS-1$
            }
          });
          actionButtonsPanel.add(lblViewContent);

          final Label lblDeleteContent = new Label(Messages.getInstance().delete());
          lblDeleteContent.setStyleName("backgroundContentAction"); //$NON-NLS-1$
          lblDeleteContent.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
              doDelete(false, currentSubscr, currSchedule[3]);
            }
          });

          actionButtonsPanel.add(new HTML("&nbsp;|&nbsp;")); //$NON-NLS-1$
          actionButtonsPanel.add(lblDeleteContent);
          subscrTable.setWidget(row, 4, actionButtonsPanel);
        }
      }
    }
  }

  /*
   * Helper method to delete the content items or the public schedule based on what's passed in.
   */
  private void doDelete(final boolean isPublicSchedule, final SubscriptionBean currentSubscr, final String fileId) {
    VerticalPanel vp = new VerticalPanel();
    if (isPublicSchedule) {
      vp.add(new Label(Messages.getInstance().deletePublicSchedule()));
    } else {
      vp.add(new Label(Messages.getInstance().deleteContentItem()));
    }

    final PromptDialogBox deleteConfirmDialog = new PromptDialogBox(Messages.getInstance().deleteConfirm(), Messages.getInstance().yes(), Messages
        .getInstance().no(), false, true, vp);

    final IDialogCallback callback = new IDialogCallback() {
      public void cancelPressed() {
        deleteConfirmDialog.hide();
      }

      public void okPressed() {
        if (isPublicSchedule) {
          deletePublicScheduleAndContents(currentSubscr);
        } else {
          deleteContentItem(currentSubscr.getId(), fileId);
        }
        refreshWorkspace();
      }
    };
    deleteConfirmDialog.setCallback(callback);
    deleteConfirmDialog.center();
  }

  /*
   * Deletes the given public schedule and all the contents belonging to it.
   * 
   * @param currSubscr Current public schedule to be deleted
   */
  private void deletePublicScheduleAndContents(final SubscriptionBean currPublicSchedule) {
    final String subscrName = currPublicSchedule.getId();
    final List<String[]> scheduleList = currPublicSchedule.getContent();
    final List<String> fileList = new ArrayList<String>();

    if (scheduleList != null) {
      AsyncCallback<String> callback = new AsyncCallback<String>() {
        public void onFailure(Throwable caught) {
          Window.alert(caught.getMessage());
        }

        public void onSuccess(String result) {
          // Don't do anything on success.
        }
      };

      final int scheduleSize = scheduleList.size();
      for (int j = 0; j < scheduleSize; j++) {
        final String[] currSchedule = scheduleList.get(j);
        final String fileId = currSchedule[3];
        fileList.add(fileId);
      }
      MantleServiceCache.getService().deletePublicScheduleAndContents(subscrName, fileList, callback);
    }
  }

  private void performActionOnSubscriptionContent(final String action, final String subscrName, final String contentID) {
    performActionOnSubscription(action, subscrName + ":" + contentID); //$NON-NLS-1$
  }

  private void performActionOnSubscription(final String action, final String subscrName) {
    final PromptDialogBox viewDialog = new PromptDialogBox(Messages.getInstance().view(), Messages.getInstance().close(), null, false, false);
    viewDialog.setContent(new VerticalPanel());
    viewDialog.setCallback(new IDialogCallback() {
      public void okPressed() {
        viewDialog.hide();
        // Refresh the view
        if (action.equals("archive") || action.equals(DELETE)) { //$NON-NLS-1$
          refreshWorkspace();
        }
      }

      public void cancelPressed() {
      }
    });

    final String url;
    if (GWT.isScript()) {
      url = "ViewAction?subscribe=" + action + "&subscribe-name=" + subscrName; //$NON-NLS-1$ //$NON-NLS-2$
    } else {
      url = "http://localhost:8080/pentaho/ViewAction?subscribe=" + action + "&subscribe-name=" + subscrName; //$NON-NLS-1$ //$NON-NLS-2$
    }

    if (action.equals("archived") || action.equals("run")) { //$NON-NLS-1$ //$NON-NLS-2$
      solutionBrowserPerspective.showNewURLTab(subscrName, subscrName, url);
    } else {
      viewDialog.center();
      final Frame iframe = new Frame(url);

      // BISERVER-1931: Reducing the size of the dialog box when
      // subscription is to be deleted
      if (action.equals(DELETE)) {
        iframe.setSize("100%", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        iframe.setPixelSize(800, 600);
      }

      ((VerticalPanel) viewDialog.getContent()).add(iframe);
    }
  }

  public void buildScheduleTable(List<JobSchedule> scheduleDetails, FlexTable scheduleTable, DisclosurePanel disclosurePanel, final int jobSource) {
    disclosurePanel.setOpen(scheduleDetails != null && scheduleDetails.size() > 0);
    for (int row = 0; row < scheduleDetails.size(); row++) {
      final JobSchedule jobSchedule = scheduleDetails.get(row);
      HorizontalPanel actionPanel = new HorizontalPanel();
      Label suspendJobLabel = new Label(Messages.getInstance().suspend());
      suspendJobLabel.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          suspendJob(jobSchedule.jobName, jobSchedule.jobGroup, jobSource);
        }

      });
      suspendJobLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      suspendJobLabel.setTitle(Messages.getInstance().suspendThisJob());

      Label resumeJobLabel = new Label(Messages.getInstance().resume());
      resumeJobLabel.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          resumeJob(jobSchedule.jobName, jobSchedule.jobGroup, jobSource);
        }

      });
      resumeJobLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      resumeJobLabel.setTitle(Messages.getInstance().resumeThisJob());

      Label runJobLabel = new Label(Messages.getInstance().run());
      runJobLabel.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          runJob(jobSchedule.jobName, jobSchedule.jobGroup, jobSource);
        }

      });
      runJobLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      runJobLabel.setTitle(Messages.getInstance().runThisJob());

      Label deleteJobLabel = new Label(Messages.getInstance().delete());
      deleteJobLabel.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          deleteJob(jobSchedule.jobName, jobSchedule.jobGroup, jobSource);
        }

      });
      deleteJobLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      deleteJobLabel.setTitle(Messages.getInstance().deleteThisJob());

      if (jobSchedule.triggerState == 0) {
        actionPanel.add(suspendJobLabel);
        actionPanel.add(new HTML("&nbsp;|&nbsp;")); //$NON-NLS-1$
      }
      if (jobSchedule.triggerState == 1) {
        actionPanel.add(resumeJobLabel);
        actionPanel.add(new HTML("&nbsp;|&nbsp;")); //$NON-NLS-1$
      }
      if (jobSchedule.triggerState != 2) {
        actionPanel.add(runJobLabel);
        actionPanel.add(new HTML("&nbsp;|&nbsp;")); //$NON-NLS-1$
      }
      actionPanel.add(deleteJobLabel);

      scheduleTable.setWidget(row + 1, 0, new HTML(jobSchedule.jobName));
      scheduleTable.setWidget(row + 1, 1, new HTML(jobSchedule.jobGroup));
      scheduleTable.setWidget(row + 1, 2, new HTML(jobSchedule.jobDescription == null ? "&nbsp;" : jobSchedule.jobDescription)); //$NON-NLS-1$
      scheduleTable.setWidget(row + 1, 3, new HTML((jobSchedule.previousFireTime == null ? Messages.getInstance().never() : jobSchedule.previousFireTime.toString()) + "<BR>" //$NON-NLS-1$
          + (jobSchedule.nextFireTime == null ? "-" : jobSchedule.nextFireTime.toString()))); //$NON-NLS-1$
      scheduleTable.setWidget(row + 1, 4, new HTML(getTriggerStateName(jobSchedule.triggerState)));
      scheduleTable.setWidget(row + 1, 5, actionPanel);
      scheduleTable.getCellFormatter().setStyleName(row + 1, 0, "backgroundContentTableCell"); //$NON-NLS-1$
      scheduleTable.getCellFormatter().setStyleName(row + 1, 1, "backgroundContentTableCell"); //$NON-NLS-1$
      scheduleTable.getCellFormatter().setStyleName(row + 1, 2, "backgroundContentTableCell"); //$NON-NLS-1$
      scheduleTable.getCellFormatter().setStyleName(row + 1, 3, "backgroundContentTableCell"); //$NON-NLS-1$
      scheduleTable.getCellFormatter().setStyleName(row + 1, 4, "backgroundContentTableCell"); //$NON-NLS-1$
      scheduleTable.getCellFormatter().setStyleName(row + 1, 5, "backgroundContentTableCellRight"); //$NON-NLS-1$
      if (row == scheduleDetails.size() - 1) {
        // last
        scheduleTable.getCellFormatter().setStyleName(row + 1, 0, "backgroundContentTableCellBottom"); //$NON-NLS-1$
        scheduleTable.getCellFormatter().setStyleName(row + 1, 1, "backgroundContentTableCellBottom"); //$NON-NLS-1$
        scheduleTable.getCellFormatter().setStyleName(row + 1, 2, "backgroundContentTableCellBottom"); //$NON-NLS-1$
        scheduleTable.getCellFormatter().setStyleName(row + 1, 3, "backgroundContentTableCellBottom"); //$NON-NLS-1$
        scheduleTable.getCellFormatter().setStyleName(row + 1, 4, "backgroundContentTableCellBottom"); //$NON-NLS-1$
        scheduleTable.getCellFormatter().setStyleName(row + 1, 5, "backgroundContentTableCellBottomRight"); //$NON-NLS-1$
      }
    }
  }

  public String getTriggerStateName(int state) {
    if (state == 0) {
      return Messages.getInstance().normal();
    } else if (state == 1) {
      return Messages.getInstance().paused();
    } else if (state == 2) {
      return Messages.getInstance().running();
    }
    return Messages.getInstance().unknown();
  }

  public void deleteContentItem(final String subscriptionName, final String fileId) {
    AsyncCallback<String> callback = new AsyncCallback<String>() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(String message) {
        Window.alert(message);
        refreshWorkspace();
      }
    };
    MantleServiceCache.getService().deleteSubscriptionArchive(subscriptionName, fileId, callback);
  }

  public void fetchWaitingBackgroundItems() {
    AsyncCallback<List<JobDetail>> callback = new AsyncCallback<List<JobDetail>>() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(List<JobDetail> scheduledJobs) {
        backgroundAlertRaised = false;
        // result is List<JobDetail>
        waitingContentTable = buildEmptyBackgroundItemTable(WAITING);
        buildJobTable(scheduledJobs, waitingContentTable, waitingContentDisclosure, WAITING);
        waitingContentDisclosure.setContent(waitingContentTable);

      }
    };
    MantleServiceCache.getService().getScheduledBackgroundContent(callback);
  }

  public void fetchCompletedBackgroundItems() {
    AsyncCallback<List<JobDetail>> callback = new AsyncCallback<List<JobDetail>>() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(List<JobDetail> result) {
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

  public void refreshWorkspace() {
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean result) {
        fetchWaitingBackgroundItems();
        fetchCompletedBackgroundItems();
        fetchMySchedules();
        fetchAllSchedules();
        fetchSubscriptions();
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {

          }

          public void onSuccess(Boolean result) {
            refreshWorkspace();
          }

        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  public void cancelBackgroundJob(final String jobName, final String jobGroup) {
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean result) {
        AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
            Window.alert(caught.toString());
          }

          public void onSuccess(Boolean result) {
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
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean result) {
        AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
            Window.alert(caught.toString());
          }

          public void onSuccess(Boolean result) {
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
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean result) {
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {

          public void onFailure(Throwable caught) {
            Window.alert(caught.toString());
          }

          public void onSuccess(Void result) {
            refreshWorkspace();
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
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean result) {
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {

          public void onFailure(Throwable caught) {
            Window.alert(caught.toString());
          }

          public void onSuccess(Void nothing) {
            refreshWorkspace();
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
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean result) {
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {

          public void onFailure(Throwable caught) {
            Window.alert(caught.toString());
          }

          public void onSuccess(Void nothing) {
            refreshWorkspace();
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
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean result) {
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {

          public void onFailure(Throwable caught) {
            Window.alert(caught.toString());
          }

          public void onSuccess(Void nothing) {
            refreshWorkspace();
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
    AsyncCallback<Void> callback = new AsyncCallback<Void>() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(Void nothing) {
        backgroundAlertRaised = false;
      }
    };
    MantleServiceCache.getService().resetBackgroundExecutionAlert(callback);
  }

  public void fetchMySchedules() {
    AsyncCallback<List<JobSchedule>> callback = new AsyncCallback<List<JobSchedule>>() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(List<JobSchedule> scheduledJobs) {
        // result is List<JobSchedule>
        myScheduledContentTable = buildEmptyScheduleTable();
        buildScheduleTable(scheduledJobs, myScheduledContentTable, myScheduledContentDisclosure, MYSCHEDULES);
        myScheduledContentDisclosure.setContent(myScheduledContentTable);
      }
    };
    MantleServiceCache.getService().getMySchedules(callback);
  }

  public void fetchSubscriptions() {
    AsyncCallback<List<SubscriptionBean>> callback = new AsyncCallback<List<SubscriptionBean>>() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(List<SubscriptionBean> subscriptionsInfo) {
        subscriptionsContentTable = buildEmptySubscriptionsTable();
        buildSubscriptionsTable(subscriptionsInfo, subscriptionsContentTable, subscriptionsContentDisclosure);
        subscriptionsContentDisclosure.setContent(subscriptionsContentTable);
      }
    };
    MantleServiceCache.getService().getSubscriptionsForMyWorkspace(callback);
  }

  public void fetchAllSchedules() {
    AsyncCallback<List<JobSchedule>> callback = new AsyncCallback<List<JobSchedule>>() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(List<JobSchedule> scheduledJobs) {
        // result is List<JobSchedule>
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

}
