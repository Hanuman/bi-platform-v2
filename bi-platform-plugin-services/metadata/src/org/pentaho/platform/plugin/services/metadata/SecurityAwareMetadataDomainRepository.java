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
 * Copyright 2007 - 2008 Pentaho Corporation.  All rights reserved.
 *  
 */
package org.pentaho.platform.plugin.services.metadata;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.model.concept.security.RowLevelSecurity;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.util.RowLevelSecurityHelper;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.messages.Messages;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;

/**
 * This is the platform implementation which implements security.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 */
public class SecurityAwareMetadataDomainRepository extends MetadataDomainRepository {
  
  @Override
  public String generateRowLevelSecurityConstraint(LogicalModel model) {
    RowLevelSecurity rls = model.getRowLevelSecurity();
    if (rls == null || rls.getType() == RowLevelSecurity.Type.NONE) {
      return null;
    }
    Authentication auth = SecurityHelper.getAuthentication(getSession(), true);
    if (auth == null) {
      logger.info(Messages.getInstance().getString("SecurityAwareCwmSchemaFactory.INFO_AUTH_NULL_CONTINUE")); //$NON-NLS-1$
      return "FALSE()"; //$NON-NLS-1$
    }
    String username = auth.getName();
    List<String> roles = new ArrayList<String>();
    for (GrantedAuthority role : auth.getAuthorities()) {
      roles.add(role.getAuthority());
    }

    RowLevelSecurityHelper helper = new RowLevelSecurityHelper();
    return helper.getOpenFormulaSecurityConstraint(rls, username, roles);
  }
  
  @Override
  public boolean hasAccess(final int accessType, final IConcept aclHolder) {
    if (aclHolder != null) {
      MetadataAclHolder newHolder = new MetadataAclHolder(aclHolder);
      int mappedActionOperation = ACCESS_TYPE_MAP[accessType];
      return SecurityHelper.hasAccess(newHolder, mappedActionOperation, getSession());
    } else {
      if (accessType == IMetadataDomainRepository.ACCESS_TYPE_SCHEMA_ADMIN) {
        return SecurityHelper.isPentahoAdministrator(getSession());
      }
    }
    return true;
  }
}
