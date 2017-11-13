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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.client.ui.group.IGroup;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

@ClassId("0f4a0100-ef2b-46e2-809b-56ed62c56006")
public abstract class AbstractAccordion extends AbstractWidget implements IAccordion {
  private boolean m_initialized;

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
  protected void callInitializer() {
    if (isInitialized()) {
      return;
    }
    interceptInitConfig();
    setInitialized(true);
  }

  protected final void interceptInitConfig() {
    initConfig();
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setExclusiveExpand(getConfiguredExclusiveExpand());
    setScrollable(getConfiguredScrollable());
    OrderedCollection<IGroup> tiles = new OrderedCollection<>();
    injectGroupsInternal(tiles);
    setGroups(tiles.getOrderedList());
  }

  @Override
  public ITypeWithClassId getContainer() {
    return (ITypeWithClassId) propertySupport.getProperty(PROP_CONTAINER);
  }

  /**
   * do not use this internal method unless you are implementing a container that holds and controls tiles.
   */
  public void setContainerInternal(ITypeWithClassId container) {
    propertySupport.setProperty(PROP_CONTAINER, container);
  }

  public boolean isInitialized() {
    return m_initialized;
  }

  protected void setInitialized(boolean initialized) {
    m_initialized = initialized;
  }

  protected void injectGroupsInternal(OrderedCollection<IGroup> tiles) {
    List<Class<? extends IGroup>> tileClasses = getConfiguredGroups();
    for (Class<? extends IGroup> tileClass : tileClasses) {
      IGroup tile = createGroupInternal(tileClass);
      if (tile != null) {
        tiles.addOrdered(tile);
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
  public void initAccordion() {
    for (IGroup group : getGroupsInternal()) {
      group.init();
    }
  }

  @Override
  public void postInitAccordionConfig() {
    for (IGroup group : getGroupsInternal()) {
      group.postInitConfig();
    }
  }

  @Override
  public void disposeAccordion() {
    for (IGroup group : getGroupsInternal()) {
      group.dispose();
    }
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
    List<? extends IGroup> oldGroups = getGroups();
    List<? extends IGroup> newGroups = ObjectUtility.nvl(groups, new ArrayList<>());

    // Dispose old groups (only if they are not in the new list)
    @SuppressWarnings("unchecked")
    List<IGroup> groupsToDelete = (List<IGroup>) oldGroups;
    groupsToDelete.removeAll(groups);
    for (IGroup group : groupsToDelete) {
      group.dispose();
    }

    // Initialize new groups
    // Only initialize when groups are added later,
    // if they are added while initConfig runs, initGroups() will take care of the initialization which will be called by the container (e.g. AccordionField)
    for (IGroup group : newGroups) {
      group.setContainer(this);
      if (isInitialized() && !oldGroups.contains(group)) {
        group.postInitConfig();
        group.init();
      }
    }

    propertySupport.setPropertyList(PROP_GROUPS, groups);
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
    setGroups(new ArrayList<IGroup>());
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
  public String classId() {
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
    if (getContainer() != null) {
      return simpleClassId + ID_CONCAT_SYMBOL + getContainer().classId();
    }
    return simpleClassId;
  }

}
