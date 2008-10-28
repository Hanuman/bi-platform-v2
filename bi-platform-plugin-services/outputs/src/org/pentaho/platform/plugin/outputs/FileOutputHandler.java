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
 * @created Dec 21, 2006
 * @author James Dixon
 * 
 */

package org.pentaho.platform.plugin.outputs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.SimpleContentItem;
import org.pentaho.platform.engine.services.outputhandler.BaseOutputHandler;
import org.pentaho.platform.plugin.services.messages.Messages;

public class FileOutputHandler extends BaseOutputHandler {
  protected static final Log logger = LogFactory.getLog(FileOutputHandler.class);

  @Override
  public IContentItem getFileOutputContentItem() {

    String contentRef = getContentRef();
    File file = new File(contentRef);
    File dir = file.getParentFile();
    if ((dir != null) && !dir.exists()) {
      boolean result = dir.mkdirs();
      if (!result) {
        logger.error(Messages.getErrorString("FileOutputHandler.ERROR_0001_COULD_NOT_CREATE_DIRECTORY", dir.getAbsolutePath()));
        return null;
      }
    }
    try {
      FileOutputStream outputStream = new FileOutputStream(file);
      SimpleContentItem content = new SimpleContentItem(outputStream);
      return content;
    } catch (FileNotFoundException e) {
      logger.error(Messages.getErrorString("FileOutputHandler.ERROR_0002_COULD_NOT_CREATE_OUTPUT_FILE", file.getAbsolutePath()), e);
    }
    return null;
  }

}
