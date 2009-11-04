package org.pentaho.platform.web.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.web.servlet.messages.Messages;
import org.safehaus.uuid.UUID;

public class UploadFileServlet extends HttpServlet implements Servlet {

  private static final long serialVersionUID = 8305367618713715640L;
  private static final long MAX_FILE_SIZE = 300000;
  private static final long MAX_FOLDER_SIZE = 3000000;
  public static final String DEFAULT_RELATIVE_UPLOAD_FILE_PATH = File.separatorChar + "system" + File.separatorChar + "metadata" + File.separatorChar + "csvfiles" + File.separatorChar; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
  public static final String CSV_EXT = ".csv"; //$NON-NLS-1$
  public static final String EXCEL_EXT = ".excel"; //$NON-NLS-1$
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
      try {
        // Retrieving the file upload defaults from pentaho.xml
        String relativePath = PentahoSystem.getSystemSetting("file-upload-defaults/relative-path", String.valueOf(DEFAULT_RELATIVE_UPLOAD_FILE_PATH));  //$NON-NLS-1$ 
        String maxFileLimit = PentahoSystem.getSystemSetting("file-upload-defaults/max-file-limit", String.valueOf(MAX_FILE_SIZE));  //$NON-NLS-1$    
        String maxFolderLimit = PentahoSystem.getSystemSetting("file-upload-defaults/max-folder-limit", String.valueOf(MAX_FOLDER_SIZE));  //$NON-NLS-1$
        
      response.setContentType("text/plain");
      FileItem uploadItem = getFileItem(request);
      if (uploadItem == null) {
        response.getWriter().write(Messages.getInstance().getErrorString("UploadFileServlet.ERROR_0001_NO_FILE_TO_UPLOAD"));
        return;
      }
      if(Long.parseLong(maxFileLimit) < uploadItem.getSize()) {
        response.getWriter().write(Messages.getInstance().getErrorString("UploadFileServlet.ERROR_0003_FILE_TOO_BIG"));
        return;        
      }
      
      String path = PentahoSystem.getApplicationContext().getSolutionPath(relativePath);
      
      File pathDir = new File(path);
      // create the path if it doesn't exist yet
      if (!pathDir.exists()) {
        pathDir.mkdirs();
      }
      
      if(uploadItem.getSize() + getFolderSize(pathDir) > Long.parseLong(maxFolderLimit)) {
        response.getWriter().write(Messages.getInstance().getErrorString("UploadFileServlet.ERROR_0004_FOLDER_SIZE_LIMIT_REACHED"));
        return;                
      }
      byte[] fileContents = uploadItem.get();
      UUID id = UUIDUtil.getUUID();
      String filename = id.toString() + CSV_EXT;
      
      if(doesFileExists(new File(path+filename))) {
        response.getWriter().write(Messages.getInstance().getErrorString("UploadFileServlet.ERROR_0002_FILE_ALREADY_EXIST"));
        return;                        
      }
      FileOutputStream outputStream = new FileOutputStream(path+filename);
      outputStream.write(fileContents);
      outputStream.flush();
      outputStream.close();
      response.getWriter().write(new String(filename));
      } catch(Exception e) {
        response.getWriter().write(Messages.getInstance().getErrorString("UploadFileServlet.ERROR_0005_UNKNOWN_ERROR",e.getLocalizedMessage()));
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
        if (!item.isFormField() && "uploadFormElement".equals(item.getFieldName())) {
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
