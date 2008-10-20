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
package org.pentaho.mantle.server;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.report.JFreeReport;
import org.jfree.report.layout.output.ReportProcessor;
import org.pentaho.mantle.client.IMantleUserSettingsConstants;
import org.pentaho.mantle.client.objects.Bookmark;
import org.pentaho.mantle.client.objects.JobDetail;
import org.pentaho.mantle.client.objects.JobSchedule;
import org.pentaho.mantle.client.objects.ReportContainer;
import org.pentaho.mantle.client.objects.ReportParameter;
import org.pentaho.mantle.client.objects.RolePermission;
import org.pentaho.mantle.client.objects.SimpleMessageException;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.objects.SubscriptionBean;
import org.pentaho.mantle.client.objects.SubscriptionSchedule;
import org.pentaho.mantle.client.objects.SubscriptionState;
import org.pentaho.mantle.client.objects.UserPermission;
import org.pentaho.mantle.client.service.MantleService;
import org.pentaho.mantle.server.helpers.BookmarkHelper;
import org.pentaho.mantle.server.reporting.ReportCreator;
import org.pentaho.platform.api.engine.IAclSolutionFile;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IBackgroundExecution;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.api.engine.IPluginSettings;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.IContentRepository;
import org.pentaho.platform.api.repository.ISchedule;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISubscribeContent;
import org.pentaho.platform.api.repository.ISubscription;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.api.scheduler.BackgroundExecutionException;
import org.pentaho.platform.api.scheduler.IJobDetail;
import org.pentaho.platform.api.scheduler.IJobSchedule;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.security.SimplePermissionMask;
import org.pentaho.platform.engine.security.SimpleRole;
import org.pentaho.platform.engine.security.SimpleUser;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube;
import org.pentaho.platform.plugin.services.versionchecker.PentahoVersionCheckReflectHelper;
import org.pentaho.platform.repository.content.ContentItemFile;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository.subscription.Schedule;
import org.pentaho.platform.repository.subscription.Subscription;
import org.pentaho.platform.repository.subscription.SubscriptionHelper;
import org.pentaho.platform.scheduler.SchedulerHelper;
import org.pentaho.platform.util.VersionHelper;
import org.pentaho.platform.util.VersionInfo;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.session.PentahoHttpSession;
import org.pentaho.platform.web.refactor.UserFilesComponent;
import org.pentaho.ui.xul.IMenuCustomization;
import org.pentaho.ui.xul.IMenuCustomization.CustomizationType;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class MantleServlet extends RemoteServiceServlet implements MantleService {

  protected static final Log logger = LogFactory.getLog(MantleServlet.class);

  protected void onBeforeRequestDeserialized(String serializedRequest) {
    PentahoSystem.systemEntryPoint();
  }

  protected void onAfterResponseSerialized(String serializedResponse) {
    PentahoSystem.systemExitPoint();
  }

  @Override
  protected void doUnexpectedFailure(Throwable e) {
    try {
      getThreadLocalResponse().sendRedirect("Home");
      PentahoSystem.systemExitPoint();
    } catch (IOException e1) {
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      resp.sendRedirect("Home");
    } catch (IOException e1) {
    }
  }

  private IPentahoSession getPentahoSession() {
    HttpSession session = getThreadLocalRequest().getSession();
    IPentahoSession userSession = (IPentahoSession) session.getAttribute(IPentahoSession.PENTAHO_SESSION_KEY);

    LocaleHelper.setLocale(getThreadLocalRequest().getLocale());
    if (userSession != null) {
      return userSession;
    }
    userSession = new PentahoHttpSession(getThreadLocalRequest().getRemoteUser(), getThreadLocalRequest().getSession(), getThreadLocalRequest().getLocale(),
        null);
    LocaleHelper.setLocale(getThreadLocalRequest().getLocale());
    session.setAttribute(IPentahoSession.PENTAHO_SESSION_KEY, userSession);
    return userSession;
  }

  public boolean isAdministrator() {
    return SecurityHelper.isPentahoAdministrator(getPentahoSession());
  }

  @SuppressWarnings("unchecked")
  private UserFilesComponent getUserFilesComponent() {
    UserFilesComponent userFiles = PentahoSystem.get(UserFilesComponent.class, "IUserFilesComponent", getPentahoSession()); //$NON-NLS-1$
    String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
    String thisUrl = baseUrl + "UserContent?"; //$NON-NLS-1$
    SimpleUrlFactory urlFactory = new SimpleUrlFactory(thisUrl);
    userFiles.setUrlFactory(urlFactory);
    userFiles.setRequest(getThreadLocalRequest());
    userFiles.setResponse(getThreadLocalResponse());
    userFiles.setMessages(new ArrayList());
    userFiles.validate(getPentahoSession(), null);
    return userFiles;
  }

  @SuppressWarnings("unchecked")
  public String getSoftwareUpdatesDocument() {
    if (PentahoVersionCheckReflectHelper.isVersionCheckerAvailable()) {
      List results = PentahoVersionCheckReflectHelper.performVersionCheck(false, -1);
      return PentahoVersionCheckReflectHelper.logVersionCheck(results, logger);
    }
    return "<vercheck><error><[!CDATA[Version Checker is disabled]]></error></vercheck>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public void executeGlobalActions() {
    PentahoSystem.publish(getPentahoSession(), org.pentaho.platform.engine.core.system.GlobalListsPublisher.class.getName());
  }

  public void refreshMetadata() {
    PentahoSystem.publish(getPentahoSession(), org.pentaho.platform.engine.services.metadata.MetadataPublisher.class.getName());
  }

  public void refreshSystemSettings() {
    PentahoSystem.publish(getPentahoSession(), org.pentaho.platform.engine.core.system.SettingsPublisher.class.getName());
  }

  public boolean getBackgroundExecutionAlert() {
    return getPentahoSession().getBackgroundExecutionAlert();
  }

  public void resetBackgroundExecutionAlert() {
    getPentahoSession().resetBackgroundExecutionAlert();
  }

  public boolean isAuthenticated() {
    return getPentahoSession() != null && getPentahoSession().isAuthenticated();
  }

  @SuppressWarnings("unchecked")
  public List<JobDetail> getScheduledBackgroundContent() {
    getPentahoSession().resetBackgroundExecutionAlert();
    IBackgroundExecution backgroundExecution = PentahoSystem.get(IBackgroundExecution.class, getPentahoSession());
    if (backgroundExecution != null) {
      try {
      List<IJobDetail> jobsList = (List<IJobDetail>) backgroundExecution.getScheduledAndExecutingBackgroundJobs(getPentahoSession());
      List<JobDetail> myJobs = new ArrayList<JobDetail>(jobsList.size());
      for (IJobDetail jobDetail : jobsList) {
        JobDetail myJobDetail = new JobDetail();
        myJobDetail.id = jobDetail.getName();
        myJobDetail.name = jobDetail.getActionName();
        myJobDetail.fullname = jobDetail.getFullName();
        myJobDetail.description = jobDetail.getDescription();
        myJobDetail.timestamp = jobDetail.getSubmissionDate();
        myJobDetail.group = jobDetail.getGroupName();
        myJobs.add(myJobDetail);
      }
      return myJobs;
      } catch (BackgroundExecutionException bee) {
        // since this is GWT-RPC we cannot serialize this particular exception
        // so we will return an empty list, like the else condition below
        return new ArrayList<JobDetail>();
      }
    } else {
      return new ArrayList<JobDetail>();
    }
  }

  @SuppressWarnings("unchecked")
  public List<JobDetail> getCompletedBackgroundContent() {
    getPentahoSession().resetBackgroundExecutionAlert();
    IBackgroundExecution backgroundExecution = PentahoSystem.get(IBackgroundExecution.class, getPentahoSession());
    if (backgroundExecution != null) {
      List<IContentItem> jobsList = (List<IContentItem>) backgroundExecution.getBackgroundExecutedContentList(getPentahoSession());
      List<JobDetail> myJobs = new ArrayList<JobDetail>(jobsList.size());
      SimpleDateFormat fmt = new SimpleDateFormat();
      for (IContentItem contentItem : jobsList) {
        JobDetail myJobDetail = new JobDetail();
        myJobDetail.id = contentItem.getId();
        String dateStr = ""; //$NON-NLS-1$
        Date time = contentItem.getFileDateTime();
        if (time != null) {
          dateStr = fmt.format(time);
        }
        myJobDetail.name = contentItem.getTitle();
        myJobDetail.fullname = contentItem.getActionName();
        myJobDetail.description = contentItem.getActionName();
        myJobDetail.timestamp = dateStr;
        myJobDetail.size = Long.toString(contentItem.getFileSize());
        myJobDetail.type = contentItem.getMimeType();
        myJobs.add(myJobDetail);
      }
      return myJobs;
    } else {
      return new ArrayList<JobDetail>();
    }
  }

  public boolean cancelBackgroundJob(String jobName, String jobGroup) {
    UserFilesComponent userFiles = getUserFilesComponent();
    boolean status = userFiles.cancelJob(jobName, jobGroup);
    return status;
  }

  public boolean deleteContentItem(String contentId) {
    UserFilesComponent userFiles = getUserFilesComponent();
    boolean status = userFiles.deleteContent(contentId);
    return status;
  }

  public void refreshRepository() {
    PentahoSystem.get(ISolutionRepository.class, getPentahoSession());
  }

  public int cleanContentRepository(int daysBack) {
    // get daysback off the input
    daysBack = Math.abs(daysBack) * -1;

    // get todays calendar
    Calendar calendar = Calendar.getInstance();
    // subtract (by adding a negative number) the daysback amount
    calendar.add(Calendar.DATE, daysBack);
    // create the new date for the content repository to use
    Date agedDate = new Date(calendar.getTimeInMillis());
    // get the content repository and tell it to remove the items older than
    // agedDate
    IContentRepository contentRepository = PentahoSystem.get(IContentRepository.class, getPentahoSession());
    int deleteCount = contentRepository.deleteContentOlderThanDate(agedDate);
    return deleteCount;
  }

  public void flushMondrianSchemaCache() {
    mondrian.rolap.agg.AggregationManager.instance().getCacheControl(null).flushSchemaCache();
  }

  public List<JobSchedule> getMySchedules() {
    List<JobSchedule> jobSchedules = null;
    try {
      List<IJobSchedule> schedules = SchedulerHelper.getMySchedules(getPentahoSession());
      jobSchedules = iJobSchedule2JobSchedule(schedules);
      // these are functionally the same exact objects (mantle JobSchedule/platform JobSchedule)
    } catch (Exception e) {
      logger.error(e.getMessage());
      jobSchedules = new ArrayList<JobSchedule>();
    }
    return jobSchedules;
  }

  public List<JobSchedule> getAllSchedules() {
    List<JobSchedule> jobSchedules = null;
    try {
      List<IJobSchedule> schedules = SchedulerHelper.getAllSchedules(getPentahoSession());
      jobSchedules = iJobSchedule2JobSchedule(schedules);
      // these are functionally the same exact objects (mantle JobSchedule/platform JobSchedule)
    } catch (Exception e) {
      logger.error(e.getMessage());
      jobSchedules = new ArrayList<JobSchedule>();
    }
    return jobSchedules;
  }

  private List<JobSchedule> iJobSchedule2JobSchedule(List<IJobSchedule> iJobSchedules) {
    List<JobSchedule> jobSchedules = new ArrayList<JobSchedule>();
    for (IJobSchedule iJobSchedule : iJobSchedules) {
      JobSchedule jobSchedule = new JobSchedule();
      jobSchedule.fullname = iJobSchedule.getFullname();
      jobSchedule.jobDescription = iJobSchedule.getJobDescription();
      jobSchedule.jobGroup = iJobSchedule.getJobGroup();
      jobSchedule.jobName = iJobSchedule.getJobName();
      jobSchedule.name = iJobSchedule.getName();
      jobSchedule.nextFireTime = iJobSchedule.getNextFireTime();
      jobSchedule.previousFireTime = iJobSchedule.getPreviousFireTime();
      jobSchedule.triggerGroup = iJobSchedule.getTriggerGroup();
      jobSchedule.triggerName = iJobSchedule.getTriggerName();
      jobSchedule.triggerState = iJobSchedule.getTriggerState();

      jobSchedules.add(jobSchedule);
    }
    return jobSchedules;
  }

  public void deleteJob(String jobName, String jobGroup) {
    SchedulerHelper.deleteJob(getPentahoSession(), jobName, jobGroup);
  }

  public void runJob(String jobName, String jobGroup) {
    SchedulerHelper.runJob(getPentahoSession(), jobName, jobGroup);
  }

  public void resumeJob(String jobName, String jobGroup) {
    SchedulerHelper.resumeJob(getPentahoSession(), jobName, jobGroup);
  }

  public void suspendJob(String jobName, String jobGroup) {
    SchedulerHelper.suspendJob(getPentahoSession(), jobName, jobGroup);
  }

  public void createCronJob(String solutionName, String path, String actionName, String cronExpression) throws SimpleMessageException {
    if ("true".equalsIgnoreCase(PentahoSystem.getSystemSetting("kiosk-mode", "false"))) {
      throw new SimpleMessageException(ServerMessages.getString("featureDisabled"));
    }
    try {
      SchedulerHelper.createCronJob(getPentahoSession(), solutionName, path, actionName, cronExpression);
    } catch (Exception e) {
      throw new SimpleMessageException(e.getMessage());
    }
  }

  public void createCronJob(String solutionName, String path, String actionName, String triggerName, String triggerGroup, String description,
      String cronExpression) throws SimpleMessageException {
    
    if ("true".equalsIgnoreCase(PentahoSystem.getSystemSetting("kiosk-mode", "false"))) {
      throw new SimpleMessageException(ServerMessages.getString("featureDisabled"));
    }
    
    try {
      IBackgroundExecution backgroundExecutionHandler = PentahoSystem.get(IBackgroundExecution.class, getPentahoSession());
      SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
      parameterProvider.setParameter(StandardSettings.SOLUTION, solutionName);
      parameterProvider.setParameter(StandardSettings.PATH, path);
      parameterProvider.setParameter(StandardSettings.ACTION, actionName);
      parameterProvider.setParameter(StandardSettings.CRON_STRING, cronExpression);
      parameterProvider.setParameter(StandardSettings.SCHEDULE_NAME, triggerName);
      parameterProvider.setParameter(StandardSettings.SCHEDULE_GROUP_NAME, getPentahoSession().getName());
      parameterProvider.setParameter(StandardSettings.DESCRIPTION, description);
      backgroundExecutionHandler.backgroundExecuteAction(getPentahoSession(), parameterProvider);
    } catch (Exception e) {
      throw new SimpleMessageException(e.getMessage());
    } finally {
    }
    createCronJob(solutionName, path, actionName, cronExpression);
  }

  public void createSimpleTriggerJob(String triggerName, String triggerGroup, String description, Date startDate, Date endDate, int repeatCount,
      int repeatInterval, String solutionName, String path, String actionName) throws SimpleMessageException {

    if ("true".equalsIgnoreCase(PentahoSystem.getSystemSetting("kiosk-mode", "false"))) {
      throw new SimpleMessageException(ServerMessages.getString("featureDisabled"));
    }
    
    try {
      IBackgroundExecution backgroundExecutionHandler = PentahoSystem.get(IBackgroundExecution.class, getPentahoSession());
      SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
      parameterProvider.setParameter(StandardSettings.SOLUTION, solutionName);
      parameterProvider.setParameter(StandardSettings.PATH, path);
      parameterProvider.setParameter(StandardSettings.ACTION, actionName);
      parameterProvider.setParameter(StandardSettings.REPEAT_COUNT, Integer.toString(repeatCount));
      parameterProvider.setParameter(StandardSettings.REPEAT_TIME_MILLISECS, Integer.toString(repeatInterval));
      parameterProvider.setParameter(StandardSettings.START_DATE_TIME, startDate);
      parameterProvider.setParameter(StandardSettings.END_DATE_TIME, endDate);
      parameterProvider.setParameter(StandardSettings.SCHEDULE_NAME, triggerName);
      parameterProvider.setParameter(StandardSettings.SCHEDULE_GROUP_NAME, getPentahoSession().getName());
      parameterProvider.setParameter(StandardSettings.DESCRIPTION, description);
      backgroundExecutionHandler.backgroundExecuteAction(getPentahoSession(), parameterProvider);
    } catch (Exception e) {
      throw new SimpleMessageException(e.getMessage());
    } finally {
    }
  }

  @SuppressWarnings("unchecked")
  public List<String> getAllRoles() {
    IUserDetailsRoleListService userDetailsRoleListService = PentahoSystem.getUserDetailsRoleListService();
    return (List<String>) userDetailsRoleListService.getAllRoles();
  }

  @SuppressWarnings("unchecked")
  public List<String> getAllUsers() {
    IUserDetailsRoleListService userDetailsRoleListService = PentahoSystem.getUserDetailsRoleListService();
    return (List<String>) userDetailsRoleListService.getAllUsers();
  }

  public SolutionFileInfo getSolutionFileInfo(String solutionName, String path, String fileName) {
    if (fileName == null || path == null || solutionName == null) {
      throw new IllegalArgumentException("getSolutionFileInfo called with null parameters");
    }

    SolutionFileInfo solutionFileInfo = new SolutionFileInfo();
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getPentahoSession());

    String fullPath = ActionInfo.buildSolutionPath(solutionName, path, fileName);
    ISolutionFile solutionFile = repository.getFileByPath(fullPath);
    solutionFileInfo.solution = solutionName;
    solutionFileInfo.path = path;
    solutionFileInfo.name = fileName;

    // Find file Type
    int lastDot = -1;
    if (solutionFile.isDirectory()) {
      solutionFileInfo.type = SolutionFileInfo.Type.FOLDER;
    } else if ((lastDot = fileName.lastIndexOf('.')) > -1 && !fileName.startsWith(".")) {
      String extension = fileName.substring(lastDot);

      // Check to see if its a plug-in
      boolean isPlugin = false;
      IPluginSettings pluginSettings = PentahoSystem.get(IPluginSettings.class, getPentahoSession()); //$NON-NLS-1$

      if (pluginSettings != null) {
        Set<String> types = pluginSettings.getContentTypes();
        for (String type : types) {
          System.out.println(type);
        }
        isPlugin = types != null && types.contains(extension);
      }

      if (isPlugin) {
        // Get the reported type from the plug-in manager
        IContentGeneratorInfo info = pluginSettings.getDefaultContentGeneratorInfoForType(extension, getPentahoSession());
        solutionFileInfo.type = SolutionFileInfo.Type.PLUGIN;
        solutionFileInfo.pluginTypeName = info.getDescription();

      } else if (fileName.endsWith("waqr.xaction")) {
        solutionFileInfo.type = SolutionFileInfo.Type.REPORT;
      } else if (fileName.endsWith("analysisview.xaction")) {
        solutionFileInfo.type = SolutionFileInfo.Type.ANALYSIS_VIEW;
      } else if (fileName.endsWith(".url")) {
        solutionFileInfo.type = SolutionFileInfo.Type.URL;
      } else {
        solutionFileInfo.type = SolutionFileInfo.Type.XACTION;
      }
    }

    // Get Localized name
    if (!solutionFile.isDirectory()) {
      solutionFileInfo.localizedName = repository.getLocalizedFileProperty(solutionFile, "title");
    }
    if (StringUtils.isEmpty(solutionFileInfo.localizedName)) {
      solutionFileInfo.localizedName = repository.getLocalizedFileProperty(solutionFile, "name");
    }

    if (solutionFile.getData() == null) {
      solutionFileInfo.size = 0;
    } else {
      solutionFileInfo.size = solutionFile.getData().length;
    }
    solutionFileInfo.lastModifiedDate = new Date(solutionFile.getLastModified());

    solutionFileInfo.isDirectory = solutionFile.isDirectory();
    if (!solutionFile.isDirectory()) {
      ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getPentahoSession());
      ISubscribeContent subscribeContent = subscriptionRepository.getContentByActionReference(solutionName + path + "/" + fileName);
      solutionFileInfo.isSubscribable = (subscribeContent != null) && (subscribeContent.getSchedules() != null && subscribeContent.getSchedules().size() > 0);
    } else {
      solutionFileInfo.isSubscribable = false;
    }

    solutionFileInfo.canEffectiveUserManage = isAdministrator() || repository.hasAccess(solutionFile, PentahoAclEntry.PERM_UPDATE_PERMS);
    solutionFileInfo.supportsAccessControls = repository.supportsAccessControls();
    if (solutionFileInfo.canEffectiveUserManage && solutionFileInfo.supportsAccessControls) {
      List<RolePermission> rolePermissions = new ArrayList<RolePermission>();
      List<UserPermission> userPermissions = new ArrayList<UserPermission>();
      if (solutionFile instanceof IAclSolutionFile) {
        Map<IPermissionRecipient, IPermissionMask> filePermissions = repository.getPermissions((solutionFile));
        for (Map.Entry<IPermissionRecipient, IPermissionMask> filePerm : filePermissions.entrySet()) {
          IPermissionRecipient permRecipient = filePerm.getKey();
          if (permRecipient instanceof SimpleRole) {
            // entry belongs to a role
            rolePermissions.add(new RolePermission(permRecipient.getName(), filePerm.getValue().getMask()));
          } else {
            // entry belongs to a user
            userPermissions.add(new UserPermission(permRecipient.getName(), filePerm.getValue().getMask()));
          }
        }
      }
      solutionFileInfo.userPermissions = userPermissions;
      solutionFileInfo.rolePermissions = rolePermissions;
    }
    return solutionFileInfo;
  }

  public boolean hasAccess(String solutionName, String path, String fileName, int actionOperation) {
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getPentahoSession());
    String fullPath = ActionInfo.buildSolutionPath(solutionName, path, fileName);
    ISolutionFile solutionFile = repository.getFileByPath(fullPath);

    return repository.hasAccess(solutionFile, actionOperation);
  }

  public void setSolutionFileInfo(SolutionFileInfo fileInfo) throws SimpleMessageException {
    try {
      ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getPentahoSession());
      if (repository.supportsAccessControls()) {
        String fullPath = ActionInfo.buildSolutionPath(fileInfo.solution, fileInfo.path, fileInfo.name);
        ISolutionFile solutionFile = repository.getFileByPath(fullPath);
        Map<IPermissionRecipient, IPermissionMask> acl = new HashMap<IPermissionRecipient, IPermissionMask>();
        for (UserPermission userPermission : fileInfo.userPermissions) {
          acl.put(new SimpleUser(userPermission.name), new SimplePermissionMask(userPermission.mask));
        }
        for (RolePermission rolePermission : fileInfo.rolePermissions) {
          acl.put(new SimpleRole(rolePermission.name), new SimplePermissionMask(rolePermission.mask));
        }
        repository.setPermissions(solutionFile, acl);
        repository.resetRepository();

        if (!solutionFile.isDirectory()) {
          ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getPentahoSession());
          String actionRef = fileInfo.solution + fileInfo.path + "/" + fileInfo.name;
          ISubscribeContent subscribeContent = subscriptionRepository.getContentByActionReference(actionRef);
          if (fileInfo.isSubscribable && subscribeContent == null) {
            // make this actionRef subscribable
            subscriptionRepository.addContent(actionRef, "");
          } else if (!fileInfo.isSubscribable && subscribeContent != null) {
            // remove this actionRef from the subscribable list
            subscriptionRepository.deleteSubscribeContent(subscribeContent);
          }
        }
      }
    } catch (Exception e) {
      // e.printStackTrace();
      throw new SimpleMessageException(e.getMessage());
    } finally {
    }
  }

  public boolean doesSolutionRepositorySupportPermissions() {
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getPentahoSession());
    return repository.supportsAccessControls();
  }

  public ReportContainer getLogicalReportPage(List<ReportParameter> reportParameters, String reportDefinitionPath, int logicalPage)
      throws SimpleMessageException {
    // ultimately we'll pull the data from the solutionFile and create a ByteArrayInputStream
    ReportProcessor proc = null;
    try {
      System.out.println("getLogicalReportPage: " + reportDefinitionPath + " Page: " + logicalPage);
      JFreeReport report = ReportCreator.createReport(reportDefinitionPath, getPentahoSession());
      report.getReportConfiguration().setConfigProperty("org.jfree.report.modules.output.table.html.BodyFragment", "true");

      ReportContainer outReportContainer = new ReportContainer();
      // TODO: for 0810 this code is needed
      // // parameter handling
      // ReportParameterHelper paramHelper = new ReportParameterHelper();
      // paramHelper.processReportParameters(report, reportParameters, outReportContainer);
      //
      // if (!outReportContainer.isPromptNeeded()) {
      // final PageableHtmlOutputProcessor outputProcessor = new PageableHtmlOutputProcessor(report.getConfiguration());
      // ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      // final StreamRepository targetRepository = new StreamRepository(null, outputStream);
      // final ContentLocation targetRoot = targetRepository.getRoot();
      //
      // File tmpDir = new File(System.getProperty("java.io.tmpdir"));
      // final FileRepository dataRepository = new FileRepository(tmpDir);
      // final ContentLocation dataRoot = dataRepository.getRoot();
      //
      // final Configuration globalConfig = JFreeReportBoot.getInstance().getGlobalConfig();
      // String htmlContentHandlerUrlPattern = globalConfig.getConfigProperty("org.pentaho.web.ContentHandler"); //$NON-NLS-1$
      //
      // final HtmlPrinter printer = new AllItemsHtmlPrinter(report.getResourceManager());
      // printer.setContentWriter(targetRoot, new DefaultNameGenerator(targetRoot, "report", "html"));
      // printer.setDataWriter(dataRoot, new DefaultNameGenerator(dataRoot, "content")); //$NON-NLS-1$
      // printer.setUrlRewriter(new PentahoURLRewriter(htmlContentHandlerUrlPattern));
      // outputProcessor.setPrinter(printer);
      // outputProcessor.setFlowSelector(new ReportPageSelector(logicalPage));
      //
      // proc = new PageableReportProcessor(report, outputProcessor);
      // proc.processReport();
      //
      // outReportContainer.setNumPages(outputProcessor.getLogicalPageCount());
      // outReportContainer.getReportPages().put(new Integer(logicalPage), outputStream.toString());
      // }
      return outReportContainer;
    } catch (Throwable e) {
      e.printStackTrace();
      throw new SimpleMessageException(e.getMessage());
    } finally {
      if (proc != null) {
        proc.close();
      }
    }
  }

  public HashMap<String, String> getMantleSettings() {
    HashMap<String, String> settings = new HashMap<String, String>();
    // read properties file
    Properties props = new Properties();
    try {
      props.load(getClass().getResourceAsStream("/org/pentaho/mantle/server/MantleSettings.properties"));
      Enumeration keys = props.keys();
      while (keys.hasMoreElements()) {
        String key = (String) keys.nextElement();
        String value = (String) props.getProperty(key);
        settings.put(key, value);
      }

      settings.put("login-show-users-list", PentahoSystem.getSystemSetting("login-show-users-list", ""));
      settings.put("documentation-url", PentahoSystem.getSystemSetting("documentation-url", ""));

      // see if we have any plugin settings
      IPluginSettings pluginSettings = PentahoSystem.get(IPluginSettings.class, getPentahoSession()); //$NON-NLS-1$
      if (pluginSettings != null) {
        // get the menu customizations for the plugins, if any
        List<IMenuCustomization> customs = pluginSettings.getMenuCustomizations();
        int fileIdx = 0;
        int fileNewIdx = 0;
        int fileManageIdx = 0;
        int viewIdx = 0;
        int toolsIdx = 0;
        int toolsRefreshIdx = 0;
        int aboutIdx = 0;
        // process each customization
        for (IMenuCustomization custom : customs) {
          // we only support appending children to the first level sub-menus
          if (custom.getCustomizationType() == CustomizationType.LAST_CHILD) {
            String anchor = custom.getAnchorId();
            // do we have any additions to the file menu?
            // TODO: support file->new
            if ("file-submenu".equals(anchor)) { //$NON-NLS-1$
              settings.put("fileMenuTitle" + fileIdx, custom.getLabel()); //$NON-NLS-1$
              settings.put("fileMenuCommand" + fileIdx, custom.getCommand()); //$NON-NLS-1$
              fileIdx++;
            } else if ("file-new-submenu".equals(anchor)) { //$NON-NLS-1$
              settings.put("file-newMenuTitle" + fileNewIdx, custom.getLabel()); //$NON-NLS-1$
              settings.put("file-newMenuCommand" + fileNewIdx, custom.getCommand()); //$NON-NLS-1$
              fileNewIdx++;
            } else if ("file-manage-submenu".equals(anchor)) { //$NON-NLS-1$
              settings.put("file-manageMenuTitle" + fileManageIdx, custom.getLabel()); //$NON-NLS-1$
              settings.put("file-manageMenuCommand" + fileManageIdx, custom.getCommand()); //$NON-NLS-1$
              fileManageIdx++;
            }
            // do we have any additions to the view menu?
            else if ("view-submenu".equals(anchor)) { //$NON-NLS-1$
              settings.put("viewMenuTitle" + viewIdx, custom.getLabel()); //$NON-NLS-1$
              settings.put("viewMenuCommand" + viewIdx, custom.getCommand()); //$NON-NLS-1$
              viewIdx++;
            }
            // do we have any additions to the tools menu?
            else if ("tools-submenu".equals(anchor)) { //$NON-NLS-1$
              settings.put("toolsMenuTitle" + toolsIdx, custom.getLabel()); //$NON-NLS-1$
              settings.put("toolsMenuCommand" + toolsIdx, custom.getCommand()); //$NON-NLS-1$
              toolsIdx++;
            }
            // do we have any additions to the refresh menu?
            else if ("tools-refresh-submenu".equals(anchor)) { //$NON-NLS-1$
              settings.put("tools-refreshMenuTitle" + toolsRefreshIdx, custom.getLabel()); //$NON-NLS-1$
              settings.put("tools-refreshMenuCommand" + toolsRefreshIdx, custom.getCommand()); //$NON-NLS-1$
              toolsRefreshIdx++;
            }
            // do we have any additions to the about menu?
            else if ("about-submenu".equals(anchor)) { //$NON-NLS-1$
              settings.put("helpMenuTitle" + aboutIdx, custom.getLabel()); //$NON-NLS-1$
              settings.put("helpMenuCommand" + aboutIdx, custom.getCommand()); //$NON-NLS-1$
              aboutIdx++;
            }
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return settings;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.mantle.client.service.MantleService#isSubscriptionContent(java.lang.String)
   */
  public Boolean isSubscriptionContent(String actionRef) {
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getPentahoSession());
    return new Boolean(subscriptionRepository.getContentByActionReference(actionRef) != null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.mantle.client.service.MantleService#getAvailableSubscriptionSchedules()
   */
  public List<SubscriptionSchedule> getAvailableSubscriptionSchedules(String actionRef) {
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getPentahoSession());
    ISubscribeContent subscribeContent = subscriptionRepository.getContentByActionReference(actionRef);
    List<ISchedule> appliedList = subscribeContent == null ? new ArrayList<ISchedule>() : subscribeContent.getSchedules();
    List<ISchedule> availableList = subscriptionRepository.getSchedules();
    List<SubscriptionSchedule> unusedScheduleList = new ArrayList<SubscriptionSchedule>();
    for (ISchedule schedule : availableList) {
      if (!appliedList.contains(schedule)) {
        SubscriptionSchedule subSchedule = new SubscriptionSchedule();
        subSchedule.id = schedule.getId();
        subSchedule.title = schedule.getTitle();
        subSchedule.scheduleReference = schedule.getScheduleReference();
        subSchedule.description = schedule.getDescription();
        subSchedule.cronString = schedule.getCronString();
        subSchedule.group = schedule.getGroup();
        subSchedule.lastTrigger = schedule.getLastTrigger();

        unusedScheduleList.add(subSchedule);
      }
    }
    return unusedScheduleList;
  }

  /**
   * Delete the contents under the public schedule and then delete the public schedule
   * 
   * @param publicScheduleName
   *          The public schedule name for the given content id
   * @param contentItemList
   *          The list of content items belonging to the given public schedule to be deleted
   * @return Error message if error occurred else success message
   */
  public String deletePublicScheduleAndContents(String publicScheduleName, List<String> contentItemList) {
    /*
     * Iterate through all the content items and delete them
     */
    if (contentItemList != null) {
      Iterator<String> iter = contentItemList.iterator();
      if (iter != null) {
        while (iter.hasNext()) {
          deleteSubscriptionArchive(publicScheduleName, iter.next());
        }
      }
    }
    /*
     * Once all the content items are deleted, go ahead and delete the actual public schedule
     */
    final String result = SubscriptionHelper.deleteSubscription(publicScheduleName, getPentahoSession());
    return result;
  }

  /**
   * Delete the given content item for the given public schedule.
   * 
   * @param publicScheduleName
   *          The public schedule name for the given content id
   * @param contentId
   *          The content item id to be deleted
   * @return Error message if error occurred else success message
   */
  public String deleteSubscriptionArchive(String publicScheduleName, String contentId) {
    HibernateUtil.beginTransaction();
    final IPentahoSession session = getPentahoSession();
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getPentahoSession());
    ISubscription subscription = subscriptionRepository.getSubscription(publicScheduleName, session);
    if (subscription == null) {
      // TODO surface an error
      return Messages.getString("SubscriptionHelper.USER_SUBSCRIPTION_DOES_NOT_EXIST"); //$NON-NLS-1$
    }
    IContentItem contentItem = subscriptionRepository.getContentItem(publicScheduleName, session);
    if (contentItem == null) {
      // TODO surface an error
      return Messages.getString("SubscriptionHelper.USER_CONTENT_ITEM_DOES_NOT_EXIST"); //$NON-NLS-1$
    }

    contentItem.removeVersion(contentId);

    HibernateUtil.commitTransaction();

    return Messages.getString("SubscriptionHelper.USER_ARCHIVE_DELETED"); //$NON-NLS-1$
  }

  /**
   * This method provides the content for the My Subscription section in the Workspace.
   * 
   * @return List<SubscriptionBean> List of subscriptions and their related information contained within the object.
   */
  public List<SubscriptionBean> getSubscriptionsForMyWorkspace() {
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getPentahoSession());
    final String currentUser = getPentahoSession().getName();
    final List<ISubscription> userSubscriptionList = subscriptionRepository.getUserSubscriptions(currentUser);
    final List<SubscriptionBean> opSubscrList = new ArrayList<SubscriptionBean>();

    Iterator<ISubscription> subscrIter = userSubscriptionList.iterator();
    while (subscrIter.hasNext()) {
      final ISubscription currentSubscr = subscrIter.next();
      final String actionSeqTitle = getActionSequenceTitle((Subscription) currentSubscr);

      Schedule schedule = null;
      final Iterator schedIterator = currentSubscr.getSchedules().iterator();
      // Get the first schedule and get out of the loop
      // The code is below to avoid null pointer exceptions and get a schedule only if it exists.
      if (schedIterator != null) {
        while (schedIterator.hasNext()) {
          schedule = (Schedule) schedIterator.next();
          break;
        }
      }

      final SubscriptionBean subscriptionBean = new SubscriptionBean();
      subscriptionBean.setId(currentSubscr.getId());
      subscriptionBean.setName(currentSubscr.getTitle());
      subscriptionBean.setXactionName(actionSeqTitle);
      if (schedule != null) {
        subscriptionBean.setScheduleDate(schedule.getTitle());
      }
      // We have static dashes here because thats the way data is being displayed currently in 1.7
      subscriptionBean.setSize("---");
      subscriptionBean.setType("---");
      subscriptionBean.setContent(getContentItems(subscriptionRepository, (Subscription) currentSubscr));
      opSubscrList.add(subscriptionBean);
    }
    return opSubscrList;
  }

  /**
   * Helper method that returns action sequence title for the given subscription.
   * 
   * @param currentSubscr
   *          Current subscription
   * @return Title of the action sequence attached to the subscription.
   */
  private String getActionSequenceTitle(final Subscription currentSubscr) {
    final String actionSeqPath = currentSubscr.getContent().getActionReference();
    final ActionInfo actionInfo = ActionInfo.parseActionString(actionSeqPath);
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getPentahoSession());
    final IActionSequence action = repository.getActionSequence(actionInfo.getSolutionName(), actionInfo.getPath(), actionInfo.getActionName(), repository
        .getLoggingLevel(), ISolutionRepository.ACTION_EXECUTE);
    return action.getTitle();
  }

  /**
   * This is a helper method that gets the content item information
   * 
   * @param subscriptionRepository
   * @param currentSubscr
   * @return List of String arrays where the array consists of formatted date of the content, file type and size, file id, name and OS path.
   */
  private List<String[]> getContentItems(final ISubscriptionRepository subscriptionRepository, final Subscription currentSubscr) {
    final List contentItemFileList = subscriptionRepository.getSubscriptionArchives(currentSubscr.getId(), getPentahoSession());
    List<String[]> archiveList = null;

    if (contentItemFileList != null) {
      archiveList = new ArrayList<String[]>();
      final int contentItemFileListSize = contentItemFileList.size();

      for (int j = 0; j < contentItemFileListSize; j++) {
        final ContentItemFile contentItemFile = (ContentItemFile) contentItemFileList.get(j);
        final Date fileItemDate = contentItemFile.getFileDateTime();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy h:mm a");
        final String formattedDateStr = dateFormat.format(fileItemDate);
        final String fileType = contentItemFile.getParent().getMimeType();
        final String fileSize = String.valueOf(contentItemFile.getFileSize());

        final String[] tempArchiveArr = new String[6];
        tempArchiveArr[0] = formattedDateStr;
        tempArchiveArr[1] = fileType;
        tempArchiveArr[2] = fileSize;
        tempArchiveArr[3] = contentItemFile.getId();
        tempArchiveArr[4] = contentItemFile.getOsFileName();
        tempArchiveArr[5] = contentItemFile.getOsPath();

        archiveList.add(tempArchiveArr);
      }
    }
    return archiveList;
  }

  public String deleteArchive(String subscrName, String fileId) {
    final String result = SubscriptionHelper.deleteSubscriptionArchive(subscrName, fileId, getPentahoSession());
    return result;
  }

  // public String viewArchive(String subscrName, String fileId) {
  // final String result = SubscriptionHelper.getArchived(subscrName, fileId, getPentahoSession());
  // return result;
  // }

  public List<SubscriptionSchedule> getAppliedSubscriptionSchedules(String actionRef) {
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getPentahoSession());
    ISubscribeContent subscribeContent = subscriptionRepository.getContentByActionReference(actionRef);
    List<ISchedule> appliedList = subscribeContent == null ? new ArrayList<ISchedule>() : subscribeContent.getSchedules();
    List<SubscriptionSchedule> appliedScheduleList = new ArrayList<SubscriptionSchedule>();
    for (ISchedule schedule : appliedList) {
      SubscriptionSchedule subSchedule = new SubscriptionSchedule();
      subSchedule.id = schedule.getId();
      subSchedule.title = schedule.getTitle();
      subSchedule.scheduleReference = schedule.getScheduleReference();
      subSchedule.description = schedule.getDescription();
      subSchedule.cronString = schedule.getCronString();
      subSchedule.group = schedule.getGroup();
      subSchedule.lastTrigger = schedule.getLastTrigger();

      appliedScheduleList.add(subSchedule);
    }
    return appliedScheduleList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.mantle.client.service.MantleService#setSubscriptions(java.lang.String, boolean, java.util.List)
   */
  public void setSubscriptions(String actionRef, boolean enabled, List<SubscriptionSchedule> currentSchedules) {
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getPentahoSession());
    ISubscribeContent subscribeContent = subscriptionRepository.getContentByActionReference(actionRef);
    HibernateUtil.beginTransaction();
    if (enabled) {
      if (subscribeContent == null) {
        subscribeContent = subscriptionRepository.addContent(actionRef, "");
      }

      subscribeContent.clearsSchedules();
      List<ISchedule> updatedSchedules = new ArrayList<ISchedule>();
      List<ISchedule> availableSchedules = subscriptionRepository.getSchedules();
      for (SubscriptionSchedule currentSchedule : currentSchedules) {
        for (ISchedule availableSchedule : availableSchedules) {
          if (currentSchedule.title.equals(availableSchedule.getTitle()) && currentSchedule.id.equals(availableSchedule.getId())) {
            updatedSchedules.add(availableSchedule);
          }
        }
      }
      subscribeContent.setSchedules(updatedSchedules);
    } else {
      if (subscribeContent != null) {
        subscriptionRepository.deleteContent(subscribeContent);
      }
    }
    HibernateUtil.commitTransaction();
  }

  /**
   * Gets the mondrian catalogs and populates a hash map with schema name as the key and list of cube names as strings.
   * 
   * @return HashMap The hashmap has schema name as keys and a list of cube names as values
   */
  public HashMap<String, List<String>> getMondrianCatalogs() {
    HashMap<String, List<String>> catalogCubeHashMap = new HashMap<String, List<String>>();

    List<MondrianCatalog> catalogs = MondrianCatalogHelper.getInstance().listCatalogs(getPentahoSession(), true);

    for (MondrianCatalog cat : catalogs) {
      List<String> cubes = new ArrayList<String>();
      catalogCubeHashMap.put(cat.getName(), cubes);
      for (MondrianCube cube : cat.getSchema().getCubes()) {
        cubes.add(cube.getName());
      }
    }
    return catalogCubeHashMap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.mantle.client.service.MantleService#getSubscriptionState(java.lang.String)
   */
  public SubscriptionState getSubscriptionState(String actionRef) {
    SubscriptionState state = new SubscriptionState();
    state.subscriptionsEnabled = isSubscriptionContent(actionRef);
    state.availableSchedules = getAvailableSubscriptionSchedules(actionRef);
    state.appliedSchedules = getAppliedSubscriptionSchedules(actionRef);
    return state;
  }

  public List<IUserSetting> getUserSettings() {
    try {
      IUserSettingService settingsService = PentahoSystem.getUserSettingService(getPentahoSession());
      List<IUserSetting> settings = settingsService.getUserSettings();
      return settings;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public void addBookmark(Bookmark bookmark) throws SimpleMessageException {
    try {
      IUserSettingService settingsService = PentahoSystem.getUserSettingService(getPentahoSession());
      List<Bookmark> bookmarks = getBookmarks();
      // just be sure (yeah, this would be better as a Set, but I care about the user's order)
      bookmarks.remove(bookmark);
      bookmarks.add(bookmark);
      settingsService.setUserSetting(IMantleUserSettingsConstants.MANTLE_BOOKMARKS, BookmarkHelper.toXML(bookmarks));
    } catch (Exception e) {
      throw new SimpleMessageException(e.getMessage());
    }
  }

  public void deleteBookmark(Bookmark bookmark) throws SimpleMessageException {
    try {
      IUserSettingService settingsService = PentahoSystem.getUserSettingService(getPentahoSession());
      List<Bookmark> bookmarks = getBookmarks();
      // just be sure (yeah, this would be better as a Set, but I care about the user's order)
      bookmarks.remove(bookmark);
      settingsService.setUserSetting(IMantleUserSettingsConstants.MANTLE_BOOKMARKS, BookmarkHelper.toXML(bookmarks));
    } catch (Exception e) {
      throw new SimpleMessageException(e.getMessage());
    }
  }

  public List<Bookmark> getBookmarks() throws SimpleMessageException {
    IUserSettingService settingsService = PentahoSystem.getUserSettingService(getPentahoSession());
    IUserSetting setting = settingsService.getUserSetting(IMantleUserSettingsConstants.MANTLE_BOOKMARKS, null);
    if (setting == null) {
      return new ArrayList<Bookmark>();
    }
    try {
      return BookmarkHelper.fromXML(setting.getSettingValue());
    } catch (Exception e) {
      throw new SimpleMessageException(e.getMessage());
    }
  }

  public void setShowNavigator(boolean showNavigator) {
    IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
    settingsService.setUserSetting(IMantleUserSettingsConstants.MANTLE_SHOW_NAVIGATOR, "" + showNavigator);
  }

  public void setShowLocalizedFileNames(boolean showLocalizedFileNames) {
    IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
    settingsService.setUserSetting(IMantleUserSettingsConstants.MANTLE_SHOW_LOCALIZED_FILENAMES, "" + showLocalizedFileNames);
  }

  public void setShowHiddenFiles(boolean showHiddenFiles) {
    IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
    settingsService.setUserSetting(IMantleUserSettingsConstants.MANTLE_SHOW_HIDDEN_FILES, "" + showHiddenFiles);
  }

  public boolean repositorySupportsACLS() {
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getPentahoSession());
    return repository.supportsAccessControls();
  }

  public String getVersion() {
    VersionInfo versionInfo = VersionHelper.getVersionInfo(PentahoSystem.class);
    return versionInfo.getVersionNumber();
  }
}