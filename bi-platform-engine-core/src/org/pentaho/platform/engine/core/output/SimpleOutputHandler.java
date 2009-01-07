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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jul 11, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.engine.core.output;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IContentOutputHandler;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IOutputDef;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class SimpleOutputHandler implements IOutputHandler {

  private Map<String,Object> responseAttributes;

  private IContentItem feedbackContent;

  boolean allowFeedback;

  private String mimeType;

  private int outputType = IOutputHandler.OUTPUT_TYPE_DEFAULT;

  private boolean contentGenerated;

  private Map<String,IContentItem> outputs;

  private IPentahoSession session;

  private IMimeTypeListener mimeTypeListener;

  protected IRuntimeContext runtimeContext;

  private static final Log logger = LogFactory.getLog(SimpleOutputHandler.class);

  public SimpleOutputHandler(final IContentItem contentItem, final boolean allowFeedback) {

    responseAttributes = new HashMap<String,Object>();
    contentGenerated = false;
    outputs = new HashMap<String,IContentItem>();
    try {
      String key = IOutputHandler.RESPONSE + "." + IOutputHandler.CONTENT; //$NON-NLS-1$
      outputs.put(key, contentItem);

      this.allowFeedback = allowFeedback;
      if (allowFeedback) {
        feedbackContent = new SimpleContentItem(contentItem.getOutputStream(null));
      }
    } catch (IOException ioe) {
      SimpleOutputHandler.logger.error(null, ioe);
    }

  }

  public SimpleOutputHandler(final OutputStream outputStream, final boolean allowFeedback) {

    this.allowFeedback = allowFeedback;
    if (allowFeedback) {
      feedbackContent = new SimpleContentItem(outputStream);
    }
    responseAttributes = new HashMap<String,Object>();
    contentGenerated = false;
    outputs = new HashMap<String,IContentItem>();
    setOutputStream(outputStream, IOutputHandler.RESPONSE, IOutputHandler.CONTENT);
  }

  public void setSession(final IPentahoSession session) {
    this.session = session;
  }

  public IPentahoSession getSession() {
    return session;
  }

  public void setOutputStream(final OutputStream outputStream, final String outputName, final String contentName) {
    String key = outputName + "." + contentName; //$NON-NLS-1$
    SimpleContentItem item = new SimpleContentItem(outputStream);
    outputs.put(key, item);
  }

  public void setOutputPreference(final int outputType) {
    this.outputType = outputType;
  }

  public boolean contentDone() {
    return contentGenerated;

  }

  public int getOutputPreference() {
    return outputType;
  }

  public void setMimeType(final String mimeType) {
    this.mimeType = mimeType;
  }

  public String getMimeType() {
    return mimeType;
  }

  public boolean allowFeedback() {
    return allowFeedback;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.solution.IOutputHandler#getOutputDefs()
   */
  public Map getOutputDefs() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.solution.IOutputHandler#getOutputDef(java.lang.String)
   */
  public IOutputDef getOutputDef(final String name) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.solution.IOutputHandler#getFeedbackStream()
   */
  public IContentItem getFeedbackContentItem() {
    if (allowFeedback) {
      contentGenerated = true;
      return feedbackContent;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.solution.IOutputHandler#getContentStream()
   */
  public IContentItem getOutputContentItem(final String outputName, final String contentName, final String solution,
      final String instanceId, final String localMimeType) {
    String key = outputName + "." + contentName; //$NON-NLS-1$
    if (outputs.get(key) != null) {
      return outputs.get(key);
    } else {
      IContentOutputHandler output = PentahoSystem.getOutputDestinationFromContentRef(contentName, session);
      if (output != null) {
        output.setInstanceId(instanceId);
        output.setMimeType(localMimeType);
        output.setSolutionName(solution);
        return output.getFileOutputContentItem();
      }
    }
    return null;
  }

  public IContentItem getOutputContentItem(final String objectName, final String contentName, final String title,
      final String url, final String solution, final String instanceId, final String localMimeType) {
    return getOutputContentItem(objectName, contentName, solution, instanceId, localMimeType);
  }

  public void setContentItem(final IContentItem content, final String objectName, final String contentName) {
    mimeType = content.getMimeType();
  }

  public void setOutput(final String name, final Object value) {
    if (value == null) {
      SimpleOutputHandler.logger.warn(Messages.getString("SimpleOutputHandler.WARN_VALUE_IS_NULL")); //$NON-NLS-1$
      return;
    }

    if (IOutputHandler.CONTENT.equalsIgnoreCase(name)) {
      IContentItem response = getOutputContentItem("response", IOutputHandler.CONTENT, null, null, null, null, null); //$NON-NLS-1$
      if (response != null) {
        try {
          if (value instanceof IContentItem) {
            IContentItem content = (IContentItem) value;
            if ((response.getMimeType() == null) || (!response.getMimeType().equalsIgnoreCase(content.getMimeType()))) {
              response.setMimeType(content.getMimeType());
            }

            InputStream inStr = content.getInputStream();
            try {
              OutputStream outStr = response.getOutputStream(response.getActionName());
              int inCnt = 0;
              byte[] buf = new byte[4096];
              while (-1 != (inCnt = inStr.read(buf))) {
                outStr.write(buf, 0, inCnt);
              }
            } finally {
              try {
                inStr.close();
              } catch (Exception ignored) {
              }
            }
            contentGenerated = true;
          } else {
            if (response.getMimeType() == null) {
              response.setMimeType("text/html"); //$NON-NLS-1$
            }

            response.getOutputStream(response.getActionName()).write(value.toString().getBytes());
            contentGenerated = true;
          }
        } catch (IOException ioe) {
          SimpleOutputHandler.logger.error(null, ioe);
        }
      }
    } else {
      responseAttributes.put(name, value);
    }

  }

  public Map getResponseAttributes() {
    return responseAttributes;
  }

  public IMimeTypeListener getMimeTypeListener() {
    return mimeTypeListener;
  }

  public void setMimeTypeListener(final IMimeTypeListener mimeTypeListener) {
    this.mimeTypeListener = mimeTypeListener;
  }

  public void setRuntimeContext(final IRuntimeContext runtimeContext) {
    this.runtimeContext = runtimeContext;
  }

}
