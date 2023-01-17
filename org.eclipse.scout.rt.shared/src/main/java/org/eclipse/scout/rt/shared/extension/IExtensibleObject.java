/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension;

import java.util.List;

/**
 * Describes an object that can hold {@link IExtension}s.
 *
 * @since 4.2
 */
public interface IExtensibleObject {

  /**
   * Gets all extensions for this object that are registered in the {@link IExtensionRegistry}.
   *
   * @return A {@link List} containing all {@link IExtension}s of this object.
   */
  List<? extends IExtension<?>> getAllExtensions();

  /**
   * Gets the extension of this object that exactly matches the given class.
   *
   * @param c
   *          The filter class to specify which {@link IExtension} to return. This class must exactly match. no
   *          <code>instanceof</code> is performed.
   * @return The requested {@link IExtension} if it exists for this object or null.
   */
  <T extends IExtension<?>> T getExtension(Class<T> c);
}
