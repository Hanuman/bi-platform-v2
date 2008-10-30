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
  
//  public void createCronJob(String solutionName, String path, String actionName, String cronExpression, AsyncCallback<Void> callback);
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
  public void deletePublicScheduleAndContents(String currSubscr, List<String> fileItemList, AsyncCallback<String> callback);
  public void runAndArchivePublicSchedule(String publicScheduleName, AsyncCallback<String> callback);
  
  // file api
  public void getSolutionFileInfo(String solutionName, String path, String fileName, AsyncCallback<SolutionFileInfo> callback);
  public void setSolutionFileInfo(SolutionFileInfo fileInfo, AsyncCallback<Void> callback);
  public void getAllUsers(AsyncCallback<List<String>> callback);
  public void getAllRoles(AsyncCallback<List<String>> callback);
  public void doesSolutionRepositorySupportPermissions(AsyncCallback<Boolean> callback);
  public void hasAccess(String solutionName, String path, String fileName, int actionOperation, AsyncCallback<Boolean> callback);
  
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
