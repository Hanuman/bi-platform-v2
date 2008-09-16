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
import java.util.List;

import org.pentaho.mantle.client.objects.Bookmark;
import org.pentaho.mantle.client.objects.ReportParameter;
import org.pentaho.mantle.client.objects.SimpleMessageException;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.objects.SubscriptionSchedule;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MantleServiceAsync {
  public void getBackgroundExecutionAlert(AsyncCallback callback);
  public void resetBackgroundExecutionAlert(AsyncCallback callback);
  public void isAuthenticated(AsyncCallback callback);
  public void isAdministrator(AsyncCallback callback);
  public void getScheduledBackgroundContent(AsyncCallback callback);
  public void getCompletedBackgroundContent(AsyncCallback callback);
  public void cancelBackgroundJob(String jobName, String jobGroup, AsyncCallback callback);
  public void deleteContentItem(String contentId, AsyncCallback callback);

  public void getSoftwareUpdatesDocument(AsyncCallback callback);

  // admin actions
  public void executeGlobalActions(AsyncCallback callback);
  public void refreshMetadata(AsyncCallback callback);
  public void refreshSystemSettings(AsyncCallback callback);
  public void refreshRepository(AsyncCallback callback);
  public void cleanContentRepository(int daysBack, AsyncCallback callback);
  public void flushMondrianSchemaCache(AsyncCallback callback);
  
  public void createCronJob(String solutionName, String path, String actionName, String cronExpression, AsyncCallback callback);
  public void createCronJob(String solutionName, String path, String actionName, String triggerName, String triggerGroup, String description, String cronExpression, AsyncCallback callback);
  public void createSimpleTriggerJob(String triggerName, String triggerGroup, String description, Date strStartDate, Date strEndDate, int repeatCount, int strRepeatInterval,
      String solutionName, String path, String actionName, AsyncCallback callback);

  public void getAllSchedules(AsyncCallback callback);
  public void getMySchedules(AsyncCallback callback);
  public void suspendJob(String jobName, String jobGroup, AsyncCallback callback);
  public void resumeJob(String jobName, String jobGroup, AsyncCallback callback);
  public void deleteJob(String jobName, String jobGroup, AsyncCallback callback);
  public void runJob(String jobName, String jobGroup, AsyncCallback callback);
  
  //subscriptions API
  public void isSubscriptionContent(String actionRef, AsyncCallback callback);
  public void getAvailableSubscriptionSchedules(String actionRef, AsyncCallback callback);
  public void getAppliedSubscriptionSchedules(String actionRef, AsyncCallback callback);
  public void setSubscriptions(String actionRef, boolean enabled, List<SubscriptionSchedule> currentSchedules, AsyncCallback callback);
  public void getSubscriptionState(String actionRef, AsyncCallback callback);
  public void getSubscriptionsForMyWorkspace(AsyncCallback callback);
  public void deleteSubscriptionArchive(String subscriptionName, String fileId, AsyncCallback callback);
  
  // file api
  public void getSolutionFileInfo(String solutionName, String path, String fileName, AsyncCallback callback);
  public void setSolutionFileInfo(SolutionFileInfo fileInfo, AsyncCallback callback);
  public void getAllUsers(AsyncCallback callback);
  public void getAllRoles(AsyncCallback callback);
  public void doesSolutionRepositorySupportPermissions(AsyncCallback callback);
  
  // mantle settings
  public void getMantleSettings(AsyncCallback callback);

  // pentaho reporting interaction api
  public void getLogicalReportPage(List<ReportParameter> reportParameters, String reportDefinitionPath, int logicalPage, AsyncCallback callback);
  // For New Analysis View
  public void getMondrianCatalogs(AsyncCallback callback);
  
  // user settings
  public void getUserSettings(AsyncCallback<List<IUserSetting>> callback);
  public void addBookmark(Bookmark bookmark, AsyncCallback callback);
  public void deleteBookmark(Bookmark bookmark, AsyncCallback callback);
  public void getBookmarks(AsyncCallback<List<Bookmark>> callback);
  public void setShowNavigator(boolean showNavigator, AsyncCallback callback);
  public void setShowLocalizedFileNames(boolean showLocalizedFileNames, AsyncCallback callback);
  public void setShowHiddenFiles(boolean showHiddenFiles, AsyncCallback callback);
  public void repositorySupportsACLS(AsyncCallback callback);
}
