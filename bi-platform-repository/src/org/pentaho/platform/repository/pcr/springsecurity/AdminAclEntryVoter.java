package org.pentaho.platform.repository.pcr.springsecurity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.acls.AclService;
import org.springframework.security.acls.Permission;
import org.springframework.security.vote.AccessDecisionVoter;
import org.springframework.security.vote.AclEntryVoter;
import org.springframework.util.Assert;

public class AdminAclEntryVoter extends AclEntryVoter {
  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(AdminAclEntryVoter.class);

  // ~ Instance fields =================================================================================================

  private String adminAuthority;

  // ~ Constructors ====================================================================================================

  public AdminAclEntryVoter(final AclService aclService, final String processConfigAttribute,
      final Permission[] requirePermission, final String adminAuthority) {
    super(aclService, processConfigAttribute, requirePermission);
    Assert.hasText(adminAuthority);
    this.adminAuthority = adminAuthority;
  }

  // ~ Methods =========================================================================================================

  @Override
  public int vote(Authentication authentication, Object object, ConfigAttributeDefinition config) {
    int vote = super.vote(authentication, object, config);
    if (vote == AccessDecisionVoter.ACCESS_DENIED) {
      if (isAdmin(authentication)) {
        if (logger.isDebugEnabled()) {
          logger.debug("AclEntryVoter denied access however principal is an administrator; allowing access");
        }
        return AccessDecisionVoter.ACCESS_GRANTED;
      }
    }
    return vote;
  }

  /**
   * Returns {@code true} if principal in given {@code authentication} has been granted 
   * {@code adminAuthority}.
   * @param authentication authentication to check
   * @return {@code true} if admin
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
