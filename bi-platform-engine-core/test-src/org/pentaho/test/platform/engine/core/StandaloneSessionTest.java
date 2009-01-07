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
 *
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jan 6, 2009
 * @author James Dixon
 */
package org.pentaho.test.platform.engine.core;

import java.util.Locale;

import org.pentaho.platform.engine.core.system.StandaloneSession;

import junit.framework.TestCase;

public class StandaloneSessionTest extends TestCase {

  public void testDefaultConstructor() {
    
    StandaloneSession session = new StandaloneSession( ); 
    
    assertEquals( "session name is wrong", "unknown", session.getName() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "session id is wrong", "unknown", session.getId() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "session locale is wrong", Locale.getDefault(), session.getLocale() ); //$NON-NLS-1$
    
    // make sure this does not blow up
    session.destroy();
    
    assertEquals( "session object name is wrong", StandaloneSession.class.getName(), session.getObjectName() ); //$NON-NLS-1$
  }
  
  public void testNameConstructor() {
    
    StandaloneSession session = new StandaloneSession( "testname" );  //$NON-NLS-1$
    
    assertEquals( "session name is wrong", "testname", session.getName() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "session id is wrong", "testname", session.getId() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "session locale is wrong", Locale.getDefault(), session.getLocale() ); //$NON-NLS-1$
  }
  
  public void testIdConstructor() {
    
    StandaloneSession session = new StandaloneSession( "testname", "testid" );  //$NON-NLS-1$ //$NON-NLS-2$
    
    assertEquals( "session name is wrong", "testname", session.getName() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "session id is wrong", "testid", session.getId() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "session locale is wrong", Locale.getDefault(), session.getLocale() ); //$NON-NLS-1$
  }

  public void testConstructor() {
    
    StandaloneSession session = new StandaloneSession( "testname", "testid", Locale.CHINESE );  //$NON-NLS-1$ //$NON-NLS-2$
    
    assertEquals( "session name is wrong", "testname", session.getName() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "session id is wrong", "testid", session.getId() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "session locale is wrong", Locale.CHINESE, session.getLocale() ); //$NON-NLS-1$
  }

  public void testAttributes() {

    StandaloneSession session = new StandaloneSession( ); 
    assertFalse( "Wrong attributes", session.getAttributeNames().hasNext() ); //$NON-NLS-1$
    
    session.setAttribute( "testattribute", this ); //$NON-NLS-1$
    assertTrue( "Wrong attributes", session.getAttributeNames().hasNext() ); //$NON-NLS-1$
    assertEquals( "Wrong attribute name", "testattribute", session.getAttributeNames().next() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "Wrong attribute value", this, session.getAttribute("testattribute") ); //$NON-NLS-1$ //$NON-NLS-2$
    
    session.removeAttribute( "testattribute" ); //$NON-NLS-1$
    assertFalse( "Wrong attributes", session.getAttributeNames().hasNext() ); //$NON-NLS-1$
    assertNull( "Wrong attribute value", session.getAttribute("testattribute") ); //$NON-NLS-1$ //$NON-NLS-2$
    
  }
  
  public void testLogger() {

    StandaloneSession session = new StandaloneSession( ); 
    assertNotNull( "Bad logger", session.getLogger() ); //$NON-NLS-1$    
  }
  
  public void testAuthenticated() {

    StandaloneSession session = new StandaloneSession( "testname" );  //$NON-NLS-1$
    assertFalse( "Wrong authenication", session.isAuthenticated() ); //$NON-NLS-1$

    session.setAuthenticated( null );
    assertFalse( "Wrong authenication", session.isAuthenticated() ); //$NON-NLS-1$
    
    session.setAuthenticated( "testname" ); //$NON-NLS-1$
    assertTrue( "Wrong authenication", session.isAuthenticated() ); //$NON-NLS-1$

    session.setNotAuthenticated();
    assertNull( "session name is wrong", session.getName() ); //$NON-NLS-1$
    assertFalse( "Wrong authenication", session.isAuthenticated() ); //$NON-NLS-1$
  
  }
  
  public void testActionProcess() {
    
    StandaloneSession session = new StandaloneSession( ); 
    assertEquals( "Wrong action name", "", session.getActionName() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "Wrong process id", null, session.getProcessId() ); //$NON-NLS-1$
    
    session.setActionName( "testaction" ); //$NON-NLS-1$
    session.setProcessId( "testprocess" ); //$NON-NLS-1$
    assertEquals( "Wrong action name", "testaction", session.getActionName() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "Wrong process id", "testprocess", session.getProcessId() ); //$NON-NLS-1$ //$NON-NLS-2$
    
  }
  
  public void testBackgroundAlert() {
    
    StandaloneSession session = new StandaloneSession( ); 
    assertFalse( "Wrong alert", session.getBackgroundExecutionAlert() ); //$NON-NLS-1$ 

    session.setBackgroundExecutionAlert();
    assertTrue( "Wrong alert", session.getBackgroundExecutionAlert() ); //$NON-NLS-1$ 

    session.resetBackgroundExecutionAlert();
    assertFalse( "Wrong alert", session.getBackgroundExecutionAlert() ); //$NON-NLS-1$ 
    
  }
  
}
