package org.pentaho.platform.api.repository;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;

/**
 * Entry point into the unified repository. The finest grained object that can be read and written to this repository
 * is a {@link RepositoryFile}.
 * 
 * @author mlowery
 */
public interface IUnifiedRepository {

  /**
   * Gets file. Use this method to test for file existence too.
   * 
   * @param absPath absolute path to file
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFile getFile(final String absPath);

  /**
   * Gets file. Use this method to test for file existence too.
   * 
   * @param fileId file id
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFile getFileById(final Serializable fileId);

  /**
   * Same as {@link #getFile(String)} except that if {@code loadMaps} is {@code true}, the maps for localized strings 
   * will be loaded as well. (Normally these are not loaded.) Use {@code true} in editing tools that can show the maps
   * for editing purposes.
   * 
   * @param absPath absolute path to file
   * @param loadLocaleMaps {@code true} to load localized string maps
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFile getFile(final String absPath, final boolean loadLocaleMaps);

  /**
   * Same as {@link #getFile(String)} except that if {@code loadMaps} is {@code true}, the maps for localized strings 
   * will be loaded as well. (Normally these are not loaded.) Use {@code true} in editing tools that can show the maps
   * for editing purposes.
   * 
   * @param fileId file id
   * @param loadLocaleMaps {@code true} to load localized string maps
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFile getFileById(final Serializable fileId, final boolean loadLocaleMaps);

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
   * @param parentFolderId parent folder id
   * @param file file to create
   * @param data file data
   * @param versionMessage optional version comment to be applied to parentFolder
   * @return file that is equal to given file except with id populated
   */
  RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final String versionMessage);

  /**
   * Creates a folder.
   * 
   * @param parentFolderId parent folder id
   * @param file file to create
   * @param versionMessage optional version comment to be applied to parentFolder
   * @return file that is equal to given file except with id populated
   */
  RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file,
      final String versionMessage);

  /**
   * Returns the children of this folder.
   * 
   * @param folderId id of folder whose children to fetch
   * @return list of children (never {@code null})
   */
  List<RepositoryFile> getChildren(final Serializable folderId);
  
  /**
   * Returns the children of this folder that match the specified filter.
   * 
   * @param folderId id of folder whose children to fetch
   * @param filter filter may be a full name or a partial name with one or more wildcard characters ("*"), or a 
   * disjunction (using the "|" character to represent logical OR) of these
   * @return list of children (never {@code null})
   */
  List<RepositoryFile> getChildren(final Serializable folderId, final String filter);

  /**
   * Updates a file and/or the data of a file.
   * 
   * @param file updated file (not a folder); must have non-null id
   * @param data updated data
   * @param versionMessageoptional version comment
   * @return updated file (possible with new version number)
   */
  RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData data,
      final String versionMessage);

  /**
   * Deletes a file.
   * 
   * @param fileId file id
   * @param permanent if {@code true}, once file is deleted, it cannot be undeleted
   * @param versionMessage optional version comment
   */
  void deleteFile(final Serializable fileId, final boolean permanent, final String versionMessage);

  /**
   * Deletes a file in a recoverable manner.
   * 
   * @param fileId file id
   * @param versionMessage optional version comment
   */
  void deleteFile(final Serializable fileId, final String versionMessage);
  
  /**
   * Recovers a deleted file if it was not permanently deleted. File is recovered to its original folder.
   * 
   * @param fileId deleted file id
   * @param versionMessage optional version comment to be applied to original parent folder
   */
  void undeleteFile(final Serializable fileId, final String versionMessage);
  
  /**
   * Gets all deleted files for the current user in this folder.
   * 
   * @param folderId folder id
   * @return list of deleted files
   */
  List<RepositoryFile> getDeletedFiles(final Serializable folderId);
  
  /**
   * Gets all deleted files for the current user in this folder.
   * 
   * @param folderId folder id
   * @param filter filter may be a full name or a partial name with one or more wildcard characters ("*"), or a 
   * disjunction (using the "|" character to represent logical OR) of these
   * @return list of deleted files
   */
  List<RepositoryFile> getDeletedFiles(final Serializable folderId, final String filter);
  
  /**
   * Gets all deleted files for the current user. This is the "recycle bin" view.
   * 
   * @return list of deleted files
   */
  List<RepositoryFile> getDeletedFiles();

  /**
   * Moves and/or renames file.
   * 
   * @param fileId if of file or folder to move and/or rename
   * @param destAbsPath absolute path to destination; if only moving then destAbsPath will be an existing path
   * @param versionMessageAndLabel optional version comment to be applied to source and destination parent folders
   */
  void moveFile(final Serializable fileId, final String destAbsPath, final String versionMessage);
  
  // ~ Lock methods ====================================================================================================

  /**
   * Locks a file.
   * 
   * @param fileId file id
   * @param lock message
   */
  void lockFile(final Serializable fileId, final String message);

  /**
   * Unlocks a file.
   * 
   * @param fileId file id
   */
  void unlockFile(final Serializable fileId);

  // ~ Access read/write methods =======================================================================================

  /**
   * Returns ACL for file.
   * 
   * @param fileId file id
   * @return access control list
   */
  RepositoryFileAcl getAcl(final Serializable fileId);

  /**
   * Updates an ACL.
   * 
   * @param acl ACL to set; must have non-null id
   * @return updated ACL as it would be if calling {@link #getAcl(Serializable)}
   */
  RepositoryFileAcl updateAcl(final RepositoryFileAcl acl);

  /**
   * Returns {@code true} if user has all permissions given.
   * 
   * @param absPath absolute path to file or folder
   * @param permissions permissions to check
   * @return {@code true} if user has all permissions given
   */
  boolean hasAccess(final String absPath, final EnumSet<RepositoryFilePermission> permissions);

  /**
   * Returns the list of access control entries (ACEs) that will be used to make an access control decision.
   * 
   * @param fileId file id
   * @return list of ACEs
   */
  List<RepositoryFileAce> getEffectiveAces(final Serializable fileId);

  // ~ Version methods =================================================================================================

  /**
   * Returns a version summary for the given file id and version id.
   * 
   * @param fileId file id
   * @param versionId version id (if {@code null}, returns the last version)
   * @return version summary
   */
  VersionSummary getVersionSummary(Serializable fileId, Serializable versionId);
  
  /**
   * Returns a list of version summary instances. The first version in the list is the root version. The last version
   * in the list is the base version. Branching and merging are not supported so this is a simple list.
   * 
   * @param fileId file id
   * @return list of version summaries (never {@code null})
   */
  List<VersionSummary> getVersionSummaries(final Serializable fileId);
  
  /**
   * Gets file as it was at the given version.
   * 
   * @param fileId file id
   * @param versionId version id
   * @return file at version
   */
  RepositoryFile getFile(final Serializable fileId, final Serializable versionId);

  /**
   * Returns the associated {@link IRepositoryLifecycleManager}.
   * @return repository event handler
   */
  IRepositoryLifecycleManager getRepositoryLifecycleManager();

  /**
   * Allows external code to alert the repository of lifecycle events like startup and new user.
   * 
   * <p>
   * Methods in this class are not called by the {@link IUnifiedRepository} implementation; they must be called 
   * by an external caller. A caller can get a reference to the {@link IRepositoryLifecycleManager} by calling 
   * {@link IUnifiedRepository#getRepositoryLifecycleManager()}. Methods should be able to be called more than once 
   * with the same arguments with no adverse effects.
   * </p>
   * 
   * <p>
   * Example: When a servlet-based application starts up, a {@code ServletContextListener} calls {@link #startup()}. 
   * When a user logs in, {@link #newTenant(String)} and {@link #onNewUser(String)} are called. Finally, the 
   * {@code ServletContextListener} calls {@link #shutdown()}.
   * </p>
   */
  interface IRepositoryLifecycleManager {

    /**
     * To be called before any users call into the {@link IUnifiedRepository}.
     */
    void startup();

    /**
     * To be called on repository shutdown.
     */
    void shutdown();

    /**
     * To be called before any users belonging to a particular tenant call into the {@link IUnifiedRepository}.
     * @param tenantId new tenant id
     */
    void newTenant(final String tenantId);

    /**
     * To be called before any users belonging to the current tenant call into the {@link IUnifiedRepository}. 
     */
    void newTenant();

    /**
     * To be called before user indicated by {@code username} calls into the {@link IUnifiedRepository}.
     * @param tenantId tenant to which the user belongs
     * @param username new username
     */
    void newUser(final String tenantId, final String username);

    /**
     * To be called before current user calls into the {@link IUnifiedRepository}.
     */
    void newUser();
  }

}
