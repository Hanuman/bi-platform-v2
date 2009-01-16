package org.pentaho.test.platform.engine.core;

import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.engine.IActionCompleteListener;
import org.pentaho.platform.api.engine.ICreateFeedbackParameterCallback;
import org.pentaho.platform.api.engine.IExecutionListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;

@SuppressWarnings({"all"})
public class TestSolutionEngine implements ISolutionEngine {

  public TestRuntimeContext testRuntime;
  
  public int executeCount = 0;
  
  public IPentahoSession initSession;
  
  public String solutionName;
  
  public String actionPath;
  
  public String actionName;
  
  public String errorMsg = null;
  
  public IRuntimeContext execute(IRuntimeContext runtime, String solutionName, String sequencePath,
      String sequenceName, String processId, boolean async, boolean instanceEnds, Map parameterProviderMap,
      IOutputHandler outputHandler) {
    // TODO Auto-generated method stub
    return null;
  }

  public IRuntimeContext execute(String actionSequenceXML, String sequenceName, String processId, boolean async,
      boolean instanceEnds, String instanceId, boolean persisted, Map parameterProviderMap,
      IOutputHandler outputHandler, IActionCompleteListener listener, IPentahoUrlFactory urlFactory, List messages) {
    // TODO Auto-generated method stub
    return null;
  }

  public IRuntimeContext execute(String solutionName, String actionPath, String actionName, String processId,
      boolean async, boolean instanceEnds, String instanceId, boolean persisted, Map parameterProviderMap,
      IOutputHandler outputHandler, IActionCompleteListener listener, IPentahoUrlFactory urlFactory, List messages) {
    this.solutionName = solutionName;
    this.actionPath = actionPath;
    this.actionName = actionName;
    executeCount++;
    if( errorMsg != null ) {
      throw new RuntimeException( errorMsg );
    }
    return testRuntime;
    
  }

  public IRuntimeContext getExecutionContext() {
    // TODO Auto-generated method stub
    return null;
  }

  public int getStatus() {
    // TODO Auto-generated method stub
    return 0;
  }

  public void init(IPentahoSession session) {
    this.initSession = session;
  }

  public void setCreateFeedbackParameterCallback(ICreateFeedbackParameterCallback callback) {
    // TODO Auto-generated method stub

  }

  public void setForcePrompt(boolean forcePrompt) {
    // TODO Auto-generated method stub

  }

  public void setParameterProvider(String name, IParameterProvider parameterProvider) {
    // TODO Auto-generated method stub

  }

  public void setParameterXsl(String xsl) {
    // TODO Auto-generated method stub

  }

  public void setSession(IPentahoSession session) {
    // TODO Auto-generated method stub

  }

  public void setlistener(IActionCompleteListener listener) {
    // TODO Auto-generated method stub

  }

  public void setlistener(IExecutionListener execListener) {
    // TODO Auto-generated method stub

  }

  public void debug(String message) {
    // TODO Auto-generated method stub

  }

  public void debug(String message, Throwable error) {
    // TODO Auto-generated method stub

  }

  public void error(String message) {
    // TODO Auto-generated method stub

  }

  public void error(String message, Throwable error) {
    // TODO Auto-generated method stub

  }

  public void fatal(String message) {
    // TODO Auto-generated method stub

  }

  public void fatal(String message, Throwable error) {
    // TODO Auto-generated method stub

  }

  public int getLoggingLevel() {
    // TODO Auto-generated method stub
    return 0;
  }

  public void info(String message) {
    // TODO Auto-generated method stub

  }

  public void info(String message, Throwable error) {
    // TODO Auto-generated method stub

  }

  public void setLoggingLevel(int loggingLevel) {
    // TODO Auto-generated method stub

  }

  public void trace(String message) {
    // TODO Auto-generated method stub

  }

  public void trace(String message, Throwable error) {
    // TODO Auto-generated method stub

  }

  public void warn(String message) {
    // TODO Auto-generated method stub

  }

  public void warn(String message, Throwable error) {
    // TODO Auto-generated method stub

  }

}