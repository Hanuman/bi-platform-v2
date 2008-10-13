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
package org.pentaho.mantle.client.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.pentaho.mantle.client.objects.Bookmark;
import org.pentaho.mantle.client.objects.JobDetail;
import org.pentaho.mantle.client.objects.JobSchedule;
import org.pentaho.mantle.client.objects.ReportContainer;
import org.pentaho.mantle.client.objects.ReportParameter;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.objects.SubscriptionBean;
import org.pentaho.mantle.client.objects.SubscriptionSchedule;
import org.pentaho.mantle.client.objects.SubscriptionState;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MantleServiceAsync {
  public void getBackgroundExecutionAlert(AsyncCallback<Boolean> callback);
  public void resetBackgroundExecutionAlert(AsyncCallback<Void> callback);
  public void isAuthenticated(AsyncCallback<Boolean> callback);
  public void isAdministrator(AsyncCallback<Boolean> callback);
  public void getScheduledBackgroundContent(AsyncCallback<List<JobDetail>> callback);
  public void getCompletedBackgroundContent(AsyncCallback<List<JobDetail>> callback);
  public void cancelBackgroundJob(String jobName, String jobGroup, AsyncCallback<Boolean> callback);
  public void deleteContentItem(String contentId, AsyncCallback<Boolean> callback);

  public void getSoftwareUpdatesDocument(AsyncCallback<String> callback);

  // admin actions
  public void executeGlobalActions(AsyncCallback<Void> callback);
  public void refreshMetadata(AsyncCallback<Void> callback);
  public void refreshSystemSettings(AsyncCallback<Void> callback);
  public void refreshRepository(AsyncCallback<Void> callback);
  public void cleanContentRepository(int daysBack, AsyncCallback<Integer> callback);
  public void flushMondrianSchemaCache(AsyncCallback<Void> callback);
  
  public void createCronJob(String solutionName, String path, String actionName, String cronExpression, AsyncCallback<Void> callback);
  public void createCronJob(String solutionName, String path, String actionName, String triggerName, String triggerGroup, String description, String cronExpression, AsyncCallback<Void> callback);
  public void createSimpleTriggerJob(String triggerName, String triggerGroup, String description, Date strStartDate, Date strEndDate, int repeatCount, int strRepeatInterval,
      String solutionName, String path, String actionName, AsyncCallback<Void> callback);

  public void getAllSchedules(AsyncCallback<List<JobSchedule>> callback);
  public void getMySchedules(AsyncCallback<List<JobSchedule>> callback);
  public void suspendJob(String jobName, String jobGroup, AsyncCallback<Void> callback);
  public void resumeJob(String jobName, String jobGroup, AsyncCallback<Void> callback);
  public void deleteJob(String jobName, String jobGroup, AsyncCallback<Void> callback);
  public void runJob(String jobName, String jobGroup, AsyncCallback<Void> callback);
  
  //subscriptions API
  public void isSubscriptionContent(String actionRef, AsyncCallback<Boolean> callback);
  public void getAvailableSubscriptionSchedules(String actionRef, AsyncCallback<List<SubscriptionSchedule>> callback);
  public void getAppliedSubscriptionSchedules(String actionRef, AsyncCallback<List<SubscriptionSchedule>> callback);
  public void setSubscriptions(String actionRef, boolean enabled, List<SubscriptionSchedule> currentSchedules, AsyncCallback<Void> callback);
  public void getSubscriptionState(String actionRef, AsyncCallback<SubscriptionState> callback);
  public void getSubscriptionsForMyWorkspace(AsyncCallback<List<SubscriptionBean>> callback);
  public void deleteSubscriptionArchive(String subscriptionName, String fileId, AsyncCallback<String> callback);
  
  // file api
  public void getSolutionFileInfo(String solutionName, String path, String fileName, AsyncCallback<SolutionFileInfo> callback);
  public void setSolutionFileInfo(SolutionFileInfo fileInfo, AsyncCallback<Void> callback);
  public void getAllUsers(AsyncCallback<List<String>> callback);
  public void getAllRoles(AsyncCallback<List<String>> callback);
  public void doesSolutionRepositorySupportPermissions(AsyncCallback<Boolean> callback);
  
  // mantle settings
  public void getMantleSettings(AsyncCallback<HashMap<String,String>> callback);

  // version information
  public void getVersion(AsyncCallback<String> callback);

  // pentaho reporting interaction api
  public void getLogicalReportPage(List<ReportParameter> reportParameters, String reportDefinitionPath, int logicalPage, AsyncCallback<ReportContainer> callback);
  // For New Analysis View
  public void getMondrianCatalogs(AsyncCallback<HashMap<String,List<String>>> callback);
  
  // user settings
  public void getUserSettings(AsyncCallback<List<IUserSetting>> callback);
  public void addBookmark(Bookmark bookmark, AsyncCallback<Void> callback);
  public void deleteBookmark(Bookmark bookmark, AsyncCallback<Void> callback);
  public void getBookmarks(AsyncCallback<List<Bookmark>> callback);
  public void setShowNavigator(boolean showNavigator, AsyncCallback<Void> callback);
  public void setShowLocalizedFileNames(boolean showLocalizedFileNames, AsyncCallback<Void> callback);
  public void setShowHiddenFiles(boolean showHiddenFiles, AsyncCallback<Void> callback);
  public void repositorySupportsACLS(AsyncCallback<Boolean> callback);
}
