/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
