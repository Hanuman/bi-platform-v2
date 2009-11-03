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

/**
 * @author aaron
 *
 */
public class SimpleOutputHandler implements IOutputHandler {

  private Map<String, Object> responseAttributes;

  private IContentItem feedbackContent;

  boolean allowFeedback;

  private String mimeType;

  private int outputType = IOutputHandler.OUTPUT_TYPE_DEFAULT;

  private boolean contentGenerated;
  
  private Map<String, IContentItem> outputs;

  private IPentahoSession session;

  private IMimeTypeListener mimeTypeListener;

  protected IRuntimeContext runtimeContext;
  
  private boolean responseExpected;

  private static final Log logger = LogFactory.getLog(SimpleOutputHandler.class);

  /**
   * Creates a {@link SimpleContentItem} copy of an {@link IContentItem}
   * @param contentItem provides the underlying outputStream this outputhandler manages.  
   * Feedback will also be written to this contentItem's output stream if allowFeedback is true
   * @param allowFeedback
   */
  public SimpleOutputHandler(final IContentItem contentItem, final boolean allowFeedback) {

    responseAttributes = new HashMap<String, Object>();
    contentGenerated = false;
    outputs = new HashMap<String, IContentItem>();
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

  /**
   * Creates a SimpleContentItem from an OutputStream.
   * @param outputStream the underlying outputStream this outputhandler manages.  
   * Feedback will be written to this output stream if allowFeedback is true
   * @param allowFeedback
   */
  public SimpleOutputHandler(final OutputStream outputStream, final boolean allowFeedback) {

    this.allowFeedback = allowFeedback;
    if (allowFeedback) {
      feedbackContent = new SimpleContentItem(outputStream);
    }
    responseAttributes = new HashMap<String, Object>();
    contentGenerated = false;
    outputs = new HashMap<String, IContentItem>();
    setOutputStream(outputStream, IOutputHandler.RESPONSE, IOutputHandler.CONTENT);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IOutputHandler#setSession(org.pentaho.platform.api.engine.IPentahoSession)
   */
  public void setSession(final IPentahoSession session) {
    this.session = session;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IOutputHandler#getSession()
   */
  public IPentahoSession getSession() {
    return session;
  }

  public void setOutputStream(final OutputStream outputStream, final String outputName, final String contentName) {
    String key = outputName + "." + contentName; //$NON-NLS-1$
    SimpleContentItem item = new SimpleContentItem(outputStream);
    outputs.put(key, item);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IOutputHandler#setOutputPreference(int)
   */
  public void setOutputPreference(final int outputType) {
    this.outputType = outputType;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IOutputHandler#contentDone()
   */
  public boolean contentDone() {
    return contentGenerated;

  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IOutputHandler#getOutputPreference()
   */
  public int getOutputPreference() {
    return outputType;
  }

  public void setMimeType(final String mimeType) {
    this.mimeType = mimeType;
  }

  public String getMimeType() {
    return mimeType;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IOutputHandler#allowFeedback()
   */
  public boolean allowFeedback() {
    return allowFeedback;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IOutputHandler#getOutputDefs()
   */
  public Map getOutputDefs() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IOutputHandler#getOutputDef(java.lang.String)
   */
  public IOutputDef getOutputDef(final String name) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IOutputHandler#getFeedbackContentItem()
   */
  public IContentItem getFeedbackContentItem() {
    if (allowFeedback) {
      contentGenerated = true;
      return feedbackContent;
    }
    return null;
  }


  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IOutputHandler#getOutputContentItem(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
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

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IOutputHandler#getOutputContentItem(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  public IContentItem getOutputContentItem(final String objectName, final String contentName, final String title,
      final String url, final String solution, final String instanceId, final String localMimeType) {
    return getOutputContentItem(objectName, contentName, solution, instanceId, localMimeType);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IOutputHandler#setContentItem(org.pentaho.platform.api.repository.IContentItem, java.lang.String, java.lang.String)
   */
  public void setContentItem(final IContentItem content, final String objectName, final String contentName) {
    mimeType = content.getMimeType();
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IOutputHandler#setOutput(java.lang.String, java.lang.Object)
   * 
   * This implementation tries to write the data in "value" to the response outputstream managed by this
   * output handler, or if "value" is null, adds it to a responseAttributes map for later retrieval.
   */
  public void setOutput(final String name, final Object value) {
    if (value == null) {
      SimpleOutputHandler.logger.info(Messages.getString("SimpleOutputHandler.INFO_VALUE_IS_NULL")); //$NON-NLS-1$
      return;
    }

    if (IOutputHandler.CONTENT.equalsIgnoreCase(name)) {
      IContentItem response = getOutputContentItem("response", IOutputHandler.CONTENT, null, null, null, null, null); //$NON-NLS-1$
      if (response != null) {
        try {
          //If "value" to set is an IContentItem, then write it to the outputstream 
          //of the response IContentItem managed by this output handler.
          if (value instanceof IContentItem) {
            IContentItem content = (IContentItem) value;
            // See if we should process the input stream. If it's from
            // the content repository, then there's an input stream.
            // SimpleContentItem and HttpContentItem both return null from
            // getInputStream().
            InputStream inStr = content.getInputStream();
            if (inStr != null) {
              if ((response.getMimeType() == null) || (!response.getMimeType().equalsIgnoreCase(content.getMimeType()))) {
                response.setMimeType(content.getMimeType());
              }
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
            }
          } else {
            //if "value" is not an IContentItem, assume it is a string and write it out
            if (response.getMimeType() == null) {
              response.setMimeType("text/html"); //$NON-NLS-1$
            }

            response.getOutputStream(response.getActionName()).write(value.toString().getBytes());
            contentGenerated = true;
          }
        } catch (IOException ioe) {
          //FIXME: do not swallow this exception
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

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IOutputHandler#getMimeTypeListener()
   */
  public IMimeTypeListener getMimeTypeListener() {
    return mimeTypeListener;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IOutputHandler#setMimeTypeListener(org.pentaho.platform.api.engine.IMimeTypeListener)
   */
  public void setMimeTypeListener(final IMimeTypeListener mimeTypeListener) {
    this.mimeTypeListener = mimeTypeListener;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IOutputHandler#setRuntimeContext(org.pentaho.platform.api.engine.IRuntimeContext)
   */
  public void setRuntimeContext(final IRuntimeContext runtimeContext) {
    this.runtimeContext = runtimeContext;
  }

  public boolean isResponseExpected() {
    // TODO Auto-generated method stub
    return false;
  }

}
