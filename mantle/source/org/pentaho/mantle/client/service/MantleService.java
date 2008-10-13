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
import org.pentaho.mantle.client.objects.SimpleMessageException;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.objects.SubscriptionBean;
import org.pentaho.mantle.client.objects.SubscriptionSchedule;
import org.pentaho.mantle.client.objects.SubscriptionState;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import com.google.gwt.user.client.rpc.RemoteService;

public interface MantleService extends RemoteService {
  public boolean getBackgroundExecutionAlert();
  public void resetBackgroundExecutionAlert();
  public boolean isAuthenticated();
  public boolean isAdministrator();
  public List<JobDetail> getScheduledBackgroundContent();
  public List<JobDetail> getCompletedBackgroundContent();
  public boolean cancelBackgroundJob(String jobName, String jobGroup);
  public boolean deleteContentItem(String contentId);
  
  public String getSoftwareUpdatesDocument();
  
  // admin
  public void executeGlobalActions();
  public void refreshMetadata();
  public void refreshSystemSettings();
  public void refreshRepository();
  public int cleanContentRepository(int daysBack);
  public void flushMondrianSchemaCache();
  
  //schedule API
  public void createCronJob(String solutionName, String path, String actionName, String cronExpression) throws SimpleMessageException;
  public void createCronJob(String solutionName, String path, String actionName, String triggerName, String triggerGroup, String description, String cronExpression) throws SimpleMessageException;
  public void createSimpleTriggerJob(String triggerName, String triggerGroup, String description, Date strStartDate, Date strEndDate, int repeatCount, int strRepeatInterval,
      String solutionName, String path, String actionName) throws SimpleMessageException;
  public List<JobSchedule> getMySchedules();
  public List<JobSchedule> getAllSchedules();
  public void suspendJob(String jobName, String jobGroup);
  public void resumeJob(String jobName, String jobGroup);
  public void deleteJob(String jobName, String jobGroup);
  public void runJob(String jobName, String jobGroup);
  
  //subscriptions API
  public Boolean isSubscriptionContent(String actionRef);
  public List<SubscriptionSchedule> getAvailableSubscriptionSchedules(String actionRef);
  public List<SubscriptionSchedule> getAppliedSubscriptionSchedules(String actionRef);
  public void setSubscriptions(String actionRef, boolean enabled, List<SubscriptionSchedule> currentSchedules);
  public SubscriptionState getSubscriptionState(String actionRef);
  public List<SubscriptionBean> getSubscriptionsForMyWorkspace();
  public String deleteSubscriptionArchive(String subscriptionName, String fileId);
  
  // file api
  public SolutionFileInfo getSolutionFileInfo(String solutionName, String path, String fileName);
  public void setSolutionFileInfo(SolutionFileInfo fileInfo) throws SimpleMessageException;
  public List<String> getAllUsers();
  public List<String> getAllRoles();
  public boolean doesSolutionRepositorySupportPermissions();
  public boolean hasAccess(String solutionName, String path, String fileName, int actionOperation);
  
  // mantle settings
  public HashMap<String,String> getMantleSettings();

  // version information
  public String getVersion();
  
  // pentaho reporting interaction api
  public ReportContainer getLogicalReportPage(List<ReportParameter> reportParameters, String reportDefinitionPath, int logicalPage) throws SimpleMessageException;
  // For New Analysis View
  public HashMap<String,List<String>> getMondrianCatalogs();  
  
  public List<IUserSetting> getUserSettings();
  public void addBookmark(Bookmark bookmark) throws SimpleMessageException;
  public void deleteBookmark(Bookmark bookmark) throws SimpleMessageException;
  public List<Bookmark> getBookmarks() throws SimpleMessageException;
  public void setShowNavigator(boolean showNavigator);
  public void setShowLocalizedFileNames(boolean showLocalizedFileNames);
  public void setShowHiddenFiles(boolean showHiddenFiles);
  
  public boolean repositorySupportsACLS();
}
