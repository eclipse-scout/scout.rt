/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest;

import javax.ws.rs.ext.ContextResolver;

import org.eclipse.scout.rt.jackson.databind.ObjectMapperFactory;
import org.eclipse.scout.rt.platform.BEANS;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides an {@link ObjectMapper} instance produced by {@link ObjectMapperFactory}. This class is intended to be
 * registered in a REST application.
 */
public class ObjectMapperResolver implements ContextResolver<ObjectMapper> {

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return BEANS.get(ObjectMapperFactory.class).create();
  }
}
