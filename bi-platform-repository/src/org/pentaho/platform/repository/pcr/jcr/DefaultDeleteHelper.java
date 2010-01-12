package org.pentaho.platform.repository.pcr.jcr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.repository.pcr.RepositoryPaths;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link IDeleteHelper}. If user {@code suzy} in tenant {@code acme} deletes a file with id
 * {@code xyz} and name {@code test} in folder with id {@code abc} then this implementation will move the file to delete
 * in such a way that the new absolute path to the deleted file will be 
 * {@code /pentaho/acme/home/suzy/.trash/pho:abc/pho:xyz/test}. This provides fast access to the deleted items of a 
 * particular folder plus fast access to all deleted items (aka the recycle bin view).
 * 
 * <p>
 * Assumptions:
 * <ul>
 * <li>User home folder and all ancestors are not versioned.</li>
 * <li>Internal folders are never versioned.</li>
 * </ul>
 * </p> 
 * 
 * <p>
 * By storing deleted files inside the user's home folder, the user's recycle bin is effectively private. This is 
 * desirable because a deleted file with confidential information should not be seen by anyone else except the deleting
 * user.
 * </p>
 * 
 * @author mlowery
 */
public class DefaultDeleteHelper implements IDeleteHelper {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(DefaultDeleteHelper.class);

  // ~ Instance fields =================================================================================================

  final IOwnerLookupHelper ownerLookupHelper;

  final ILockTokenHelper lockTokenHelper;

  // ~ Constructors ====================================================================================================

  public DefaultDeleteHelper(final IOwnerLookupHelper ownerLookupHelper, final ILockTokenHelper lockTokenHelper) {
    this.ownerLookupHelper = ownerLookupHelper;
    this.lockTokenHelper = lockTokenHelper;
  }

  // ~ Methods =========================================================================================================

  private static final String FOLDER_NAME_TRASH = ".trash"; //$NON-NLS-1$

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Moves file to subfolder of .trash folder in same folder as file.
   * </p>
   */
  public void deleteFile(final Session session, final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId)
      throws RepositoryException {
    Node fileToDeleteNode = session.getNodeByUUID(fileId.toString());
    // move file to .trash subfolder named with the UUID of the file to delete
    Node trashFileIdNode = getOrCreateTrashFileIdNode(session, pentahoJcrConstants, fileId);
    session.move(fileToDeleteNode.getPath(), trashFileIdNode.getPath() + RepositoryFile.SEPARATOR
        + fileToDeleteNode.getName());

  }

