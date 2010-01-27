package org.pentaho.platform.repository.pcr.ws;

import java.util.Arrays;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.VersionSummary;

public class VersionSummaryAdapter extends XmlAdapter<JaxbSafeVersionSummary, VersionSummary> {

  private static final Log logger = LogFactory.getLog(VersionSummaryAdapter.class);

  @Override
  public JaxbSafeVersionSummary marshal(final VersionSummary v) throws Exception {
    try {
      JaxbSafeVersionSummary s = new JaxbSafeVersionSummary();
      s.id = v.getId().toString();
      s.author = v.getAuthor();
      s.date = v.getDate();
      s.message = v.getMessage();
      s.versionedFileId = v.getVersionedFileId().toString();
      s.labels = v.getLabels().toArray(new String[v.getLabels().size()]);
      return s;
    } catch (Exception e) {
      logger.error(String.format("error marshalling %s to %s", VersionSummary.class.getName(),
          JaxbSafeVersionSummary.class.getName()), e);
      throw e;
    }
  }

  @Override
  public VersionSummary unmarshal(final JaxbSafeVersionSummary v) throws Exception {
    try {
      return new VersionSummary(v.id, v.versionedFileId, v.date, v.author, v.message, Arrays.asList(v.labels));
    } catch (Exception e) {
      logger.error(String.format("error unmarshalling %s to %s", JaxbSafeVersionSummary.class.getName(),
          VersionSummary.class.getName()), e);
      throw e;
    }
  }

}
