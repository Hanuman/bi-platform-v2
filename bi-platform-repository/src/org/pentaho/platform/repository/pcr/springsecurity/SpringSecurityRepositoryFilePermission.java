package org.pentaho.platform.repository.pcr.springsecurity;

import org.springframework.security.acls.Permission;
import org.springframework.security.acls.domain.AbstractPermission;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.DefaultPermissionFactory;

/**
 * A set of standard permissions. Permissions loosely based on 
 * <a href="http://developer.apple.com/mac/library/documentation/Security/Conceptual/Security_Overview/Concepts/Concepts.html#//apple_ref/doc/uid/TP30000976-CH203-SW3">File System Access Control Policy</a>.
 * 
 * @author mlowery
 */
public class SpringSecurityRepositoryFilePermission extends AbstractPermission {

  private static final long serialVersionUID = -5386252944598776600L;

  /**
   * All bits off. The second argument in the constructor is not used. ({@link #getPattern()} is overridden.)
   */
  public static final Permission NONE = new SpringSecurityRepositoryFilePermission(0, 'N'); // 1

  public static final Permission READ = new SpringSecurityRepositoryFilePermission(1 << 0, 'R'); // 1

  public static final Permission WRITE = new SpringSecurityRepositoryFilePermission(1 << 1, 'W'); // 2

  public static final Permission EXECUTE = new SpringSecurityRepositoryFilePermission(1 << 2, 'X'); // 4

  public static final Permission DELETE = new SpringSecurityRepositoryFilePermission(1 << 3, 'D'); // 8

  public static final Permission APPEND = new SpringSecurityRepositoryFilePermission(1 << 4, 'A'); // 16

  public static final Permission DELETE_CHILD = new SpringSecurityRepositoryFilePermission(1 << 5, 'C'); // 32

  public static final Permission READ_ACL = new SpringSecurityRepositoryFilePermission(1 << 8, 'P'); // 256

  public static final Permission WRITE_ACL = new SpringSecurityRepositoryFilePermission(1 << 9, 'L'); // 512

  /**
   * All bits on. This value is future-proof in that new permissions will automatically be included in this value. The
   * second argument in the constructor is not used. ({@link #getPattern()} is overridden.)
   */
  public static final Permission ALL = new SpringSecurityRepositoryFilePermission(-1, 'Z');

  protected static DefaultPermissionFactory defaultPermissionFactory = new DefaultPermissionFactory();

  /**
     * Registers the public static permissions defined on this class. This is mandatory so
     * that the static methods will operate correctly.
     */
  static {
    registerPermissionsFor(SpringSecurityRepositoryFilePermission.class);
  }

  protected SpringSecurityRepositoryFilePermission(int mask, char code) {
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

  /**
   * Override to handle NONE and ALL specially.
   */
  @Override
  public String getPattern() {
    if (mask == -1) {
      return "ALL"; //$NON-NLS-1$
    } else if (mask == 0) {
      return "NONE"; //$NON-NLS-1$
    } else {
      return super.getPattern();
    }
  }

  public static void main(String[] args) {
    System.out.println(NONE.toString());
    System.out.println(new CumulativePermission().set(READ).set(DELETE_CHILD));
    System.out.println(WRITE_ACL.toString());
    System.out.println(ALL.toString());

  }

}
