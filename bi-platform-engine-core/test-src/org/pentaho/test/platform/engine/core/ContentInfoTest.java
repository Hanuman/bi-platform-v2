package org.pentaho.test.platform.engine.core;

import java.util.List;

import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.engine.core.solution.ContentInfo;
import org.pentaho.platform.engine.core.solution.PluginOperation;

import junit.framework.TestCase;

public class ContentInfoTest extends TestCase {

  public void testPluginOperation() {
    
    IPluginOperation op = new PluginOperation( "name1", "command1" ); //$NON-NLS-1$ //$NON-NLS-2$
    
    assertEquals( "Name is wrong", "name1", op.getId() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "Command is wrong", "command1", op.getCommand() ); //$NON-NLS-1$ //$NON-NLS-2$
    
  }
  
  public void testContentInfo() {
    
    ContentInfo contentInfo = new ContentInfo();
    
    contentInfo.setExtension( "type1" ); //$NON-NLS-1$
    contentInfo.setTitle( "Type 1" ); //$NON-NLS-1$
    contentInfo.setMimeType( "text/text" ); //$NON-NLS-1$
    contentInfo.setDescription("Description 1"); //$NON-NLS-1$
    contentInfo.setIconUrl( "url1" ); //$NON-NLS-1$
    
    IPluginOperation operation = new PluginOperation( "name1", "command1" ); //$NON-NLS-1$ //$NON-NLS-2$
    contentInfo.addOperation(operation);
    operation = new PluginOperation( "name2", "command2" ); //$NON-NLS-1$ //$NON-NLS-2$
    contentInfo.addOperation(operation);
    
    IContentInfo info = contentInfo;
    assertEquals( "Type is wrong", "type1", info.getExtension() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "Title is wrong", "Type 1", info.getTitle() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "Mime type is wrong", "text/text", info.getMimeType() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "Description is wrong", "Description 1", info.getDescription() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "Icon url is wrong", "url1", info.getIconUrl() ); //$NON-NLS-1$ //$NON-NLS-2$

    List<IPluginOperation> ops = info.getOperations();
    assertNotNull( "Operations are null", ops ); //$NON-NLS-1$
    assertEquals( "Wrong number of ops", 2, ops.size() ); //$NON-NLS-1$
    
    assertEquals( "Operation name is wrong", "name1", ops.get(0).getId() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "Operation command is wrong", "command1", ops.get(0).getCommand() ); //$NON-NLS-1$ //$NON-NLS-2$

    assertEquals( "Operation name is wrong", "name2", ops.get(1).getId() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "Operation command is wrong", "command2", ops.get(1).getCommand() ); //$NON-NLS-1$ //$NON-NLS-2$

  }

}
