package org.pentaho.platform.dataaccess.datasource.utils;

public class ExceptionParser {
    public static String DELIMETER = "-"; //$NON-NLS-1$
    
    public static String getErrorMessage(Throwable throwable, String defaultErrorMessage) {
      String message = throwable.getLocalizedMessage();
      if(message != null && message.length() > 0) {
        int index = message.indexOf(DELIMETER);
        if(index > 0) {
          return message.substring(index + 1);
        } else {
          return defaultErrorMessage;
        }
      } else {
        return defaultErrorMessage;
      }
      
    }
    
    public static String getErrorHeader(Throwable throwable, String defaultErrorHeader) {
      String message = throwable.getLocalizedMessage();
      if(message != null && message.length() > 0) {
        int index = message.indexOf(DELIMETER);
        if(index > 0) {
          return message.substring(0, index -1);
        } else {
          return defaultErrorHeader;
        }
      } else {
        return defaultErrorHeader;
      }
      
    }
  }
