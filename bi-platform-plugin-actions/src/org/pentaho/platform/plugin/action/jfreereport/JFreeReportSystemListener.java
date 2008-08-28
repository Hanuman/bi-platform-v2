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
 * Created Jan 9, 2006 
 * @author mbatchel
 */
package org.pentaho.platform.plugin.action.jfreereport;

import org.jfree.report.JFreeReportBoot;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoReportConfiguration;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

public class JFreeReportSystemListener implements IPentahoSystemListener {
  public JFreeReportSystemListener() {
  }

  public boolean startup(final IPentahoSession session) {
    try {
      synchronized (JFreeReportBoot.class) {
        JFreeReportBoot.setUserConfig(new PentahoReportConfiguration());
        JFreeReportBoot.getInstance().start();
      }
    } catch (Exception ex) {
      Logger.warn(JFreeReportSystemListener.class.getName(), Messages
          .getErrorString("JFreeReportSystemListener.ERROR_0001_JFREEREPORT_INITIALIZATION_FAILED"), //$NON-NLS-1$
          ex);
    }
    return true;
  }

  public void shutdown() {
    // Nothing required
  }

}
