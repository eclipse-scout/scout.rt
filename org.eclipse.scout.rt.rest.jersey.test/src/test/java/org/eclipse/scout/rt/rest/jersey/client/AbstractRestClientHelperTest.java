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

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.ext.ContextResolver;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.rest.client.AbstractRestClientHelper;
import org.eclipse.scout.rt.rest.client.AntiCsrfClientFilter;
import org.eclipse.scout.rt.rest.client.HttpHeadersRequestFilter;
import org.eclipse.scout.rt.rest.client.proxy.RestClientProxyFactory;
import org.eclipse.scout.rt.rest.jackson.ObjectMapperResolver;
import org.eclipse.scout.rt.rest.jersey.JerseyTestRestClientHelper;
import org.eclipse.scout.rt.rest.jersey.LanguageAndCorrelationIdRestRequestFilter;
import org.eclipse.scout.rt.rest.jersey.client.multipart.MultipartMessageBodyWriter;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

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

  @Test
  public void testDuplicatedContextResolverAsBean() {
    IBean<?> bean = BeanTestingHelper.get().registerBean(new BeanMetaData(ObjectMapperResolver2.class));
    try {
      JerseyTestRestClientHelper restClientHelper = new JerseyTestRestClientHelper();  // use separate instance to force re-init
      assertThrows(AssertionException.class, () -> restClientHelper.client());
    }
    finally {
      BeanTestingHelper.get().unregisterBean(bean);
    }
  }

  @Test
  public void testDuplicatedContextResolverManualRegister() {
    JerseyTestRestClientHelper restClientHelper = new JerseyTestRestClientHelperEx();
    assertThrows(AssertionException.class, () -> restClientHelper.client());
  }

  static class JerseyTestRestClientHelperEx extends JerseyTestRestClientHelper {
    @Override
    protected List<ContextResolver> getContextResolversToRegister() {
      List<ContextResolver> resolvers = super.getContextResolversToRegister();
      resolvers.add(new ObjectMapperResolver2());
      return resolvers;
    }
  }

  protected static class ObjectMapperResolver2 implements ContextResolver<ObjectMapper> {
    @Override
    public ObjectMapper getContext(Class<?> type) {
      return null;
    }
  }
}
