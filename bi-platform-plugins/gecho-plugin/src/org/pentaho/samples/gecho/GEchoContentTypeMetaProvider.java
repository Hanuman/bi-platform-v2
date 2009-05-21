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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 *
 * Created May 20, 2009
 * @author Aaron Phillips
 */

package org.pentaho.samples.gecho;

import java.io.InputStream;

import org.dom4j.Document;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.SolutionFileMetaAdapter;
import org.pentaho.platform.engine.core.solution.FileInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.actionsequence.SequenceDefinition;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

public class GEchoContentTypeMetaProvider extends SolutionFileMetaAdapter {

  public IFileInfo getFileInfo(ISolutionFile solutionFile, InputStream in) {
    try {
      Document actionSequenceDocument = XmlDom4JHelper.getDocFromStream(in);
      if (actionSequenceDocument == null) {
        return null;
      }

      String filename = solutionFile.getFileName();
      String path = solutionFile.getSolutionPath();
      String solution = solutionFile.getSolution();

      //FIXME: we are getting a NPE here.  The fallback file info is being returned by the plugin manager
      IActionSequence actionSequence = SequenceDefinition.ActionSequenceFactory(actionSequenceDocument, filename, path,
          solution, logger, PentahoSystem.getApplicationContext(), Logger.getLogLevel());
      if (actionSequence == null) {
        Logger.error(getClass().toString(), "failed to get meta information on action sequence "+solutionFile.getFullPath());
        return null;
      }

      IFileInfo info = new FileInfo();
      info.setAuthor(actionSequence.getAuthor());
      info.setDescription(actionSequence.getDescription());
      info.setDisplayType(actionSequence.getResultType());
      info.setIcon(actionSequence.getIcon());
      info.setTitle(actionSequence.getTitle());
      return info;
    } catch (Exception e) {
      if (logger != null) {
        logger.error(getClass().toString(), e);
      }
      return null;
    }
  }

}
