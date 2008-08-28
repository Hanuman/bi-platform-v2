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
 * @created Jul 28, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.engine.services;

import java.util.List;

import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.services.messages.Messages;

public abstract class PentahoMessenger extends PentahoBase {

  private List messages;

  public List getMessages() {
    return messages;
  }

  public void setMessages(final List messages) {
    this.messages = messages;
  }

  @Override
  public void trace(final String message) {
    if (loggingLevel <= ILogger.TRACE) {
      if (messages != null) {
        messages.add(Messages.getString("Message.USER_DEBUG", message, getClass().getName())); //$NON-NLS-1$
      }
      super.trace(message);
    }
  }

  @Override
  public void debug(final String message) {
    if (loggingLevel <= ILogger.DEBUG) {
      if (messages != null) {
        messages.add(Messages.getString("Message.USER_DEBUG", message, getClass().getName())); //$NON-NLS-1$
      }
      super.debug(message);
    }
  }

  @Override
  public void info(final String message) {
    if (loggingLevel <= ILogger.INFO) {
      if (messages != null) {
        messages.add(Messages.getString("Message.USER_INFO", message, getClass().getName())); //$NON-NLS-1$
      }
      super.info(message);
    }
  }

  @Override
  public void warn(final String message) {
    if (loggingLevel <= ILogger.WARN) {
      if (messages != null) {
        messages.add(Messages.getString("Message.USER_WARNING", message, getClass().getName())); //$NON-NLS-1$
      }
      super.warn(message);
    }
  }

  @Override
  public void error(final String message) {
    if (loggingLevel <= ILogger.ERROR) {
      if (messages != null) {
        messages.add(Messages.getString("Message.USER_ERROR", message, getClass().getName())); //$NON-NLS-1$
      }
      super.error(message);
    }
  }

  @Override
  public void fatal(final String message) {
    if (loggingLevel <= ILogger.FATAL) {
      if (messages != null) {
        messages.add(Messages.getString("Message.USER_ERROR", message, getClass().getName())); //$NON-NLS-1$
      }
      super.fatal(message);
    }
  }

  @Override
  public void trace(final String message, final Throwable error) {
    if (loggingLevel <= ILogger.TRACE) {
      if (messages != null) {
        messages.add(Messages.getString("Message.USER_DEBUG", message, getClass().getName())); //$NON-NLS-1$
      }
      super.trace(message, error);
    }
  }

  @Override
  public void debug(final String message, final Throwable error) {
    if (loggingLevel <= ILogger.DEBUG) {
      if (messages != null) {
        messages.add(Messages.getString("Message.USER_DEBUG", message, getClass().getName())); //$NON-NLS-1$
      }
      super.debug(message, error);
    }
  }

  @Override
  public void info(final String message, final Throwable error) {
    if (loggingLevel <= ILogger.INFO) {
      if (messages != null) {
        messages.add(Messages.getString("Message.USER_INFO", message, getClass().getName())); //$NON-NLS-1$
      }
      super.info(message, error);
    }
  }

  @Override
  public void warn(final String message, final Throwable error) {
    if (loggingLevel <= ILogger.WARN) {
      if (messages != null) {
        messages.add(Messages.getString("Message.USER_WARNING", message, getClass().getName())); //$NON-NLS-1$
      }
      super.warn(message, error);
    }
  }

  @Override
  public void error(final String message, final Throwable error) {
    if (loggingLevel <= ILogger.ERROR) {
      if (messages != null) {
        messages.add(Messages.getString("Message.USER_ERROR_EX", message, getClass().getName(), error.toString())); //$NON-NLS-1$
      }
      super.error(message, error);
    }
  }

  @Override
  public void fatal(final String message, final Throwable error) {
    if (loggingLevel <= ILogger.FATAL) {
      if (messages != null) {
        messages.add(Messages.getString("Message.USER_ERROR_EX", message, getClass().getName(), error.toString())); //$NON-NLS-1$ 
      }
      super.fatal(message, error);
    }
  }

  public static String getUserString(final String type) {
    return Messages.getString("Message.USER_" + type); //$NON-NLS-1$
  }

}
