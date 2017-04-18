/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.databind;

import java.util.TimeZone;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Factory that creates the {@link ObjectMapper} instance.
 */
@ApplicationScoped
public class ObjectMapperFactory {

  public ObjectMapper create() {
    ObjectMapper mapper = new ObjectMapper();
    initializeObjectMapper(mapper);
    return mapper;
  }

  protected void initializeObjectMapper(ObjectMapper mapper) {
    // Use host's local time zone to (de)serialize dates
    mapper.setTimeZone(TimeZone.getDefault());
    // Use custom deserialization problem handler
    mapper.addHandler(BEANS.get(DefaultDeserializationProblemHandler.class));
    // Use custom annotation introspector
    mapper.setAnnotationIntrospector(BEANS.get(JandexJacksonAnnotationIntrospector.class));
  }
}
