package org.pentaho.platform.repository.pcr.springsecurity;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IPentahoContentDao;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttribute;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.SpringSecurityMessageSource;
import org.springframework.security.acls.Acl;
import org.springframework.security.acls.AclService;
import org.springframework.security.acls.NotFoundException;
import org.springframework.security.acls.Permission;
import org.springframework.security.acls.objectidentity.ObjectIdentity;
import org.springframework.security.acls.sid.Sid;
import org.springframework.security.afterinvocation.AbstractAclProvider;
import org.springframework.security.afterinvocation.AclEntryAfterInvocationProvider;
import org.springframework.util.Assert;

public class TraverseFolderAclEntryAfterInvocationProvider extends AbstractAclProvider implements MessageSourceAware {
  //~ Static fields/initializers =====================================================================================

  protected static final Log logger = LogFactory.getLog(AclEntryAfterInvocationProvider.class);

  //~ Instance fields ================================================================================================

  protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

  private String adminAuthority;

  private IPentahoContentDao contentDao;

  //~ Constructors ===================================================================================================

  public TraverseFolderAclEntryAfterInvocationProvider(final AclService aclService,
      final String processConfigAttribute, final Permission[] requirePermission, final String adminAuthority,
      final IPentahoContentDao contentDao) {
    super(aclService, processConfigAttribute, requirePermission);
    Assert.hasText(adminAuthority);
    Assert.hasText(processConfigAttribute);
    Assert.notNull(contentDao);
    this.adminAuthority = adminAuthority;
    this.contentDao = contentDao;
  }

  //~ Methods ========================================================================================================

  public Object decide(Authentication authentication, Object object, ConfigAttributeDefinition config,
      Object returnedObject) throws AccessDeniedException {

    Iterator iter = config.getConfigAttributes().iterator();

    if (returnedObject == null) {
      // AclManager interface contract prohibits nulls
      // As they have permission to null/nothing, grant access
      if (logger.isDebugEnabled()) {
        logger.debug("Return object is null, skipping");
      }

      return null;
    }

    if (!getProcessDomainObjectClass().isAssignableFrom(returnedObject.getClass())) {
      if (logger.isDebugEnabled()) {
        logger.debug("Return object is not applicable for this provider, skipping");
      }

      return returnedObject;
    }

    while (iter.hasNext()) {
      ConfigAttribute attr = (ConfigAttribute) iter.next();

      if (!this.supports(attr)) {
        continue;
      }

      Assert.isInstanceOf(RepositoryFile.class, returnedObject);

      // Need to make an access decision on this invocation

      if (hasExecutePermissionOnParentFolder(authentication, (RepositoryFile) returnedObject)) {
        return returnedObject;
      } else if (isAdmin(authentication)) {
        if (logger.isDebugEnabled()) {
          logger.debug("principal is an administrator; allowing access");
        }
        return returnedObject;
      }

      logger.debug("Denying access");

      throw new AccessDeniedException(messages.getMessage("BasicAclEntryAfterInvocationProvider.noPermission",
          new Object[] { authentication.getName(), returnedObject },
          "Authentication {0} does not have permission to traverse to the domain object {1}"));
    }

    return returnedObject;
  }

  /**
   * Returns {@code true} if {@code Authentication} has execute access to all parent folders of given 
   * {@code file}. (Recursively calls itself to find the answer.)
   * @param authentication authentication to check for access
   * @param file file to check for access
   * @return {@code true} if has access
   */
  private boolean hasExecutePermissionOnParentFolder(final Authentication authentication, final RepositoryFile file) {
    if (file.getParentId() == null) {
      return true;
    }
    RepositoryFile parentFolder = contentDao.getFile(file.getAbsolutePath().substring(0,
        file.getAbsolutePath().lastIndexOf(RepositoryFile.SEPARATOR)));
    return hasExecutePermission(authentication, parentFolder)
        && hasExecutePermissionOnParentFolder(authentication, parentFolder);
  }

  protected boolean hasExecutePermission(Authentication authentication, Object domainObject) {
    // Obtain the OID applicable to the domain object
    ObjectIdentity objectIdentity = objectIdentityRetrievalStrategy.getObjectIdentity(domainObject);

    // Obtain the SIDs applicable to the principal
    Sid[] sids = sidRetrievalStrategy.getSids(authentication);

    Acl acl = null;

    try {
      // Lookup only ACLs for SIDs we're interested in
      acl = aclService.readAclById(objectIdentity, sids);

      return acl.isGranted(requirePermission, sids, false);
    } catch (NotFoundException ignore) {
      return false;
    }
  }

  public void setMessageSource(MessageSource messageSource) {
    this.messages = new MessageSourceAccessor(messageSource);
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
