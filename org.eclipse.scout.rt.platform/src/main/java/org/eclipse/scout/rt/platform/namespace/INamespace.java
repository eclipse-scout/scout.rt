/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.namespace;

import org.eclipse.scout.rt.platform.inventory.ClassInventory;

/**
 * Namespaces should be final classes and must have an empty constructor, see {@link ScoutNamespace} for an
 * example.<br/>
 * Use {@link Namespaces} to access the namespaces.
 * <p>
 * Namespaces are collected via {@link ClassInventory}, no registration is required.
 */
public interface INamespace {

  /**
   * Namespace IDs should be lowercase.
   *
   * @return non-empty unique ID for the namespace.
   */
  String getId();

  /**
   * @return Order of namespace
   */
  double getOrder();
}
