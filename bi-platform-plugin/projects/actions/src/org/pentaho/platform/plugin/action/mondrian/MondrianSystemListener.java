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
package org.pentaho.platform.plugin.action.mondrian;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import mondrian.olap.MondrianProperties;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

public class MondrianSystemListener implements IPentahoSystemListener {

  public boolean startup(final IPentahoSession session) {
    try {
      System.setProperty("pentaho.olap.xmladatasources", "system/olap/datasources.xml"); //$NON-NLS-1$ //$NON-NLS-2$
    } catch (Exception ex) {
      Logger.error(MondrianSystemListener.class.getName(), Messages
          .getErrorString("MondrianSystemListener.ERROR_0001_PROPERTY_SET_FAILED"), ex); //$NON-NLS-1$
    }

    loadMondrianProperties(session);

    return true;
  }

  /**
   * on pentaho system startup, load the mondrian.properties file
   * from system/mondrian/mondrian.properties
   */
  public void loadMondrianProperties(final IPentahoSession session) {
    /* Load the mondrian.properties file */
    String mondrianPropsFilename = "system" + File.separator + "mondrian" + File.separator + "mondrian.properties"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    InputStream is = null;
    try {
      ISolutionRepository repository = PentahoSystem.getSolutionRepository(session);
      if (repository.resourceExists(mondrianPropsFilename)) {
        is = repository.getResourceInputStream(mondrianPropsFilename, false);
        MondrianProperties.instance().load(is);
        Logger.debug(MondrianSystemListener.class.getName(), Messages.getString(
            "MondrianSystemListener.PROPERTY_FILE_LOADED", mondrianPropsFilename)); //$NON-NLS-1$
      } else {
        Logger.warn(MondrianSystemListener.class.getName(), Messages.getString(
            "MondrianSystemListener.PROPERTY_FILE_NOT_FOUND", mondrianPropsFilename)); //$NON-NLS-1$
      }
    } catch (IOException ioe) {
      Logger.error(MondrianSystemListener.class.getName(), Messages.getString(
          "MondrianSystemListener.ERROR_0002_PROPERTY_FILE_READ_FAILED", ioe.getMessage()), ioe); //$NON-NLS-1$
    } finally {
      try {
        if (is != null) {
          is.close();
        }
      } catch (IOException e) {
        // ignore
      }
    }
  }

  public void shutdown() {
    // Nothing required
  }
}
