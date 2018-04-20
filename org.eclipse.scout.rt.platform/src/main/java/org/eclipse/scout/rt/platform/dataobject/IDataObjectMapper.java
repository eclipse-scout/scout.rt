/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.platform.dataobject;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Interface to a data mapper implementation handling the mapping between data objects and their serialized String
 * representation.
 *
 * @see IDoEntity
 * @see DoEntity
 */
@Bean
public interface IDataObjectMapper {

  /**
   * Deserialize from input stream to a data object.
   */
  <T> T readValue(InputStream inputStream, Class<T> valueType);

  /**
   * Deserialize a string value to a data object.
   */
  <T> T readValue(String value, Class<T> valueType);

  /**
   * Serializes a data object to the output stream.
   */
  void writeValue(OutputStream outputStream, Object value);

  /**
   * Serializes a data object to its string representation.
   */
  String writeValue(Object value);
}
