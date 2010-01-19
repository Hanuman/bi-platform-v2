package org.pentaho.platform.repository.pcr;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;

import org.pentaho.platform.api.repository.RepositoryFileAcl;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.api.repository.RepositoryFileSid;

/**
 * A data access object for reading and writing {@code RepositoryFileAcl} instances. The methods in this interface might 
 * closely resemble those in {@link IUnifiedRepository} but this interface is not part of the public Pentaho API and
 * can evolve independently.
 * 
 * @author mlowery
 */
public interface IRepositoryFileAclDao {

  /**
   * Returns the list of access control entries that will be used to make an access control decision.
   * 
   * @param fileId file id
   * @return list of ACEs
   */
  List<RepositoryFileAcl.Ace> getEffectiveAces(final Serializable id);

  /**
   * Returns {@code true} if the user has all of the permissions. The implementation should return {@code false} if 
   * either the user does not have access or the file does not exist.
   * 
   * @param absPath absolute path to file
   * @param permissions permissions to check
   * @return {@code true} if user has access
   */
  boolean hasAccess(final String absPath, final EnumSet<RepositoryFilePermission> permissions);

  /**
   * Returns ACL for file.
   * 
   * @param fileId file id
   * @return access control list
   */
  RepositoryFileAcl getAcl(final Serializable id);

  /**
   * Updates an ACL.
   * 
   * @param acl ACL to set; must have non-null id
   * @return updated ACL
   */
  RepositoryFileAcl updateAcl(final RepositoryFileAcl acl);

  /**
   * Creates a new ACL and initializes it. {@link #updateAcl(RepositoryFileAcl)} should not need to be called after this method
   * returns.
   * 
   * <p>An example implementation might be: 
   * <ol>
   * <li>Call {@link #createAcl(Serializable)}.</li>
   * <li>Set parentAcl.</li>
   * <li>Set entriesInheriting.</li>
   * <li>Set owner.</li>
   * <li>Give full control to owner.</li>
   * <li>Call {@link #updateAcl(RepositoryFileAcl)}.</li>
   * </ol>
   * </p>
   * 
   * @param oid object identity
   * @param entriesInheriting {@code true} if this ACL should inherit ACEs from parent ACL
   * @param owner owner of the domain object associated with this ACL
   * @param allPermission permission representing all permissions (aka admin permission)
   * @return initialized ACL
   */
  RepositoryFileAcl createAcl(final Serializable id, final boolean entriesInheriting, final RepositoryFileSid owner,
      final RepositoryFilePermission allPermission);

  /**
   * Adds ACE to end of ACL. ACL should already have been created. {@link #updateAcl(RepositoryFileAcl)} should not need to be 
   * called after this method returns.
   * 
   * @param oid object identity
   * @param recipient recipient of permission
   * @param permission permission to set
   */
  void addPermission(final Serializable id, final RepositoryFileSid recipient,
      final EnumSet<RepositoryFilePermission> permission);

  /**
   * Gives full control (all permissions) to given sid. {@link #updateAcl(RepositoryFileAcl)} should not need to be called 
   * after this method returns.
   * 
   * @param oid object identity
   * @param sid sid that should own the domain object associated with this ACL
   * @param permision permission representing full control
   */
  void setFullControl(final Serializable id, RepositoryFileSid sid, final RepositoryFilePermission permission);

}
