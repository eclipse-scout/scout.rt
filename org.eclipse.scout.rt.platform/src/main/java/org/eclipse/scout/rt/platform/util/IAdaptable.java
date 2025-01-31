/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

/**
 * Adaptable objects allows to be converted into other objects without having to implement the particular interface.
 * This is kind of a dynamic extension on object level, and lets clients to query whether an object has a particular
 * extension.
 * <p>
 * For example,
 *
 * <pre>
 *     IAdaptable a = [some adaptable];
 *     IFoo x = (IFoo)a.getAdapter(IFoo.class);
 *     if (x != null)
 *         [do IFoo things with x]
 * </pre>
 *
 * @since 5.1
 */
public interface IAdaptable {

  /**
   * Asks the adaptable object to provide a representation of the given type.
   *
   * @param type
   *     the requested type.
   * @return the object requested, or <code>null</code> if not supported by the adaptable object.
   */
  <T> T getAdapter(Class<T> type);
}
