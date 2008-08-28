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
 * @created Sep 16, 2007 
 * @author David Kincade
 */
package org.pentaho.platform.plugin.services.versionchecker;

import java.util.Map;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.VersionHelper;
import org.pentaho.platform.util.VersionInfo;
import org.pentaho.versionchecker.IVersionCheckDataProvider;

public class PentahoVersionCheckDataProvider implements IVersionCheckDataProvider {

  /**
   * The version information for the pentaho platform is in the core jar. The PentahoSystem
   * class will guarantee that we get the information. This information will contain the 
   * product id and the version number.
   */
  protected static final VersionInfo versionInfo = VersionHelper.getVersionInfo(PentahoSystem.class);

  protected int versionRequestFlags = IVersionCheckDataProvider.DEPTH_MINOR_MASK
      + IVersionCheckDataProvider.DEPTH_GA_MASK;

  public void setVersionRequestFlags(final int flags) {
    versionRequestFlags = flags;
  }

  /**
   * Returns the application id (code) for this application (the pentaho platform)
   */
  public String getApplicationID() {
    return PentahoVersionCheckDataProvider.versionInfo == null ? null : PentahoVersionCheckDataProvider.versionInfo
        .getProductID();
  }

  /**
   * Returns the application version number found in the manifest
   */
  public String getApplicationVersion() {
    return PentahoVersionCheckDataProvider.versionInfo == null ? null : PentahoVersionCheckDataProvider.versionInfo
        .getVersionNumber();
  }

  /**
   * Returns the base url for the connection to the pentaho version checking server.
   * Currently, there is no reason to use anything other than the default.
   */
  public String getBaseURL() {
    return null;
  }

  /**
   * Returns the extra information that can be provided.
   */
  public Map getExtraInformation() {
    return null;
  }

  protected int computeOSMask() {
    try {
      String os = System.getProperty("os.name"); //$NON-NLS-1$
      if (os != null) {
        os = os.toLowerCase();
        if (os.indexOf("windows") >= 0) { //$NON-NLS-1$
          return IVersionCheckDataProvider.DEPTH_WINDOWS_MASK;
        } else if (os.indexOf("mac") >= 0) { //$NON-NLS-1$
          return IVersionCheckDataProvider.DEPTH_MAC_MASK;
        } else if (os.indexOf("linux") >= 0) { //$NON-NLS-1$
          return IVersionCheckDataProvider.DEPTH_LINUX_MASK;
        } else {
          return IVersionCheckDataProvider.DEPTH_ALL_MASK;
        }
      }
    } catch (Exception e) {
      // ignore any issues
    }
    return IVersionCheckDataProvider.DEPTH_ALL_MASK;
  }

  /**
   * generates the depth flags
   */
  public int getDepth() {

    int depth = versionRequestFlags + computeOSMask();
    return depth;
  }
}
