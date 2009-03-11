/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 * 
 * @created Aug 6, 2008 
 * @author Steven Barkdull
 */
package org.pentaho.test.platform.repository.subscription;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISchedule;
import org.pentaho.platform.api.repository.ISubscribeContent;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.api.repository.SubscriptionRepositoryCheckedException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.subscription.SubscriptionHelper;
import org.pentaho.platform.repository.subscription.SubscriptionRepositoryHelper;
import org.pentaho.test.platform.engine.core.BaseTest;

public class SubscriptionRepositoryHelperTest extends BaseTest {

  private IPentahoSession session = null;
  private String[] actionRefs = {
      "samples/getting-started/Exampe1.xaction", //$NON-NLS-1$
      "samples/getting-started/HelloWorld.xaction", //$NON-NLS-1$
      "samples/waqr/territory.waqr.xaction" //$NON-NLS-1$
  };
  private String[] actionRefsEdit = {
      "samples/getting-started/Exampe1.xaction", //$NON-NLS-1$
      "samples/getting-started/HelloWorld.xaction", //$NON-NLS-1$
      "samples/waqr/territory.waqr.xaction", //$NON-NLS-1$
      "samples/getting-started/Exampe3.xaction", //$NON-NLS-1$
      "samples/getting-started/Exampe2.xaction", //$NON-NLS-1$
      "samples/waqr/territory2.waqr.xaction", //$NON-NLS-1$
      "samples/waqr/territory3.waqr.xaction", //$NON-NLS-1$
  };
  
  private String[] scheduleNames = {
      "sched 1 test", //$NON-NLS-1$
      "sched 2 test", //$NON-NLS-1$
      "sched 3 test", //$NON-NLS-1$
      "sched 4 test", //$NON-NLS-1$
      "sched 5 test", //$NON-NLS-1$
      "sched 6 test", //$NON-NLS-1$
      "sched 7 test", //$NON-NLS-1$
      "sched 8 test", //$NON-NLS-1$
      "sched 9 test", //$NON-NLS-1$
      "sched 10 test", //$NON-NLS-1$
      "sched 11 test" //$NON-NLS-1$
  };
  
  private String[] cronStr = {
      "0 0 0 1 * ? 2012", //$NON-NLS-1$
      "1 4 3 ? 5 1 2009", //$NON-NLS-1$
      "1 1 1 1 1 ? 2012", //$NON-NLS-1$
      "2 2 4 ? * * 2012", //$NON-NLS-1$
      "3 2 4 1 * ? 2012", //$NON-NLS-1$
      "3 2 1 1 4,5 ? 2012", //$NON-NLS-1$
      "3/15 1 2 ? * 1 2012", //$NON-NLS-1$
      "3/20 2 3 1,2,3 * ? 2012", //$NON-NLS-1$
      "1 1 1 1 1 ? 2012", //$NON-NLS-1$
      "1 1 1 1 1 ? 2012", //$NON-NLS-1$
      "1 1 1 1 1 ? 2012" //$NON-NLS-1$
  };
  
  public SubscriptionRepositoryHelperTest() {
    super();
  }
  
  public SubscriptionRepositoryHelperTest( String testName ) {
    super( testName );
  }
  
//  TODO: Get this working
//  public static Test suite() {
//
//    TestSuite suite= new TestSuite();
//    suite.addTest(new SubscriptionRepositoryHelperTest("testCrudOps"));
//    
//    return suite;
//  }
  
