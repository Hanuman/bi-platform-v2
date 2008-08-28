package org.pentaho.test.platform.engine.core;

import junit.framework.TestCase;

import org.pentaho.platform.engine.core.solution.ActionInfo;

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

  }

}
