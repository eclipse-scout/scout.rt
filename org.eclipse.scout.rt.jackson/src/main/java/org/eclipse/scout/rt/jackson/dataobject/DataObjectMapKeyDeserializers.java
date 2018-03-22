/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject;

import org.eclipse.scout.rt.platform.ApplicationScoped;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.KeyDeserializers;

/**
 * Class that defines API for extensions that can provide additional deserializers for deserializer Map keys of various
 * types, from JSON property names.
 * <p>
 * <b>Note:</b> The difference between a key deserializer and an ordinary one is that the former transforms a JSON name
 * into a map key, whereas the latter reads any type of JSON value.
 */
@ApplicationScoped
public class DataObjectMapKeyDeserializers implements KeyDeserializers {

  @Override
  public KeyDeserializer findKeyDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
    return null;
  }
}
