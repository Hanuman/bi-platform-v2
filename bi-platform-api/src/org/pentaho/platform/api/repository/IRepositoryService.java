package org.pentaho.platform.api.repository;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;

/**
 * Entry point into the content repository.
 * 
 * @author mlowery
 */
public interface IRepositoryService {

  /**
   * Gets file. Use this method to test for file existence too.
   * 
   * @param absPath absolute path to file
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFile getFile(final String absPath);

  RepositoryFile getFileById(final Serializable fileId);

  /**
   * Same as {@link #getFile(String)} except that if {@code loadMaps} is {@code true}, the maps for localized strings 
   * will be loaded as well. (Normally these are not loaded.) Use {@code true} in editing tools that can show the maps
   * for editing purposes.
   * 
   * @param absPath absolute path to file
   * @param loadMaps {@code true} to load localized string maps
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFile getFile(final String absPath, final boolean loadMaps);

  RepositoryFile getFileById(final Serializable fileId, final boolean loadMaps);

  /**
   * Gets data at base version for read.
   * 
   * @param fileId file id
   * @param dataClass class that implements {@link IRepositoryFileData}
   * @return data
   */
  <T extends IRepositoryFileData> T getDataForRead(final Serializable fileId, final Class<T> dataClass);

  /**
   * Gets data at given version for read.
   * 
   * @param fileId file id
   * @param versionId version id
   * @param dataClass class that implements {@link IRepositoryFileData}
   * @return data
   */
  <T extends IRepositoryFileData> T getDataForRead(final Serializable fileId, final Serializable versionId,
      final Class<T> dataClass);

  /**
   * Gets data at base version for execute.
   * 
   * @param fileId file id
   * @param dataClass class that implements {@link IRepositoryFileData}
   * @return data
   */
  <T extends IRepositoryFileData> T getDataForExecute(final Serializable fileId, final Class<T> dataClass);

  /**
   * Gets data at given version for read.
   * 
   * @param fileId file id
   * @param versionId version id
   * @param dataClass class that implements {@link IRepositoryFileData}
   * @return data
   */
  <T extends IRepositoryFileData> T getDataForExecute(final Serializable fileId, final Serializable versionId,
      final Class<T> dataClass);

  /**
   * Creates a file.
   * 
   * @param parentFolderAbsPath parent folder absolute path
   * @param file file to create
   * @param content file content
   * @param versionMessageAndLabel optional version comment [0] and label [1] to be applied to parentFolder
   * @return file that is equal to given file except with id populated
   */
  RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData content, final String... versionMessageAndLabel);

  /**
   * Creates a folder.
   * 
   * @param parentFolderAbsPath parent folder absolute path
   * @param file file to create
   * @param versionMessageAndLabel optional version comment [0] and label [1] to be applied to parentFolder
   * @return file that is equal to given file except with id populated
   */
  RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file,
      final String... versionMessageAndLabel);

  /**
   * Returns the children of this folder.
   * 
   * @param folderAbsPath absolute path of folder whose children to fetch
   * @return list of children (never {@code null})
   */
  List<RepositoryFile> getChildren(final Serializable folderId);

  /**
   * Updates a file and/or the content of a file.
   * 
   * @param file updated file (not a folder); must have non-null id
   * @param content updated content
   * @param versionMessageAndLabel optional version comment [0] and label [1]
   * @return updated file (possible with new version number)
   */
  RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData content,
      final String... versionMessageAndLabel);

  /**
   * Deletes a file or folder.
   * 
   * @param absPath file to delete
   * @param versionMessageAndLabel optional version comment [0] and label [1]
   */
  void deleteFile(final Serializable fileId, final String... versionMessageAndLabel);

  // ~ Lock methods ====================================================================================================

  /**
   * Locks a file.
   * 
   * @param absPath absolute path to file
   * @param lock message
   */
  void lockFile(final Serializable fileId, final String message);

  /**
   * Unlocks a file.
   * 
   * @param absPath absolute path to file
   */
  void unlockFile(final Serializable fileId);

  // ~ Access read/write methods =======================================================================================

  /**
   * Returns ACL for file.
   * 
   * @param absPath absolute path to file
   * @return access control list
   */
  RepositoryFileAcl getAcl(final Serializable fileId);

  /**
   * Updates an ACL.
   * 
   * @param acl ACL to set; must have non-null id
   */
  void updateAcl(final RepositoryFileAcl acl);

  /**
   * Returns {@code true} if user has all permissions given.
   * 
   * @param absPath absolute path to file or folder
   * @param permissions permissions to check
   * @return {@code true} if user has all permissions given
   */
  boolean hasAccess(final String absPath, final EnumSet<RepositoryFilePermission> permissions);

  /**
   * Returns the list of access control entries that will be used to make an access control decision.
   * 
   * @param absPath absolute path to file
   * @return list of ACEs
   */
  List<RepositoryFileAcl.Ace> getEffectiveAces(final Serializable fileId);

  // ~ Version methods =================================================================================================

  /**
   * Returns a list of version summary instances. The first version in the list is the root version. The last version
   * in the list is the base version. Branching and merging are not supported so this is a simple list.
   * 
   * @param absPath absolute path to file
   * @return list of version summaries (never {@code null})
   */
  List<VersionSummary> getVersionSummaries(final Serializable fileId);

  /**
   * Gets file as it was at the given version. Use this method to test for file existence too.
   * 
   * @param final String absPath
   * @param versionId version id
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFile getFile(final Serializable fileId, final Serializable versionId);

  /**
   * Returns the associated {@link IRepositoryEventHandler}.
   * @return repository event handler
   */
  IRepositoryEventHandler getRepositoryEventHandler();

  /**
   * Handles various events like startup and new user. 
   * 
   * <p>
   * Methods in this class are not called by the {@link IRepositoryService} implementation; they must be called 
   * by an external caller. A caller can get a reference to the {@link IRepositoryEventHandler} by calling 
   * {@link IRepositoryService#getRepositoryEventHandler()}. Methods should be able to be called more than once 
   * with the same arguments with no adverse effects.
   * </p>
   * 
   * <p>
   * Example: When a servlet-based application starts up, a {@code ServletContextListener} calls {@link #onStartup()}. 
   * When a user logs in, {@link #onNewTenant(String)} and {@link #onNewUser(String)} are called. Finally, the 
   * {@code ServletContextListener} calls {@link #onShutdown()}.
   * </p>
   */
  interface IRepositoryEventHandler {

    /**
     * To be called before any users call into the {@link IRepositoryService}.
     */
    void onStartup();

    /**
     * To be called on repository shutdown.
     */
    void onShutdown();

    /**
     * To be called before any users belonging to a particular tenant call into the {@link IRepositoryService}.
     * @param tenantId new tenant id
     */
    void onNewTenant(final String tenantId);

    /**
     * To be called before any users belonging to the current tenant call into the {@link IRepositoryService}. 
     */
    void onNewTenant();

    /**
     * To be called before user indicated by {@code username} calls into the {@link IRepositoryService}.
     * @param tenantId tenant to which the user belongs
     * @param username new username
     */
    void onNewUser(final String tenantId, final String username);

    /**
     * To be called before current user calls into the {@link IRepositoryService}.
     */
    void onNewUser();
  }
}
