package org.pentaho.platform.dataaccess.datasource.utils;

public class ExceptionParser {
    public static String DELIMETER = "-"; //$NON-NLS-1$
    public static String DEFAULT_ERROR_HEADER = "Error"; //$NON-NLS-1$
    public static String DEFAULT_ERROR_MESSAGE = "Unknown error has occurred"; //$NON-NLS-1$
    
    public static String getErrorMessage(Throwable throwable) {
      String message = throwable.getLocalizedMessage();
      if(message != null && message.length() > 0) {
        int index = message.indexOf(DELIMETER);
        if(index > 0) {
          return message.substring(index + 1);
        } else {
          return DEFAULT_ERROR_MESSAGE;
        }
      } else {
        return DEFAULT_ERROR_MESSAGE;
      }
      
    }
    
    public static String getErrorHeader(Throwable throwable) {
      String message = throwable.getLocalizedMessage();
      if(message != null && message.length() > 0) {
        int index = message.indexOf(DELIMETER);
        if(index > 0) {
          return message.substring(0, index -1);
        } else {
          return DEFAULT_ERROR_HEADER;
        }
      } else {
        return DEFAULT_ERROR_HEADER;
      }
      
    }
  }
