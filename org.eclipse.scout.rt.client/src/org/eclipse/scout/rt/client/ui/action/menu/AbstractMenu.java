/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.tree.AbstractActionNode;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;

public abstract class AbstractMenu extends AbstractActionNode<IMenu> implements IMenu {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractMenu.class);

  private boolean m_singleSelectionAction;
  private boolean m_multiSelectionAction;
  private boolean m_emptySpaceAction;
  private boolean m_visibleProperty;
  private IPropertyObserver m_owner;
  private Object m_ownerValue;

  public AbstractMenu() {
    this(true);
  }

  public AbstractMenu(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * @return
   * @deprecated use {@link AbstractTableMenu} as superclass and
   *             {@link AbstractTableMenu#getConfiguredSingleSelectionAction()} instead.
   */
  @Deprecated
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  protected boolean getConfiguredSingleSelectionAction() {
    return true;
  }

  /**
   * @return
   * @deprecated use {@link AbstractTableMenu} as superclass and
   *             {@link AbstractTableMenu#getConfiguredMultiSelectionAction()} instead.
   */
  @Deprecated
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  protected boolean getConfiguredMultiSelectionAction() {
    return false;
  }

  /**
   * @return
   * @deprecated use {@link AbstractTableMenu} as superclass and
   *             {@link AbstractTableMenu#getConfiguredEmptySpaceAction()} instead.
   */
  @Deprecated
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  protected boolean getConfiguredEmptySpaceAction() {
    return false;
  }

  @Override
  public final void handleOwnerValueChanged(Object newValue) throws ProcessingException {
    if (!CompareUtility.equals(m_ownerValue, newValue)) {
      m_ownerValue = newValue;
      execOwnerValueChanged(newValue);
      calculateAvailability(newValue);
    }
  }

  /**
   * @param newValue
   */
  protected void calculateAvailability(Object newOwnerValue) {
    if (hasChildActions()) {
      setAvailableInternal(true);
      return;
    }
    // lagacy support
    boolean available = false;
    if (newOwnerValue instanceof Collection) {

      Collection collectionValue = (Collection) newOwnerValue;
      if (isEmptySpaceAction()) {
        available = collectionValue.isEmpty();
      }
      else {
        Collection<ITableRow> rows = convertToTableRows(collectionValue);
        if (rows != null) {
          boolean allEnabled = true;
          for (ITableRow r : rows) {
            if (!r.isEnabled()) {
              allEnabled = false;
              break;
            }
          }
          if (allEnabled) {
            available |= isSingleSelectionAction() && collectionValue.size() == 1;
            available |= isMultiSelectionAction() && collectionValue.size() > 1;
          }
        }
        else {
          // try tree
          Collection<ITreeNode> treeNodes = convertToTreeNodes(collectionValue);
          if (treeNodes != null) {
            boolean allEnabled = true;
            for (ITreeNode node : treeNodes) {
              if (!node.isEnabled()) {
                allEnabled = false;
                break;
              }
            }
            if (allEnabled) {
              available |= isSingleSelectionAction() && collectionValue.size() == 1;
              available |= isMultiSelectionAction() && collectionValue.size() > 1;
            }
          }
        }
      }
    }
    else {
      available |= isSingleSelectionAction() && newOwnerValue != null;
      available |= isMultiSelectionAction() && newOwnerValue != null;
      available |= isEmptySpaceAction() && newOwnerValue == null;
    }
    setAvailableInternal(available);
  }

  /**
   * AFTER a new valid master value was stored, this method is called
   */
  @ConfigOperation
  @Order(50.0)
  protected void execOwnerValueChanged(Object newOwnerValue) throws ProcessingException {

  }

  @ConfigOperation
  @Order(60.0)
  protected void execAboutToShow() throws ProcessingException {

  }

  @Override
  public final void aboutToShow() {
    try {
      aboutToShowInternal();
      execAboutToShow();
      // children
      for (IMenu m : getChildActions()) {
        m.aboutToShow();
      }
    }
    catch (Throwable t) {
      LOG.warn("Action " + getClass().getName(), t);
    }
  }

  /**
   * do not use this method, it is used internally by subclasses
   */
  protected void aboutToShowInternal() {
  }

  /**
   * converts a untyped collection into a type collection of table rows.
   * 
   * @param input
   * @return null if the input is null or not all elements of the input are {@link ITableRow}s.
   */
  protected Collection<ITableRow> convertToTableRows(Collection<?> input) {
    if (input == null) {
      return null;
    }
    List<ITableRow> rows = new ArrayList<ITableRow>(input.size());
    for (Object o : input) {
      if (o instanceof ITableRow) {
        rows.add((ITableRow) o);
      }
    }
    if (rows.size() == input.size()) {
      return rows;
    }
    return null;
  }

  /**
   * converts a untyped collection into a type collection of tree nodes.
   * 
   * @param input
   * @return null if the input is null or not all elements of the input are {@link ITreeNode}s.
   */
  protected Collection<ITreeNode> convertToTreeNodes(Collection<?> input) {
    if (input == null) {
      return null;
    }
    List<ITreeNode> rows = new ArrayList<ITreeNode>(input.size());
    for (Object o : input) {
      if (o instanceof ITreeNode) {
        rows.add((ITreeNode) o);
      }
    }
    if (rows.size() == input.size()) {
      return rows;
    }
    return null;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    // default
    setSingleSelectionAction(getConfiguredSingleSelectionAction());
    setMultiSelectionAction(getConfiguredMultiSelectionAction());
    setEmptySpaceAction(getConfiguredEmptySpaceAction());
    if (isSingleSelectionAction() || isMultiSelectionAction() || isEmptySpaceAction()) {
      // ok
    }
    else {
      // legacy case of implicit new menu
      setEmptySpaceAction(true);
    }
    // calculate initial availability (emtpySpace = true, multi = false, single = false)
    calculateAvailability(null);
  }

  @Override
  public void addChildActions(List<? extends IMenu> actionList) {
    super.addChildActions(actionList);
    afterChildMenusAdd(actionList);
  }

  @Override
  public void removeChildActions(List<? extends IMenu> actionList) {
    super.removeChildActions(actionList);
    afterChildMenusRemove(actionList);
  }

  protected void afterChildMenusAdd(List<? extends IMenu> newChildMenus) {
    if (CollectionUtility.hasElements(newChildMenus)) {
      final IPropertyObserver owner = getOwner();
      final Object ownerValue = m_ownerValue;
      IActionVisitor visitor = new IActionVisitor() {
        @Override
        public int visit(IAction action) {
          if (action instanceof IMenu) {
            IMenu menu = (IMenu) action;
            menu.setOwnerInternal(owner);
            try {
              menu.handleOwnerValueChanged(ownerValue);
            }
            catch (ProcessingException e) {
              LOG.error("error during handle owner value changed.", e);
            }
          }
          return CONTINUE;
        }
      };
      for (IMenu m : newChildMenus) {
        m.acceptVisitor(visitor);
      }
    }
  }

  protected void afterChildMenusRemove(List<? extends IMenu> childMenusToRemove) {
    if (CollectionUtility.hasElements(childMenusToRemove)) {
      IActionVisitor visitor = new IActionVisitor() {
        @Override
        public int visit(IAction action) {
          if (action instanceof IMenu) {
            IMenu menu = (IMenu) action;
            menu.setOwnerInternal(null);
            try {
              menu.handleOwnerValueChanged(null);
            }
            catch (ProcessingException e) {
              LOG.error("error during handle owner value changed.", e);
            }
          }
          return CONTINUE;
        }
      };
      for (IMenu m : childMenusToRemove) {
        m.acceptVisitor(visitor);
      }
    }
  }

  @Override
  public void setOwnerInternal(IPropertyObserver owner) {
    m_owner = owner;
  }

  @Override
  public IPropertyObserver getOwner() {
    return m_owner;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public boolean isSingleSelectionAction() {
    return m_singleSelectionAction;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void setSingleSelectionAction(boolean b) {
    m_singleSelectionAction = b;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public boolean isMultiSelectionAction() {
    return m_multiSelectionAction;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void setMultiSelectionAction(boolean b) {
    m_multiSelectionAction = b;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public boolean isEmptySpaceAction() {
    return m_emptySpaceAction;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void setEmptySpaceAction(boolean b) {
    m_emptySpaceAction = b;
  }

  @Override
  public boolean isAvailable() {
    return propertySupport.getPropertyBool(PROP_AVAILABLE);
  }

  protected void setAvailableInternal(boolean available) {
    propertySupport.setPropertyBool(PROP_AVAILABLE, available);
  }

}
