/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
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
 * @created Dec 21, 2006
 * @author James Dixon
 * 
 */

package org.pentaho.platform.engine.services.outputhandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.SimpleContentItem;

public class ZipOutputHandler extends BaseOutputHandler {

  public IContentItem getFileOutputContentItem() {

    String contentRef = getContentRef();
    File file = new File(contentRef);
    File dir = file.getParentFile();
    if (!dir.exists()) {
      dir.mkdirs();
    }
    try {
      FileOutputStream outputStream = new FileOutputStream(file);
      SimpleContentItem content = new SimpleContentItem(outputStream);
      return content;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

}
