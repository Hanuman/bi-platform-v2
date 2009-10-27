/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.engine.security;

import java.security.Principal;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IAclSolutionFile;
import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

/**
 * A utility class with several static methods that are used to
 * either bind the <tt>Authentication</tt> to the <tt>IPentahoSession</tt>, retrieve
 * the <tt>Authentication</tt> from the <tt>IPentahoSession</tt>, and other various helper
 * functions.
 * @author mbatchel
 *
 */

public class SecurityHelper {

  private static final Log logger = LogFactory.getLog(SecurityHelper.class);

  public static final String SESSION_PRINCIPAL = "SECURITY_PRINCIPAL"; //$NON-NLS-1$

  public static final String DefaultAnonymousRole = PentahoSystem.getSystemSetting(
      "anonymous-authentication/anonymous-role", "Anonymous"); //$NON-NLS-1$ //$NON-NLS-2$

  public static final String DefaultAnonymousUser = PentahoSystem.getSystemSetting(
      "anonymous-authentication/anonymous-user", "anonymous"); //$NON-NLS-1$ //$NON-NLS-2$
  
  public static final String DefaultRolePrefix = PentahoSystem.getSystemSetting("role-prefix", "ROLE_"); //$NON-NLS-1$ //$NON-NLS-2$

  /**
   * Looks in the provided session to get the Spring Security Authentication object out.
   * Optionally returns an "anonymous" Authentication if desired.
   * @param session Users' IPentahoSession object
   * @param allowAnonymous If true, will return an anonymous Authentication object.
   * @return the Authentication object from the session
   */
  public static Authentication getAuthentication(final IPentahoSession session, final boolean allowAnonymous) {
    Principal principal = (Principal) session.getAttribute(SecurityHelper.SESSION_PRINCIPAL);
    if (SecurityHelper.logger.isDebugEnabled()) {
      SecurityHelper.logger.debug("principal from IPentahoSession: " + principal); //$NON-NLS-1$
      if (null != principal) {
        SecurityHelper.logger.debug("principal class: " + principal.getClass().getName()); //$NON-NLS-1$
      }
    }
    if (principal instanceof Authentication) {
      if (SecurityHelper.logger.isDebugEnabled()) {
        SecurityHelper.logger.debug("principal is an instance of Authentication"); //$NON-NLS-1$
      }
      return (Authentication) principal;
    } else if (principal != null) {
      if (SecurityHelper.logger.isDebugEnabled()) {
        SecurityHelper.logger.debug("principal is not an instance of Authentication"); //$NON-NLS-1$
        SecurityHelper.logger.debug("attempting role fetch with username"); //$NON-NLS-1$
      }

      // OK - Not Spring Security somehow.
      // However, since the principal interface doesn't specify the
      // roles a user is in, we need to dispatch a call to the
      // UserRoleListProvider to get that information from there.

      IUserDetailsRoleListService roleListService = PentahoSystem.getUserDetailsRoleListService();
      List roles = roleListService.getRolesForUser(principal.getName());
      if (SecurityHelper.logger.isDebugEnabled()) {
        SecurityHelper.logger.debug("rolesForUser from roleListService:" + roles); //$NON-NLS-1$
      }
      if (!roles.isEmpty()) {
        GrantedAuthority[] grantedAuthorities = new GrantedAuthority[roles.size()];
        for (int i = 0; i < roles.size(); i++) {
          grantedAuthorities[i] = new GrantedAuthorityImpl((String) roles.get(i));
        }

        Authentication auth = new UsernamePasswordAuthenticationToken(principal.getName(), null, grantedAuthorities);

        return auth;
      }
    }
    if (SecurityHelper.logger.isDebugEnabled()) {
      SecurityHelper.logger.debug("either principal is null or user has no roles"); //$NON-NLS-1$
    }

    if (allowAnonymous) {
      if (SecurityHelper.logger.isDebugEnabled()) {
        SecurityHelper.logger.debug("there is no principal in IPentahoSession"); //$NON-NLS-1$
        SecurityHelper.logger.debug("creating token with username anonymous and role Anonymous"); //$NON-NLS-1$
      }
      // Hmmm - at this point, we're being asked for an authentication on
      // an un-authenticated user. For now, we'll default to returning
      // an authentication that has the user as anonymous.
      Authentication auth = new UsernamePasswordAuthenticationToken(SecurityHelper.DefaultAnonymousUser, null,
          new GrantedAuthorityImpl[] { new GrantedAuthorityImpl(SecurityHelper.DefaultAnonymousRole + SecurityHelper.DefaultAnonymousRole) });
      return auth;
    } else {
      if (SecurityHelper.logger.isDebugEnabled()) {
        SecurityHelper.logger.debug("there is no principal in IPentahoSession"); //$NON-NLS-1$
        SecurityHelper.logger.debug("and allowAnonymous is false"); //$NON-NLS-1$
      }
      // If we're here - we require a properly authenticated user and
      // there's nothing
      // else we can do aside from returning null.
      return null;
    }
  }

