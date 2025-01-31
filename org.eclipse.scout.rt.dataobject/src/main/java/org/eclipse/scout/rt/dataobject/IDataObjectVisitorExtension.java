/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.Order;

/**
 * Visitor extensions are used to implement visit/replace behavior for custom composite objects, i.e. objects that
 * itself need further visiting for its children.
 * <p>
 * If a more specific implementation for a certain subclass is required, use a lower {@link Order} annotation.
 */
@ApplicationScoped
public interface IDataObjectVisitorExtension<T> {

  /**
   * @return Class that needs custom handling (applied for all subclasses of this class too).
   */
  Class<T> valueClass();

  /**
   * Visit implementation for given value. The value itself doesn't need to be handled, only its children.
   *
   * @param value
   *     Value to handle its children
   * @param chain
   *     Visit chain to call for children (<code>null</code>-safe chain call). Do not call chain for value itself,
   *     would result in an endless recursive loop.
   */
  void visit(T value, Consumer<Object> chain);

  /**
   * Replace or visit implementation for the given value. The value itself doesn't need to be handled, only its
   * children.
   * <p>
   * It's recommended to check for changes within the children before creating a new instance of the given value class.
   * If the children didn't change, return the value provided via parameter to prevent unnecessary instance creation.
   * <p>
   * A data object visitor extension is most likely used for immutable objects. When children values change, a new
   * instance of the given value class is created. In case of mutable objects, it's not recommended changing the
   * existing instance, but creating a new instance instead.
   *
   * @param value
   *     Value to handle its children
   * @param chain
   *     'Replace or visit' chain to call for children (<code>null</code>-safe chain call). Do not call chain for
   *     value itself, would result in an endless recursive loop
   * @return Same or replaced value
   */
  T replaceOrVisit(T value, UnaryOperator<Object> chain);
}
