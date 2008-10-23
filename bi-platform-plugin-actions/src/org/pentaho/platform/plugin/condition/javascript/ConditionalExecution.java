/*
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created
 * @author
 */
package org.pentaho.platform.plugin.condition.javascript;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IConditionalExecution;
import org.pentaho.platform.plugin.services.connections.javascript.JavaScriptResultSet;

public class ConditionalExecution implements IConditionalExecution {

  private String script;

  public ConditionalExecution() {
    super();
  }

  public String getScript() {
    return script;
  }

  public void setScript(final String script) {
    this.script = script;
  }

  public boolean shouldExecute(final Map currentInputs, final Log logger) throws Exception {
    boolean shouldExecute = true;
    Context cx = ContextFactory.getGlobal().enterContext();
    try {
      ScriptableObject scriptable = new RhinoScriptable();
      // initialize the standard javascript objects
      Scriptable scope = cx.initStandardObjects(scriptable);
      ScriptableObject.defineClass(scope, JavaScriptResultSet.class);
      Object inputValue;
      IActionParameter inputParameter;
      String inputName;
      Iterator inputs = currentInputs.entrySet().iterator();
      Map.Entry mapEntry;
      while (inputs.hasNext()) {
        mapEntry = (Map.Entry) inputs.next();
        inputName = (String) mapEntry.getKey();
        if (inputName.indexOf('-') >= 0) {
          logger.info("Ignoring Input: " + inputName); //$NON-NLS-1$
          continue;
        }
        inputParameter = (IActionParameter) mapEntry.getValue();
        inputValue = inputParameter.getValue();

        Object wrapper;
        if (inputValue instanceof IPentahoResultSet) {
          JavaScriptResultSet results = new JavaScriptResultSet();
          
          //Required as of Rhino 1.7R1 to resolve caching, base object 
          //inheritance and property tree
          results.setPrototype(scriptable);
          
          results.setResultSet((IPentahoResultSet) inputValue);
          wrapper = Context.javaToJS(inputValue, results);
        } else {
          wrapper = Context.javaToJS(inputValue, scope);
        }
        ScriptableObject.putProperty(scope, inputName, wrapper);
      }
      Object wrappedOut = Context.javaToJS(System.out, scope);
      Object wrappedThis = Context.javaToJS(this, scope);
      ScriptableObject.putProperty(scope, "out", wrappedOut); //$NON-NLS-1$
      ScriptableObject.putProperty(scope, "rule", wrappedThis); //$NON-NLS-1$

      // evaluate the script
      Object resultObject = cx.evaluateString(scope, script, "<cmd>", 1, null); //$NON-NLS-1$

      Object actualObject = null;
      if (resultObject instanceof org.mozilla.javascript.NativeJavaObject) {
        actualObject = ((org.mozilla.javascript.NativeJavaObject) resultObject).unwrap();
      } else {
        actualObject = resultObject;
      }
      if (actualObject instanceof Boolean) {
        return ((Boolean) actualObject).booleanValue();
      } else if (actualObject instanceof String) {
        return ("true".equalsIgnoreCase(actualObject.toString())) || ("yes".equalsIgnoreCase(actualObject.toString())); //$NON-NLS-1$ //$NON-NLS-2$
      } else if (actualObject instanceof Number) {
        return ((Number) actualObject).intValue() > 0;
      } else if (actualObject instanceof IPentahoResultSet) {
        return ((IPentahoResultSet) actualObject).getRowCount() > 0;
      }

      //    } catch (Exception e) {
      //      logger.error("Error executing conditional execution script.", e);
    } finally {
      Context.exit();
    }
    return shouldExecute;
  }

}
