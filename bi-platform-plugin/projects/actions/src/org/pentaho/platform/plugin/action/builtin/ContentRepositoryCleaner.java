/*
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
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
 * Created Feb 21, 2006 
 * @author wseyler
 */

package org.pentaho.platform.plugin.action.builtin;

import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IContentRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;

public class ContentRepositoryCleaner extends ComponentBase {

  private static final long serialVersionUID = 1L;

  private static final String AGE = "days_old"; //$NON-NLS-1$

  private static final String DELETE_COUNT = "delete_count"; //$NON-NLS-1$

  @Override
  protected boolean validateAction() {
    if (!isDefinedInput(ContentRepositoryCleaner.AGE)) {
      return false;
    }
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  public void done() {
  }

  @Override
  protected boolean executeAction() throws Throwable {
    // get daysback off the input
    int daysBack = Integer.parseInt(getInputValue(ContentRepositoryCleaner.AGE).toString());
    // make sure it's a negative number
    daysBack = Math.abs(daysBack) * -1;

    // get todays calendar
    Calendar calendar = Calendar.getInstance();
    // subtract (by adding a negative number) the daysback amount
    calendar.add(Calendar.DATE, daysBack);
    // create the new date for the content repository to use
    Date agedDate = new Date(calendar.getTimeInMillis());
    // get the content repository and tell it to remove the items older than
    // agedDate
    IContentRepository contentRepository = PentahoSystem.getContentRepository(getSession());
    int deleteCount = contentRepository.deleteContentOlderThanDate(agedDate);
    // return the number of files deleted
    setOutputValue(ContentRepositoryCleaner.DELETE_COUNT, Integer.toString(deleteCount));

    OutputStream feedbackOutputStream = getFeedbackOutputStream();
    if (feedbackOutputStream != null) { // We have a feedback stream so we'll send some messages to it.
      feedbackOutputStream.write(Messages.getString("ContentRepositoryCleaner.INFO_0001").getBytes()); //$NON-NLS-1$
      feedbackOutputStream.write(Integer.toString(deleteCount).getBytes());
      feedbackOutputStream.write(Messages.getString("ContentRepositoryCleaner.INFO_0002").getBytes()); //$NON-NLS-1$
      feedbackOutputStream.write(Integer.toString(Math.abs(daysBack)).getBytes());
      feedbackOutputStream.write(Messages.getString("ContentRepositoryCleaner.INFO_0003").getBytes()); //$NON-NLS-1$
    }
    return true;
  }

  @Override
  public boolean init() {
    return true;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog(ContentRepositoryCleaner.class);
  }

}
