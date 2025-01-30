/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop.hybrid.converter;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;

/**
 * Instances of this interface are used to convert a model element to a JSON representation and vice versa. Each
 * instance typically only handles a specific type of element. Because elements have no common structure, they are
 * always accompanied by the widget that owns them (here represented by the corresponding {@link IJsonAdapter}).
 * <p>
 * How to add a new converter:
 * <ul>
 * <li>Create a subclass of the abstract class {@link AbstractHybridActionContextElementConverter}.
 * <li>Specify the types of the generic parameters {@link ADAPTER}, {@link JSON_ELEMENT} and {@link MODEL_ELEMENT}.
 * <li>Implement the abstract methods `jsonToElement` and `elementToJson`. They are only called if the arguments were
 * accepted by the corresponding methods. Otherwise, the task is delegated to the next converter.
 * </ul>
 *
 * @param <ADAPTER>
 *     {@link IJsonAdapter} type of the owner widget (e.g. JsonTree)
 * @param <JSON_ELEMENT>
 *     Type of the JSON representation of the element (e.g. String)
 * @param <MODEL_ELEMENT>
 *     Type of the model representation of the element (e.g. TreeNode)
 */
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
