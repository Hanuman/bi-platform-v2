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
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.utils.StringUtils;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.JobDetail;
import org.pentaho.mantle.client.objects.JobSchedule;
import org.pentaho.mantle.client.objects.SimpleMessageException;
import org.pentaho.mantle.client.objects.SubscriptionBean;
import org.pentaho.mantle.client.objects.WorkspaceContent;
import org.pentaho.mantle.client.perspective.IPerspectiveCallback;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.login.client.MantleLoginDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
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
  public DeleteSubscriptionClickListener deleteSubscriptionClickListener;
  private static final int WAITING = 0;
  private static final int COMPLETE = 1;
  private static final int MYSCHEDULES = 2;
  private static final int ALLSCHEDULES = 3;
  private static final String DELETE = "delete"; //$NON-NLS-1$

  private DisclosurePanel allScheduledContentDisclosure = new DisclosurePanel(Messages.getString("allSchedulesAdminOnly"), false); //$NON-NLS-1$
  private DisclosurePanel subscriptionsContentDisclosure = new DisclosurePanel(Messages.getString("publicSchedules"), false); //$NON-NLS-1$
  private DisclosurePanel myScheduledContentDisclosure = new DisclosurePanel(Messages.getString("mySchedules"), false); //$NON-NLS-1$
  private DisclosurePanel waitingContentDisclosure = new DisclosurePanel(Messages.getString("waiting"), false); //$NON-NLS-1$
  private DisclosurePanel completedContentDisclosure = new DisclosurePanel(Messages.getString("complete"), false); //$NON-NLS-1$
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
    table.setWidget(0, 0, new Label(Messages.getString("name"))); //$NON-NLS-1$
    table.setWidget(0, 1, new Label(Messages.getString("date"))); //$NON-NLS-1$
    if (tableType == COMPLETE) {
      table.setWidget(0, 2, new Label(Messages.getString("size"))); //$NON-NLS-1$
      table.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);
      table.setWidget(0, 3, new Label(Messages.getString("type"))); //$NON-NLS-1$
      table.setWidget(0, 4, new Label(Messages.getString("actions"))); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 0, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 1, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 2, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 3, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 4, "backgroundContentHeaderTableCellRight"); //$NON-NLS-1$
    } else {
      table.setWidget(0, 2, new Label(Messages.getString("actions"))); //$NON-NLS-1$
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
    table.setWidget(0, 0, new Label(Messages.getString("jobName"))); //$NON-NLS-1$
    table.setWidget(0, 1, new Label(Messages.getString("jobGroup"))); //$NON-NLS-1$
    table.setWidget(0, 2, new Label(Messages.getString("description"))); //$NON-NLS-1$
    table.setWidget(0, 3, new Label(Messages.getString("lastRunNextRun"))); //$NON-NLS-1$
    table.setWidget(0, 4, new Label(Messages.getString("state"))); //$NON-NLS-1$
    table.setWidget(0, 5, new Label(Messages.getString("actions"))); //$NON-NLS-1$
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
    table.setWidget(0, 0, new Label(Messages.getString("name"))); //$NON-NLS-1$
    table.setWidget(0, 1, new Label(Messages.getString("scheduleDate"))); //$NON-NLS-1$
    table.setWidget(0, 2, new Label(Messages.getString("type"))); //$NON-NLS-1$
    table.setWidget(0, 3, new Label(Messages.getString("size"))); //$NON-NLS-1$
    table.setWidget(0, 4, new Label(Messages.getString("actions"))); //$NON-NLS-1$
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
        workspaceTable.setWidget(0, 0, new HTML(Messages.getString("workspaceMessage"))); //$NON-NLS-1$
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
        Label viewLabel = new Label(Messages.getString("view")); //$NON-NLS-1$
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
        viewLabel.setTitle(Messages.getString("viewContent")); //$NON-NLS-1$
        Label deleteLabel = new Label(Messages.getString("delete")); //$NON-NLS-1$
        deleteLabel.addClickListener(new ClickListener() {

          public void onClick(Widget sender) {
            deleteContentItem(jobDetail.id);
          }

        });
        deleteLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
        deleteLabel.setTitle(Messages.getString("deleteContent")); //$NON-NLS-1$

        actionPanel.add(viewLabel);
        actionPanel.add(new HTML("&nbsp;|&nbsp;")); //$NON-NLS-1$
        actionPanel.add(deleteLabel);
      } else if (tableType == WAITING) {
        Label cancelLabel = new Label(Messages.getString("cancel")); //$NON-NLS-1$
        cancelLabel.addClickListener(new ClickListener() {

          public void onClick(Widget sender) {
            cancelBackgroundJob(jobDetail.id, jobDetail.group);
          }

        });
        cancelLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
        cancelLabel.setTitle(Messages.getString("cancelExecution")); //$NON-NLS-1$
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
      final String subscriptionId = currentSubscr.getId();

      Label lblRunNow = new Label(Messages.getString("run")); //$NON-NLS-1$
      lblRunNow.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      lblRunNow.addClickListener(new RunSubscriptionClickListener(currentSubscr));

      Label lblArchive = new Label(Messages.getString("archive")); //$NON-NLS-1$
      lblArchive.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      lblArchive.addClickListener(new RunAndArchiveClickListener(subscriptionId));

      Label lblEdit = new Label(Messages.getString("edit")); //$NON-NLS-1$
      lblEdit.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      lblEdit.addClickListener(new EditSubscriptionClickListener(currentSubscr));

      Label lblDelete = new Label(Messages.getString("delete")); //$NON-NLS-1$
      lblDelete.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      deleteSubscriptionClickListener = new DeleteSubscriptionClickListener(currentSubscr, lblDelete);
      lblDelete.addClickListener(deleteSubscriptionClickListener);

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

          final Label lblViewContent = new Label(Messages.getString("view")); //$NON-NLS-1$
          lblViewContent.setStyleName("backgroundContentAction"); //$NON-NLS-1$
          lblViewContent.addClickListener(new ClickListener() {

            public void onClick(Widget sender) {
              final String fileId = currSchedule[3];
              final String name = subscriptionId;
              performActionOnSubscriptionContent("archived", currentSubscr, name, fileId); //$NON-NLS-1$
            }
          });
          actionButtonsPanel.add(lblViewContent);

          final Label lblDeleteContent = new Label(Messages.getString("delete")); //$NON-NLS-1$
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

  /**
   * Runs and Archives the report attached to the given public schedule
   * 
   * @param publicSchedule
   *          Public schedule name
   */
  void runAndArchive(final String publicSchedule) {
    AsyncCallback<String> callback = null;
    if (publicSchedule != null) {
      callback = new AsyncCallback<String>() {
        public void onFailure(Throwable caught) {
          if (caught instanceof SimpleMessageException) {
            new MessageDialogBox(Messages.getString("error"), caught.getMessage(), false, false, true).center(); //$NON-NLS-1$
          } else {
            new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotSchedule"), false, false, true).center(); //$NON-NLS-1$ //$NON-NLS-2$
          }
        }

        public void onSuccess(String result) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("runInBackground"), //$NON-NLS-1$
              Messages.getString("reportIsScheduledForBE"), false, false, true); //$NON-NLS-1$
          dialogBox.center();
        }
      };
      MantleServiceCache.getService().runAndArchivePublicSchedule(publicSchedule, callback);
    }
  }

  void doDelete(final boolean isPublicSchedule, final SubscriptionBean currentSubscr, final String fileId) {
    if (isPublicSchedule) {
      VerticalPanel vp = new VerticalPanel();
      vp.add(new Label(Messages.getString("deletePublicSchedule"))); //$NON-NLS-1$
      final PromptDialogBox deleteConfirmDialog = new PromptDialogBox(
          Messages.getString("deleteConfirm"), Messages.getString("yes"), Messages.getString("no"), false, true, vp); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

      final IDialogCallback callback = new IDialogCallback() {
        public void cancelPressed() {
          deleteConfirmDialog.hide();
          deleteSubscriptionClickListener.getLblDelete().setVisible(true);
        }

        public void okPressed() {
          deletePublicScheduleAndContents(currentSubscr);
        }
      };
      deleteConfirmDialog.setCallback(callback);
      deleteConfirmDialog.center();
    } else {
      deleteContentItem(currentSubscr.getId(), fileId);
    }
  }

  /*
   * Deletes the given public schedule and all the contents belonging to it.
   * 
   * @param currSubscr Current public schedule to be deleted
   */
  private void deletePublicScheduleAndContents(final SubscriptionBean currPublicSchedule) {
    final String subscrName = currPublicSchedule.getId();
    final List<String[]> scheduleList = currPublicSchedule.getContent() == null ? new ArrayList<String[]>() : currPublicSchedule.getContent();
    final List<String> fileList = new ArrayList<String>();

    AsyncCallback<String> callback = new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
        new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotDeletePublicScheduleAndContents"), false, false, true).center(); //$NON-NLS-1$
      }

      public void onSuccess(String result) {
        refreshWorkspace();

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

  private void performActionOnSubscriptionContent(final String action, final SubscriptionBean subscription, final String subscrName, final String contentID) {
    performActionOnSubscription(action, subscription, subscrName + ":" + contentID); //$NON-NLS-1$
  }

  void performActionOnSubscription(final String action, final SubscriptionBean subscription, final String subscrName) {
    final PromptDialogBox viewDialog = new PromptDialogBox(Messages.getString("view"), Messages.getString("close"), null, false, false); //$NON-NLS-1$ //$NON-NLS-2$
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

    String url;
    if (action.equals("edit") && !StringUtils.isEmpty(subscription.getPluginUrl())) {
      url = subscription.getPluginUrl();
      if (!GWT.isScript()) {
        // for debug mode
        url = "http://localhost:8080/pentaho/" + url;
      }
    } else {
      url = "ViewAction?subscribe=" + action + "&subscribe-name=" + subscrName; //$NON-NLS-1$ //$NON-NLS-2$
      if (!GWT.isScript()) {
        // for debug mode
        url = "http://localhost:8080/pentaho/" + url;
      }
    }

    if (action.equals("archived") || action.equals("run") || action.equals("edit")) { //$NON-NLS-1$ //$NON-NLS-2$
      solutionBrowserPerspective.showNewURLTab(subscription.getName(), subscription.getId(), url);
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
      Label suspendJobLabel = new Label(Messages.getString("suspend")); //$NON-NLS-1$
      suspendJobLabel.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          suspendJob(jobSchedule.jobName, jobSchedule.jobGroup, jobSource);
        }

      });
      suspendJobLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      suspendJobLabel.setTitle(Messages.getString("suspendThisJob")); //$NON-NLS-1$

      Label resumeJobLabel = new Label(Messages.getString("resume")); //$NON-NLS-1$
      resumeJobLabel.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          resumeJob(jobSchedule.jobName, jobSchedule.jobGroup, jobSource);
        }

      });
      resumeJobLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      resumeJobLabel.setTitle(Messages.getString("resumeThisJob")); //$NON-NLS-1$

      Label runJobLabel = new Label(Messages.getString("run")); //$NON-NLS-1$
      runJobLabel.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          runJob(jobSchedule.jobName, jobSchedule.jobGroup, jobSource);
        }

      });
      runJobLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      runJobLabel.setTitle(Messages.getString("runThisJob")); //$NON-NLS-1$

      Label deleteJobLabel = new Label(Messages.getString("delete")); //$NON-NLS-1$
      deleteJobLabel.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          deleteJob(jobSchedule.jobName, jobSchedule.jobGroup, jobSource);
        }

      });
      deleteJobLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      deleteJobLabel.setTitle(Messages.getString("deleteThisJob")); //$NON-NLS-1$

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
        // actionPanel.add(new HTML("&nbsp;|&nbsp;")); //$NON-NLS-1$
      }
      // actionPanel.add(deleteJobLabel);

      if (actionPanel.getWidgetCount() == 0) {
        actionPanel.add(new HTML("&nbsp;")); //$NON-NLS-1$
      }

      scheduleTable.setWidget(row + 1, 0, new HTML(jobSchedule.jobName));
      scheduleTable.setWidget(row + 1, 1, new HTML(jobSchedule.jobGroup));
      scheduleTable.setWidget(row + 1, 2, new HTML(
          jobSchedule.jobDescription == null || jobSchedule.jobDescription.trim().length() == 0 ? "&nbsp;" : jobSchedule.jobDescription)); //$NON-NLS-1$
      scheduleTable.setWidget(row + 1, 3, new HTML(
          (jobSchedule.previousFireTime == null ? Messages.getString("never") : jobSchedule.previousFireTime.toString()) + "<BR>" //$NON-NLS-1$ //$NON-NLS-2$
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
      return Messages.getString("normal"); //$NON-NLS-1$
    } else if (state == 1) {
      return Messages.getString("paused"); //$NON-NLS-1$
    } else if (state == 2) {
      return Messages.getString("running"); //$NON-NLS-1$
    }
    return Messages.getString("unknown"); //$NON-NLS-1$
  }

  public void deleteContentItem(final String subscriptionName, final String fileId) {
    AsyncCallback<String> callback = new AsyncCallback<String>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
            Messages.getString("couldNotDeleteContentItem"), false, false, true); //$NON-NLS-1$
        dialogBox.center();
      }

      public void onSuccess(String message) {
        refreshWorkspace();
      }
    };
    MantleServiceCache.getService().deleteSubscriptionArchive(subscriptionName, fileId, callback);
  }

  public void refreshWorkspace() {
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean result) {
        fetchWorkspaceContent();
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

  public void fetchWorkspaceContent() {
    AsyncCallback<WorkspaceContent> callback = new AsyncCallback<WorkspaceContent>() {

      public void onSuccess(WorkspaceContent result) {
        // scheduled jobs
        waitingContentTable = buildEmptyBackgroundItemTable(WAITING);
        buildJobTable(result.getScheduledJobs(), waitingContentTable, waitingContentDisclosure, WAITING);
        waitingContentDisclosure.setContent(waitingContentTable);
        // completed background items
        completedContentTable = buildEmptyBackgroundItemTable(COMPLETE);
        buildJobTable(result.getCompletedJobs(), completedContentTable, completedContentDisclosure, COMPLETE);
        completedContentDisclosure.setContent(completedContentTable);
        // my schedules
        myScheduledContentTable = buildEmptyScheduleTable();
        buildScheduleTable(result.getMySchedules(), myScheduledContentTable, myScheduledContentDisclosure, MYSCHEDULES);
        myScheduledContentDisclosure.setContent(myScheduledContentTable);
        // all schedules
        allScheduledContentTable = buildEmptyScheduleTable();
        buildScheduleTable(result.getAllSchedules(), allScheduledContentTable, allScheduledContentDisclosure, ALLSCHEDULES);
        allScheduledContentDisclosure.setContent(allScheduledContentTable);
        // my subscriptions
        subscriptionsContentTable = buildEmptySubscriptionsTable();
        buildSubscriptionsTable(result.getSubscriptions(), subscriptionsContentTable, subscriptionsContentDisclosure);
        subscriptionsContentDisclosure.setContent(subscriptionsContentTable);
      }

      public void onFailure(Throwable caught) {
      }
    };
    MantleServiceCache.getService().getWorkspaceContent(callback);
  }

  public void cancelBackgroundJob(final String jobName, final String jobGroup) {
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean result) {
        AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
                Messages.getString("couldNotCancelBackgroundJob"), false, false, true); //$NON-NLS-1$
            dialogBox.center();
          }

          public void onSuccess(Boolean result) {
            fetchWorkspaceContent();
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
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
                Messages.getString("couldNotDeleteContentItem"), false, false, true); //$NON-NLS-1$
            dialogBox.center();
          }

          public void onSuccess(Boolean result) {
            fetchWorkspaceContent();
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
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
                Messages.getString("couldNotSuspendJob"), false, false, true); //$NON-NLS-1$
            dialogBox.center();
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
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
                Messages.getString("couldNotResumeJob"), false, false, true); //$NON-NLS-1$
            dialogBox.center();
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
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
                Messages.getString("couldNotDeleteJob"), false, false, true); //$NON-NLS-1$
            dialogBox.center();
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
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
                Messages.getString("couldNotBackgroundExecute"), false, false, true); //$NON-NLS-1$
            dialogBox.center();
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

  // Event classes
  public class RunAndArchiveClickListener implements ClickListener {
    String subscriptionId;

    public RunAndArchiveClickListener(String subscriptionID) {
      this.subscriptionId = subscriptionID;
    }

    public void onClick(Widget arg0) {
      runAndArchive(subscriptionId);
    }
  }

  public class RunSubscriptionClickListener implements ClickListener {
    SubscriptionBean subscription;

    public RunSubscriptionClickListener(SubscriptionBean subscription) {
      this.subscription = subscription;
    }

    public void onClick(Widget arg0) {
      performActionOnSubscription("run", subscription, subscription.getId()); //$NON-NLS-1$
    }
  }

  public class EditSubscriptionClickListener implements ClickListener {
    SubscriptionBean subscription;

    public EditSubscriptionClickListener(SubscriptionBean subscription) {
      this.subscription = subscription;
    }

    public void onClick(Widget arg0) {
      performActionOnSubscription("edit", subscription, subscription.getId()); //$NON-NLS-1$
    }
  }

  public class DeleteSubscriptionClickListener implements ClickListener {
    SubscriptionBean subscription;
    Label lblDelete;

    public Label getLblDelete() {
      return lblDelete;
    }

    public void setLblDelete(Label lblDelete) {
      this.lblDelete = lblDelete;
    }

    public DeleteSubscriptionClickListener(SubscriptionBean subscription, Label lblDelete) {
      this.subscription = subscription;
      this.lblDelete = lblDelete;
    }

    public void onClick(Widget sender) {
      lblDelete.setVisible(false);
      doDelete(true, subscription, ""); //$NON-NLS-1$
    }
    
  }

}
