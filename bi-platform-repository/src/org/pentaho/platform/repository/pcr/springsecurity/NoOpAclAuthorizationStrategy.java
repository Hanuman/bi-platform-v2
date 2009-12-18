package org.pentaho.platform.repository.pcr.springsecurity;

import org.springframework.security.acls.Acl;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;

/**
 * An {@link AclAuthorizationStrategy} that never throws an {@code AccessDeniedException}. 
 * 
 * <p>
 * {@code AclAuthorizationStrategy} is used by {@code AclImpl} for access control when modifying the ACL itself. This
 * implementation of {@code AclAuthorizationStrategy} is applicable when the datastore itself enforces ACL 
 * modifications.
 * </p>
 * 
 * @author mlowery
 */
public class NoOpAclAuthorizationStrategy implements AclAuthorizationStrategy {

  public void securityCheck(final Acl acl, final int changeType) {

  }

}
