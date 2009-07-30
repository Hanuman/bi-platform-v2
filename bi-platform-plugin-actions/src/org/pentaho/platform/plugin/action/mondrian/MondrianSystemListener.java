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
 * Copyright 2006 - 2009 Pentaho Corporation.  All rights reserved.
 *
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
      ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, session);
      if (repository.resourceExists(mondrianPropsFilename, ISolutionRepository.ACTION_EXECUTE)) {
        is = repository.getResourceInputStream(mondrianPropsFilename, false, ISolutionRepository.ACTION_EXECUTE);
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
