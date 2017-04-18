/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.databind;

import java.io.IOException;

import org.eclipse.scout.rt.platform.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;

/**
 * Default {@link DeserializationProblemHandler} implementation. Provides callback methods to handle all kind of
 * deserialization problems.
 * <p>
 * The default core implementation ignores unknown properties and does not handle other kind of deserialization
 * problems.
 */
@Bean
public class DefaultDeserializationProblemHandler extends DeserializationProblemHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultDeserializationProblemHandler.class);

  @Override
  public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException {
    LOG.warn("Ignoring unknown property {} on object {}", propertyName, beanOrClass);
    return true;
  }
}
