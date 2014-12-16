/*******************************************************************************
 * Copyright (c) 2010,2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.annotations.OrderedCollection;
import org.eclipse.scout.commons.annotations.OrderedComparator;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.extension.ui.action.tree.IActionNodeExtension;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractActionNode<T extends IActionNode> extends AbstractAction implements IActionNode<T>, IContributionOwner {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractActionNode.class);
  private T m_parent;
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
    // menus
    List<Class<? extends IActionNode>> configuredChildActions = getConfiguredChildActions();
    OrderedCollection<T> actionNodes = new OrderedCollection<T>();
    for (Class<? extends IActionNode> a : configuredChildActions) {

      try {
        IActionNode node = ConfigurationUtility.newInnerInstance(this, a);
        node.setParent(this);
        actionNodes.addOrdered((T) node);
      }
      catch (Exception e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("error creating instance of class '" + a.getName() + "'.", e));
      }
    }
    m_contributionHolder = new ContributionComposite(this);
    List<IActionNode> contributedActions = m_contributionHolder.getContributionsByClass(IActionNode.class);
    for (IActionNode n : contributedActions) {
      actionNodes.addOrdered((T) n);
    }
    try {
      injectActionNodesInternal(actionNodes);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contribute action nodes.", e);
    }
    // add
    setChildActions(actionNodes.getOrderedList());
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to add/remove menus.<br>
   * To change the order or specify the insert position use {@link IMenu#setOrder(double)}.
   *
   * @param fieldList
   *          live and mutable collection of configured menus, not yet initialized
   */
  protected void injectActionNodesInternal(OrderedCollection<T> actionNodes) {
  }

  @Override
  public void setContainerInternal(ITypeWithClassId container) {
    super.setContainerInternal(container);
    // children
    setContainerOnActions(getChildActionsInternal());
  }

  protected void setContainerOnActions(List<? extends T> actions) {
    if (actions != null) {
      for (T childAction : actions) {
        if (childAction != null) {
          childAction.setContainerInternal(getContainer());
        }
      }
    }
  }

  /*
   * Runtime
   */
  @Override
  public T getParent() {
    return m_parent;
  }

  @Override
  public void setParent(T parent) {
    m_parent = parent;
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
  public void setChildActions(Collection<? extends T> newList) {
    // remove old
    removeChildActions(getChildActionsInternal());
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
    if (!normalizedList.isEmpty()) {
      setContainerOnActions(normalizedList);
      List<T> childList = getChildActionsInternal();
      if (childList == null) {
        childList = new ArrayList<T>(normalizedList.size());
      }
      childList.addAll(normalizedList);
      Collections.sort(childList, new OrderedComparator());
      propertySupport.setPropertyAlwaysFire(PROP_CHILD_ACTIONS, childList);
    }
  }

  @Override
  public void removeChildAction(T action) {
    removeChildActions(CollectionUtility.arrayList(action));
  }

  @Override
  public void removeChildActions(Collection<? extends T> actionList) {
    List<T> normalizedList = CollectionUtility.arrayListWithoutNullElements(actionList);
    if (!normalizedList.isEmpty()) {
      List<T> childList = getChildActionsInternal();
      boolean listChanged = false;
      for (T a : normalizedList) {
        if (childList.remove(a)) {
          listChanged = true;
          a.setContainerInternal(null);
        }
      }
      if (listChanged) {
        propertySupport.setPropertyAlwaysFire(PROP_CHILD_ACTIONS, childList);
      }
    }
  }

  @Override
  public int acceptVisitor(IActionVisitor visitor) {
    switch (visitor.visit(this)) {
      case IActionVisitor.CANCEL:
        return IActionVisitor.CANCEL;
      case IActionVisitor.CANCEL_SUBTREE:
        return IActionVisitor.CONTINUE;
      case IActionVisitor.CONTINUE_BRANCH:
        visitChildren(visitor);
        return IActionVisitor.CANCEL;
      default:
        return visitChildren(visitor);
    }
  }

  private int visitChildren(IActionVisitor visitor) {
    for (IAction t : getChildActions()) {
      switch (t.acceptVisitor(visitor)) {
        case IActionVisitor.CANCEL:
          return IActionVisitor.CANCEL;
      }
    }
    return IActionVisitor.CONTINUE;
  }

  protected static class LocalActionNodeExtension<T extends IActionNode, OWNER extends AbstractActionNode<T>> extends LocalActionExtension<OWNER> implements IActionNodeExtension<T, OWNER> {

    public LocalActionNodeExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IActionNodeExtension<T, ? extends AbstractActionNode<T>> createLocalExtension() {
    return new LocalActionNodeExtension<T, AbstractActionNode<T>>(this);
  }

}
