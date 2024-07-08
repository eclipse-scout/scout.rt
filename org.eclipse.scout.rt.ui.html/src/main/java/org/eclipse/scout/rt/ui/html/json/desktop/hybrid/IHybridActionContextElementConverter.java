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
public interface IHybridActionContextElementConverter<ADAPTER extends IJsonAdapter<?>, JSON_ELEMENT, MODEL_ELEMENT> {

  /**
   * Tries to convert the given JSON representation of an element (e.g. a string id) to the corresponding model element.
   * <p>
   * If the arguments are not supported by this converter, {@code null} is returned.
   */
  Object tryConvertFromJson(IJsonAdapter<?> adapter, Object jsonElement);

  /**
   * Tries to convert the given model element to a JSON representation (e.g. a string id).
   * <p>
   * If the arguments are not supported by this converter, {@code null} is returned.
   */
  Object tryConvertToJson(IJsonAdapter<?> adapter, Object modelElement);
}
