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
import org.eclipse.scout.rt.jackson.dataobject.JacksonIdSignatureDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.rest.RestApplication.IRestApplicationSingletonsContributor;
import org.eclipse.scout.rt.rest.container.IRestContainerRequestFilter;
import org.eclipse.scout.rt.rest.container.IRestContainerResponseFilter;
import org.eclipse.scout.rt.rest.param.IIdParamConverterProvider;
import org.eclipse.scout.rt.rest.param.IIdParamConverterProvider.AbstractIdCodecParamConverter;
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
 * Filter that adds an object mapper supporting signature creation for serialization/deserialization of request and
 * response. In addition, the {@link AbstractIdCodecParamConverter}s provided by {@link IIdParamConverterProvider} are
 * will also use signatures. This feature can be enabled depending on the context e.g. headers of the request. By
 * default, it is always active.
 */
public class IdSignatureRestContainerFilter implements IRestContainerRequestFilter, IRestContainerResponseFilter {
  public static final String ID_SIGNATURE_HTTP_HEADER = "X-ScoutIdSignature";

  protected static final Set<IIdCodecFlag> SIGNATURE_FLAGS = unmodifiableSet(hashSet(IdCodecFlag.SIGNATURE));

  /**
   * {@link IIdCodecFlag}s passed to this holder will be available to the {@link AbstractIdCodecParamConverter}s
   * provided by {@link IIdParamConverterProvider}. There will be a new holder for each request.
   */
  @Inject
  protected Provider<IdCodecFlags> m_idCodecFlagsProvider;

  protected void setIdCodecFlags(Set<IIdCodecFlag> idCodecFlags) {
    Optional.ofNullable(m_idCodecFlagsProvider)
        .map(Provider::get)
        .ifPresent(icf -> icf.set(idCodecFlags));
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    ObjectReaderInjector.set(null);
    if (!enableSignature(requestContext)) {
      return;
    }
    ObjectReaderInjector.set(BEANS.get(IdSignatureObjectReaderModifier.class));
    setIdCodecFlags(SIGNATURE_FLAGS);
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    ObjectWriterInjector.set(null);
    if (!enableSignature(requestContext, responseContext)) {
      return;
    }
    ObjectWriterInjector.set(BEANS.get(IdSignatureObjectWriterModifier.class));
    setIdCodecFlags(SIGNATURE_FLAGS);
  }

  /**
   * Check the {@link ContainerRequestContext} and {@link ContainerResponseContext} if signature creation needs to be
   * enabled. Default implementation returns {@code false}.
   */
  protected boolean enableSignature(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    return enableSignature(requestContext) || enableSignature(responseContext);
  }

  /**
   * Check the {@link ContainerRequestContext} if signature creation needs to be enabled. Default implementation checks
   * the {@link IdSignatureRestContainerFilter#ID_SIGNATURE_HTTP_HEADER}.
   */
  protected boolean enableSignature(ContainerRequestContext requestContext) {
    return Boolean.TRUE.toString().equalsIgnoreCase(requestContext.getHeaderString(ID_SIGNATURE_HTTP_HEADER));
  }

  /**
   * Check the {@link ContainerResponseContext} if signature creation needs to be enabled. Default implementation
   * returns {@code false}.
   */
  protected boolean enableSignature(ContainerResponseContext responseContext) {
    return false;
  }

  @Bean
  public static class IdSignatureObjectReaderModifier extends ObjectReaderModifier {

    @Override
    public ObjectReader modify(EndpointConfigBase<?> endpoint, MultivaluedMap<String, String> httpHeaders, JavaType resultType, ObjectReader r, JsonParser p) {
      //noinspection deprecation
      var objectMapper = BEANS.get(JacksonIdSignatureDataObjectMapper.class).getObjectMapper();
      p.setCodec(objectMapper);
      return objectMapper.readerFor(r.getValueType());
    }
  }

  @Bean
  public static class IdSignatureObjectWriterModifier extends ObjectWriterModifier {

    @Override
    public ObjectWriter modify(EndpointConfigBase<?> endpoint, MultivaluedMap<String, Object> responseHeaders, Object valueToWrite, ObjectWriter w, JsonGenerator g) {
      //noinspection deprecation
      var objectMapper = BEANS.get(JacksonIdSignatureDataObjectMapper.class).getObjectMapper();
      g.setCodec(objectMapper);
      return objectMapper.writer();
    }
  }

  /**
   * Contributes an {@link AbstractBinder} that binds {@link IdCodecFlags} in a {@link RequestScoped}. This will create
   * a single instance of {@link IdCodecFlags} per request and inject it e.g. into member variables of the type
   * {@link IdCodecFlags} which are annotated with {@link Inject}.
   */
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
