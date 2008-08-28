package org.pentaho.platform.api.engine;

/**
 * Describes a content type. This class is used to describe content types that
 * users can get to. Implementations of this class are also used as keys to
 * content generators (IContentGenerator)
 * @author jamesdixon
 *
 */
public interface IContentInfo {

	/**
	 * The extension of files that generate this content type.
	 * e.g. 'xaction'
	 * @return file extension
	 */
	public String getExtension();
	
	/**
	 * The title of this content type as presented to the user
	 * Implementors of this interface should provide localization for the
	 * title
	 * e.g. 'Executable action'
	 * @return title
	 */
	public String getTitle();
	
	/**
	 * The description of this content type as presented to the user
	 * Implementors of this interface should provide localization for the
	 * description
	 * @return title
	 */
	public String getDescription();
	
	/**
	 * The mime-type of the generated content
	 * e.g. 'text/html'
	 * @return mime type
	 */
	public String getMimeType();
	
}
