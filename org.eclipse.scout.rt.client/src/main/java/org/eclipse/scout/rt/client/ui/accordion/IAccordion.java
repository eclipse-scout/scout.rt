/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.accordion;

import java.util.Comparator;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.group.IGroup;

public interface IAccordion extends IWidget {

  String PROP_GROUPS = "groups";
  String PROP_EXCLUSIVE_EXPAND = "exclusiveExpand";
  String PROP_SCROLLABLE = "scrollable";

  /**
   * @return list of groups. Return value is never <code>null</code>.
   */
  List<? extends IGroup> getGroups();

  int getGroupCount();

  /**
   * @param groups
   *     the new list of groups to be set.
   */
  void setGroups(List<? extends IGroup> groups);

  /**
   * @return true if only one group may be expanded at the same time, false if not
   */
  boolean isExclusiveExpand();

  void setExclusiveExpand(boolean exclusiveExpand);

  /**
   * @return true if the accordion should be vertically scrollable, false if not
   */
  boolean isScrollable();

  void setScrollable(boolean scrollable);

  void addGroups(List<? extends IGroup> groups);

  void addGroup(IGroup group);

  void deleteGroups(List<? extends IGroup> groups);

  void deleteGroup(IGroup group);

  void deleteAllGroups();

  /**
   * Sets a comparator which is used by {@link #sort()} and calls {@link #sort()} immediately. The groups are sorted as
   * well whenever new groups are added.
   */
  void setComparator(Comparator<? extends IGroup> comparator);

  void setComparator(Comparator<? extends IGroup> comparator, boolean sortNow);

  Comparator<? extends IGroup> getComparator();

  /**
   * Sorts the groups by using the active {@link Comparator}. If no comparator is set the groups are displayed according
   * to the insertion order.
   * <p>
   * This method is typically executed automatically, but if you set a comparator with sortNow parameter set to false,
   * you need to call this method by yourself.
   */
  void sort();
}
