package org.pentaho.platform.repository.pcr.jcr.jackrabbit;

import java.security.Principal;

import org.apache.jackrabbit.core.security.authorization.JackrabbitAccessControlList;

/**
 * Extension of {@link JackrabbitAccessControlList} that adds owner and inheriting flag getters and setters. This 
 * interface is required as PentahoJackrabbitAccessControlList is default scoped. Therefore, could outside of Jackrabbit
 * cannot do {@code instanceof} checks on {@code AccessControlPolicy} instances. Instead, use this type with 
 * {@code instanceof}.
 * 
 * <p>
 * <pre>
 * {@code 
 * IPentahoJackrabbitAccessControlList jrPolicy = (IPentahoJackrabbitAccessControlList) acPolicy;
 * jrPolicy.setOwner(jrSession.getPrincipalManager().getPrincipal("jerry"));
 * }
 * </pre>
 * </p>
 * 
 * @author mlowery
 */
public interface IPentahoJackrabbitAccessControlList extends JackrabbitAccessControlList {

  public Principal getOwner();

  public boolean isEntriesInheriting();

  public void setOwner(Principal owner);

  public void setEntriesInheriting(boolean entriesInheriting);

}
