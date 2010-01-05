package org.pentaho.platform.repository.pcr.jcr.transform;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.pentaho.platform.api.repository.IRepositoryFileData;
import org.pentaho.platform.repository.pcr.SampleRepositoryFileData;
import org.pentaho.platform.repository.pcr.jcr.ITransformer;
import org.pentaho.platform.repository.pcr.jcr.PentahoJcrConstants;

public class SampleRepositoryFileDataTransformer implements ITransformer<SampleRepositoryFileData> {

  // ~ Static fields/initializers ======================================================================================

  private static final String PROPERTY_NAME_SAMPLE_STRING = "sampleString"; //$NON-NLS-1$

  private static final String PROPERTY_NAME_SAMPLE_BOOLEAN = "sampleBoolean"; //$NON-NLS-1$

  private static final String PROPERTY_NAME_SAMPLE_INTEGER = "sampleInteger"; //$NON-NLS-1$

  private static final String SUPPORTED_EXTENSION = "sample"; //$NON-NLS-1$

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  public SampleRepositoryFileDataTransformer() {
    super();
  }

  // ~ Methods =========================================================================================================

  /**
   * {@inheritDoc}
   */
  public boolean supports(final String extension, final Class<? extends IRepositoryFileData> clazz) {
    return SUPPORTED_EXTENSION.equals(extension) && clazz.isAssignableFrom(SampleRepositoryFileData.class);
  }

  /**
   * {@inheritDoc}
   */
  public void createContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final SampleRepositoryFileData data, final Node fileNode) throws RepositoryException, IOException {
    Node unstructuredNode = fileNode.addNode(pentahoJcrConstants.getJCR_CONTENT(), pentahoJcrConstants
        .getNT_UNSTRUCTURED());
    unstructuredNode.setProperty(PROPERTY_NAME_SAMPLE_STRING, data.getSampleString());
    unstructuredNode.setProperty(PROPERTY_NAME_SAMPLE_BOOLEAN, data.getSampleBoolean());
    unstructuredNode.setProperty(PROPERTY_NAME_SAMPLE_INTEGER, data.getSampleInteger());
  }

  /**
   * {@inheritDoc}
   */
  public SampleRepositoryFileData fromContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node fileNode) throws RepositoryException, IOException {
    Node unstructuredNode = fileNode.getNode(pentahoJcrConstants.getJCR_CONTENT());
    String sampleString = unstructuredNode.getProperty(PROPERTY_NAME_SAMPLE_STRING).getString();
    boolean sampleBoolean = unstructuredNode.getProperty(PROPERTY_NAME_SAMPLE_BOOLEAN).getBoolean();
    int sampleInteger = (int) unstructuredNode.getProperty(PROPERTY_NAME_SAMPLE_INTEGER).getLong();
    return new SampleRepositoryFileData(sampleString, sampleBoolean, sampleInteger);
  }

  /**
   * {@inheritDoc}
   */
  public void updateContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final SampleRepositoryFileData data, final Node fileNode) throws RepositoryException, IOException {
    Node unstructuredNode = fileNode.getNode(pentahoJcrConstants.getJCR_CONTENT());
    unstructuredNode.setProperty(PROPERTY_NAME_SAMPLE_STRING, data.getSampleString());
    unstructuredNode.setProperty(PROPERTY_NAME_SAMPLE_BOOLEAN, data.getSampleBoolean());
    unstructuredNode.setProperty(PROPERTY_NAME_SAMPLE_INTEGER, data.getSampleInteger());
  }

}
