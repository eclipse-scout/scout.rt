package org.eclipse.scout.rt.server.jaxws.handler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.server.jaxws.MessageContexts;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 5.2
 */
@RunWith(PlatformTestRunner.class)
public class WsProviderCorrelationIdHandlerTest {

  public static final String STATIC_CORRELATION_ID = "staticCorrelationIdFor" + WsProviderCorrelationIdHandler.class.getSimpleName();
  private static IBean<?> s_beans;

  @BeforeClass
  public static void beforeClass() {
    s_beans = TestingUtility.registerBean(new BeanMetaData(P_FixtureCorrelationId.class, new P_FixtureCorrelationId()).withApplicationScoped(true).withReplace(true));
  }

  private static final class P_FixtureCorrelationId extends CorrelationId {
    @Override
    public String newCorrelationId() {
      return STATIC_CORRELATION_ID;
    }
  }

  @AfterClass
  public static void afterClass() {
    TestingUtility.unregisterBean(s_beans);
  }

  @Test
  public void testSetup() {
    assertEquals(STATIC_CORRELATION_ID, BEANS.get(CorrelationId.class).newCorrelationId());
  }

  @Test
  public void testHandleInboundMessageHeaderSet() {
    String cid = "correlationIdProvidedByHttpHeader";
    assertHandleInboundMessage(cid, cid);
  }

  @Test
  public void testHandleInboundMessageHeaderEmpty() {
    assertHandleInboundMessage(STATIC_CORRELATION_ID, "");
  }

  @Test
  public void testHandleInboundMessageHeaderNotSet() {
    assertHandleInboundMessage(STATIC_CORRELATION_ID, null);
  }

  @Test
  public void testHandleOutboundMessage() {
    // mock message context
    SOAPMessageContext ctx = mock(SOAPMessageContext.class);
    when(ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(Boolean.TRUE);

    // handle outbound message
    WsProviderCorrelationIdHandler handler = new WsProviderCorrelationIdHandler();
    handler.handleMessage(ctx);

    // verify invocation
    verify(ctx, times(1)).get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    verifyNoMoreInteractions(ctx);
  }

  protected void assertHandleInboundMessage(String expectedCid, String cidHeaderValue) {
    // create HTTP headers
    Map<String, List<String>> httpHeaders = new HashMap<String, List<String>>();
    if (cidHeaderValue != null) {
      httpHeaders.put(CorrelationId.HTTP_HEADER_NAME, Collections.singletonList(cidHeaderValue));
    }

    // mock message context
    SOAPMessageContext ctx = mock(SOAPMessageContext.class);
    when(ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(Boolean.FALSE);
    when(ctx.get(MessageContext.HTTP_REQUEST_HEADERS)).thenReturn(httpHeaders);

    // handle inbound message
    WsProviderCorrelationIdHandler handler = new WsProviderCorrelationIdHandler();
    handler.handleMessage(ctx);

    // verify invocation
    verify(ctx, times(1)).get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    verify(ctx, times(1)).get(MessageContext.HTTP_REQUEST_HEADERS);
    verify(ctx, times(1)).put(MessageContexts.PROP_CORRELATION_ID, expectedCid);
    verify(ctx, times(1)).setScope(MessageContexts.PROP_CORRELATION_ID, Scope.APPLICATION);
    verifyNoMoreInteractions(ctx);
  }
}
