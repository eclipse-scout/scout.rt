/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 5.2
 */
@RunWith(PlatformTestRunner.class)
public class WsConsumerCorrelationIdHandlerTest {

  @Test
  public void testHandleInboundMessage() {
    // mock message context
    SOAPMessageContext ctx = mock(SOAPMessageContext.class);
    when(ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(Boolean.FALSE);

    // handle outbound message
    WsConsumerCorrelationIdHandler handler = new WsConsumerCorrelationIdHandler();
    handler.handleMessage(ctx);

    // verify invocation
    verify(ctx, times(1)).get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    verifyNoMoreInteractions(ctx);
  }

  @Test
  public void testHandleOutboundMessageCidSet() {
    testHandleOutboundMessage("correlationIdProvidedByContext");
  }

  @Test
  public void testHandleOutboundMessageEmptyCidSet() {
    testHandleOutboundMessage("");
  }

  @Test
  public void testHandleOutboundMessageCidNotSet() {
    testHandleOutboundMessage(null);
  }

  protected void testHandleOutboundMessage(final String cid) {
    RunContexts.copyCurrent()
        .withCorrelationId(cid)
        .run(new IRunnable() {
          @Override
          public void run() throws Exception {
            assertHandleOutboundMessage(cid);
          }
        });
  }

  protected void assertHandleOutboundMessage(String expectedCid) {
    // mock request context
    Map<String, Object> headers = new HashMap<>();

    // mock message context
    SOAPMessageContext ctx = mock(SOAPMessageContext.class);
    when(ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(Boolean.TRUE);
    when(ctx.get(MessageContext.HTTP_REQUEST_HEADERS)).thenReturn(headers);

    // handle inbound message
    WsConsumerCorrelationIdHandler handler = new WsConsumerCorrelationIdHandler();
    handler.handleMessage(ctx);

    // verify invocation
    verify(ctx, times(1)).get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

    if (expectedCid != null) {
      verify(ctx, times(1)).get(MessageContext.HTTP_REQUEST_HEADERS);
      verify(ctx, times(1)).put(MessageContext.HTTP_REQUEST_HEADERS, headers);

      // verify cid
      assertEquals(1, headers.size());
      assertTrue(headers.containsKey(CorrelationId.HTTP_HEADER_NAME));
      Object cids = headers.get(CorrelationId.HTTP_HEADER_NAME);
      assertNotNull(cids);
      assertTrue(cids instanceof List<?>);
      List<?> cidList = (List<?>) cids;
      assertEquals(1, cidList.size());
      assertEquals(expectedCid, cidList.get(0));
    }
    verifyNoMoreInteractions(ctx);
  }
}
