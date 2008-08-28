package org.pentaho.platform.api.engine;

import java.util.List;
import java.util.Map;

public interface ICreateFeedbackParameterCallback {
  public void createFeedbackParameter(IRuntimeContext runtimeContext, String fieldName, final String displayName, String hint, Object defaultValues,
      final List values, final Map dispNames, final String displayStyle, final boolean optional, final boolean visible);
}
