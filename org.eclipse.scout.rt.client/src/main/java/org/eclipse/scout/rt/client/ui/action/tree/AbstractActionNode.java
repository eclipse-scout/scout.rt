/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.tree;

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.action.tree.IActionNodeExtension;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IReadOnlyMenu;
import org.eclipse.scout.rt.platform.OrderedComparator;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;

@ClassId("bacb13e3-6627-4d87-bb8c-fc578ceb1bfe")
public abstract class AbstractActionNode<T extends IActionNode> extends AbstractAction implements IActionNode<T>, IContributionOwner {
  private IContributionOwner m_contributionHolder;

  public AbstractActionNode() {
    super();
  }

  public AbstractActionNode(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  public final List<Object> getAllContributions() {
    return m_contributionHolder.getAllContributions();
  }

  @Override
  public final <TYPE> List<TYPE> getContributionsByClass(Class<TYPE> type) {
    return m_contributionHolder.getContributionsByClass(type);
  }

  @Override
  public final <TYPE> TYPE getContribution(Class<TYPE> contribution) {
    return m_contributionHolder.getContribution(contribution);
  }

  @Override
  public final <TYPE> TYPE optContribution(Class<TYPE> contribution) {
    return m_contributionHolder.optContribution(contribution);
  }

  /*
   * Configuration
   */
  private List<Class<? extends IActionNode>> getConfiguredChildActions() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IActionNode>> filtered = ConfigurationUtility.filterClasses(dca, IActionNode.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void initConfig() {
    super.initConfig();

    OrderedCollection<T> actionNodes = new OrderedCollection<>();
    List<Class<? extends IActionNode>> configuredChildActions = getConfiguredChildActions();
    for (Class<? extends IActionNode> a : configuredChildActions) {
      T node = (T) ConfigurationUtility.newInnerInstance(this, a);
      actionNodes.addOrdered(node);
    }
    m_contributionHolder = new ContributionComposite(this);
    List<IActionNode> contributedActions = m_contributionHolder.getContributionsByClass(IActionNode.class);
    for (IActionNode n : contributedActions) {
      actionNodes.addOrdered((T) n);
    }
    injectActionNodesInternal(actionNodes);
    setChildActions(actionNodes.getOrderedList());
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to add/remove menus.<br>
   * To change the order or specify the insert position use {@link IMenu#setOrder(double)}.
   *
   * @param actionNodes
   *     live and mutable collection of configured menus, not yet initialized
   */
  protected void injectActionNodesInternal(OrderedCollection<T> actionNodes) {
  }

  protected static <T extends IActionNode> void connectActionNode(T child, IActionNode<T> parent) {
    assertNotNull(child);
    if (child instanceof IReadOnlyMenu) {
      // it is a wrapped menu: cannot change anything. nothing to connect
      return;
    }

    if (parent == null) {
      // disconnect from existing parent
      child.setParentInternal(null);
      child.setContainerInternal(null);
      return;
    }

    IWidget currentChildParent = child.getParent();
    if (currentChildParent != parent) {
      // connect to new parent
      assertNull(currentChildParent, "Action '{}' cannot be added to '{}' because it is still connected to '{}'.", child, parent, child.getParent());
      child.setParentInternal(parent);
    }

    IWidget containerOfChild = child.getContainer();
    IWidget containerOfParent = parent.getContainer();
    if (containerOfChild != containerOfParent) {
      // connect to new container
      child.setContainerInternal(containerOfParent);
    }
  }

  @Override
  public void setContainerInternal(IWidget container) {
    super.setContainerInternal(container);
    // children
    List<T> childActions = getChildActionsInternal();
    if (childActions == null || childActions.isEmpty()) {
      return;
    }
    childActions.forEach(a -> connectActionNode(a, this));
  }

  private List<T> getChildActionsInternal() {
    return propertySupport.getPropertyList(PROP_CHILD_ACTIONS);
  }

  @Override
  public boolean hasChildActions() {
    return CollectionUtility.hasElements(getChildActionsInternal());
  }

  @Override
  public int getChildActionCount() {
    return CollectionUtility.size(getChildActionsInternal());
  }

  @Override
  public List<T> getChildActions() {
    return CollectionUtility.arrayList(getChildActionsInternal());
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), getChildActionsInternal());
  }

  @Override
  public void setChildActions(Collection<? extends T> newList) {
    // remove old
    List<T> oldList = getChildActions();
    removeChildActions(oldList);
    // add new
    addChildActions(newList);
  }

  @Override
  public void addChildAction(T action) {
    addChildActions(CollectionUtility.arrayList(action));
  }

  @Override
  public void addChildActions(Collection<? extends T> actionList) {
    List<T> normalizedList = CollectionUtility.arrayListWithoutNullElements(actionList);
    if (normalizedList.isEmpty()) {
      return;
    }

    List<T> childList = getChildActionsInternal();
    if (childList == null) {
      childList = new ArrayList<>(normalizedList.size());
    }
    childList.addAll(normalizedList);
    childList.sort(new OrderedComparator());
    for (T child : normalizedList) {
      connectActionNode(child, this);
    }
    propertySupport.setPropertyAlwaysFire(PROP_CHILD_ACTIONS, childList);
  }

  @Override
  public void removeChildAction(T action) {
    removeChildActions(CollectionUtility.arrayList(action));
  }

  @Override
  public void removeChildActions(Collection<? extends T> actionList) {
    List<T> normalizedList = CollectionUtility.arrayListWithoutNullElements(actionList);
    if (normalizedList.isEmpty()) {
      return;
    }

    List<T> childList = getChildActionsInternal();
    if (childList == null || childList.isEmpty()) {
      return;
    }

    boolean listChanged = false;
    for (T a : normalizedList) {
      if (childList.remove(a)) {
        listChanged = true;
        connectActionNode(a, null); // disconnect
      }
    }

    if (listChanged) {
      propertySupport.setPropertyAlwaysFire(PROP_CHILD_ACTIONS, childList);
    }
  }

  protected static class LocalActionNodeExtension<T extends IActionNode, OWNER extends AbstractActionNode<T>> extends LocalActionExtension<OWNER> implements IActionNodeExtension<T, OWNER> {

    public LocalActionNodeExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IActionNodeExtension<T, ? extends AbstractActionNode<T>> createLocalExtension() {
    return new LocalActionNodeExtension<>(this);
  }
}