  /**
   * Creates and/or returns an internal folder to store a single deleted file. This folder is uniquely named and thus
   * prevents same-name siblings.
   * 
   * @param fileId id of file to delete
   */
  private Node getOrCreateTrashFileIdNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId) throws RepositoryException {
    final String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    final String folderName = prefix + ":" + fileId.toString(); //$NON-NLS-1$
    Node trashFolderIdNode = getOrCreateTrashFolderIdNode(session, pentahoJcrConstants, JcrRepositoryFileUtils
        .getParentId(session, fileId));
    if (trashFolderIdNode.hasNode(folderName)) {
      return trashFolderIdNode.getNode(folderName);
    } else {
      return trashFolderIdNode.addNode(folderName, pentahoJcrConstants.getPHO_NT_INTERNALFOLDER());
    }
  }

  /**
   * Creates and/or returns an internal folder to store all files deleted from a given folder. Provides fast access when 
   * searching for files deleted from a given folder.
   * 
   * @param folderId id of folder that is parent of file to delete
   */
  private Node getOrCreateTrashFolderIdNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable folderId) throws RepositoryException {
    final String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    final String folderName = prefix + ":" + folderId.toString(); //$NON-NLS-1$
    Node trashInternalFolderNode = getOrCreateTrashInternalFolderNode(session, pentahoJcrConstants);
    if (trashInternalFolderNode.hasNode(folderName)) {
      return trashInternalFolderNode.getNode(folderName);
    } else {
      return trashInternalFolderNode.addNode(folderName, pentahoJcrConstants.getPHO_NT_INTERNALFOLDER());
    }
  }

  /**
   * Creates and/or returns an internal folder called {@code .trash} located just below the user's home folder.
   */
  private Node getOrCreateTrashInternalFolderNode(final Session session, final PentahoJcrConstants pentahoJcrConstants)
      throws RepositoryException {
    Node userHomeFolderNode = (Node) session.getItem(RepositoryPaths.getUserHomeFolderPath());
    if (userHomeFolderNode.hasNode(FOLDER_NAME_TRASH)) {
      return userHomeFolderNode.getNode(FOLDER_NAME_TRASH);
    } else {
      return userHomeFolderNode.addNode(FOLDER_NAME_TRASH, pentahoJcrConstants.getPHO_NT_INTERNALFOLDER());
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFile> getDeletedFiles(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable folderId) throws RepositoryException {
    List<RepositoryFile> deletedFiles = new ArrayList<RepositoryFile>();
    Node trashFolderIdNode = getOrCreateTrashFolderIdNode(session, pentahoJcrConstants, folderId);
    NodeIterator nodes = trashFolderIdNode.getNodes();
    while (nodes.hasNext()) {
      Node trashFileIdNode = nodes.nextNode();
      if (trashFileIdNode.getNodes().hasNext()) {
        // each fileId node has at most one child that is the deleted file
        deletedFiles.add(JcrRepositoryFileUtils.nodeToFile(session, pentahoJcrConstants, ownerLookupHelper,
            trashFileIdNode.getNodes().nextNode()));
      }
    }
    return deletedFiles;
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFile> getDeletedFiles(final Session session, final PentahoJcrConstants pentahoJcrConstants)
      throws RepositoryException {
    final String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    List<RepositoryFile> deletedFiles = new ArrayList<RepositoryFile>();
    Node trashNode = getOrCreateTrashInternalFolderNode(session, pentahoJcrConstants);
    NodeIterator nodes = trashNode.getNodes();
    while (nodes.hasNext()) {
      Node trashFolderIdNode = nodes.nextNode();
      // strip off the prefix and colon
      final String folderId = trashFolderIdNode.getName().substring(prefix.length() + 1);
      deletedFiles.addAll(getDeletedFiles(session, pentahoJcrConstants, folderId));
    }
    return deletedFiles;
  }

  /**
   * {@inheritDoc}
   */
  public void permanentlyDeleteFile(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId) throws RepositoryException {
    Assert.notNull(fileId);
    JcrRepositoryFileUtils.permanentlyDeleteFile(session, pentahoJcrConstants, fileId, lockTokenHelper);
  }

  /**
   * {@inheritDoc}
   */
  public void undeleteFile(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId) throws RepositoryException {
    final String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    Node fileToUndeleteNode = session.getNodeByUUID(fileId.toString());
    // determine original location
    Node trashFolderIdNode = fileToUndeleteNode.getParent().getParent();
    // strip off the prefix and colon
    final String folderId = trashFolderIdNode.getName().substring(prefix.length() + 1);
    Node originalParentFolderNode = session.getNodeByUUID(folderId);
    session.move(fileToUndeleteNode.getPath(), originalParentFolderNode.getPath() + RepositoryFile.SEPARATOR
        + fileToUndeleteNode.getName());
  }

  /**
   * {@inheritDoc}
   */
  public Serializable getOriginalParentFolderId(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId) throws RepositoryException {
    final String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS);
    Node fileToUndeleteNode = session.getNodeByUUID(fileId.toString());
    // determine original location
    Node trashFolderIdNode = fileToUndeleteNode.getParent().getParent();
    // strip off the prefix and colon
    return trashFolderIdNode.getName().substring(prefix.length() + 1);
  }

}
