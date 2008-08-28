/*
 * Copyright 2007 - 2008 Pentaho Corporation.  All rights reserved. 
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
 * @created Sep 17, 2007 
 * @author Will Gorman
 */
package org.pentaho.platform.plugin.action.versionchecker;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.services.versionchecker.PentahoVersionCheckReflectHelper;

/**
 * Version Check Component
 * This component makes a call to pentaho's server to see if a new version
 * is a vailable.
 * 
 * Uses reflection helper so that versioncheck.jar can be deleted without 
 * problems
 * 
 * input param "ignoreExistingUpdates" - if true, ignore existing updates discovered
 * 
 * @author Will Gorman
 * 
 */
public class PentahoVersionCheckComponent extends ComponentBase {

  private static final long serialVersionUID = 8178713714323100555L;

  private static final String DOCUMENT = "document"; //$NON-NLS-1$

  @Override
  public Log getLogger() {
    return LogFactory.getLog(PentahoVersionCheckComponent.class);
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  protected boolean validateAction() {
    return true;
  }

  @Override
  public void done() {

  }

  @Override
  protected boolean executeAction() {

    String output = null;

    boolean ignoreExistingUpdates = getInputBooleanValue("ignoreExistingUpdates", true); //$NON-NLS-1$

    // pull out release flags from the releaseFlags string
    int versionRequestFlags = -1;
    try {
      Object releaseFlagsObj = getInputValue("releaseFlags"); //$NON-NLS-1$
      String releaseFlags = ""; //$NON-NLS-1$
      if (releaseFlagsObj instanceof String[]) {
        String[] arr = (String[]) releaseFlagsObj;
        if (arr.length > 0) {
          releaseFlags += arr[0];
          for (int i = 1; i < arr.length; i++) {
            releaseFlags += "," + arr[i]; //$NON-NLS-1$
          }
        }
      } else {
        releaseFlags = releaseFlagsObj.toString();
      }

      if (releaseFlags != null) {
        releaseFlags = releaseFlags.toLowerCase();
        boolean requestMajorReleases = releaseFlags.indexOf("major") >= 0; //$NON-NLS-1$
        boolean requestMinorReleases = releaseFlags.indexOf("minor") >= 0; //$NON-NLS-1$
        boolean requestRCReleases = releaseFlags.indexOf("rc") >= 0; //$NON-NLS-1$
        boolean requestGAReleases = releaseFlags.indexOf("ga") >= 0; //$NON-NLS-1$
        boolean requestMilestoneReleases = releaseFlags.indexOf("milestone") >= 0; //$NON-NLS-1$

        versionRequestFlags = (requestMajorReleases ? 4 : 0) + (requestMinorReleases ? 8 : 0)
            + (requestRCReleases ? 16 : 0) + (requestGAReleases ? 32 : 0) + (requestMilestoneReleases ? 64 : 0);
      }
    } catch (Exception e) {
      // ignore errors
    }

    if (PentahoVersionCheckReflectHelper.isVersionCheckerAvailable()) {
      List results = PentahoVersionCheckReflectHelper.performVersionCheck(ignoreExistingUpdates, versionRequestFlags);
      output = PentahoVersionCheckReflectHelper.logVersionCheck(results, getLogger());
    } else {
      output = "<vercheck><error><[!CDATA[" + Messages.getString("VersionCheck.VERSION_CHECK_DISABLED") + "]]></error></vercheck>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    if (isDefinedOutput(PentahoVersionCheckComponent.DOCUMENT)) {
      setOutputValue(PentahoVersionCheckComponent.DOCUMENT, output);
    }
    return true;
  }

  @Override
  public boolean init() {
    return true;
  }

}
