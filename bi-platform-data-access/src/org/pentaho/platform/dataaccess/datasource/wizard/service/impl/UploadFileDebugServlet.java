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

public class UploadFileDebugServlet extends HttpServlet implements Servlet {

  private static final long serialVersionUID = 8305367618713715640L;
  private static final long MAX_FILE_SIZE = 300000;
  private static final long MAX_FOLDER_SIZE = 900000;
  public static final String DEFAULT_UPLOAD_FILEPATH_FILE_NAME = "debug_upload_filepath.properties"; //$NON-NLS-1$
  public static final String UPLOAD_FILE_PATH = "upload.file.path"; //$NON-NLS-1$
 
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
    try {
    response.setContentType("text/plain");
    FileItem uploadItem = getFileItem(request);
    if (uploadItem == null) {
        response.getWriter().write(Messages.getErrorString("UploadFileDebugServlet.ERROR_0001_NO_FILE_TO_UPLOAD"));
      return;
    }
    if(MAX_FILE_SIZE < uploadItem.getSize()) {
        response.getWriter().write(Messages.getErrorString("UploadFileDebugServlet.ERROR_0003_FILE_TOO_BIG"));
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
      path = (String) properties.get( UPLOAD_FILE_PATH );
    } catch (IOException e) {
      response.getWriter().write(Messages.getErrorString("UploadFileDebugServlet.ERROR_0005_UNKNOW_ERROR",e.getLocalizedMessage()));
    }

    if(uploadItem.getSize() + getFolderSize(new File(path)) > MAX_FOLDER_SIZE) {
        response.getWriter().write(Messages.getErrorString("UploadFileDebugServlet.ERROR_0004_FOLDER_SIZE_LIMIT_REACHED"));
      return;                
    }
    byte[] fileContents = uploadItem.get();
    String filename = uploadItem.getName();
    int index = filename.lastIndexOf(File.separatorChar);
    if(index > 0) {
      filename = filename.substring(index);
    }
    if(doesFileExists(new File(path+filename))) {
        response.getWriter().write(Messages.getErrorString("UploadFileDebugServlet.ERROR_0002_FILE_ALREADY_EXIST"));
      return;                        
    }
    FileOutputStream outputStream = new FileOutputStream(path+filename);
    outputStream.write(fileContents);
    outputStream.flush();
    outputStream.close();
      response.getWriter().write(new String(path+filename));
    } catch(Exception e) {
        response.getWriter().write(Messages.getErrorString("UploadFileDebugServlet.ERROR_0005_UNKNOW_ERROR",e.getLocalizedMessage()));
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
