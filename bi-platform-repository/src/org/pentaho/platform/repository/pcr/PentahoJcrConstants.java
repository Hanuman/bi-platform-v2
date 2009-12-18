package org.pentaho.platform.repository.pcr;

/**
 * Pentaho JCR constants for node and property names.
 * 
 * <p>
 * The naming convention for the constants is {@code <NS>_<NAME>} where {@code <NS>} is a namespace prefix (sometimes a
 * reserved prefix such as {@code jcr}) and {@code <NAME>} is the node or property name as all capital letters with no 
 * underscores. This matches Jackrabbit's convention.
 * </p>
 * 
 * @author mlowery
 */
public interface PentahoJcrConstants {

  /**
   * All of Pentaho's node types use this namespace.
   */
  String PENTAHO_NAMESPACE_URI = "http://www.pentaho.org/jcr/1.0";

  /**
   * Pentaho custom mixin type. To be applied to nt:file or nt:linkedfile nodes.
   */
  String PENTAHO_MIXIN_PENTAHOFILE = "pentahoFile";

  /**
   * Pentaho custom mixin type. To be applied to nt:resource nodes.
   */
  String PENTAHO_MIXIN_PENTAHORESOURCE = "pentahoResource";
  
  String JCR_ENCODING = "jcr:encoding";

  String JCR_DATA = "jcr:data";

  String JCR_CREATED = "jcr:created";

  String JCR_CONTENT = "jcr:content";

  String JCR_PRIMARYTYPE = "jcr:primaryType";

  String JCR_MIXINTYPES = "jcr:mixinTypes";

  String JCR_MIMETYPE = "jcr:mimeType";

  String JCR_LASTMODIFIED = "jcr:lastModified";

  String NT_FOLDER = "nt:folder";

  String NT_FILE = "nt:file";

  String NT_LINKEDFILE = "nt:linkedFile";

  String NT_UNSTRUCTURED = "nt:unstructured";

  String NT_RESOURCE = "nt:resource";

  String MIX_VERSIONABLE = "mix:versionable";

  String MIX_REFERENCEABLE = "mix:referenceable";

  String PENTAHO_RUNARGUMENTS = "runArguments";

  String PENTAHO_AUX = "aux";

//  String PENTAHO_PREVIEWLASTMODIFIED = "previewLastModified";

  //String PENTAHO_VERSIONED = "versioned";

  String PENTAHO_CONTENTTYPE = "contentType";
}
