/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 3 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * Created Apr 18, 2006
 *
 * @author mbatchel
 */
package org.pentaho.test.platform.security;

import org.pentaho.test.platform.engine.core.BaseTest;
import org.springframework.context.support.GenericApplicationContext;

public class BaseSecurityTestImpl extends BaseTest {

  GenericApplicationContext applContext;

  public BaseSecurityTestImpl(String arg0) {
    super(arg0);
  }

  public BaseSecurityTestImpl() {
    super();
  }

  public void setUp() {
    super.setUp();
    applContext = MockSecurityUtility.setupApplicationContext();
  }

  public void tearDown() {
    super.tearDown();
    applContext.close();
  }

  public GenericApplicationContext getApplicationContext() {
    return this.applContext;
  }

}
