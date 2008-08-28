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
 * @created Jul 11, 2005 
 * @author James Dixon
 * 
 */
package org.pentaho.platform.web.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Date;
import java.util.List;

import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.repository.ContentException;
import org.pentaho.platform.api.repository.IContentItem;


public class HttpContentItem implements IContentItem {

  private String mimeType;

  private OutputStream outputStream;

  private HttpOutputHandler outputHandler;

  private IMimeTypeListener mimeTypeListener = null;

  public HttpContentItem(final OutputStream outputStream, final HttpOutputHandler outputHandler) {
    this.outputStream = outputStream;
    this.outputHandler = outputHandler;
  }

  public void closeOutputStream() {
    // nothing to do here
  }

  public String getId() {
    return null;
  }

  public String getPath() {
    return null;
  }

  public String getName() {
    return null;
  }

  public String getTitle() {
    return null;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(final String mimeType) {
    this.mimeType = mimeType;
    outputHandler.setMimeType(mimeType);
    if (mimeTypeListener != null) {
      mimeTypeListener.setMimeType(mimeType);
    }
  }
  
  public void setName( String name ) {
	  
  }

  public String getUrl() {
    return null;
  }

  public List getFileVersions() {
    return null;
  }

  public void removeAllVersions() {
  }

  public void removeVersion(final String fileId) {
  }

  public InputStream getInputStream() throws ContentException {
    return null;
  }

  public IPentahoStreamSource getDataSource() {
    // TODO
    return null;
  }

  public Reader getReader() throws ContentException {
    return null;
  }

  public OutputStream getOutputStream(final String actionName) {
    if (mimeType == null) {
      setMimeType("text/html"); //$NON-NLS-1$
    }
    return outputStream;
  }

  public void setOutputStream(final OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public String getActionName() {
    return null;
  }

  public String getFileId() {
    return null;
  }

  public long getFileSize() {
    return 0;
  }

  public Date getFileDateTime() {
    return null;
  }

  public void makeTransient() {
    // NOOP
  }

  public IMimeTypeListener getMimeTypeListener() {
    return mimeTypeListener;
  }

  public void setMimeTypeListener(final IMimeTypeListener mimeTypeListener) {
    this.mimeTypeListener = mimeTypeListener;
  }

}
