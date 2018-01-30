/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.platform.dataobject;

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
   * Deserialize a string value to a data object.
   */
  <T> T readValue(String value, Class<T> valueType);

  /**
   * Serializes a data object to its string representation.
   */
  String writeValue(Object value);
}
