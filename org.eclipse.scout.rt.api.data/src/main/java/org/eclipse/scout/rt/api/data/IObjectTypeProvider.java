/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Provides the {@link ObjectType} for a class.
 */
@ApplicationScoped
public interface IObjectTypeProvider {

  /**
   * Returns the object type for the given class.
   *
   * @param type
   *     The class for which an object type should be provided.
   * @return The object type or {@code null} if this provider does not provide
   * an object type for the given class.
   */
  String objectTypeOf(Class<?> type);
}