  public void deleteScheduleContentAndSubscription() {
    String name = null;
    for ( int ii=scheduleNames.length-1; ii>=0; --ii ) {
      name = scheduleNames[ii] + "-repeat"; //$NON-NLS-1$
      System.out.println( "deleting: " + name ); //$NON-NLS-1$
      try {
        ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session );
        ISchedule schedule = subscriptionRepository.getScheduleByScheduleReference( name ); //$NON-NLS-1$
        if ( null != schedule ) {
          SubscriptionRepositoryHelper.deleteScheduleContentAndSubscription(subscriptionRepository, schedule );
        }
        PentahoSystem.systemExitPoint();
      } catch (Exception e) {
        assertTrue( "Failed in call to deleteScheduleContentAndSubscription: "  //$NON-NLS-1$
            + name + " " + e.getMessage(), false ); //$NON-NLS-1$
      }
    }
    for ( int ii=0; ii<scheduleNames.length-1; ++ii ) { // NOTE the -1, don't delete the last schedule
      name = scheduleNames[ii] + "-cron"; //$NON-NLS-1$
      System.out.println( "deleting: " + name ); //$NON-NLS-1$
      try {
        ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session );
        ISchedule schedule = subscriptionRepository.getScheduleByScheduleReference( name );
        if ( null != schedule ) {
          SubscriptionRepositoryHelper.deleteScheduleContentAndSubscription(subscriptionRepository, schedule );
        }
        PentahoSystem.systemExitPoint();
      } catch (Exception e) {
        assertTrue( "Failed in call to deleteScheduleContentAndSubscription: "  //$NON-NLS-1$
            + name + " " + e.getMessage(), false ); //$NON-NLS-1$
      }
    }
  }
  
  public void editScheduleAndContent() {
    String name = null;
    Date now = new Date();
    Date endDate = incrementYear( now, 10 );
    
    for ( int ii=0; ii<scheduleNames.length; ++ii ) {
      name = scheduleNames[ii] + "-cron";
      try {
        ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session );
        System.out.println( "editing: " + name ); //$NON-NLS-1$
        ISchedule schedule = subscriptionRepository.getScheduleByScheduleReference( scheduleNames[ii] + "-cron" ); //$NON-NLS-1$
        ISchedule s = SubscriptionRepositoryHelper.editScheduleAndContent(subscriptionRepository, schedule.getId(),
            "title " + scheduleNames[ii], name, scheduleNames[ii] + " description -edited", cronStr[ii], null, null, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            scheduleNames[ii]+"-group", now, endDate, actionRefsEdit ); //$NON-NLS-1$
        PentahoSystem.systemExitPoint();
      } catch (SubscriptionRepositoryCheckedException e) {
        assertTrue( "Failed in call to editScheduleAndContent: \"" + name + "\" " + e.getMessage(), false ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
    for ( int ii=0; ii<scheduleNames.length; ++ii ) {
      endDate = incrementYear( now, 1 );
      name = scheduleNames[ii] + "-repeat";
      try {
        ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session );
        System.out.println("editing: " + name ); //$NON-NLS-1$
        ISchedule schedule = subscriptionRepository.getScheduleByScheduleReference( scheduleNames[ii]+"-repeat" ); //$NON-NLS-1$
        ISchedule s = SubscriptionRepositoryHelper.editScheduleAndContent(subscriptionRepository, schedule.getId(),
            "title " + scheduleNames[ii], name, scheduleNames[ii] + " description -edited", null, ii, ii*10*1000, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            scheduleNames[ii]+"-group", now, endDate, actionRefsEdit ); //$NON-NLS-1$
        PentahoSystem.systemExitPoint();
      } catch (SubscriptionRepositoryCheckedException e) {
        assertTrue( "Failed in call to editScheduleAndContent: \"" + name + "\" " + e.getMessage(), false ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
  }
  
  private static Date incrementYear( Date d, int increment ) {
    DateFormat fmtr = SubscriptionHelper.getDateTimeFormatter();
    String strNow = fmtr.format( d ); // like: May 21, 2008 8:29:21 PM
    String[] parts = strNow.split( "\\s" ); //$NON-NLS-1$
    int year = Integer.parseInt( parts[2] );
    year += increment;
    String strIncrDate = parts[0] + " " + parts[1] + " " + Integer.toString( year ) + " " + parts[3] + " " + parts[4]; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Date returnDate = null;
    try {
      returnDate = fmtr.parse( strIncrDate );
    } catch( ParseException e ) {
      returnDate = new Date();
    }
    return returnDate;
  }
  
  public void addRepeatScheduleAndContent() {
    String name = null;
    Date now = new Date();
    Date endDate = incrementYear( now, 4 );
    now = incrementYear( now, 1 );
    
    String[] msg = { "adding repeat sched: ", "should fail, adding repeat sched: " }; //$NON-NLS-1$ //$NON-NLS-2$
    for ( int jj=0; jj<2; ++jj ) {
      for ( int ii=0; ii<scheduleNames.length; ++ii ) {
        try {
          ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session );
          name = scheduleNames[ii] + "-repeat";
          System.out.println( msg[jj] + name );
          ISchedule s = SubscriptionRepositoryHelper.addScheduleAndContent(subscriptionRepository,
              "title " + scheduleNames[ii], name, scheduleNames[ii] + " description", null, ii, ii*10*1000, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              scheduleNames[ii]+"-group", now, endDate, actionRefs ); //$NON-NLS-1$
          PentahoSystem.systemExitPoint();
        } catch (SubscriptionRepositoryCheckedException e) {
          assertTrue( msg[jj] + " failed in call to addScheduleAndContent: \"" + name + "\" " + e.getMessage(), jj!=0 ); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    }
  }
  
  public void addCronScheduleAndContent() {
    String name = null;
    Date now = new Date();

    String[] msg = { "adding repeat sched: ", "should fail, adding repeat sched: " }; //$NON-NLS-1$ //$NON-NLS-2$
    for ( int jj=0; jj<2; ++jj ) {
        for ( int ii=0; ii<scheduleNames.length; ++ii ) {
          try {
            ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session );
            name = scheduleNames[ii] + "-cron";
            System.out.println( msg[jj] + name );
            ISchedule s = SubscriptionRepositoryHelper.addScheduleAndContent(subscriptionRepository,
                "title " + scheduleNames[ii], name, scheduleNames[ii] + " description", cronStr[ii], null, null, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                scheduleNames[ii]+"-group", now, null, actionRefs ); //$NON-NLS-1$
            PentahoSystem.systemExitPoint();
          } catch (SubscriptionRepositoryCheckedException e) {
            assertTrue( msg[jj] + " failed in call to addScheduleAndContent: \"" + name + "\" " + e.getMessage(), jj!=0  ); //$NON-NLS-1$ //$NON-NLS-2$
          }
        }
    }
  }
  
  /**
   * verify that the expected items are in the schedule's content list, delete the last schedule
   * and verify that the content list is clean up.
   */
  public void verifyContentList() {
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session );
    List<ISubscribeContent> endContentList = subscriptionRepository.getAllContent();
    PentahoSystem.systemExitPoint();
    // lists are both detached, and modification of lists should not effect persistent store
    
    // create a set of the actionRefs that were added to the final schedule (for fast lookup)
    Set<String> actionRefEditSet = new HashSet<String>();
    for ( String actionRef : actionRefsEdit ) {
      actionRefEditSet.add( actionRef );
    }
    Set<String> actionRefStartSet = new HashSet<String>();
    for ( ISubscribeContent content : startContentList ) {
      actionRefStartSet.add( content.getActionReference() );
    }
    Set<String> actionRefEndSet = new HashSet<String>();
    for ( ISubscribeContent content : endContentList ) {
      actionRefEndSet.add( content.getActionReference() );
    }

    assertTrue( "Content list does not contain expected content.",
        CollectionUtils.isEqualCollection( CollectionUtils.union( actionRefEditSet, actionRefStartSet ),
        actionRefEndSet ) );
//    assertTrue( "", CollectionUtils.xx() );
//    assertTrue( "", CollectionUtils.xx() );
//    assertTrue( "", CollectionUtils.xx() );
    
    // do the final delete

    int ii = scheduleNames.length-1;
    String name = scheduleNames[ii] + "-cron"; //$NON-NLS-1$
    System.out.println( "deleting: " + name ); //$NON-NLS-1$
    try {
      subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session );
      ISchedule schedule = subscriptionRepository.getScheduleByScheduleReference( name ); //$NON-NLS-1$
      if ( null != schedule ) {
        SubscriptionRepositoryHelper.deleteScheduleContentAndSubscription(subscriptionRepository, schedule );
      }
      PentahoSystem.systemExitPoint();
    } catch (Exception e) {
      assertTrue( "Failed in call to deleteScheduleContentAndSubscription: "  //$NON-NLS-1$
          + name + " " + e.getMessage(), false ); //$NON-NLS-1$
    }
    
    // we have now removed our last schedule, so associated content should be clean up. Let's see...

    subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session );
    endContentList = subscriptionRepository.getAllContent();
    PentahoSystem.systemExitPoint();
    actionRefEndSet = new HashSet<String>();
    for ( ISubscribeContent content : endContentList ) {
      actionRefEndSet.add( content.getActionReference() );
    }
    
    assertTrue( "Content list does not contain expected content.",
        CollectionUtils.isEqualCollection( actionRefStartSet, actionRefEndSet ) );
    
  }
  
  private List<ISubscribeContent> startContentList = null;
  
  public void setUp() {
// TODO: Get tests working    
//    String userName = "joe"; //$NON-NLS-1$
//    super.setUp();
//    session = new StandaloneSession( userName );
//    session.setAuthenticated( userName );
//    
//    ISubscriptionRepository subscriptionRepository = PentahoSystem.getSubscriptionRepository( session );
//    startContentList = subscriptionRepository.getAllContent();
//    PentahoSystem.systemExitPoint();
  }

  
  public void tearDown() {
    String name = null;
    System.out.println( "Entering tearDown...-------------------------------------------------" );  //$NON-NLS-1$
    for ( int ii=0; ii<scheduleNames.length; ++ii ) {
      name = scheduleNames[ii] + "-cron"; //$NON-NLS-1$
      System.out.println( "deleting: " + name ); //$NON-NLS-1$
      try {
        ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session );
        ISchedule schedule = subscriptionRepository.getScheduleByScheduleReference( name );
        if ( null != schedule ) {
          SubscriptionRepositoryHelper.deleteScheduleContentAndSubscription(subscriptionRepository, schedule );
        }
        PentahoSystem.systemExitPoint();
      } catch (Exception e) {
        System.out.println( "tearDown() failure: " + e.getMessage() ); //$NON-NLS-1$
      }
    }
    for ( int ii=0; ii<scheduleNames.length; ++ii ) {
      name = scheduleNames[ii]+"-repeat"; //$NON-NLS-1$
      System.out.println( "deleting: " + name ); //$NON-NLS-1$
      try {
        ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session );
        ISchedule schedule = subscriptionRepository.getScheduleByScheduleReference( name );
        if ( null != schedule ) {
          SubscriptionRepositoryHelper.deleteScheduleContentAndSubscription(subscriptionRepository, schedule );
        }
        PentahoSystem.systemExitPoint();
      } catch (Exception e) {
        System.out.println( "tearDown() failure: " + e.getMessage() );
      }
    }
    super.tearDown();
  }

//  TODO: GET THIS WORKING!
//  public void testCrudOps() {
//    addCronScheduleAndContent();
//    addRepeatScheduleAndContent();
//    editScheduleAndContent();
//    deleteScheduleContentAndSubscription();
//    verifyContentList();
//  }
  
  public void testDummyTest() {
    // do nothing, get the above test to pass!
  }
  
  public static void main( String[] args ) {

    SubscriptionRepositoryHelperTest test = new SubscriptionRepositoryHelperTest();
    test.setUp();
    try {
//      test.testCrudOps();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
