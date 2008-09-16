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
 */
package org.pentaho.platform.engine.security;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.acegisecurity.GrantedAuthorityImpl;
import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionMgr;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;

public class AcegiPermissionMgr implements IPermissionMgr {

  private static final AcegiPermissionMgr singletonPermMgr = new AcegiPermissionMgr();

  private AcegiPermissionMgr() {
  }

  public static AcegiPermissionMgr instance() {
    return AcegiPermissionMgr.singletonPermMgr;
  }

  public Map<IPermissionRecipient, IPermissionMask> getPermissions(final Object object) {
    IAclHolder aclHolder = (IAclHolder) object;
    List<IPentahoAclEntry> aclList = aclHolder.getAccessControls();
    Map<IPermissionRecipient, IPermissionMask> permissionsMap = new LinkedHashMap<IPermissionRecipient, IPermissionMask>();
    for (Object element : aclList) {
      IPentahoAclEntry pentahoAclEntry = (IPentahoAclEntry) element;
      IPermissionRecipient permissionRecipient = null;
      if (pentahoAclEntry.getRecipient() instanceof GrantedAuthorityImpl) {
        GrantedAuthorityImpl grantedAuthorityImpl = (GrantedAuthorityImpl) pentahoAclEntry.getRecipient();
        permissionRecipient = new SimpleRole(grantedAuthorityImpl.toString());
      } else if (pentahoAclEntry.getRecipient() instanceof SimpleRole) {
        permissionRecipient = new SimpleRole((String) pentahoAclEntry.getRecipient());
      } else {
        permissionRecipient = new SimpleUser((String) pentahoAclEntry.getRecipient());
      }
      IPermissionMask permissionMask = new SimplePermissionMask(pentahoAclEntry.getMask());
      permissionsMap.put(permissionRecipient, permissionMask);
    }
    return permissionsMap;
  }

  public boolean hasPermission(final IPermissionRecipient permissionRecipient, final IPermissionMask permissionMask,
      final Object object) {
    if (object == null || !(object instanceof IAclHolder)) {
	  // i would argue that the "object" parameter should be IAclHolder!
      return true;
    }
    IAclHolder aclHolder = (IAclHolder) object;
    IPentahoSession session = null;
    boolean isPermitted = false;
    // For PermissionRecipient being on SimpleSession we will get the session from the recipient and create voter and let the voter
    if (permissionRecipient instanceof SimpleSession) {
      SimpleSession simpleSession = (SimpleSession) permissionRecipient;
      session = simpleSession.getSession();
      IAclVoter voter = PentahoSystem.getAclVoter(session);
      int aclMask = permissionMask.getMask();
      if (aclMask == IPentahoAclEntry.PERM_ADMINISTRATION) {
        isPermitted = voter.isPentahoAdministrator(session);
      } else {
        // TODO mlowery Voter only needs Authentication. Why pass in IPentahoSession?
        isPermitted = voter.hasAccess(session, aclHolder, aclMask);
      }
    } else { // Otherwise we will get the permission for the object and check if object has permission or not
      Map<IPermissionRecipient, IPermissionMask> map = getPermissions(aclHolder);
      IPermissionMask mask = map.get(permissionRecipient);
      if (mask != null) {
        PentahoAclEntry pentahoAclEntry = new PentahoAclEntry();
        if (permissionRecipient instanceof SimpleRole) {
          pentahoAclEntry.setRecipient(new GrantedAuthorityImpl(permissionRecipient.getName()));
        } else {
          pentahoAclEntry.setRecipient(permissionRecipient.getName());
        }
        pentahoAclEntry.addPermission(mask.getMask());
        isPermitted = pentahoAclEntry.isPermitted(permissionMask.getMask());
      }
    }
    return isPermitted;
  }

  public void setPermission(final IPermissionRecipient permissionRecipient, final IPermissionMask permission,
      final Object object) {
    if (object == null || !(object instanceof IAclHolder)) {
      // i would argue that the "object" parameter should be IAclHolder!
      return;
    }
	  IAclHolder aclHolder = (IAclHolder) object;
    PentahoAclEntry entry = new PentahoAclEntry();
    // TODO mlowery instanceof is undesirable as it doesn't allow new concrete classes.    
    if (permissionRecipient instanceof SimpleRole) {
      entry.setRecipient(new GrantedAuthorityImpl(permissionRecipient.getName()));
    } else {
      entry.setRecipient(permissionRecipient.getName());
    }
    entry.addPermission(permission.getMask());
    //    HibernateUtil.beginTransaction(); - This is now handled by the RepositoryFile
    aclHolder.getAccessControls().add(entry);
    //    HibernateUtil.commitTransaction(); - This should be covered by the exitPoint call
  }

  public void setPermissions(final Map<IPermissionRecipient, IPermissionMask> permissionsMap, final Object object) {
    if (object == null || !(object instanceof IAclHolder)) {
      // i would argue that the "object" parameter should be IAclHolder!
      return;
    }
    IAclHolder aclHolder = (IAclHolder) object;
    Set<Map.Entry<IPermissionRecipient, IPermissionMask>> mapEntrySet = permissionsMap.entrySet();
    ArrayList<PentahoAclEntry> aclList = new ArrayList<PentahoAclEntry>();
    for (Entry<IPermissionRecipient, IPermissionMask> mapEntry : mapEntrySet) {
      PentahoAclEntry pentahoAclEntry = new PentahoAclEntry();
      IPermissionRecipient permissionRecipient = mapEntry.getKey();
      if (permissionRecipient instanceof SimpleRole) {
        pentahoAclEntry.setRecipient(new GrantedAuthorityImpl(permissionRecipient.getName()));
      } else {
        pentahoAclEntry.setRecipient(permissionRecipient.getName());
      }
      pentahoAclEntry.addPermission(mapEntry.getValue().getMask());
      aclList.add(pentahoAclEntry);
    }
    //    HibernateUtil.beginTransaction(); - This is now handled in the RepositoryFile
    aclHolder.resetAccessControls(aclList);
    //    HibernateUtil.commitTransaction(); - This is covered by the exitPoint
  }

}
