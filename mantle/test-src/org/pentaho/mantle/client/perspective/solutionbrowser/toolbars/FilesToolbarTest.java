/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
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
 * @created Aug 27, 2008 
 * @author wseyler
 */

package org.pentaho.mantle.client.perspective.solutionbrowser.toolbars;

import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.perspective.solutionbrowser.IFileItemCallback;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * @author wseyler
 *
 */
public class FilesToolbarTest extends GWTTestCase {

  /* (non-Javadoc)
   * @see com.google.gwt.junit.client.GWTTestCase#getModuleName()
   */
  @Override
  public String getModuleName() {

    return "org.pentaho.mantle.MantleApplication"; //$NON-NLS-1$
  }

  public void testCreate() {
    FilesToolbar toolbar = new FilesToolbar(new MockCallback());
    assertNotNull(toolbar);
  }

  /**
   * @author wseyler
   *
   */
  public class MockCallback implements IFileItemCallback {

    /* (non-Javadoc)
     * @see org.pentaho.mantle.client.perspective.solutionbrowser.IFileItemCallback#createSchedule(java.lang.String)
     */
    public void createSchedule(String cronExpression) {
      // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.pentaho.mantle.client.perspective.solutionbrowser.IFileItemCallback#createSchedule()
     */
    public void createSchedule() {
      // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.pentaho.mantle.client.perspective.solutionbrowser.IFileItemCallback#editFile()
     */
    public void editFile() {
      // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.pentaho.mantle.client.perspective.solutionbrowser.IFileItemCallback#getSelectedFileItem()
     */
    public FileItem getSelectedFileItem() {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see org.pentaho.mantle.client.perspective.solutionbrowser.IFileItemCallback#loadPropertiesDialog()
     */
    public void loadPropertiesDialog() {
      // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.pentaho.mantle.client.perspective.solutionbrowser.IFileItemCallback#openFile(int)
     */
    public void openFile(int mode) {
      // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.pentaho.mantle.client.perspective.solutionbrowser.IFileItemCallback#setSelectedFileItem(org.pentaho.mantle.client.perspective.solutionbrowser.FileItem)
     */
    public void setSelectedFileItem(FileItem fileItem) {
      // TODO Auto-generated method stub

    }

  }

}
