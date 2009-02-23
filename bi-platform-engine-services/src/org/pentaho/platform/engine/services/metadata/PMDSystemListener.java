/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Jan 9, 2006 
 * @author mbatchel
 */
package org.pentaho.platform.engine.services.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.pms.core.CWM;

public class PMDSystemListener implements IPentahoSystemListener {

  public boolean startup(final IPentahoSession session) {

    Properties props = new Properties();
    String kettlePropsFilename = PentahoSystem.getApplicationContext().getSolutionPath(
        "system" + File.separator + "metadata" + File.separator + "repository.properties"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    InputStream is = null;
    try {
      File propsFile = new File(kettlePropsFilename);
      if (propsFile.exists()) {
        is = new FileInputStream(propsFile);
        props.load(is);

        InputStream xmiInputStream = null;
        try {
          xmiInputStream = PentahoSystem.get(ISolutionRepository.class, session).getResourceInputStream(
              "system/metadata/PentahoCWM.xml", false); //$NON-NLS-1$        
          CWM.getRepositoryInstance(props, xmiInputStream);
        } catch (Throwable t) {
          t.printStackTrace();
        } finally {
          if (xmiInputStream != null) {
            xmiInputStream.close();
          }
        }
      } else {
        Logger.error(PMDSystemListener.class.getName(), Messages
            .getErrorString("PMDSystemListener.ERROR_0001_PROPERTIES_NOT_FOUND")); //$NON-NLS-1$
        return true;
      }
    } catch (Throwable t) {
      Logger.error(PMDSystemListener.class.getName(), Messages
          .getErrorString("PMDSystemListener.ERROR_0002_COULD_NOT_INITIALIZE"), t); //$NON-NLS-1$
      return true; // Service is not required
    }
    return true;
  }

  public void shutdown() {
    // Nothing required
  }

}
