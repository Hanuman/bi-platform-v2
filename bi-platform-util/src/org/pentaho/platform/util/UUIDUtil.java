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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Mar 18, 2005 
 * @author Marc Batchelor
 * 
 */

package org.pentaho.platform.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.util.messages.Messages;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

public class UUIDUtil {
  private static final Log log = LogFactory.getLog(UUIDUtil.class);

  static boolean nativeInitialized = false;

  static UUIDGenerator ug;

  static org.safehaus.uuid.EthernetAddress eAddr;

  static {
    // Try loading the EthernetAddress library. If this fails, then fallback
    // to
    // using another method for generating UUID's.
    /*
     * This is always going to fail at the moment try {
     * System.loadLibrary("EthernetAddress"); //$NON-NLS-1$
     * nativeInitialized = true; } catch (Throwable t) { //
     * log.warn(Messages.getErrorString("UUIDUtil.ERROR_0001_LOADING_ETHERNET_ADDRESS") );
     * //$NON-NLS-1$ //$NON-NLS-2$ // Ignore for now. }
     */
    UUIDUtil.ug = UUIDGenerator.getInstance();
    if (UUIDUtil.nativeInitialized) {
      try {
        com.ccg.net.ethernet.EthernetAddress ea = com.ccg.net.ethernet.EthernetAddress.getPrimaryAdapter();
        UUIDUtil.eAddr = new org.safehaus.uuid.EthernetAddress(ea.getBytes());
      } catch (Exception ex) {
        UUIDUtil.log.error(Messages.getErrorString("UUIDUtil.ERROR_0002_GET_MAC_ADDR"), ex); //$NON-NLS-1$
      } catch (UnsatisfiedLinkError ule) {
        UUIDUtil.log.error(Messages.getErrorString("UUIDUtil.ERROR_0002_GET_MAC_ADDR"), ule); //$NON-NLS-1$
        UUIDUtil.nativeInitialized = false;
      }
    }

    /*
     * Add support for running in clustered environments. In this way, the MAC address of the
     * running server can be added to the environment with a -DMAC_ADDRESS=00:50:56:C0:00:01
     */
    if (UUIDUtil.eAddr == null) {
      String macAddr = System.getProperty("MAC_ADDRESS"); //$NON-NLS-1$
      if (macAddr != null) {
        // On Windows machines, people would be inclined to get the MAC
        // address with ipconfig /all. The format of this would be
        // something like 00-50-56-C0-00-08. So, replace '-' with ':' before
        // creating the address.
        // 
        macAddr = macAddr.replace('-', ':');
        UUIDUtil.eAddr = new org.safehaus.uuid.EthernetAddress(macAddr);
      }
    }

    if (UUIDUtil.eAddr == null) {
      // Still don't have an Ethernet Address - generate a dummy one.
      UUIDUtil.eAddr = UUIDUtil.ug.getDummyAddress();
    }

    // Generate a UUID to make sure everything is running OK.
    UUID olduuId = UUIDUtil.ug.generateTimeBasedUUID(UUIDUtil.eAddr);
    if (olduuId == null) {
      UUIDUtil.log.error(Messages.getErrorString("UUIDUtil.ERROR_0003_GENERATEFAILED")); //$NON-NLS-1$
    }

  }

  public static String getUUIDAsString() {
    return UUIDUtil.getUUID().toString();
  }

  public static UUID getUUID() {
    UUID uuId = UUIDUtil.ug.generateTimeBasedUUID(UUIDUtil.eAddr);
    //        while (uuId.toString().equals(olduuId.toString())) {
    //          uuId = ug.generateTimeBasedUUID(eAddr);
    //        }
    //        olduuId = uuId;
    return uuId;
  }

}
