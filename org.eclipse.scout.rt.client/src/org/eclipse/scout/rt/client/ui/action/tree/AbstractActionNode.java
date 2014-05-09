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
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractActionNode<T extends IActionNode> extends AbstractAction implements IActionNode<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractActionNode.class);
  private T m_parent;

  public AbstractActionNode() {
    super();
  }

  public AbstractActionNode(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  private List<? extends Class<? extends IActionNode>> getConfiguredChildActions() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IActionNode>> filtered = ConfigurationUtility.filterClasses(dca, IActionNode.class);
    List<Class<? extends IActionNode>> foca = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, IActionNode.class);
    return ConfigurationUtility.removeReplacedClasses(foca);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void initConfig() {
    super.initConfig();
    // menus
    List<T> nodeList = new ArrayList<T>();
    List<? extends Class<? extends IActionNode>> ma = getConfiguredChildActions();
    for (Class<? extends IActionNode> a : ma) {

      try {
        IActionNode node = ConfigurationUtility.newInnerInstance(this, a);
        node.setParent(this);
        nodeList.add((T) node);
      }
      catch (Exception e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("error creating instance of class '" + a.getName() + "'.", e));
      }
    }

    try {
      injectActionNodesInternal(nodeList);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contribute action nodes.", e);
    }
    // add
    setChildActions(nodeList);
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to manage menu list and add/remove menus
   * 
   * @param fieldList
   *          live and mutable list of configured menus, not yet initialized
   *          and added to composite field
   */
  protected void injectActionNodesInternal(List<T> nodeList) {
  }

  @Override
  public void setContainerInternal(ITypeWithClassId container) {
    super.setContainerInternal(container);
    // children
    updateContaineronChildren();
  }

  protected void updateContaineronChildren() {
    for (T childAction : getChildActions()) {
      childAction.setContainerInternal(getContainer());
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

  /**
   * override to prepare child menus as well
   * 
   * @throws ProcessingException
   */
  @SuppressWarnings("deprecation")
  @Override
  protected void prepareActionInternal() throws ProcessingException {
    super.prepareActionInternal();
    // child menus
    for (T node : getChildActionsInternal()) {
      node.prepareAction();
    }
  }

  private List<T> getChildActionsInternal() {
    return propertySupport.getPropertyList(PROP_CHILD_ACTIONS);
  }

  @Override
  public boolean hasChildActions() {
    return getChildActionsInternal().size() > 0;
  }

  @Override
  public int getChildActionCount() {
    return getChildActionsInternal().size();
  }

  @Override
  public List<T> getChildActions() {
    return CollectionUtility.arrayList(getChildActionsInternal());
  }

  @Override
  public void setChildActions(List<? extends T> newList) {
    propertySupport.setPropertyList(PROP_CHILD_ACTIONS, CollectionUtility.<T> arrayList(newList));
    updateContaineronChildren();
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

}
