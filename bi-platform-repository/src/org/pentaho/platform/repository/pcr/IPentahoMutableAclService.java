package org.pentaho.platform.repository.pcr;

import org.springframework.security.acls.MutableAcl;
import org.springframework.security.acls.MutableAclService;
import org.springframework.security.acls.Permission;
import org.springframework.security.acls.objectidentity.ObjectIdentity;
import org.springframework.security.acls.sid.Sid;

/**
 * Extends {@link MutableAclService} adding some convenience methods.
 * 
 * @author mlowery
 */
public interface IPentahoMutableAclService extends MutableAclService {

  /**
   * Creates a new ACL and initializes it. {@link #updateAcl(MutableAcl)} should not need to be called after this method
   * returns.
   * 
   * <p>An example implementation might be: 
   * <ol>
   * <li>Call {@link #createAcl(ObjectIdentity)}.</li>
   * <li>Set parentAcl.</li>
   * <li>Set entriesInheriting.</li>
   * <li>Set owner.</li>
   * <li>Give full control to owner.</li>
   * <li>Call {@link #updateAcl(MutableAcl)}.</li>
   * </ol>
   * </p>
   * 
   * @param oid object identity
   * @param parentOid parent object identity or {@code null} if no parent
   * @param entriesInheriting {@code true} if this ACL should inherit ACEs from parent ACL
   * @param owner owner of the domain object associated with this ACL
   * @param allPermission permission representing all permissions (aka admin permission)
   * @return initialized ACL
   */
  MutableAcl createAndInitializeAcl(final ObjectIdentity oid, final ObjectIdentity parentOid,
      final boolean entriesInheriting, final Sid owner, final Permission allPermission);

  /**
   * Adds ACE to end of ACL. ACL should already have been created. {@link #updateAcl(MutableAcl)} should not need to be 
   * called after this method returns.
   * 
   * @param oid object identity
   * @param recipient recipient of permission
   * @param permission permission to set
   * @param granting {@code true} is this ACE is granting (as opposed to denying)
   */
  void addPermission(final ObjectIdentity oid, final Sid recipient, final Permission permission, final boolean granting);

  /**
   * Gives full control (all permissions) to given sid. {@link #updateAcl(MutableAcl)} should not need to be called 
   * after this method returns.
   * 
   * @param oid object identity
   * @param sid sid that should own the domain object associated with this ACL
   * @param permision permission representing full control
   */
  void setFullControl(final ObjectIdentity oid, final Sid sid, final Permission permission);
}
