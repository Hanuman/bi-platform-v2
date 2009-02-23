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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *  
 * @created Jul 13, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.engine.services.metadata;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.BasePublisher;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.pms.core.CWM;

public class MetadataPublisher extends BasePublisher {

  private static final long serialVersionUID = 1843038346011563927L;

  private static final Log logger = LogFactory.getLog(MetadataPublisher.class);

  public static final int NO_ERROR = 0;

  public static final int UNABLE_TO_DELETE = (int) Math.pow(2, 0);

  public static final int UNABLE_TO_IMPORT = (int) Math.pow(2, 1);

  public static final int NO_META = (int) Math.pow(2, 2);

  public static String XMI_FILENAME = "metadata.xmi"; //$NON-NLS-1$
  
  private static int numberUpdated = 0;

  @Override
  public Log getLogger() {
    return MetadataPublisher.logger;
  }

  public String getName() {
    return Messages.getString("MetadataPublisher.USER_PUBLISHER_NAME"); //$NON-NLS-1$
  }

  public String getDescription() {
    return Messages.getString("MetadataPublisher.USER_PUBLISHER_DESCRIPTION"); //$NON-NLS-1$
  }

  @Override
  public String publish(final IPentahoSession session) {
    MetadataPublisher.numberUpdated = 0;
    List messages = new ArrayList();
    int result = MetadataPublisher.loadAllMetadata(session, true);
    if (result == MetadataPublisher.NO_ERROR) {
      return Messages.getString(
          "MetadataPublisher.USER_METADATA_RELOADED", Integer.toString(MetadataPublisher.numberUpdated)); //$NON-NLS-1$
    }
    if ((result & MetadataPublisher.UNABLE_TO_DELETE) == MetadataPublisher.UNABLE_TO_DELETE) {
      messages.add(Messages.getString("MetadataPublisher.USER_DELETE_META_FAILED")); //$NON-NLS-1$
    }
    if ((result & MetadataPublisher.UNABLE_TO_IMPORT) == MetadataPublisher.UNABLE_TO_IMPORT) {
      messages.add(Messages.getString("MetadataPublisher.USER_IMPORT_META_FAILED")); //$NON-NLS-1$
    }
    //    if ((result & NO_META) == NO_META) {
    //      messages.add(Messages.getString("MetadataPublisher.USER_SOME_RELOAD_FAILED"));  //$NON-NLS-1$
    //    }
    StringBuffer buffer = new StringBuffer();
    buffer.append("<small>"); //$NON-NLS-1$
    Iterator iter = messages.iterator();
    while (iter.hasNext()) {
      buffer.append("<br/>" + iter.next().toString()); //$NON-NLS-1$
    }
    buffer.append("<br/><b>" + Messages.getString("MetadataPublisher.INFO_0001_CHECK_LOG") + "</b></small>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    return buffer.toString();
  }

  public static int loadAllMetadata(final IPentahoSession session, final boolean forceLoad) {
    ISolutionRepository repo = PentahoSystem.get(ISolutionRepository.class, session);
    Document doc = repo.getSolutions(ISolutionRepository.ACTION_EXECUTE);
    List nodes = doc.selectNodes("/repository/file[@type='FILE.FOLDER']"); //$NON-NLS-1$
    String solution;
    Iterator it = nodes.iterator();
    int allSuccess = MetadataPublisher.NO_ERROR;
    while (it.hasNext()) {
      Node elem = ((Element) it.next()).selectSingleNode("solution"); //$NON-NLS-1$
      if (elem != null) {
        solution = elem.getText();
        allSuccess |= MetadataPublisher.loadMetadata(solution, session, forceLoad);
      }
    }
    return allSuccess;
  }

  public static int loadMetadata(final String solution, final IPentahoSession session, boolean forceLoad) {
    int result = MetadataPublisher.NO_ERROR;
    String resourceName;
    InputStream xmiInputStream;
    resourceName = solution + "/" + XMI_FILENAME; //$NON-NLS-1$
    xmiInputStream = null;
    ISolutionRepository repo = PentahoSystem.get(ISolutionRepository.class, session);
    if (repo.resourceExists(resourceName)) {
      try {
        // try to delete the old one...
        String[] modelNames = CWM.getDomainNames();
        for (String element : modelNames) {
          if (element.equals(solution) && forceLoad) {
            // it already exists
            MetadataPublisher.logger.info(Messages.getString("MetadataPublisher.INFO_DELETING_METADATA", solution)); //$NON-NLS-1$
            CWM delCwm = CWM.getInstance(solution);
            delCwm.removeDomain();
          }
        }
      } catch (Throwable t) {
        MetadataPublisher.logger.error(Messages
            .getString("MetadataPublisher.ERROR_0001_COULD_NOT_DELETE", resourceName), t); //$NON-NLS-1$
        result |= MetadataPublisher.UNABLE_TO_DELETE;
      }

      CWM cwm = CWM.getInstance(solution);
      if ((cwm.getSchemas().length > 0) && !forceLoad) {
        MetadataPublisher.logger.debug(Messages.getString("MetadataPublisher.DEBUG_ALREADY_LOADED", resourceName)); //$NON-NLS-1$
        return result;
      }
      try {
        MetadataPublisher.logger.info(Messages.getString("MetadataPublisher.INFO_IMPORTING_METADATA", solution)); //$NON-NLS-1$
        xmiInputStream = repo.getResourceInputStream(resourceName, true);
        cwm.importFromXMI(xmiInputStream);
        return result;

      } catch (Throwable t) {
        MetadataPublisher.logger.error(Messages
            .getString("MetadataPublisher.ERROR_0002_COULD_NOT_IMPORT", resourceName), t); //$NON-NLS-1$
      } finally {
        if (xmiInputStream != null) {
          try {
            xmiInputStream.close();
            MetadataPublisher.numberUpdated++;
            return result;
          } catch (Throwable t) {
            MetadataPublisher.logger.error(Messages.getString(
                "MetadataPublisher.ERROR_0002_COULD_NOT_IMPORT", resourceName), t); //$NON-NLS-1$
            return result | MetadataPublisher.UNABLE_TO_IMPORT;
          }
        }
      }
    } else {
      return result;
    }
    return result;
  }

}
