/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.accordion;

import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.group.IGroup;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;

public interface IAccordion extends IWidget, ITypeWithClassId {

  String PROP_GROUPS = "groups";
  String PROP_EXCLUSIVE_EXPAND = "exclusiveExpand";
  String PROP_SCROLLABLE = "scrollable";
  String PROP_CONTAINER = "container";

  ITypeWithClassId getContainer();

  /**
   * @return list of groups. Return value is never <code>null</code>.
   */
  List<? extends IGroup> getGroups();

  int getGroupCount();

  /**
   * @param groups
   *          the new list of groups to be set.
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

}
