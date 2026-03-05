/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.cancellation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.security.auth.Subject;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.holders.StringHolder;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.rest.RestHttpHeaders;
import org.eclipse.scout.rt.rest.client.RestClientProperties;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

@RunWith(PlatformTestRunner.class)
public class RestRequestCancellationClientRequestFilterTest {

  @Test
  public void testSubjectSameRunContext() {
    Subject subject = mock(Subject.class);
    RunContext runContext = RunContexts.empty().withSubject(subject);

    RunContext cancellationRunContext = testCancellationSubject(runContext, runContext);
    assertEquals(subject, cancellationRunContext.getSubject());
  }

  @Test
  public void testSubjectFallback() {
    Subject subject = mock(Subject.class);
    RunContext runContext = RunContexts.empty().withSubject(subject);

    // if context calling the cancellation does not include a Subject, the Subject of the original request should be used
    RunContext cancellationRunContext = testCancellationSubject(runContext, RunContexts.empty());
    assertEquals(subject, cancellationRunContext.getSubject());
  }

  @Test
  public void testSubjectDifferent() {
    Subject subject1 = mock(Subject.class);
    Subject subject2 = mock(Subject.class);

    // normally the Subject of the context calling the cancellation should be used for cancel call
    RunContext cancellationRunContext = testCancellationSubject(RunContexts.empty().withSubject(subject1), RunContexts.empty().withSubject(subject2));
    assertEquals(subject2, cancellationRunContext.getSubject());
  }

  @Test
  public void testSubjectNone() {
    // if original request and context calling the cancellation does not include a Subject there is not much we can do, we try to run cancellation also without Subject (which might fail though)
    RunContext cancellationRunContext = testCancellationSubject(RunContexts.empty(), RunContexts.empty());
    assertNull(cancellationRunContext.getSubject());
  }

  protected RunContext testCancellationSubject(RunContext requestRunContext, RunContext cancellationRunContext) {
    Holder<RunContext> cancellationRunContextHolder = new Holder<>();
    StringHolder cancellationRequestIdHolder = new StringHolder();

    ClientRequestContext clientRequestContext = mock(ClientRequestContext.class, RETURNS_DEFAULTS);
    MultivaluedHashMap<String, Object> headerMap = new MultivaluedHashMap<>();
    when(clientRequestContext.getHeaders()).thenReturn(headerMap);

    requestRunContext.run(() -> new RestRequestCancellationClientRequestFilter(requestId -> {
      cancellationRunContextHolder.setValue(RunContext.CURRENT.get());
      cancellationRequestIdHolder.setValue(requestId);
    }).filter(clientRequestContext));

    cancellationRunContext.run(() -> {
      ArgumentCaptor<ICancellable> cancellable = ArgumentCaptor.forClass(ICancellable.class);
      verify(clientRequestContext, times(1)).setProperty(eq(RestClientProperties.CANCELLABLE), cancellable.capture());
      cancellable.getValue().cancel(false);
    });

    assertNotNull(cancellationRequestIdHolder.getValue());
    assertEquals(1, headerMap.size()); // requestId is added as header
    assertEquals(CollectionUtility.firstElement(headerMap.get(RestHttpHeaders.REQUEST_ID)), cancellationRequestIdHolder.getValue()); // requestId is also added as header

    RunContext cancellationRunContextHolderValue = cancellationRunContextHolder.getValue();
    assertNotNull(cancellationRunContextHolderValue);
    assertNotEquals(cancellationRunContext, cancellationRunContextHolderValue); // cancellation uses different RunContext
    return cancellationRunContextHolderValue;
  }
}
