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
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.test.platform.engine.core;

import junit.framework.TestCase;

import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.solution.ActionInfo.ActionInfoParseException;

@SuppressWarnings({"all"})
public class ActionInfoTest extends TestCase {

  public void testConstructor() {
    ActionInfo info = new ActionInfo("solution", "path", "action");

    assertEquals("solution is wrong", "solution", info.getSolutionName());
    assertEquals("pth is wrong", "path", info.getPath());
    assertEquals("action is wrong", "action", info.getActionName());
  }

  public void testParsing() {

    ActionInfo info = ActionInfo.parseActionString("solution/path/action");
    assertNotNull("action info should not be null", info);
    assertEquals("solution is wrong", "solution", info.getSolutionName());
    assertEquals("path is wrong", "path", info.getPath());
    assertEquals("action is wrong", "action", info.getActionName());

    // test removal of leading '/'
    info = ActionInfo.parseActionString("/solution/path/action");
    assertNotNull("action info should not be null", info);
    assertEquals("solution is wrong", "solution", info.getSolutionName());
    assertEquals("path is wrong", "path", info.getPath());
    assertEquals("action is wrong", "action", info.getActionName());

    info = ActionInfo.parseActionString("solution", false);
    assertNotNull("action info should not be null", info);
    assertEquals("solution is wrong", "solution", info.getSolutionName());
    assertEquals("path is wrong", null, info.getPath());
    assertEquals("action is wrong", null, info.getActionName());

    info = ActionInfo.parseActionString("solution/action", true);
    assertNotNull("action info should not be null", info);
    assertEquals("solution is wrong", "solution", info.getSolutionName());
    // JD - this is inconsistent
    assertEquals("path is wrong", "", info.getPath());
    assertEquals("action is wrong", "action", info.getActionName());

    info = ActionInfo.parseActionString("solution/action.xaction", true);
    assertNotNull("action info should not be null", info);
    assertEquals("solution is wrong", "solution", info.getSolutionName());
    // JD - this is inconsistent
    assertEquals("path is wrong", "", info.getPath());
    assertEquals("action is wrong", "action.xaction", info.getActionName());

    info = ActionInfo.parseActionString("solution/action.xaction", false);
    assertNotNull("action info should not be null", info);
    assertEquals("solution is wrong", "solution", info.getSolutionName());
    // JD - this is inconsistent
    assertEquals("path is wrong", "", info.getPath());
    assertEquals("action is wrong", "action.xaction", info.getActionName());

    info = ActionInfo.parseActionString("solution/path", false);
    assertNotNull("action info should not be null", info);
    assertEquals("solution is wrong", "solution", info.getSolutionName());
    assertEquals("path is wrong", "path", info.getPath());
    assertEquals("action is wrong", null, info.getActionName());

    info = ActionInfo.parseActionString("solution/path/path", false);
    assertNotNull("action info should not be null", info);
    assertEquals("solution is wrong", "solution", info.getSolutionName());
    assertEquals("path is wrong", "path/path", info.getPath());
    assertEquals("action is wrong", null, info.getActionName());

    info = ActionInfo.parseActionString("solution/path/action", true);
    assertNotNull("action info should not be null", info);
    assertEquals("solution is wrong", "solution", info.getSolutionName());
    assertEquals("path is wrong", "path", info.getPath());
    assertEquals("action is wrong", "action", info.getActionName());

    info = ActionInfo.parseActionString("solution/path/action.xaction", true);
    assertNotNull("action info should not be null", info);
    assertEquals("solution is wrong", "solution", info.getSolutionName());
    assertEquals("path is wrong", "path", info.getPath());
    assertEquals("action is wrong", "action.xaction", info.getActionName());

    info = ActionInfo.parseActionString("solution/path/action.xaction", false);
    assertNotNull("action info should not be null", info);
    assertEquals("solution is wrong", "solution", info.getSolutionName());
    assertEquals("path is wrong", "path", info.getPath());
    assertEquals("action is wrong", "action.xaction", info.getActionName());

  }

  public void testBadParsing() {

    ActionInfo info = ActionInfo.parseActionString(null);
    assertNull("action info should be null", info);

    info = ActionInfo.parseActionString("solution", true);
    assertNull("action info should be null", info);

    info = ActionInfo.parseActionString("", true);
    assertNull("action info should be null", info);

  }

  public void testToString() {

    ActionInfo info = new ActionInfo("solution", "path", "action");
    assertEquals("toString is invalid", "solution/path/action", info.toString());

  }

  public void testBuildSolutionPath() {

    String str = ActionInfo.buildSolutionPath("solution", "path", "action");
    assertEquals("buildSolutionPath is invalid", "solution/path/action", str);

    str = ActionInfo.buildSolutionPath("solution", null, "action");
    assertEquals("buildSolutionPath is invalid", "solution/action", str);

    str = ActionInfo.buildSolutionPath("solution", "/", "action");
    assertEquals("buildSolutionPath is invalid", "solution/action", str);

    str = ActionInfo.buildSolutionPath("solution", null, "/action");
    assertEquals("buildSolutionPath is invalid", "solution/action", str);

    str = ActionInfo.buildSolutionPath("solution", "", "/action");
    assertEquals("buildSolutionPath is invalid", "solution/action", str);

    str = ActionInfo.buildSolutionPath("solution", "", "solution");
    assertEquals("buildSolutionPath is invalid", "solution/", str);

    str = ActionInfo.buildSolutionPath("solution", "/path", "action");
    assertEquals("buildSolutionPath is invalid", "solution/path/action", str);

    str = ActionInfo.buildSolutionPath("solution", "/path", "/action");
    assertEquals("buildSolutionPath is invalid", "solution/path/action", str);

    str = ActionInfo.buildSolutionPath("solution", "path", "action");
    assertEquals("buildSolutionPath is invalid", "solution/path/action", str);

    str = ActionInfo.buildSolutionPath("solution", "path", "/action");
    assertEquals("buildSolutionPath is invalid", "solution/path/action", str);

  }

  public void testActionInfoParseException() {
    
    ActionInfoParseException e = new ActionInfoParseException();
    assertEquals( "wrong message", null, e.getMessage() );
    assertNull( "wrong cause", e.getCause() );
    
    e = new ActionInfoParseException( "test message" );
    assertEquals( "wrong message", "test message", e.getMessage() );
    assertNull( "wrong cause", e.getCause() );
    
    Throwable t = new IllegalArgumentException("test cause");
    
    e = new ActionInfoParseException( t );
    assertEquals( "wrong message", "java.lang.IllegalArgumentException: test cause", e.getMessage() );
    assertEquals( "wrong cause", t, e.getCause() );
    
    e = new ActionInfoParseException( "test message", t );
    assertEquals( "wrong message", "test message", e.getMessage() );
    assertEquals( "wrong cause", t, e.getCause() );
    
    
  }
}
