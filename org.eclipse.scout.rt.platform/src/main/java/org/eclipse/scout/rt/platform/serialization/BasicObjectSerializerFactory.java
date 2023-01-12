/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.serialization;

/**
 * Factory for creating {@link BasicObjectSerializer} instances.
 *
 * @since 3.8.2
 */
public class BasicObjectSerializerFactory implements IObjectSerializerFactory {

  @Override
  public IObjectSerializer createObjectSerializer(IObjectReplacer objectReplacer) {
    return new BasicObjectSerializer(objectReplacer);
  }

  @Override
  public ClassLoader getClassLoader() {
    return BasicObjectSerializer.class.getClassLoader();
  }
}
