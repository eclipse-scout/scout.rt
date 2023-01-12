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
 * This {@link IObjectSerializer} implementation is designed to be used outside an OSGi environment. All classes are
 * expected to be loaded by the application class loader.
 *
 * @since 3.8.2
 */
public class BasicObjectSerializer extends AbstractObjectSerializer {

  public BasicObjectSerializer(IObjectReplacer objectReplacer) {
    super(objectReplacer);
  }
}
