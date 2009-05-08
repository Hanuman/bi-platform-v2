package org.pentaho.test.platform.engine.services;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.metadata.MetadataDomainRepository;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.pms.messages.util.LocaleHelper;
import org.pentaho.test.platform.engine.core.MicroPlatform;

@SuppressWarnings("nls")
public class MetadataDomainRepositoryTest {
  
  private MicroPlatform microPlatform;
  
  @Before
  public void init0() {
    microPlatform = new MicroPlatform("plugin-mgr/test-res/PluginManagerTest/");
    microPlatform.define(ISolutionEngine.class, SolutionEngine.class);
    microPlatform.define(IMetadataDomainRepository.class, MetadataDomainRepository.class);
  }
  
  @Test
  public void testMetadataDomainRepo() {
    
    String locale = LocaleHelper.getLocale().toString();
    
    microPlatform.init();
    //Assert.assertTrue( "Initialization of the platform failed", init() );
    
    Domain domain = new Domain();
    
    // try and store null id domain
    
    IMetadataDomainRepository repo = PentahoSystem.get(IMetadataDomainRepository.class, null);
    
    Assert.assertNotNull(repo);
    try  {
      repo.storeDomain(domain, false);
      Assert.fail();
    } catch (DomainIdNullException e) {
      // we should get this exception
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("expecting exception");
    }
    
    domain.setId(UUIDUtil.getUUIDAsString());
    
    // store a domain with an id
    try  {
      repo.storeDomain(domain, false);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
    
    // verify that this file was created on disk, could do this with a reload
    
    
    // store again, without overwrite
    try  {
      repo.storeDomain(domain, false);
      Assert.fail("expecting exception");
    } catch (DomainAlreadyExistsException e) {
      // should occur
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
    
    // store again, with overwrite
    
    domain.setName(new LocalizedString(locale, "Second Save"));
    
    try  {
      repo.storeDomain(domain, true);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }    
   
    // store a second domain
    
    Domain domain2 = new Domain();
    domain2.setId(UUIDUtil.getUUIDAsString());
    domain2.setName(new LocalizedString(locale, "Second Domain"));
    try  {
      repo.storeDomain(domain2, false);
    } catch (Exception e) {
      Assert.fail();
    }

    // make sure domains are in the repo
    
    Assert.assertNotNull(repo.getDomain(domain.getId()));
    Assert.assertNotNull(repo.getDomain(domain2.getId()));

    // rename first domain
    
    domain.setName(new LocalizedString(locale, "Won't Save"));
    
    // flush domains from memory
    repo.flushDomains();

    // make sure the renaming isn't there
    Assert.assertEquals("Second Save", repo.getDomain(domain.getId()).getName().getString(locale));
    
    // reload domains from disk
    repo.reloadDomains();

    Assert.assertNotNull(repo.getDomain(domain.getId()));
    Assert.assertNotNull(repo.getDomain(domain2.getId()));

    Assert.assertEquals("Second Save", repo.getDomain(domain.getId()).getName().getString(locale));
    
    // clear domains, reload, make sure they are gone

    repo.removeDomain(domain.getId());
    repo.removeDomain(domain2.getId());
    
    Assert.assertNull(repo.getDomain(domain.getId()));
    Assert.assertNull(repo.getDomain(domain2.getId()));

    repo.flushDomains();
    repo.reloadDomains();
    
    Assert.assertNull(repo.getDomain(domain.getId()));
    Assert.assertNull(repo.getDomain(domain2.getId()));
  }

}
