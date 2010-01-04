package org.pentaho.platform.repository.pcr.jcr;

import javax.jcr.Session;

import org.springframework.extensions.jcr.JcrConstants;

/**
 * Pentaho JCR constants. The {@code get* } methods automatically prepend the appropriate namespace prefix.
 * 
 * @author mlowery
 */
public class PentahoJcrConstants extends JcrConstants {

  // ~ Static fields/initializers ======================================================================================

  /**
   * Pentaho item name namespace.
   */
  public static final String PHO_NS = "http://www.pentaho.org/jcr/1.0";

  /**
   * Pentaho node type namespace.
   */
  public static final String PHO_NT_NS = "http://www.pentaho.org/jcr/nt/1.0";

  /**
   * Pentaho mixin type namespace.
   */
  public static final String PHO_MIX_NS = "http://www.pentaho.org/jcr/mix/1.0";

  private static final String PHO_MIX_LOCKABLE = "pentahoLockable";

  private static final String PHO_MIX_VERSIONABLE = "pentahoVersionable";

  private static final String PHO_NT_PENTAHOFILE = "pentahoFile";

  private static final String PHO_NT_PENTAHOFOLDER = "pentahoFolder";

  private static final String PHO_NT_INTERNALFOLDER = "pentahoInternalFolder";

  private static final String PHO_NT_LOCKTOKENSTORAGE = "pentahoLockTokenStorage";

  private static final String PHO_NT_LOCALIZEDSTRING = "localizedString";

  private static final String PHO_NT_PENTAHOHIERARCHYNODE = "pentahoHierarchyNode";

  private static final String PHO_LASTMODIFIED = "lastModified";

  private static final String PHO_LOCKMESSAGE = "lockMessage";

  private static final String PHO_LOCKDATE = "lockDate";

  private static final String PHO_LOCKEDNODEREF = "lockedNodeRef";

  private static final String PHO_LOCKTOKEN = "lockToken";

  private static final String PHO_VERSIONAUTHOR = "versionAuthor";

  private static final String PHO_VERSIONMESSAGE = "versionMessage";

  private static final String PHO_ACLOWNERNAME = "aclOwnerName";

  private static final String PHO_TITLE = "title";

  private static final String PHO_DESCRIPTION = "description";

  /**
   * Same abstraction as {@code Locale.ROOT}.
   */
  private static final String PHO_ROOTLOCALE = "rootLocale";

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  public PentahoJcrConstants(final Session session) {
    super(session);
  }

  public PentahoJcrConstants(final Session session, final boolean cache) {
    super(session, cache);
  }

  // ~ Methods ========================================================================================================= 

  public String getPHO_NT_PENTAHOFILE() {
    return resolveName(PHO_NT_NS, PHO_NT_PENTAHOFILE);
  }

  public String getPHO_MIX_LOCKABLE() {
    return resolveName(PHO_MIX_NS, PHO_MIX_LOCKABLE);
  }

  public String getPHO_MIX_VERSIONABLE() {
    return resolveName(PHO_MIX_NS, PHO_MIX_VERSIONABLE);
  }

  public String getPHO_NT_INTERNALFOLDER() {
    return resolveName(PHO_NT_NS, PHO_NT_INTERNALFOLDER);
  }

  public String getPHO_NT_LOCKTOKENSTORAGE() {
    return resolveName(PHO_NT_NS, PHO_NT_LOCKTOKENSTORAGE);
  }

  public String getPHO_LOCKMESSAGE() {
    return resolveName(PHO_NS, PHO_LOCKMESSAGE);
  }

  public String getPHO_LOCKDATE() {
    return resolveName(PHO_NS, PHO_LOCKDATE);
  }

  public String getPHO_LOCKEDNODEREF() {
    return resolveName(PHO_NS, PHO_LOCKEDNODEREF);
  }

  public String getPHO_LOCKTOKEN() {
    return resolveName(PHO_NS, PHO_LOCKTOKEN);
  }

  public String getPHO_VERSIONAUTHOR() {
    return resolveName(PHO_NS, PHO_VERSIONAUTHOR);
  }

  public String getPHO_VERSIONMESSAGE() {
    return resolveName(PHO_NS, PHO_VERSIONMESSAGE);
  }

  public String getPHO_ACLOWNERNAME() {
    return resolveName(PHO_NS, PHO_ACLOWNERNAME);
  }

  public String getPHO_LASTMODIFIED() {
    return resolveName(PHO_NS, PHO_LASTMODIFIED);
  }

  public String getPHO_TITLE() {
    return resolveName(PHO_NS, PHO_TITLE);
  }

  public String getPHO_DESCRIPTION() {
    return resolveName(PHO_NS, PHO_DESCRIPTION);
  }

  public String getPHO_ROOTLOCALE() {
    return resolveName(PHO_NS, PHO_ROOTLOCALE);
  }

  public String getPHO_NT_LOCALIZEDSTRING() {
    return resolveName(PHO_NT_NS, PHO_NT_LOCALIZEDSTRING);
  }

  public String getPHO_NT_PENTAHOFOLDER() {
    return resolveName(PHO_NT_NS, PHO_NT_PENTAHOFOLDER);
  }

  public String getPHO_NT_PENTAHOHIERARCHYNODE() {
    return resolveName(PHO_NT_NS, PHO_NT_PENTAHOHIERARCHYNODE);
  }

}
