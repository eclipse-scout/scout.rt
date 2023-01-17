/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui;

import java.util.Collection;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * This interface collects some recent events of a model objects for the purpose of retaining them for late-attaching ui
 * widgets.
 * <p>
 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=375522
 * <p>
 * The history may contain all or just some events that are of special interest for late attaching ui components. This
 * basically just includes those events that have no state associated with it, such as requestFocus, scrollToSelection
 * (the trigger variant) etc.
 * <p>
 * see {@link ITree#getEventHistory()}, {@link ITable#getEventHistory()}, {@link IForm#getEventHistory()}
 * <p>
 * This object should be thread safe.
 *
 * @since 3.8
 */
public interface IEventHistory<T> {
  /**
   * This method is called whenever the tree fires out an event
   */
  void notifyEvent(T event);

  /**
   * @return a list with the most recent events within the timeout range
   */
  Collection<T> getRecentEvents();

}
