/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * Copyright 2007-2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.repository.solution.filebased;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.operations.FileOperations;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.solution.ActionInfo;

public class SolutionRepositoryVfsFileObject implements FileObject {

  private String solution;

  private String path;

  private String action;

  private String fileRef;

  private ISolutionRepository repository;

  private FileContent content = null;

  private FileName name;

  private FileType type;

  public SolutionRepositoryVfsFileObject(final String fileRef, final ISolutionRepository repository) {
    super();
    this.repository = repository;
    this.fileRef = fileRef;
    // try to guess the file type
    ActionInfo info = ActionInfo.parseActionString(fileRef, false);
    if (info != null) {
      solution = info.getSolutionName();
      path = info.getPath();
      action = info.getActionName();
      if (action == null) {
        type = FileType.FOLDER;
      } else {
        type = FileType.FILE;
      }
    } else {
      type = FileType.FOLDER;
    }
    name = new SolutionRepositoryFileName(fileRef, type);

  }

  public SolutionRepositoryVfsFileObject(final String fileRef, final ISolutionRepository repository, final FileType type) {
    super();
    this.repository = repository;
    this.fileRef = fileRef;
    ActionInfo info = ActionInfo.parseActionString(fileRef, false);
    if (info != null) {
      solution = info.getSolutionName();
      path = info.getPath();
      action = info.getActionName();
    } else {
      solution = null;
      path = null;
      action = null;
    }
    this.type = type;
    name = new SolutionRepositoryFileName(fileRef, type);

  }

  public ISolutionRepository getRepository() {
    return repository;
  }

  public String getFileRef() {
    return fileRef;
  }

  public FileName getName() {
    return name;
  }

  public URL getURL() throws FileSystemException {
    URL url = null;
    try {
      url = new URL("solution:/" + solution + "/" + path + "/" + action); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } catch (Exception e) {

    }
    return url;
  }

  public boolean exists() throws FileSystemException {
    return repository.resourceExists(fileRef, ISolutionRepository.ACTION_EXECUTE);
  }

  public boolean isHidden() throws FileSystemException {
    // not needed for our usage
    return false;
  }

  public boolean isReadable() throws FileSystemException {
    // not needed for our usage
    return exists();
  }

  public boolean isWriteable() throws FileSystemException {
    // not needed for our usage
    return false;
  }

  public FileType getType() throws FileSystemException {
    return type;
  }

  public FileObject getParent() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileSystem getFileSystem() {
    // not needed for our usage
    return null;
  }

  public FileObject[] getChildren() throws FileSystemException {

    List fileList = new ArrayList();
    if (solution == null) {
      // return a list of solutions
      ISolutionFile files[] = repository.getRootFolder(ISolutionRepository.ACTION_EXECUTE).listFiles();
      for (ISolutionFile element : files) {
        if (element.isDirectory()) {
          //System.out.println("solution: " + element.getFileName());//$NON-NLS-1$
          SolutionRepositoryVfsFileObject fileInfo = new SolutionRepositoryVfsFileObject(
              "/" + element.getFileName(), repository, FileType.FOLDER);//$NON-NLS-1$
          fileList.add(fileInfo);
        }
      }
    } else {
      ISolutionFile file = repository.getSolutionFile(fileRef, ISolutionRepository.ACTION_EXECUTE);
      if (file == null) { // no access
        return new FileObject[0];
      }
      ISolutionFile files[] = file.listFiles();
      for (ISolutionFile element : files) {
        if (element.isDirectory()) {
          //System.out.println("folder: " + element.getFileName());//$NON-NLS-1$
          SolutionRepositoryVfsFileObject fileInfo = new SolutionRepositoryVfsFileObject(
              "/" + element.getFileName(), repository, FileType.FOLDER);//$NON-NLS-1$
          fileList.add(fileInfo);
        } else {
          //System.out.println("file: " + element.getFileName());//$NON-NLS-1$
          SolutionRepositoryVfsFileObject fileInfo = new SolutionRepositoryVfsFileObject(fileRef
              + "/" + element.getFileName(), repository, FileType.FILE);//$NON-NLS-1$
          fileList.add(fileInfo);
        }
      }

    }
    FileObject fileObjects[] = new FileObject[fileList.size()];
    fileList.toArray(fileObjects);
    return fileObjects;
  }

  public FileObject getChild(final String arg0) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileObject resolveFile(final String arg0, final NameScope arg1) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileObject resolveFile(final String arg0) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileObject[] findFiles(final FileSelector arg0) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public void findFiles(final FileSelector arg0, final boolean arg1, final List arg2) throws FileSystemException {
    // not needed for our usage
  }

  public boolean delete() throws FileSystemException {
    // not needed for our usage
    return false;
  }

  public int delete(final FileSelector arg0) throws FileSystemException {
    // not needed for our usage
    return 0;
  }

  public void createFolder() throws FileSystemException {
    // not needed for our usage

  }

  public void createFile() throws FileSystemException {
    // not needed for our usage

  }

  public void copyFrom(final FileObject arg0, final FileSelector arg1) throws FileSystemException {
    // not needed for our usage

  }

  public void moveTo(final FileObject arg0) throws FileSystemException {
    // not needed for our usage
  }

  public boolean canRenameTo(final FileObject arg0) {
    // not needed for our usage
    return false;
  }

  public FileContent getContent() throws FileSystemException {
    content = new SolutionRepositoryVfsFileContent(this);
    return content;
  }

  public void close() throws FileSystemException {
    if (content != null) {
      content.close();
      content = null;
    }
  }

  public void refresh() throws FileSystemException {
    // not needed for our usage
  }

  public boolean isAttached() {
    // not needed for our usage
    return false;
  }

  public boolean isContentOpen() {
    return (content != null) && content.isOpen();
  }

  public FileOperations getFileOperations() throws FileSystemException {
    // not needed for our usage
    return null;
  }

}
