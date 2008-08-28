/*
 * Copyright 2007 - 2008 Pentaho Corporation.  All rights reserved.
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
package org.pentaho.platform.plugin.services.security.userrole.memory;

import org.acegisecurity.userdetails.memory.UserMap;
import org.acegisecurity.userdetails.memory.UserMapEditor;
import org.springframework.beans.factory.FactoryBean;

/**
 * Takes as input the string that defines a <code>UserMap</code>. When
 * Spring instantiates this bean, it outputs a
 * <code>UserMap</code>.
 *
 * <p>This class allows a string that defines a <code>UserMap</code> to be defined once in
 * Spring beans XML, then used by multiple client beans that need access to user to role mappings.</p>
 *
 * <p>This class is necessary
 * since <code>UserMap</code> does not define a constructor or setter
 * necessary to populate a <code>UserMap</code> bean, nor does it provide any
 * way to extract its mappings once created.</p>
 *
 * <p>Example usage:</p>
 *
 * <pre>
 *     &lt;bean id=&quot;userMap&quot; class=&quot;java.lang.String&quot;&gt;
 *       &lt;constructor-arg type=&quot;java.lang.String&quot;&gt;
 *         &lt;value&gt;
 *           &lt;![CDATA[
 *           joe=password,Admin,ceo,Authenticated
 *           ...
 *           ]]&gt;
 *         &lt;/value&gt;
 *       &lt;/constructor-arg&gt;
 *     &lt;/bean&gt;
 *
 *     &lt;bean id=&quot;userMapFactoryBean&quot;
 *       class=&quot;org.pentaho.security.UserMapFactoryBean&quot;&gt;
 *       &lt;property name=&quot;userMap&quot; ref=&quot;userMap&quot; /&gt;
 *     &lt;/bean&gt;
 *
 *     &lt;bean id=&quot;memoryAuthenticationDao&quot;
 *       class=&quot;org.acegisecurity.userdetails.memory.InMemoryDaoImpl&quot;&gt;
 *       &lt;property name=&quot;userMap&quot; ref=&quot;userMapFactoryBean&quot; /&gt;
 *     &lt;/bean&gt;
 * </pre>
 *
 * @author mlowery
 * @see UserRoleListEnhancedUserMapFactoryBean
 */
public class UserMapFactoryBean implements FactoryBean {
  /*
   * The user map text which will be processed by property editor.
   */
  private String userMap;

  public Object getObject() throws Exception {
    UserMapEditor userMapEditor = new UserMapEditor();
    userMapEditor.setAsText(userMap);
    return userMapEditor.getValue();
  }

  public Class getObjectType() {
    return UserMap.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void setUserMap(final String userMap) {
    this.userMap = userMap;
  }

}
