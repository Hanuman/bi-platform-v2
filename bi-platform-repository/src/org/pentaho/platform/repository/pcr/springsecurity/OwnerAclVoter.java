package org.pentaho.platform.repository.pcr.springsecurity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.Authentication;
import org.springframework.security.AuthorizationServiceException;
import org.springframework.security.ConfigAttribute;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.acls.Acl;
import org.springframework.security.acls.AclService;
import org.springframework.security.acls.NotFoundException;
import org.springframework.security.acls.Permission;
import org.springframework.security.acls.objectidentity.ObjectIdentity;
import org.springframework.security.acls.objectidentity.ObjectIdentityRetrievalStrategy;
import org.springframework.security.acls.objectidentity.ObjectIdentityRetrievalStrategyImpl;
import org.springframework.security.acls.sid.PrincipalSid;
import org.springframework.security.acls.sid.Sid;
import org.springframework.security.acls.sid.SidRetrievalStrategy;
import org.springframework.security.acls.sid.SidRetrievalStrategyImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.vote.AbstractAclVoter;
import org.springframework.security.vote.AccessDecisionVoter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Copy-and-paste of <code>AclEntryVoter</code>. The difference here is that if <code>grantIfOwner</code> is set to 
 * <code>true</code>, then a decision of <code>AccessDecisionVoter.ACCESS_GRANTED</code> will be returned if the 
 * principal is the owner, regardless of the result of {@link Acl#isGranted(Permission[], Sid[], boolean)}.
 * 
 * @author mlowery
 */
public class OwnerAclVoter extends AbstractAclVoter {
  //~ Static fields/initializers =====================================================================================

  private static final Log logger = LogFactory.getLog(OwnerAclVoter.class);

  //~ Instance fields ================================================================================================

  private AclService aclService;

  private ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy = new ObjectIdentityRetrievalStrategyImpl();

  private SidRetrievalStrategy sidRetrievalStrategy = new SidRetrievalStrategyImpl();

  private String internalMethod;

  private String processConfigAttribute;

  private Permission[] requirePermission;

  private boolean grantIfOwner;

  //~ Constructors ===================================================================================================

  public OwnerAclVoter(AclService aclService, String processConfigAttribute, Permission[] requirePermission) {
    Assert.notNull(processConfigAttribute, "A processConfigAttribute is mandatory");
    Assert.notNull(aclService, "An AclService is mandatory");

    if ((requirePermission == null) || (requirePermission.length == 0)) {
      throw new IllegalArgumentException("One or more requirePermission entries is mandatory");
    }

    this.aclService = aclService;
    this.processConfigAttribute = processConfigAttribute;
    this.requirePermission = requirePermission;
  }

  //~ Methods ========================================================================================================

  /**
   * Optionally specifies a method of the domain object that will be used to obtain a contained domain
   * object. That contained domain object will be used for the ACL evaluation. This is useful if a domain object
   * contains a parent that an ACL evaluation should be targeted for, instead of the child domain object (which
   * perhaps is being created and as such does not yet have any ACL permissions)
   *
   * @return <code>null</code> to use the domain object, or the name of a method (that requires no arguments) that
   *         should be invoked to obtain an <code>Object</code> which will be the domain object used for ACL
   *         evaluation
   */
  protected String getInternalMethod() {
    return internalMethod;
  }

  public void setInternalMethod(String internalMethod) {
    this.internalMethod = internalMethod;
  }

  protected String getProcessConfigAttribute() {
    return processConfigAttribute;
  }

  public void setObjectIdentityRetrievalStrategy(ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy) {
    Assert.notNull(objectIdentityRetrievalStrategy, "ObjectIdentityRetrievalStrategy required");
    this.objectIdentityRetrievalStrategy = objectIdentityRetrievalStrategy;
  }

  public void setSidRetrievalStrategy(SidRetrievalStrategy sidRetrievalStrategy) {
    Assert.notNull(sidRetrievalStrategy, "SidRetrievalStrategy required");
    this.sidRetrievalStrategy = sidRetrievalStrategy;
  }

  public boolean supports(ConfigAttribute attribute) {
    if ((attribute.getAttribute() != null) && attribute.getAttribute().equals(getProcessConfigAttribute())) {
      return true;
    } else {
      return false;
    }
  }

  public int vote(Authentication authentication, Object object, ConfigAttributeDefinition config) {
    Iterator iter = config.getConfigAttributes().iterator();

    while (iter.hasNext()) {
      ConfigAttribute attr = (ConfigAttribute) iter.next();

      if (!this.supports(attr)) {
        continue;
      }
      // Need to make an access decision on this invocation
      // Attempt to locate the domain object instance to process
      Object domainObject = getDomainObjectInstance(object);

      // If domain object is null, vote to abstain
      if (domainObject == null) {
        if (logger.isDebugEnabled()) {
          logger.debug("Voting to abstain - domainObject is null");
        }

        return AccessDecisionVoter.ACCESS_ABSTAIN;
      }

      // Evaluate if we are required to use an inner domain object
      if (StringUtils.hasText(internalMethod)) {
        try {
          Class clazz = domainObject.getClass();
          Method method = clazz.getMethod(internalMethod, new Class[0]);
          domainObject = method.invoke(domainObject, new Object[0]);
        } catch (NoSuchMethodException nsme) {
          throw new AuthorizationServiceException("Object of class '" + domainObject.getClass()
              + "' does not provide the requested internalMethod: " + internalMethod);
        } catch (IllegalAccessException iae) {
          logger.debug("IllegalAccessException", iae);

          throw new AuthorizationServiceException("Problem invoking internalMethod: " + internalMethod
              + " for object: " + domainObject);
        } catch (InvocationTargetException ite) {
          logger.debug("InvocationTargetException", ite);

          throw new AuthorizationServiceException("Problem invoking internalMethod: " + internalMethod
              + " for object: " + domainObject);
        }
      }

      // Obtain the OID applicable to the domain object
      ObjectIdentity objectIdentity = objectIdentityRetrievalStrategy.getObjectIdentity(domainObject);

      // Obtain the SIDs applicable to the principal
      Sid[] sids = sidRetrievalStrategy.getSids(authentication);

      Acl acl;

      try {
        // Lookup only ACLs for SIDs we're interested in
        acl = aclService.readAclById(objectIdentity, sids);
      } catch (NotFoundException nfe) {
        if (logger.isDebugEnabled()) {
          logger.debug("Voting to deny access - no ACLs apply for this principal");
        }

        return AccessDecisionVoter.ACCESS_DENIED;
      }

      try {
        if (acl.isGranted(requirePermission, sids, false)) {
          if (logger.isDebugEnabled()) {
            logger.debug("Voting to grant access");
          }

          return AccessDecisionVoter.ACCESS_GRANTED;
        } else {

          // begin mlowery edits

          if (isGrantIfOwner() && acl.getOwner().equals(new PrincipalSid(getUsername()))) {
            if (logger.isDebugEnabled()) {
              logger.debug("Voting to grant access - principal is owner");
            }

            return AccessDecisionVoter.ACCESS_GRANTED;
          }

          // end mlowery edits

          if (logger.isDebugEnabled()) {
            logger.debug("Voting to deny access - ACLs returned, but insufficient permissions for this principal");
          }

          return AccessDecisionVoter.ACCESS_DENIED;
        }
      } catch (NotFoundException nfe) {
        if (logger.isDebugEnabled()) {
          logger.debug("Voting to deny access - no ACLs apply for this principal");
        }

        return AccessDecisionVoter.ACCESS_DENIED;
      }
    }

    // No configuration attribute matched, so abstain
    return AccessDecisionVoter.ACCESS_ABSTAIN;
  }

  private String getUsername() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth.getPrincipal() instanceof UserDetails) {
      return ((UserDetails) auth.getPrincipal()).getUsername();
    } else {
      return auth.getPrincipal().toString();
    }
  }

  public boolean isGrantIfOwner() {
    return grantIfOwner;
  }

  public void setGrantIfOwner(boolean grantIfOwner) {
    this.grantIfOwner = grantIfOwner;
  }
}
