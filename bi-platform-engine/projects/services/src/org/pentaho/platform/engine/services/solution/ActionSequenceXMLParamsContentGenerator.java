package org.pentaho.platform.engine.services.solution;

import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.engine.ICreateFeedbackParameterCallback;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;

public class ActionSequenceXMLParamsContentGenerator extends
		ActionSequenceContentGenerator implements ICreateFeedbackParameterCallback {

	@Override
	protected void setupListeners( ISolutionEngine solutionEngine ) {
	    // setup any listeners
	    ICreateFeedbackParameterCallback feedbackParameterCallback = (ICreateFeedbackParameterCallback) getCallback( ICreateFeedbackParameterCallback.class );
	    if( feedbackParameterCallback != null ) {
		    solutionEngine.setCreateFeedbackParameterCallback(feedbackParameterCallback);
	    }
	}

	public void createFeedbackParameter( IRuntimeContext context, String fieldName, String displayName, String hint, Object defaultValues, List values, Map dispNames,
            String displayStyle, boolean optional, boolean visible) {
          System.out.println("createFeedbackParameterCallback::fieldName = " + fieldName);
          System.out.println("createFeedbackParameterCallback::displayStyle = " + displayStyle);
          if (values != null) {
            for (Object value : values) {
              System.out.println("createFeedbackParameterCallback::values[i] = " + value);
            }
          }
          if (defaultValues != null) {
            if (defaultValues instanceof List) {
              for (Object value : (List) defaultValues) {
                System.out.println("createFeedbackParameterCallback::defaultValues[i] = " + value);
              }
            } else {
              System.out.println("createFeedbackParameterCallback::defaultValue = " + defaultValues);
            }
          }
	}
	
	public void feedbackParametersDone() {
		
	}
	
}
