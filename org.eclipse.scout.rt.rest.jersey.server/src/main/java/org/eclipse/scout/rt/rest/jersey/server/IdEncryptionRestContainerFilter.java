/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.server;

import static java.util.Collections.*;
import static org.eclipse.scout.rt.platform.util.CollectionUtility.hashSet;

import java.util.Optional;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedMap;

import org.eclipse.scout.rt.dataobject.id.IdCodec.IIdCodecFlag;
import org.eclipse.scout.rt.dataobject.id.IdCodec.IdCodecFlag;
import org.eclipse.scout.rt.jackson.dataobject.JacksonIdEncryptionDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.rest.RestApplication.IRestApplicationSingletonsContributor;
import org.eclipse.scout.rt.rest.container.IRestContainerRequestFilter;
import org.eclipse.scout.rt.rest.container.IRestContainerResponseFilter;
import org.eclipse.scout.rt.rest.param.IIdParamConverterProvider.IdCodecFlags;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.cfg.EndpointConfigBase;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.cfg.ObjectReaderInjector;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.cfg.ObjectReaderModifier;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.cfg.ObjectWriterInjector;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.cfg.ObjectWriterModifier;
import org.glassfish.jersey.process.internal.RequestScoped;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Filter that adds an object mapper supporting encryption for serialization/deserialization of request and response.
 * This feature can be enabled depending on the context e.g. headers of the request. By default, it is always active.
 */
public class IdEncryptionRestContainerFilter implements IRestContainerRequestFilter, IRestContainerResponseFilter {

  protected static final Set<IIdCodecFlag> ENCRYPTION_FLAGS = unmodifiableSet(hashSet(IdCodecFlag.ENCRYPTION));

  @Inject
  private Provider<IdCodecFlags> m_idCodecFlagsProvider;

  protected void setIdCodecFlags(Set<IIdCodecFlag> idCodecFlags) {
    Optional.ofNullable(m_idCodecFlagsProvider)
        .map(Provider::get)
        .ifPresent(icf -> icf.set(idCodecFlags));
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    if (!enableEncryption(requestContext)) {
      return;
    }
    ObjectReaderInjector.set(BEANS.get(IdEncryptionObjectReaderModifier.class));
    setIdCodecFlags(ENCRYPTION_FLAGS);
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    if (!enableEncryption(requestContext, responseContext)) {
      return;
    }
    ObjectReaderInjector.set(BEANS.get(IdEncryptionObjectReaderModifier.class));
    ObjectWriterInjector.set(BEANS.get(IdEncryptionObjectWriterModifier.class));
    setIdCodecFlags(ENCRYPTION_FLAGS);
  }

  protected boolean enableEncryption(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    return enableEncryption(requestContext) || enableEncryption(responseContext);
  }

  protected boolean enableEncryption(ContainerRequestContext requestContext) {
    return true;
  }

  protected boolean enableEncryption(ContainerResponseContext responseContext) {
    return true;
  }

  @Bean
  public static class IdEncryptionObjectReaderModifier extends ObjectReaderModifier {

    @Override
    public ObjectReader modify(EndpointConfigBase<?> endpoint, MultivaluedMap<String, String> httpHeaders, JavaType resultType, ObjectReader r, JsonParser p) {
      //noinspection deprecation
      var objectMapper = BEANS.get(JacksonIdEncryptionDataObjectMapper.class).getObjectMapper();
      p.setCodec(objectMapper);
      return objectMapper.readerFor(r.getValueType());
    }
  }

  @Bean
  public static class IdEncryptionObjectWriterModifier extends ObjectWriterModifier {

    @Override
    public ObjectWriter modify(EndpointConfigBase<?> endpoint, MultivaluedMap<String, Object> responseHeaders, Object valueToWrite, ObjectWriter w, JsonGenerator g) {
      //noinspection deprecation
      var objectMapper = BEANS.get(JacksonIdEncryptionDataObjectMapper.class).getObjectMapper();
      g.setCodec(objectMapper);
      return objectMapper.writer();
    }
  }

  public static class IdCodecFlagsBinderSingletonContributor implements IRestApplicationSingletonsContributor {

    @Override
    public Set<Object> contribute() {
      return singleton(new AbstractBinder() {
        @Override
        protected void configure() {
          bindFactory(IdCodecFlags::new)
              .to(IdCodecFlags.class)
              .in(RequestScoped.class);
        }
      });
    }
  }
}
