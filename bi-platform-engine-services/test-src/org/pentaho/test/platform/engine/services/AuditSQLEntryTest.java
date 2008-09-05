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
 * @created Aug 15, 2005 
 * @author James Dixon
 */

package org.pentaho.test.platform.engine.services;

import java.math.BigDecimal;

import org.pentaho.platform.engine.core.audit.AuditSQLEntry;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.engine.core.BaseTestCase;

public class AuditSQLEntryTest extends BaseTestCase {
  private static final String SOLUTION_PATH = "test-src/solution";
  public String getSolutionPath() {
       return SOLUTION_PATH;  
  }
  public void testAuditSQLEntry() {
    AuditSQLEntry auditSqlEntry = new AuditSQLEntry();
    try {
      auditSqlEntry.auditAll("234234", "2342342342", "234234234", "String", "actor", "messageType", "messageName", "messageTxtValue", new BigDecimal(2324323.23), 23); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
    } catch(Exception e) {
      assertTrue("Should not have thrown the exception", false);
    }
    assertTrue(true);
  }

  public static void main(String[] args) {
    AuditSQLEntryTest test = new AuditSQLEntryTest();
    test.testAuditSQLEntry();
    try {

    } finally {
      BaseTest.shutdown();
    }
  }

}
