package org.pentaho.mantle.client.objects;

import java.io.Serializable;
import java.util.List;

public class WorkspaceContent implements Serializable
{
  private List<JobDetail> scheduledJobs;
  private List<JobDetail> completedJobs;
  private List<JobSchedule> mySchedules;
  private List<JobSchedule> allSchedules;
  private List<SubscriptionBean> subscriptions;

  public WorkspaceContent()
  {
  }

  public List<JobDetail> getScheduledJobs()
  {
    return scheduledJobs;
  }

  public void setScheduledJobs(List<JobDetail> scheduledJobs)
  {
    this.scheduledJobs = scheduledJobs;
  }

  public List<JobDetail> getCompletedJobs()
  {
    return completedJobs;
  }

  public void setCompletedJobs(List<JobDetail> completedJobs)
  {
    this.completedJobs = completedJobs;
  }

  public List<JobSchedule> getMySchedules()
  {
    return mySchedules;
  }

  public void setMySchedules(List<JobSchedule> mySchedules)
  {
    this.mySchedules = mySchedules;
  }

  public List<JobSchedule> getAllSchedules()
  {
    return allSchedules;
  }

  public void setAllSchedules(List<JobSchedule> allSchedules)
  {
    this.allSchedules = allSchedules;
  }

  public List<SubscriptionBean> getSubscriptions()
  {
    return subscriptions;
  }

  public void setSubscriptions(List<SubscriptionBean> subscriptions)
  {
    this.subscriptions = subscriptions;
  }
}
