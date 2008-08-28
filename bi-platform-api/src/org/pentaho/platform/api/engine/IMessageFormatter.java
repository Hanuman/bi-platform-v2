package org.pentaho.platform.api.engine;

import java.util.List;

import org.pentaho.commons.connection.IPentahoResultSet;

public interface IMessageFormatter {

  void formatErrorMessage(final String mimeType, final String title, final String message,
      final StringBuffer messageBuffer);

  /**
   * If PentahoMessenger.getUserString("ERROR") returns the string:
   * "Error: {0} ({1})" (which is the case for English)
   * Find the substring before the first "{". In this case, that 
   * would be: "Error: ".
   * Return the first string in the messages list that contains
   * the string "Error: ". If no string in the list contains
   * "Error: ", return null;
   * @param messages
   * @return
   */
  String getFirstError(final List messages);

  void formatErrorMessage(final String mimeType, final String title, final List messages,
      final StringBuffer messageBuffer);

  void formatFailureMessage(final String mimeType, final IRuntimeContext context, final StringBuffer messageBuffer,
      final List defaultMessages);

  void formatFailureMessage(final String mimeType, final IRuntimeContext context, final StringBuffer messageBuffer);

  void formatResultSetAsHTMLRows(final IPentahoResultSet resultSet, final StringBuffer messageBuffer);

  void formatSuccessMessage(final String mimeType, final IRuntimeContext context, final StringBuffer messageBuffer,
      final boolean doMessages);

  void formatSuccessMessage(final String mimeType, final IRuntimeContext context, final StringBuffer messageBuffer,
      final boolean doMessages, final boolean doWrapper);

}