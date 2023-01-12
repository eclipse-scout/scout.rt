/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Collector for all items in a tree traversal.<br>
 * Converts the items of a tree into a {@link List}.<br>
 * The items are collected in pre-order (top-down).
 *
 * @since 8.0
 */
public class CollectingVisitor<T> extends DepthFirstTreeVisitor<T> {

  private final List<T> m_list = new ArrayList<>();
  private final Predicate<T> m_filter;

  public CollectingVisitor() {
    this(null);
  }

  public CollectingVisitor(Predicate<T> filter) {
    m_filter = filter;
  }

  @Override
  public TreeVisitResult preVisit(T element, int level, int index) {
    if (accept(element)) {
      collect(element);
    }
    return TreeVisitResult.CONTINUE;
  }

  /**
   * Filters the collected items.
   */
  protected boolean accept(T element) {
    return m_filter == null || m_filter.test(element);
  }

  /**
   * Add the specified item to the collected list.
   */
  public void collect(T o) {
    m_list.add(o);
  }

  /**
   * @return All collected items.
   */
  public List<T> getCollection() {
    return m_list;
  }
}
