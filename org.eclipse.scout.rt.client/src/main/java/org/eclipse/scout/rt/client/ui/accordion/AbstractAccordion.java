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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.group.IGroup;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

@ClassId("0f4a0100-ef2b-46e2-809b-56ed62c56006")
public abstract class AbstractAccordion extends AbstractWidget implements IAccordion {
  private Comparator<? extends IGroup> m_comparator;

  public AbstractAccordion() {
    this(true);
  }

  public AbstractAccordion(boolean callInitializer) {
    super(false);
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setExclusiveExpand(getConfiguredExclusiveExpand());
    setScrollable(getConfiguredScrollable());
    OrderedCollection<IGroup> groups = new OrderedCollection<>();
    injectGroupsInternal(groups);
    setGroups(groups.getOrderedList());
  }

  protected void injectGroupsInternal(OrderedCollection<IGroup> groups) {
    List<Class<? extends IGroup>> groupClasses = getConfiguredGroups();
    for (Class<? extends IGroup> groupClass : groupClasses) {
      IGroup group = createGroupInternal(groupClass);
      if (group != null) {
        groups.addOrdered(group);
      }
    }
  }

  protected IGroup createGroupInternal(Class<? extends IGroup> tileClass) {
    return ConfigurationUtility.newInnerInstance(this, tileClass);
  }

  protected List<Class<? extends IGroup>> getConfiguredGroups() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IGroup>> filtered = ConfigurationUtility.filterClasses(dca, IGroup.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  @Override
  public List<? extends IGroup> getGroups() {
    return CollectionUtility.arrayList(propertySupport.getPropertyList(PROP_GROUPS));
  }

  @Override
  public int getGroupCount() {
    return getGroupsInternal().size();
  }

  /**
   * @return the live list of the groups
   */
  protected List<? extends IGroup> getGroupsInternal() {
    return propertySupport.getPropertyList(PROP_GROUPS);
  }

  @Override
  public void setGroups(List<? extends IGroup> groups) {
    if (CollectionUtility.equalsCollection(getGroupsInternal(), groups, true)) {
      return;
    }
    List<? extends IGroup> existingGroups = ObjectUtility.nvl(getGroupsInternal(), new ArrayList<>());
    groups = ObjectUtility.nvl(groups, new ArrayList<>());

    // Dispose old groups (only if they are not in the new list)
    List<IGroup> groupsToDelete = new ArrayList<>(existingGroups);
    groupsToDelete.removeAll(groups);
    deleteGroupsInternal(groupsToDelete);

    // Initialize new groups
    // Only initialize when groups are added later,
    // if they are added while initConfig runs, initGroups() will take care of the initialization which will be called by the container (e.g. GroupsField)
    List<IGroup> groupsToInsert = new ArrayList<>(groups);
    groupsToInsert.removeAll(existingGroups);
    addGroupsInternal(groupsToInsert);

    sortInternal(groups);
    setGroupsInternal(groups);
  }

  protected void addGroupsInternal(List<IGroup> groupsToInsert) {
    for (IGroup group : groupsToInsert) {
      addGroupInternal(group);
    }
    // Initialize after every group has been linked to the container, so that it is possible to access other groups in group.execInit
    if (isInitConfigDone()) {
      for (IGroup group : groupsToInsert) {
        group.init();
      }
    }
  }

  protected void addGroupInternal(IGroup group) {
    group.setParentInternal(this);
  }

  protected void deleteGroupsInternal(List<IGroup> groupsToDelete) {
    for (IGroup group : groupsToDelete) {
      deleteGroupInternal(group);
    }
  }

  protected void deleteGroupInternal(IGroup group) {
    group.dispose();
  }

  protected void setGroupsInternal(List<? extends IGroup> groups) {
    propertySupport.setPropertyList(PROP_GROUPS, groups);
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), getGroups());
  }

  @Override
  public void addGroups(List<? extends IGroup> groupsToAdd) {
    List<IGroup> groups = new ArrayList<>(getGroupsInternal());
    groups.addAll(groupsToAdd);
    setGroups(groups);
  }

  @Override
  public void addGroup(IGroup group) {
    addGroups(CollectionUtility.arrayList(group));
  }

  @Override
  public void deleteGroups(List<? extends IGroup> groupsToDelete) {
    List<IGroup> groups = new ArrayList<>(getGroupsInternal());
    groups.removeAll(groupsToDelete);
    setGroups(groups);
  }

  @Override
  public void deleteGroup(IGroup group) {
    deleteGroups(CollectionUtility.arrayList(group));
  }

  @Override
  public void deleteAllGroups() {
    setGroups(new ArrayList<>());
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  protected boolean getConfiguredExclusiveExpand() {
    return true;
  }

  @Override
  public boolean isExclusiveExpand() {
    return propertySupport.getPropertyBool(PROP_EXCLUSIVE_EXPAND);
  }

  @Override
  public void setExclusiveExpand(boolean exclusiveExpand) {
    propertySupport.setPropertyBool(PROP_EXCLUSIVE_EXPAND, exclusiveExpand);
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  protected boolean getConfiguredScrollable() {
    return true;
  }

  @Override
  public boolean isScrollable() {
    return propertySupport.getPropertyBool(PROP_SCROLLABLE);
  }

  @Override
  public void setScrollable(boolean scrollable) {
    propertySupport.setPropertyBool(PROP_SCROLLABLE, scrollable);
  }

  @Override
  public void setComparator(Comparator<? extends IGroup> comparator) {
    setComparator(comparator, true);
  }

  @Override
  public void setComparator(Comparator<? extends IGroup> comparator, boolean sortNow) {
    if (m_comparator == comparator) {
      return;
    }
    m_comparator = comparator;
    if (sortNow) {
      sort();
    }
  }

  @Override
  public Comparator<? extends IGroup> getComparator() {
    return m_comparator;
  }

  /**
   * May be overridden to add some logic to determine the active comparator. Returns {@link #getComparator()} by
   * default.
   */
  protected Comparator<? extends IGroup> resolveComparator() {
    return getComparator();
  }

  @Override
  public void sort() {
    if (resolveComparator() == null) {
      return;
    }
    List<? extends IGroup> groups = getGroups();
    sortInternal(groups);
    setGroupsInternal(groups);
  }

  @SuppressWarnings("unchecked")
  public void sortInternal(List<? extends IGroup> groups) {
    Comparator<? extends IGroup> comparator = resolveComparator();
    if (comparator == null) {
      return;
    }
    groups.sort((Comparator<? super IGroup>) comparator);
  }
}