  /**
   * Gets the java.security.principal object from the IPentahoSession object
   * @param session The users' session
   * @return The bound Principal
   */
  public static Principal getPrincipal(final IPentahoSession session) {
    Principal principal = (Principal) session.getAttribute(SecurityHelper.SESSION_PRINCIPAL);
    return principal;
  }

  /**
   * Sets the java.security.principal object into the IPentahoSession object.
   * @param principal The principal from the servlet context
   * @param session The users' IPentahoSession object
   */
  public static void setPrincipal(final Principal principal, final IPentahoSession session) {
    session.setAttribute(SecurityHelper.SESSION_PRINCIPAL, principal);
  }

  /**
   * Utility method that communicates with the installed ACLVoter to determine
   * administrator status
   * @param session The users IPentahoSession object
   * @return true if the user is considered a Pentaho administrator
   */
  public static boolean isPentahoAdministrator(final IPentahoSession session) {
    IAclVoter voter = PentahoSystem.get(IAclVoter.class, session);
    return voter.isPentahoAdministrator(session);
  }

  /**
   * Utility method that communicates with the installed ACLVoter to determine
   * whether a particular role is granted to the specified user.
   * @param session The users' IPentahoSession
   * @param role The role to look for
   * @return true if the user is granted the specified role.
   */
  public static boolean isGranted(final IPentahoSession session, final GrantedAuthority role) {
    IAclVoter voter = PentahoSystem.get(IAclVoter.class, session);
    return voter.isGranted(session, role);
  }

  /**
   * @param aFile
   * @return a boolean that indicates if this file can have ACLS placed on it.
   */
  public static boolean canHaveACLS(final ISolutionFile aFile) {
    if (aFile.isDirectory()) { // All Directories can have ACLS
      return true;
    }

    // Otherwise anything in the PentahoSystem extension list.
    return PentahoSystem.getACLFileExtensionList().contains(aFile.getExtension());
  }

  public static boolean hasAccess(final IAclHolder aHolder, final int actionOperation, final IPentahoSession session) {
    IAclVoter voter = PentahoSystem.get(IAclVoter.class, session);
    int aclMask = -1;

    switch (actionOperation) {
      case (IAclHolder.ACCESS_TYPE_READ): {
        aclMask = IPentahoAclEntry.PERM_EXECUTE;
        break;
      }
      case IAclHolder.ACCESS_TYPE_WRITE:
      case IAclHolder.ACCESS_TYPE_UPDATE: {
        aclMask = IPentahoAclEntry.PERM_UPDATE;
        break;
      }
      case IAclHolder.ACCESS_TYPE_DELETE: {
        aclMask = IPentahoAclEntry.PERM_DELETE;
        break;
      }
      case IAclHolder.ACCESS_TYPE_ADMIN: {
        aclMask = IPentahoAclEntry.PERM_ADMINISTRATION;
        break;
      }
      default: {
        aclMask = IPentahoAclEntry.PERM_EXECUTE;
        break;
      }

    }
    return voter.hasAccess(session, aHolder, aclMask);
  }

  /**
   * Utility method for access negotiation. For performance, not all files will
   * be checked against the supplied voter.
   * @param aFile
   * @param actionOperation
   * @param session
   * @return
   */
  public static boolean hasAccess(final IAclSolutionFile aFile, final int actionOperation, final IPentahoSession session) {
    if (aFile == null) {
      return false;
    }
    if (!aFile.isDirectory()) {
      List extensionList = PentahoSystem.getACLFileExtensionList();
      String fName = aFile.getFileName();
      int posn = fName.lastIndexOf('.');
      if (posn >= 0) {
        if (extensionList.indexOf(fName.substring(posn)) < 0) {
          // Non-acl'd file. Return true.
          return true;
        }
      } else {
        // Untyped file. Allow access.
        return true;
      }
    }
    IAclVoter voter = PentahoSystem.get(IAclVoter.class, session);
    int aclMask = -1;
    switch (actionOperation) {
      case ISolutionRepository.ACTION_EXECUTE: {
        aclMask = IPentahoAclEntry.PERM_EXECUTE;
        break;
      }
      case ISolutionRepository.ACTION_ADMIN: {
        // aclMask = PentahoAclEntry.ADMINISTRATION;
        // break;
        return SecurityHelper.isPentahoAdministrator(session);
      }
      case ISolutionRepository.ACTION_SUBSCRIBE: {
        aclMask = IPentahoAclEntry.PERM_SUBSCRIBE;
        break;
      }
      case ISolutionRepository.ACTION_CREATE: {
        aclMask = IPentahoAclEntry.PERM_CREATE;
        break;
      }
      case ISolutionRepository.ACTION_UPDATE: {
        aclMask = IPentahoAclEntry.PERM_UPDATE;
        break;
      }
      case ISolutionRepository.ACTION_DELETE: {
        aclMask = IPentahoAclEntry.PERM_DELETE;
        break;
      }
      case ISolutionRepository.ACTION_SHARE: {
        aclMask = IPentahoAclEntry.PERM_UPDATE_PERMS;
        break;
      }
      default: {
        aclMask = IPentahoAclEntry.PERM_EXECUTE;
        break;
      }
    }
    return voter.hasAccess(session, aFile, aclMask);
  }

}
