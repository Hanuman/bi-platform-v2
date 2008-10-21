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
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.perspective.solutionbrowser;

import org.pentaho.mantle.client.perspective.solutionbrowser.FileCommand.COMMAND;

public interface IFileItemCallback {
  public FileItem getSelectedFileItem();
  public void setSelectedFileItem(FileItem fileItem);
  public void openFile(COMMAND mode);
  public void editFile();
  public void editActionFile();
  public void createSchedule(String cronExpression);
  public void loadPropertiesDialog();
  public void shareFile();
  public void createSchedule();
  public void selectNextItem(FileItem currentItem);
  public void selectPreviousItem(FileItem currentItem);
}
