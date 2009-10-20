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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Dec 1, 2008
 * @author Will Gorman
 */
package org.pentaho.mantle.client.solutionbrowser;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;

/**
 * This class defines the options available for individual file extensions.
 * Individual file extensions are configured in the SolutionBrowserPersepective.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 */
public class FileTypeEnabledOptions {
  
  private String fileExtension;
  
  // eventually replace with treeset, once GWT 2906 is fixed
  private Map<COMMAND, Boolean> enabledOptions = new HashMap<COMMAND, Boolean>();
  
  public FileTypeEnabledOptions(String fileExtension) {
    this.fileExtension = fileExtension;
  }
  
  public void applyOptions(String options) {
    String opts[] = options.split(",");
    for (String option : opts) {
      enabledOptions.put(COMMAND.valueOf(option), true);
    }
  }
  
  public void addCommand(COMMAND command) {
    enabledOptions.put(command, true);
  }
  
  public boolean isCommandEnabled(COMMAND command) {
    return enabledOptions.containsKey(command);
  }
  
  public boolean isSupportedFile(String filename) {
    // default FileType
    if (fileExtension == null) {
      return true;
    } else {
      return filename != null && filename.endsWith(fileExtension);
    }
  }
}