/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop.hybrid;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;

@ApplicationScoped
public interface IHybridActionContextElementResolver {

  /**
   * Convert the given "json representation" of an element (e.g. a string id) to the corresponding model element.
   */
  Object resolveElement(IJsonAdapter<?> adapter, Object element);

  /**
   * Converts the given model element to a "json representation" (e.g. a string id).
   */
  Object dissolveElement(IJsonAdapter<?> adapter, Object element);
}
