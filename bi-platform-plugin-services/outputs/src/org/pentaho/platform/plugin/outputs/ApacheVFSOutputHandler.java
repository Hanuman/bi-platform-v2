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

import java.io.OutputStream;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.SimpleContentItem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.engine.services.outputhandler.BaseOutputHandler;
import org.pentaho.platform.util.logging.Logger;

public class ApacheVFSOutputHandler extends BaseOutputHandler {

  @Override
  public IContentItem getFileOutputContentItem() {

    String contentRef = getContentRef();
    try {
      String contentName = getHandlerId().substring(4) + ":" + contentRef; //$NON-NLS-1$
      FileSystemManager fsManager = VFS.getManager();
      if (fsManager == null) {
        Logger.error(ApacheVFSOutputHandler.class.getName(), Messages
            .getString("ApacheVFSOutputHandler.ERROR_0001_CANNOT_GET_VFSMGR")); //$NON-NLS-1$
        return null;
      }
      FileObject file = fsManager.resolveFile(contentName);
      if (file == null) {
        Logger.error(ApacheVFSOutputHandler.class.getName(), Messages.getString(
            "ApacheVFSOutputHandler.ERROR_0002_CANNOT_GET_VF", contentName)); //$NON-NLS-1$
        return null;
      }
      if (!file.isWriteable()) {
        Logger.error(ApacheVFSOutputHandler.class.getName(), Messages.getString(
            "ApacheVFSOutputHandler.ERROR_0003_CANNOT_WRITE", contentName)); //$NON-NLS-1$
        return null;
      }
      FileContent fileContent = file.getContent();
      if (fileContent == null) {
        Logger.error(ApacheVFSOutputHandler.class.getName(), Messages.getString(
            "ApacheVFSOutputHandler.ERROR_0004_CANNOT_GET_CTX", contentName)); //$NON-NLS-1$
        return null;
      }
      OutputStream outputStream = fileContent.getOutputStream();

      SimpleContentItem content = new SimpleContentItem(outputStream);
      return content;
    } catch (Throwable t) {
      Logger.error(ApacheVFSOutputHandler.class.getName(), Messages.getString(
          "ApacheVFSOutputHandler.ERROR_0005_CANNOT_GET_HANDLER", contentRef), t); //$NON-NLS-1$
    }

    return null;
  }

}
