package org.pentaho.platform.api.engine;

import java.util.List;
import java.util.Set;

import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.ui.IMenuCustomization;

public interface IPluginSettings {

	public Set<String> getContentTypes();
	
	public IContentInfo getContentInfoFromExtension( String extension, IPentahoSession session );
	
	public List<IObjectCreator> getContentGeneratorsForType( String type, IPentahoSession session );
	public IContentGenerator getContentGenerator( String id, IPentahoSession session );
	
	public IContentGeneratorInfo getContentGeneratorInfo( String id, IPentahoSession session );
	
	public IContentGeneratorInfo getDefaultContentGeneratorInfoForType( String type, IPentahoSession session );
	
	public String getContentGeneratorIdForType( String type, IPentahoSession session );
	
	public String getContentGeneratorTitleForType( String type, IPentahoSession session );
	
	public IContentGenerator getContentGeneratorForType( String type, IPentahoSession session );
	
	public List<IMenuCustomization> getMenuCustomizations();
	
	public boolean updatePluginSettings( IPentahoSession session, StringBuilder comments );

}
