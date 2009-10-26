package org.pentaho.platform.repository.pcr.springsecurity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.acls.AclService;
import org.springframework.security.acls.Permission;
import org.springframework.security.afterinvocation.AclEntryAfterInvocationProvider;
import org.springframework.util.Assert;

/**
 * Calls {@link AclEntryAfterInvocationProvider#decide(Authentication, Object, ConfigAttributeDefinition, Object)}. If
 * an <code>AccessDeniedException</code> is thrown, check if the user has been granted the authority 
 * <code>adminAuthority</code>. If granted, return the object as superclass would. Otherwise, throw original exception.
 * 
 * @author mlowery
 */
public class AdminAclEntryAfterInvocationProvider extends AclEntryAfterInvocationProvider {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(AdminAclEntryAfterInvocationProvider.class);

  // ~ Instance fields =================================================================================================

  private String adminAuthority;

  // ~ Constructors ====================================================================================================

  public AdminAclEntryAfterInvocationProvider(final AclService aclService, final String processConfigAttribute,
      final Permission[] requirePermission, final String adminAuthority) {
    super(aclService, requirePermission);
    Assert.hasText(adminAuthority);
    Assert.hasText(processConfigAttribute);
    this.adminAuthority = adminAuthority;
    setProcessConfigAttribute(processConfigAttribute);
  }

  // ~ Methods =========================================================================================================

  @Override
  public Object decide(final Authentication authentication, final Object object,
      final ConfigAttributeDefinition config, Object returnedObject) throws AccessDeniedException {
    Object result = null;
    try {
      result = super.decide(authentication, object, config, returnedObject);
    } catch (AccessDeniedException e) {
      if (!isAdmin(authentication)) {
        throw e;
      }
      if (logger.isDebugEnabled()) {
        logger
            .debug("AclEntryAfterInvocationProvider denied access however principal is an administrator; allowing access");
      }
    }
    return result;
  }

  /**
   * Returns <code>true</code> if principal in given <code>authentication</code> has been granted 
   * <code>adminAuthority</code>.
   * @param authentication authentication to check
   * @return <code>true</code> if admin
   */
  private boolean isAdmin(final Authentication authentication) {
    GrantedAuthority[] authorities = authentication.getAuthorities();

    for (int i = 0; i < authorities.length; i++) {
      if (authorities[i].getAuthority().equals(adminAuthority)) {
        return true;
      }
    }

    return false;
  }

}
