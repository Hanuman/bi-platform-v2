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
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.scheduler;

import org.pentaho.platform.repository.subscription.SubscriptionExecute;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

public class QuartzSubscriptionJob implements Job {
  public static String TRIGGER_GROUP = "MANUAL_TRIGGER"; //$NON-NLS-1$
  public QuartzSubscriptionJob() {
  }

  public void execute(final JobExecutionContext context) throws JobExecutionException {
    SubscriptionExecute subscriptionExecute = new SubscriptionExecute();
    Trigger trigger = context.getTrigger();
    subscriptionExecute.execute( context.getJobDetail().getName(), /*is last fire time?*/ !TRIGGER_GROUP.equals(trigger.getGroup()) && context.getNextFireTime() == null );

    ;
  }

}
