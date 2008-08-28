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
package org.pentaho.platform.plugin.services.versionchecker;

import java.util.ArrayList;

import org.pentaho.versionchecker.IVersionCheckErrorHandler;
import org.pentaho.versionchecker.IVersionCheckResultHandler;
import org.pentaho.versionchecker.VersionChecker;

/**
 * Avoid loading this class without reflection, so if someone
 * deletes the versionchecker.jar, there will be no class loading
 * exceptions
 *
 * @author Will Gorman
 *
 */
public class PentahoVersionCheckHelper {

  protected boolean ignoreExistingUpdates = false;

  protected ArrayList resultList = new ArrayList();

  protected int versionRequestFlags = -1;

  public void setVersionRequestFlags(final int versionRequestFlags) {
    this.versionRequestFlags = versionRequestFlags;
  }

  public void setIgnoreExistingUpdates(final boolean ignoreExistingUpdates) {
    this.ignoreExistingUpdates = ignoreExistingUpdates;
  }

  public ArrayList getResults() {
    return resultList;
  }

  public void performUpdate() {
    IVersionCheckResultHandler resultHandler = new IVersionCheckResultHandler() {
      public void processResults(String results) {
        // parse xml results vs spewing out xml?
        resultList.add(results);
      }
    };

    IVersionCheckErrorHandler errorHandler = new IVersionCheckErrorHandler() {
      public void handleException(Exception e) {
        resultList.add("<vercheck><error><![CDATA[" + e.getMessage() + "]]></error></vercheck>"); //$NON-NLS-1$ //$NON-NLS-2$
      }
    };

    PentahoVersionCheckDataProvider dataProvider = new PentahoVersionCheckDataProvider();
    if (versionRequestFlags != -1) {
      dataProvider.setVersionRequestFlags(versionRequestFlags);
    }

    VersionChecker vc = new VersionChecker();

    vc.setDataProvider(dataProvider);
    vc.addResultHandler(resultHandler);
    vc.addErrorHandler(errorHandler);

    vc.performCheck(ignoreExistingUpdates);
  }
}
