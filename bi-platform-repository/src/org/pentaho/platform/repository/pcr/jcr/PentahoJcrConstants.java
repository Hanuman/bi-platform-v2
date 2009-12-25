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
  private static final String PHO_NS = "http://www.pentaho.org/jcr/1.0";

  /**
   * Pentaho node type namespace.
   */
  protected static final String PHO_NT_NS = "http://www.pentaho.org/jcr/nt/1.0";

  /**
   * Pentaho mixin type namespace.
   */
  protected static final String PHO_MIX_NS = "http://www.pentaho.org/jcr/mix/1.0";

  private static final String PHO_MIX_PENTAHOFILE = "pentahoFile";

  private static final String PHO_MIX_PENTAHORESOURCE = "pentahoResource";

  private static final String PHO_MIX_LOCKABLE = "pentahoLockable";

  private static final String PHO_MIX_VERSIONABLE = "pentahoVersionable";

  private static final String PHO_NT_INTERNALFOLDER = "pentahoInternalFolder";

  private static final String PHO_NT_LOCKTOKENSTORAGE = "pentahoLockTokenStorage";

  private static final String PHO_RUNARGUMENTS = "runArguments";

  private static final String PHO_AUX = "aux";

  private static final String PHO_CONTENTTYPE = "contentType";

  private static final String PHO_LOCKMESSAGE = "lockMessage";

  private static final String PHO_LOCKDATE = "lockDate";

  private static final String PHO_LOCKEDNODEREF = "lockedNodeRef";

  private static final String PHO_LOCKTOKEN = "lockToken";

  private static final String PHO_VERSIONAUTHOR = "versionAuthor";

  private static final String PHO_VERSIONMESSAGE = "versionMessage";

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  public PentahoJcrConstants(final Session session) {
    super(session);
  }

  public PentahoJcrConstants(final Session session, final boolean cache) {
    super(session, cache);
  }

  // ~ Methods ========================================================================================================= 

  public String getPHO_MIX_PENTAHOFILE() {
    return resolveName(PHO_MIX_NS, PHO_MIX_PENTAHOFILE);
  }

  public String getPHO_MIX_PENTAHORESOURCE() {
    return resolveName(PHO_MIX_NS, PHO_MIX_PENTAHORESOURCE);
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

  public String getPHO_RUNARGUMENTS() {
    return resolveName(PHO_NS, PHO_RUNARGUMENTS);
  }

  public String getPHO_AUX() {
    return resolveName(PHO_NS, PHO_AUX);
  }

  public String getPHO_CONTENTTYPE() {
    return resolveName(PHO_NS, PHO_CONTENTTYPE);
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

}
