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

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;

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
  private Class<? extends IActionNode>[] getConfiguredChildActions() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    Class<IActionNode>[] foca = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dca, IActionNode.class);
    return ConfigurationUtility.removeReplacedClasses(foca);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void initConfig() {
    super.initConfig();
    // menus
    ArrayList<T> nodeList = new ArrayList<T>();
    Class<? extends IActionNode>[] ma = getConfiguredChildActions();
    for (int i = 0; i < ma.length; i++) {
      try {
        IActionNode node = ConfigurationUtility.newInnerInstance(this, ma[i]);
        node.setParent(this);
        nodeList.add((T) node);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }

    try {
      injectActionNodesInternal(nodeList);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contribute action nodes.", e);
    }
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
  @Override
  protected void prepareActionInternal() throws ProcessingException {
    super.prepareActionInternal();
    // child menus
    for (T node : getChildActionsInternal()) {
      node.prepareAction();
    }
  }

  @SuppressWarnings("unchecked")
  private List<T> getChildActionsInternal() {
    return (List<T>) propertySupport.getProperty(PROP_CHILD_ACTIONS);
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
    return new ArrayList<T>(getChildActionsInternal());
  }

  @Override
  public void setChildActions(List<T> newList) {
    propertySupport.setProperty(PROP_CHILD_ACTIONS, new ArrayList<T>(newList));
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
