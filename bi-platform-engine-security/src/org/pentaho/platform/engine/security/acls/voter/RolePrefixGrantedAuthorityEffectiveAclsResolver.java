package org.pentaho.platform.engine.security.acls.voter;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.acl.AclEntry;
import org.springframework.security.acl.basic.BasicAclEntry;
import org.springframework.security.acl.basic.EffectiveAclsResolver;
import org.springframework.security.userdetails.UserDetails;

/**
 * Copy-and-paste of GrantedAuthorityEffectiveAclsResolver except that a role prefix is prepended to recipient before 
 * comparison.
 */
public class RolePrefixGrantedAuthorityEffectiveAclsResolver implements EffectiveAclsResolver {
  //~ Static fields/initializers =====================================================================================

  private static final Log logger = LogFactory.getLog(RolePrefixGrantedAuthorityEffectiveAclsResolver.class);

  private static String rolePrefix;

  static {
    rolePrefix = PentahoSystem.getSystemSetting("role-prefix", "ROLE_");//$NON-NLS-1$ //$NON-NLS-2$
  }

  //~ Methods ========================================================================================================

  public AclEntry[] resolveEffectiveAcls(AclEntry[] allAcls, Authentication filteredBy) {
    if ((allAcls == null) || (allAcls.length == 0)) {
      return null;
    }

    List list = new Vector();

    if (logger.isDebugEnabled()) {
      logger.debug("Locating AclEntry[]s (from set of " + ((allAcls == null) ? 0 : allAcls.length)
          + ") that apply to Authentication: " + filteredBy);
    }

    for (int i = 0; i < allAcls.length; i++) {
      if (!(allAcls[i] instanceof BasicAclEntry)) {
        continue;
      }

      Object recipient = ((BasicAclEntry) allAcls[i]).getRecipient();

      // Allow the Authentication's getPrincipal to decide whether
      // the presented recipient is "equal" (allows BasicAclDaos to
      // return Strings rather than proper objects in simple cases)
      if (filteredBy.getPrincipal().equals(recipient)) {
        if (logger.isDebugEnabled()) {
          logger.debug("Principal matches AclEntry recipient: " + recipient);
        }

        list.add(allAcls[i]);
      } else if (filteredBy.getPrincipal() instanceof UserDetails
          && ((UserDetails) filteredBy.getPrincipal()).getUsername().equals(recipient)) {
        if (logger.isDebugEnabled()) {
          logger.debug("Principal (from UserDetails) matches AclEntry recipient: " + recipient);
        }

        list.add(allAcls[i]);
      } else {
        // No direct match against principal; try each authority.
        // As with the principal, allow each of the Authentication's
        // granted authorities to decide whether the presented
        // recipient is "equal"
        GrantedAuthority[] authorities = filteredBy.getAuthorities();

        if ((authorities == null) || (authorities.length == 0)) {
          if (logger.isDebugEnabled()) {
            logger.debug("Did not match principal and there are no granted authorities, "
                + "so cannot compare with recipient: " + recipient);
          }

          continue;
        }

        for (int k = 0; k < authorities.length; k++) {
          if (authorities[k].equals(rolePrefix + recipient)) {
            if (logger.isDebugEnabled()) {
              logger.debug("GrantedAuthority: " + authorities[k] + " matches recipient: " + recipient);
            }

            list.add(allAcls[i]);
          }
        }
      }
    }

    // return null if appropriate (as per interface contract)
    if (list.size() > 0) {
      if (logger.isDebugEnabled()) {
        logger.debug("Returning effective AclEntry array with " + list.size() + " elements");
      }

      return (BasicAclEntry[]) list.toArray(new BasicAclEntry[] {});
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("Returning null AclEntry array as zero effective AclEntrys found");
      }

      return null;
    }
  }
}
