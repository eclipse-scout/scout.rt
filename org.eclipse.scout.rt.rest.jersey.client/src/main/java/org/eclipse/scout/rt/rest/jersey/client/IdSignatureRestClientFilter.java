/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.client;

import java.io.IOException;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.core.MultivaluedMap;

import org.eclipse.scout.rt.jackson.dataobject.JacksonIdSignatureDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.rest.client.IGlobalRestRequestFilter;
import org.eclipse.scout.rt.rest.client.IGlobalRestResponseFilter;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.EndpointConfigBase;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.ObjectReaderInjector;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.ObjectReaderModifier;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.ObjectWriterInjector;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.ObjectWriterModifier;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;

/**
 * Filter that adds an object mapper supporting signature creation for serialization/deserialization of request and
 * response. This feature can be enabled depending on the context e.g. headers of the request. By default, it checks the
 * {@link IdSignatureRestClientFilter#ID_SIGNATURE_HTTP_HEADER}.
 */
@Priority(Priorities.ENTITY_CODER)
public class IdSignatureRestClientFilter implements IGlobalRestRequestFilter, IGlobalRestResponseFilter {
  public static final String ID_SIGNATURE_HTTP_HEADER = "X-ScoutIdSignature";

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    ObjectWriterInjector.set(null);
    if (!enableSignature(requestContext)) {
      return;
    }
    ObjectWriterInjector.set(BEANS.get(IdSignatureObjectWriterModifier.class));
  }

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
    ObjectReaderInjector.set(null);
    if (!enableSignature(requestContext, responseContext)) {
      return;
    }
    ObjectReaderInjector.set(BEANS.get(IdSignatureObjectReaderModifier.class));
  }

  /**
   * Check the {@link ClientRequestContext} and {@link ClientResponseContext} if signature creation needs to be enabled.
   * Default implementation returns {@code false}.
   */
  protected boolean enableSignature(ClientRequestContext requestContext, ClientResponseContext responseContext) {
    return enableSignature(requestContext) || enableSignature(responseContext);
  }

  /**
   * Check the {@link ClientRequestContext} if signature creation needs to be enabled. Default implementation checks the
   * {@link IdSignatureRestClientFilter#ID_SIGNATURE_HTTP_HEADER}.
   */
  protected boolean enableSignature(ClientRequestContext requestContext) {
    return Boolean.TRUE.toString().equalsIgnoreCase(requestContext.getHeaderString(ID_SIGNATURE_HTTP_HEADER));
  }

  /**
   * Check the {@link ClientResponseContext} if signature creation needs to be enabled. Default implementation returns
   * {@code false}.
   */
  protected boolean enableSignature(ClientResponseContext responseContext) {
    return false;
  }

  @Bean
  public static class IdSignatureObjectReaderModifier extends ObjectReaderModifier {

    @Override
    public ObjectReader modify(EndpointConfigBase<?> endpoint, MultivaluedMap<String, String> httpHeaders, JavaType resultType, ObjectReader r, JsonParser p) {
      // FIXME rsb check this
      //noinspection deprecation
      return BEANS.get(JacksonIdSignatureDataObjectMapper.class)
          .getObjectMapper()
          .readerFor(resultType);
    }
  }

  @Bean
  public static class IdSignatureObjectWriterModifier extends ObjectWriterModifier {

    @Override
    public ObjectWriter modify(EndpointConfigBase<?> endpoint, MultivaluedMap<String, Object> responseHeaders, Object valueToWrite, ObjectWriter w) {
      // FIXME rsb check this
      //noinspection deprecation
      return BEANS.get(JacksonIdSignatureDataObjectMapper.class)
          .getObjectMapper()
          .writer();
    }
  }
}
