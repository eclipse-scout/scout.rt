/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.client;

import static org.junit.Assert.assertEquals;

import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import jakarta.ws.rs.client.Client;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.client.AbstractRestClientHelper;
import org.eclipse.scout.rt.rest.client.AntiCsrfClientFilter;
import org.eclipse.scout.rt.rest.client.HttpHeadersRequestFilter;
import org.eclipse.scout.rt.rest.client.proxy.RestClientProxyFactory;
import org.eclipse.scout.rt.rest.jackson.ObjectMapperResolver;
import org.eclipse.scout.rt.rest.jersey.JerseyTestRestClientHelper;
import org.eclipse.scout.rt.rest.jersey.LanguageAndCorrelationIdRestRequestFilter;
import org.eclipse.scout.rt.rest.jersey.client.multipart.MultipartMessageBodyWriter;
import org.junit.Test;

/**
 * Various test cases for {@link AbstractRestClientHelper}.
 */
public class AbstractRestClientHelperTest {

  @Test
  public void testDefaultSslContext() throws NoSuchAlgorithmException {
    JerseyTestRestClientHelper restClientHelper = BEANS.get(JerseyTestRestClientHelper.class);
    assertEquals(SSLContext.getDefault(), restClientHelper.rawClient().getSslContext());
  }

  @Test
  public void testBuildClient() {
    JerseyTestRestClientHelper restClientHelper = BEANS.get(JerseyTestRestClientHelper.class);
    Set<Class<?>> actualClasses = restClientHelper.rawClient().getConfiguration().getClasses();
    Set<Class<?>> expectedClasses = Set.of(ScoutInvocationBuilderListener.class, ScoutJobExecutorServiceProvider.class, MultipartMessageBodyWriter.class);
    assertEquals(expectedClasses, actualClasses);

    Set<Class<?>> actualInstances = restClientHelper.rawClient().getConfiguration().getInstances().stream().map(Object::getClass).collect(Collectors.toSet());
    Set<Class<?>> expectedInstances = Set.of(ObjectMapperResolver.class, AntiCsrfClientFilter.class, HttpHeadersRequestFilter.class, LanguageAndCorrelationIdRestRequestFilter.class);
    assertEquals(expectedInstances, actualInstances);
  }

  @Test
  public void testLazyClientInstance() {
    JerseyTestRestClientHelper restClientHelper = BEANS.get(JerseyTestRestClientHelper.class);
    Client client1 = BEANS.get(RestClientProxyFactory.class).unwrap(restClientHelper.client());
    Client client2 = BEANS.get(RestClientProxyFactory.class).unwrap(restClientHelper.client());
    assertEquals("expect same client instance on multiple calls", client1, client2);
  }
}
