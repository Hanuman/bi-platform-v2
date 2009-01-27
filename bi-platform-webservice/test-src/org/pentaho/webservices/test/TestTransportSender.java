package org.pentaho.webservices.test;

import java.io.ByteArrayOutputStream;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.TransportUtils;

public class TestTransportSender implements TransportSender {

  public static String transportOutStr = null;
  
  public void cleanup(MessageContext msgContext) throws AxisFault {
    System.out.println( "TestTransportSender.cleanup 1 " ); //$NON-NLS-1$
  }

  public void init(ConfigurationContext confContext, TransportOutDescription transportOut) throws AxisFault {
    System.out.println( "TestTransportSender.init 1 " ); //$NON-NLS-1$
  }

  public void stop() {
    System.out.println( "TestTransportSender.stop " ); //$NON-NLS-1$
  }

  public void cleanup() {
    System.out.println( "TestTransportSender.cleanup 2 " ); //$NON-NLS-1$
  }

  public void flowComplete(MessageContext msgContext) {
    System.out.println( "TestTransportSender.flowComplete " ); //$NON-NLS-1$
  }

  public HandlerDescription getHandlerDesc() {
    System.out.println( "TestTransportSender.getHandlerDesc " ); //$NON-NLS-1$
    return null;
  }

  public String getName() {
    System.out.println( "TestTransportSender.getName " ); //$NON-NLS-1$
    return "testname"; //$NON-NLS-1$
  }

  public Parameter getParameter(String name) {
    System.out.println( "TestTransportSender.getParameter "+name ); //$NON-NLS-1$
    return null;
  }

  public void init(HandlerDescription handlerDesc) {
    System.out.println( "TestTransportSender.init 2 " ); //$NON-NLS-1$

  }

  public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    TransportUtils.writeMessage( msgContext, out);
    TestTransportSender.transportOutStr = new String( out.toByteArray() );
    System.out.println( "TestTransportSender.invoke " ); //$NON-NLS-1$
    return null;
  }

}
