/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.notification;

import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Every implementation of this interface can be applied to coalesce objects of the same super class (generic type of
 * this class).
 */
@FunctionalInterface
@ApplicationScoped
public interface ICoalescer<T> {

  /**
   * @param data
   *     all objects to coalesce. Ordered, such that new data is inserted at the end of the list
   * @return a coalesced result. Never <code>null</code>.
   */
  List<T> coalesce(List<T> data);
}
