/*
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.platform.plugin.services.security.userrole.ldap;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LdapUserDetailsServiceMessages {
  private static final String BUNDLE_NAME = "org.pentaho.platform.engine.security.messages.ldapuserdetailsservice_messages"; //$NON-NLS-1$

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
      .getBundle(LdapUserDetailsServiceMessages.BUNDLE_NAME);

  private LdapUserDetailsServiceMessages() {
  }

  public static String getString(final String key) {
    try {
      return LdapUserDetailsServiceMessages.RESOURCE_BUNDLE.getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
}
