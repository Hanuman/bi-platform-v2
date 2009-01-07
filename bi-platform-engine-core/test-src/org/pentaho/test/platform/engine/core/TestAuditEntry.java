/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.test.platform.engine.core;

import java.math.BigDecimal;

import org.pentaho.platform.api.engine.AuditException;
import org.pentaho.platform.api.engine.IAuditEntry;

@SuppressWarnings({"all"})
public class TestAuditEntry implements IAuditEntry {

  String jobId;
  String instId;
  String objId;
  String objType;
  String actor;
  String messageType;
  String messageName;
  String messageTxtValue;
  BigDecimal messageNumValue;
  double duration;
  
  public void auditAll(String jobId, String instId, String objId, String objType, String actor, String messageType,
      String messageName, String messageTxtValue, BigDecimal messageNumValue, double duration) throws AuditException {

    this.jobId = jobId;
    this.instId = instId;
    this.objId = objId;
    this.objType = objType;
    this.actor = actor;
    this.messageType = messageType;
    this.messageName = messageName;
    this.messageTxtValue = messageTxtValue;
    this.messageNumValue = messageNumValue;
    this.duration = duration;

  }

}
