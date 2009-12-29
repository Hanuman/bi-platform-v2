package org.pentaho.platform.repository.pcr.springsecurity;

import org.springframework.security.acls.Permission;
import org.springframework.security.acls.domain.AbstractPermission;
import org.springframework.security.acls.domain.DefaultPermissionFactory;

/**
 * A set of standard permissions.
 * 
 * <p>
 * Note: There is no "full control" mask. To be granted access to a domain object, the user must have an ACE specifying
 * one of user's sids as the recipient and a single bit as the permission value. This could be expensive in terms of
 * database rows, however ACL inheritance will go far in reducing the number of rows.
 * TODO mlowery add full control bit mask
 * </p>
 * 
 * @author mlowery
 */
public class RepositoryFilePermission extends AbstractPermission {
  
  private static final long serialVersionUID = -5386252944598776600L;
  
  public static final Permission READ = new RepositoryFilePermission(1 << 0, 'R'); // 1

  public static final Permission WRITE = new RepositoryFilePermission(1 << 1, 'W'); // 2

  public static final Permission EXECUTE = new RepositoryFilePermission(1 << 2, 'E'); // 4

  public static final Permission DELETE = new RepositoryFilePermission(1 << 3, 'D'); // 8

  public static final Permission APPEND = new RepositoryFilePermission(1 << 4, 'A'); // 16

  public static final Permission DELETE_CHILD = new RepositoryFilePermission(1 << 5, 'C'); // 32

  public static final Permission READ_ACL = new RepositoryFilePermission(1 << 8, 'P'); // 256

  public static final Permission WRITE_ACL = new RepositoryFilePermission(1 << 9, 'L'); // 512

  protected static DefaultPermissionFactory defaultPermissionFactory = new DefaultPermissionFactory();

  /**
     * Registers the public static permissions defined on this class. This is mandatory so
     * that the static methods will operate correctly.
     */
  static {
    registerPermissionsFor(RepositoryFilePermission.class);
  }

  protected RepositoryFilePermission(int mask, char code) {
    super(mask, code);
  }

  protected final static void registerPermissionsFor(Class subClass) {
    defaultPermissionFactory.registerPublicPermissions(subClass);
  }

  public final static Permission buildFromMask(int mask) {
    return defaultPermissionFactory.buildFromMask(mask);
  }

  public final static Permission[] buildFromMask(int[] masks) {
    return defaultPermissionFactory.buildFromMask(masks);
  }

  public final static Permission buildFromName(String name) {
    return defaultPermissionFactory.buildFromName(name);
  }

  public final static Permission[] buildFromName(String[] names) {
    return defaultPermissionFactory.buildFromName(names);
  }
}
