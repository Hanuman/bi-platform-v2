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
 *
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jul 25, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.engine.services.actionsequence;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.engine.services.messages.Messages;

public class ActionResource implements IActionSequenceResource {

  private String name;

  private String mimeType;

  private String address;

  private int sourceType;

  public ActionResource(final String name, final int sourceType, final String mimeType, final String address) {

    this.name = name;
    this.mimeType = mimeType;
    this.sourceType = sourceType;
    this.address = address;
  }

  public ActionResource(final String name, final int sourceType, final String mimeType, final String solutionName,
      final String solutionPath, final String location) {

    this(name, sourceType, mimeType, null);
    if (sourceType == IActionSequenceResource.SOLUTION_FILE_RESOURCE) {
      address = ActionResource.getLocationInSolution(solutionName, solutionPath, location);
    } else {
      address = location;
    }
  }

  // TODO sbarkdull, refactor, this method probably belongs in filebased...SolutionRepository,
  // and/or could possibly work with FileSolutionFile, or be incorporated in that class
  private static String getLocationInSolution(final String solutionName, final String solutionPath,
      final String location) {
    if (StringUtils.isEmpty(location)) {
      return (null);
    }
    if ((location.charAt(0) == '\\') || (location.charAt(0) == '/')) {
      return solutionName + File.separator + location;
    } else if (location.startsWith("..", 0)) { //$NON-NLS-1$
      // TODO: support relative paths...
      assert false : Messages.getErrorString("ActionResource.ERROR_0001_FEATURE_NOT_IMPLEMENTED"); //$NON-NLS-1$
      return null;
    } else {
      if ("".equals(solutionPath)) { //$NON-NLS-1$
        return solutionName + File.separator + location;
      }
      return solutionName + File.separator + solutionPath + File.separator + location;
    }
  }

  public static int getResourceType(final String sourceTypeName) {
    if ("solution-file".equals(sourceTypeName)) { //$NON-NLS-1$
      return IActionSequenceResource.SOLUTION_FILE_RESOURCE;
    } else if ("file".equals(sourceTypeName)) { //$NON-NLS-1$
      return IActionSequenceResource.FILE_RESOURCE;
    } else if ("url".equals(sourceTypeName)) { //$NON-NLS-1$
      return IActionSequenceResource.URL_RESOURCE;
    } else if ("xml".equals(sourceTypeName)) { //$NON-NLS-1$
      return IActionSequenceResource.XML;
    } else if ("string".equals(sourceTypeName)) { //$NON-NLS-1$
      return IActionSequenceResource.STRING;
    } else {
      return IActionSequenceResource.UNKNOWN_RESOURCE;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.solution.IActionResource#getName()
   */
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.solution.IActionResource#getMimeType()
   */
  public String getMimeType() {
    return mimeType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.solution.IActionResource#getSourceType()
   */
  public int getSourceType() {
    return sourceType;
  }

  public String getAddress() {
    return address;
  }

}
