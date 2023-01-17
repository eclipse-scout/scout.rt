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
 * Factory for creating new {@link IObjectSerializer} instances.
 *
 * @since 3.8.2
 */
public interface IObjectSerializerFactory {

  /**
   * Creates a new instance of an {@link IObjectSerializer} using the given {@link IObjectReplacer}. Implementing
   * classes may share expensive objects like class loaders.
   *
   * @param objectReplacer
   *          an {@link IObjectReplacer} instance of <code>null</code>
   * @return
   */
  IObjectSerializer createObjectSerializer(IObjectReplacer objectReplacer);

  /**
   * @return Returns an environment-dependent ClassLoader that is able to load all classes that are available in the
   *         running environment. The class loader should also be used by {@link IObjectSerializer}s returned by
   *         {@link #createObjectSerializer(IObjectReplacer)}. At least it has to support the same set of classes.
   */
  ClassLoader getClassLoader();
}
