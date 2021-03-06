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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created May 26, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.util.UUIDUtil;
import org.safehaus.uuid.UUID;

public class UploadFileDebugServlet extends HttpServlet implements Servlet {

  private static final long serialVersionUID = 8305367618713715640L;

  private static final long MAX_FILE_SIZE = 300000;

  private static final long MAX_FOLDER_SIZE = 900000;

  public static final String DEFAULT_UPLOAD_FILEPATH_FILE_NAME = "debug_upload_filepath.properties"; //$NON-NLS-1$

  public static final String UPLOAD_FILE_PATH = "upload.file.path"; //$NON-NLS-1$

  public static final String CSV_EXT = ".csv"; //$NON-NLS-1$

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      response.setContentType("text/plain"); //$NON-NLS-1$
      FileItem uploadItem = getFileItem(request);
      if (uploadItem == null) {
        response.getWriter().write(Messages.getInstance().getErrorString("UploadFileDebugServlet.ERROR_0001_NO_FILE_TO_UPLOAD")); //$NON-NLS-1$
        return;
      }
      if (MAX_FILE_SIZE < uploadItem.getSize()) {
        response.getWriter().write(Messages.getInstance().getErrorString("UploadFileDebugServlet.ERROR_0003_FILE_TOO_BIG")); //$NON-NLS-1$
        return;
      }

      URL url = ClassLoader.getSystemResource(DEFAULT_UPLOAD_FILEPATH_FILE_NAME);
      URI uri = url.toURI();
      File file = new File(uri);
      FileInputStream fis = new FileInputStream(file);
      Properties properties = new Properties();
      String path = null;
      try {
        properties.load(fis);
        path = (String) properties.get(UPLOAD_FILE_PATH);
      } catch (IOException e) {
        response.getWriter().write(
            Messages.getInstance().getErrorString("UploadFileDebugServlet.ERROR_0005_UNKNOWN_ERROR", e.getLocalizedMessage()));//$NON-NLS-1$
      } finally {
        fis.close();
      }

      if (uploadItem.getSize() + getFolderSize(new File(path)) > MAX_FOLDER_SIZE) {
        response.getWriter().write(
            Messages.getInstance().getErrorString("UploadFileDebugServlet.ERROR_0004_FOLDER_SIZE_LIMIT_REACHED")); //$NON-NLS-1$ 
        return;
      }
      byte[] fileContents = uploadItem.get();
      UUID id = UUIDUtil.getUUID();
      String filename = id.toString() + CSV_EXT;
      // File name is path + / + name of the file
      String filenameWithPath = path + File.separatorChar + filename;
      if (doesFileExists(new File(filenameWithPath))) {
        response.getWriter().write(Messages.getInstance().getErrorString("UploadFileDebugServlet.ERROR_0002_FILE_ALREADY_EXIST")); //$NON-NLS-1$
        return;
      }
      FileOutputStream outputStream = new FileOutputStream(filenameWithPath);
      
      outputStream.write(fileContents);
      outputStream.flush();
      outputStream.close();
      response.getWriter().write(new String(filename));
    } catch (Exception e) {
      response.getWriter().write(
          Messages.getInstance().getErrorString("UploadFileDebugServlet.ERROR_0005_UNKNOWN_ERROR", e.getLocalizedMessage())); //$NON-NLS-1$
    }
  }

  private FileItem getFileItem(HttpServletRequest request) {
    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    try {
      List items = upload.parseRequest(request);
      Iterator it = items.iterator();
      while (it.hasNext()) {
        FileItem item = (FileItem) it.next();
        if (!item.isFormField() && "uploadFormElement".equals(item.getFieldName())) {//$NON-NLS-1$
          return item;
        }
      }
    } catch (FileUploadException e) {
      return null;
    }
    return null;
  }

  private long getFolderSize(File folder) {
    long foldersize = 0;
    File[] filelist = folder.listFiles();
    for (int i = 0; i < filelist.length; i++) {
      if (filelist[i].isDirectory()) {
        foldersize += getFolderSize(filelist[i]);
      } else {
        foldersize += filelist[i].length();
      }
    }
    return foldersize;
  }

  private boolean doesFileExists(File file) {
    return file.exists();
  }
}
