/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
