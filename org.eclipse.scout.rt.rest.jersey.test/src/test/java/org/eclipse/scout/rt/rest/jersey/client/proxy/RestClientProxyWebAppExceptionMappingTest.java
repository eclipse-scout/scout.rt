/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.jersey.client.proxy;

import static org.junit.Assert.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.client.RestClientProperties;
import org.eclipse.scout.rt.rest.client.proxy.RestClientProxyFactory;
import org.eclipse.scout.rt.rest.jersey.JerseyTestApplication;
import org.eclipse.scout.rt.rest.jersey.JerseyTestRestClientHelper;
import org.glassfish.jersey.client.JerseyInvocation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class RestClientProxyWebAppExceptionMappingTest {

  @BeforeClass
  public static void beforeClass() {
    BEANS.get(JerseyTestApplication.class).ensureStarted();
  }

  /**
   * Verify that {@link RestClientProxyFactory#convertToWebAppException} behaves the same as {@link JerseyInvocation}.
   */
  @Test
  public void testExceptionMapping() {
    TestingRestClientProxyFactory proxyFactory = new TestingRestClientProxyFactory();
    JerseyTestRestClientHelper helper = BEANS.get(JerseyTestRestClientHelper.class);

    // do not follow redirects (otherwise 301 check will not work)
    helper.client().property(RestClientProperties.FOLLOW_REDIRECTS, false);

    WebTarget target = helper
        .target("echo", null); // use identity exception transformer

    // status 1xx and 2xx are not checked because some of them change the behavior of HTTP clients
    for (int status = 300; status < 1000; status++) {
      // invoke REST service and let jersey transform the status code into an exception
      final int finalStatus = status;
      final WebApplicationException remoteException = Assert.assertThrows(WebApplicationException.class, () -> target
          .queryParam("status", finalStatus)
          .request()
          .get());

      // mock response and convert it into an exception using duplicated converter method
      try (Response mockResponse = Response.status(status).build()) {
        WebApplicationException convertedException = proxyFactory.convertToWebAppException(mockResponse);
        assertSame(mockResponse, convertedException.getResponse());
        assertEquals(status, convertedException.getResponse().getStatus());

        // converted exception must have same type as the one thrown by jersey
        assertSame("status: " + status, remoteException.getClass(), convertedException.getClass());
      }
    }
  }

  /**
   * Testing class required because of method visibility restrictions.
   */
  private static class TestingRestClientProxyFactory extends RestClientProxyFactory {
    @Override
    protected WebApplicationException convertToWebAppException(Response response) {
      return super.convertToWebAppException(response);
    }
  }
}
