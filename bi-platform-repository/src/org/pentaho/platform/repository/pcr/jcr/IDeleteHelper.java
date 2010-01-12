package org.pentaho.platform.repository.pcr.jcr;

import java.io.Serializable;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.pentaho.platform.api.repository.RepositoryFile;

/**
 * Handles delete, undelete, and permanent delete. Handles listing deleted files and purging some or all deleted files.
 * 
 * @author mlowery
 */
public interface IDeleteHelper {

  /**
   * Deletes a file in a way that it can be recovered.
   * 
   * @param fileId
   */
  void deleteFile(final Session session, final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId)
      throws RepositoryException;

  /**
   * Recovers a deleted file to its original location.
   * 
   * @param fileId
   */
  void undeleteFile(final Session session, final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId)
      throws RepositoryException;

  /**
   * Deletes a file in a way that it cannot be recovered. (Note that "cannot be recovered" doesn't mean "shred"--it 
   * means that the file cannot be recovered using this API.)
   * 
   * @param fileId
   */
  void permanentlyDeleteFile(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId) throws RepositoryException;

  /**
   * Lists deleted files for this folder and user.
   * 
   * @param folderId
   * @return list of deleted files IDs for this folder and user
   */
  List<RepositoryFile> getDeletedFiles(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable folderId) throws RepositoryException;

  /**
   * Lists deleted files for this user. In this case, the path field of each file is the original path where it was 
   * located prior to deletion. This is the "recycle bin" view.
   * 
   * @return list of deleted files for this user
   */
  List<RepositoryFile> getDeletedFiles(final Session session, final PentahoJcrConstants pentahoJcrConstants)
      throws RepositoryException;

  /**
   * Returns the ID of the original parent folder. Can be used by caller to checkout parent folder before calling 
   * {@link #undeleteFile(Session, PentahoJcrConstants, Serializable)}.
   * 
   * @param fileId file id of deleted file
   */
  Serializable getOriginalParentFolderId(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId) throws RepositoryException;
}
