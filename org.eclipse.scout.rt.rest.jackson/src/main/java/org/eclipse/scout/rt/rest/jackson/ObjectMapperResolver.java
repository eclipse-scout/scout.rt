/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.jackson;

import javax.ws.rs.ext.ContextResolver;

import org.eclipse.scout.rt.jackson.dataobject.JacksonDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.rest.RestApplication;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides an {@link ObjectMapper} instance produced by {@link JacksonDataObjectMapper}.
 * <p>
 * This class is intended to be registered in a REST application class.
 *
 * @see RestApplication#registerContextResolver()
 */
@Bean
public class ObjectMapperResolver implements ContextResolver<ObjectMapper> {

  @Override
  @SuppressWarnings("deprecation") // allow access to internal object mapper instance
  public ObjectMapper getContext(Class<?> type) {
    return BEANS.get(JacksonDataObjectMapper.class).getObjectMapper();
  }
}
