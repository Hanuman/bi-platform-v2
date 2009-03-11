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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.platform.scheduler;

import java.io.Serializable;
import java.util.Date;

import org.pentaho.platform.api.scheduler.IJobSchedule;

public class JobSchedule implements IJobSchedule, Serializable {
  private String name;

  private String fullname;

  private String triggerName;

  private String triggerGroup;

  private int triggerState;

  private Date nextFireTime;

  private Date previousFireTime;

  private String jobName;

  private String jobGroup;

  private String jobDescription;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFullname() {
    return fullname;
  }

  public void setFullname(String fullname) {
    this.fullname = fullname;
  }

  public String getTriggerName() {
    return triggerName;
  }

  public void setTriggerName(String triggerName) {
    this.triggerName = triggerName;
  }

  public String getTriggerGroup() {
    return triggerGroup;
  }

  public void setTriggerGroup(String triggerGroup) {
    this.triggerGroup = triggerGroup;
  }

  public int getTriggerState() {
    return triggerState;
  }

  public void setTriggerState(int triggerState) {
    this.triggerState = triggerState;
  }

  public Date getNextFireTime() {
    return nextFireTime;
  }

  public void setNextFireTime(Date nextFireTime) {
    this.nextFireTime = nextFireTime;
  }

  public Date getPreviousFireTime() {
    return previousFireTime;
  }

  public void setPreviousFireTime(Date previousFireTime) {
    this.previousFireTime = previousFireTime;
  }

  public String getJobName() {
    return jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public String getJobGroup() {
    return jobGroup;
  }

  public void setJobGroup(String jobGroup) {
    this.jobGroup = jobGroup;
  }

  public String getJobDescription() {
    return jobDescription;
  }

  public void setJobDescription(String jobDescription) {
    this.jobDescription = jobDescription;
  }
}
