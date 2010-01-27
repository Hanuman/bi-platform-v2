package org.pentaho.platform.repository.pcr.ws;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.RepositoryFileAce;
import org.pentaho.platform.api.repository.RepositoryFileAcl;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.api.repository.RepositoryFileSid;

import edu.emory.mathcs.backport.java.util.Arrays;

public class RepositoryFileAclAdapter extends XmlAdapter<JaxbSafeRepositoryFileAcl, RepositoryFileAcl> {

  private static final Log logger = LogFactory.getLog(RepositoryFileAclAdapter.class);

  @Override
  public JaxbSafeRepositoryFileAcl marshal(final RepositoryFileAcl v) throws Exception {
    try {
      JaxbSafeRepositoryFileAcl jaxbAcl = new JaxbSafeRepositoryFileAcl();
      jaxbAcl.id = v.getId().toString();
      jaxbAcl.owner = v.getOwner().getName();
      if (v.getOwner().getType() == RepositoryFileSid.Type.USER) {
        jaxbAcl.user = true;
      } else {
        jaxbAcl.user = false;
      }
      jaxbAcl.entriesInheriting = v.isEntriesInheriting();
      jaxbAcl.aces = toJaxbAces(v.getAces());
      return jaxbAcl;
    } catch (Exception e) {
      logger.error(String.format("error marshalling %s to %s", RepositoryFileAcl.class.getName(),
          JaxbSafeRepositoryFileAcl.class.getName()), e);
      throw e;
    }
  }

  protected JaxbSafeRepositoryFileAclAce[] toJaxbAces(final List<RepositoryFileAce> aces) {
    List<JaxbSafeRepositoryFileAclAce> jaxbAces = new ArrayList<JaxbSafeRepositoryFileAclAce>();
    for (RepositoryFileAce ace : aces) {
      JaxbSafeRepositoryFileAclAce jaxbAce = new JaxbSafeRepositoryFileAclAce();
      jaxbAce.recipient = ace.getSid().getName();
      if (ace.getSid().getType() == RepositoryFileSid.Type.USER) {
        jaxbAce.user = true;
      } else {
        jaxbAce.user = false;
      }
      jaxbAce.permissions = ace.getPermissions().toArray(new RepositoryFilePermission[ace.getPermissions().size()]);
      jaxbAces.add(jaxbAce);
    }
    return jaxbAces.toArray(new JaxbSafeRepositoryFileAclAce[jaxbAces.size()]);
  }

  @Override
  public RepositoryFileAcl unmarshal(final JaxbSafeRepositoryFileAcl v) throws Exception {
    try {
      RepositoryFileAcl.Builder builder = null;
      if (v.user) {
        builder = new RepositoryFileAcl.Builder(v.id, v.owner, RepositoryFileSid.Type.USER);
      } else {
        builder = new RepositoryFileAcl.Builder(v.id, v.owner, RepositoryFileSid.Type.ROLE);
      }
      builder.entriesInheriting(v.entriesInheriting);
      for (JaxbSafeRepositoryFileAclAce jaxbAce : v.aces) {
        if (jaxbAce.user) {
          builder.ace(jaxbAce.recipient, RepositoryFileSid.Type.USER, EnumSet
              .copyOf(Arrays.asList(jaxbAce.permissions)));
        } else {
          builder.ace(jaxbAce.recipient, RepositoryFileSid.Type.ROLE, EnumSet
              .copyOf(Arrays.asList(jaxbAce.permissions)));
        }
      }
      return builder.build();
    } catch (Exception e) {
      logger.error(String.format("error unmarshalling %s to %s", JaxbSafeRepositoryFileAcl.class.getName(),
          RepositoryFileAcl.class.getName()), e);
      throw e;
    }
  }

}
