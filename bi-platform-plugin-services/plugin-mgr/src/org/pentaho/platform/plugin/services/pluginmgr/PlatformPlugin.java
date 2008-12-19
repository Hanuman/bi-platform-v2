package org.pentaho.platform.plugin.services.pluginmgr;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.ui.xul.IMenuCustomization;
import org.pentaho.ui.xul.XulOverlay;

public class PlatformPlugin implements IPlatformPlugin, IPentahoInitializer {

  private List<IContentGeneratorInfo> contentGenerators = new ArrayList<IContentGeneratorInfo>();
  
  private List<IContentInfo> contentInfos = new ArrayList<IContentInfo>();
  
  private List<XulOverlay> overlays = new ArrayList<XulOverlay>();
  
  private List<IPentahoInitializer> initializers = new ArrayList<IPentahoInitializer>();
  
  private List menuOverlays = new ArrayList(); 
  
  private String name;
  
  private String sourceDescription;
  
  public PlatformPlugin( ) {
  }
  
  public void init( IPentahoSession session ) {
    for( IPentahoInitializer initializer : initializers ) {
      initializer.init(session);
    }
  }
  
  public List<IContentGeneratorInfo> getContentGenerators() {
    return contentGenerators;
  }

  public List<IContentInfo> getContentInfos() {
    return contentInfos;
  }

  public String getName() {
    return name;
  }

  public List<XulOverlay> getOverlays() {
    return overlays;
  }

  /**
   * Sets the name for this plug-in
   * @param name
   */
  public void setName( String name ) {
    this.name = name;
  }
  
  /**
   * Adds an initializer to this plug-in
   * @param initializer
   */
  public void addInitializer( IPentahoInitializer initializer ) {
    initializers.add( initializer );
  }
  
  /**
   * Adds a content generator to this plug-in
   * @param contentGenerator
   */
  public void addContentGenerator( IContentGeneratorInfo contentGenerator ) {
    contentGenerators.add(contentGenerator);
  }
  
  /**
   * Adds a content info type to this plug-in
   * @param contentInfo
   */
  public void addContentInfo( IContentInfo contentInfo ) {
    contentInfos.add( contentInfo );
  }
  
  /**
   * Adds an overlay to this plug-in
   * @param overlay
   */
  public void addOverlay( XulOverlay overlay ) {
    overlays.add( overlay );
  }
  
  public List getMenuCustomizations() {
    return menuOverlays;
  }

  public void addMenuCustomization( IMenuCustomization customization ) {
    menuOverlays.add( customization );
  }

  public String getSourceDescription() {
    return sourceDescription;
  }

  public void setSourceDescription(String sourceDescription) {
    this.sourceDescription = sourceDescription;
  }
  
}
