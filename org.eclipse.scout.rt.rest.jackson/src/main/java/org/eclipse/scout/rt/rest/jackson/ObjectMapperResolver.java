/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jackson;

import jakarta.ws.rs.ext.ContextResolver;

import org.eclipse.scout.rt.jackson.dataobject.JacksonDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.rest.RestApplication;
import org.eclipse.scout.rt.rest.RestApplicationContributors.ContextResolverContributor;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides an {@link ObjectMapper} instance produced by {@link JacksonDataObjectMapper}.
 * <p>
 * This class is intended to be registered in a REST application class.
 *
 * @see RestApplication
 * @see ContextResolverContributor
 */
@Bean
public class ObjectMapperResolver implements ContextResolver<ObjectMapper> {

  @Override
  @SuppressWarnings("deprecation") // allow access to internal object mapper instance
  public ObjectMapper getContext(Class<?> type) {
    return BEANS.get(JacksonDataObjectMapper.class).getObjectMapper();
  }
}
