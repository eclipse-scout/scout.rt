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

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;

/**
 *
 */
public class AbstractTreeMenu extends AbstractMenu implements ITreeMenu {

  private boolean m_skipCalculateAvailability;

  public AbstractTreeMenu() {
    super();
  }

  public AbstractTreeMenu(boolean callInitializer) {
    super(callInitializer);
  }

  @ConfigProperty(ConfigProperty.TREE_MENU_TYPE)
  @Order(140)
  protected EnumSet<TreeMenuType> getConfiguredMenuType() {
    return EnumSet.<TreeMenuType> of(TreeMenuType.SingleSelection);
  }

  @Override
  protected final void execOwnerValueChanged(Object newOwnerValue) throws ProcessingException {
    if (getOwner() != null) {
      Set<ITreeNode> newSelection = CollectionUtility.hashSet(getOwner().getSelectedNodes());
      execTreeSelectionChanged(newSelection);
    }
  }

  @Override
  protected void calculateAvailability(Object newOwnerValue) {
    if (m_skipCalculateAvailability) {
      return;
    }
    if (hasChildActions()) {
      setAvailableInternal(true);
      return;
    }
    Set<ITreeNode> newSelection = CollectionUtility.<ITreeNode> emptyHashSet();
    if (getOwner() != null) {
      newSelection = getOwner().getSelectedNodes();
    }
    boolean available = false;
    if (newSelection.isEmpty()) {
      available = getMenuType().contains(TreeMenuType.EmptySpace);
    }
    else {
      boolean allEnabled = true;
      for (ITreeNode r : newSelection) {
        if (!r.isEnabled()) {
          allEnabled = false;
          break;
        }
      }
      if (allEnabled) {
        available |= (newSelection.size() == 1 && getMenuType().contains(TreeMenuType.SingleSelection));
        available |= (newSelection.size() > 1 && getMenuType().contains(TreeMenuType.MultiSelection));
      }
    }
    setAvailableInternal(available);

  }

  /**
   * selection of the owner table changed. Might be used to call {@link #setVisible(boolean)},
   * {@link #setEnabled(boolean)}, etc.
   * 
   * @param newSelection
   *          the current selection of the tree
   */
  @ConfigOperation
  @Order(70.0)
  protected void execTreeSelectionChanged(Set<ITreeNode> newSelection) {
  }

  @SuppressWarnings("deprecation")
  @Override
  protected void initConfig() {
    // guard to ensure calculate availability is not called when only  legacy from super type is initialized
    try {
      m_skipCalculateAvailability = true;
      super.initConfig();
    }
    finally {
      m_skipCalculateAvailability = false;
    }
    if (!ConfigurationUtility.isMethodOverwrite(AbstractTreeMenu.class, "getConfiguredMenuType", new Class[0], this.getClass())) {
      // legacy
      Set<TreeMenuType> menuType = new HashSet<AbstractTreeMenu.TreeMenuType>();
      if (isSingleSelectionAction()) {
        menuType.add(TreeMenuType.SingleSelection);
      }
      if (isMultiSelectionAction()) {
        menuType.add(TreeMenuType.MultiSelection);
      }
      if (isEmptySpaceAction()) {
        menuType.add(TreeMenuType.EmptySpace);
      }
      EnumSet<TreeMenuType> menuTypeEnumSet = EnumSet.<TreeMenuType> copyOf(menuType);
      setMenuType(menuTypeEnumSet);
    }
    else {
      setMenuType(getConfiguredMenuType());
    }
    calculateAvailability(null);
  }

  @Override
  public ITree getOwner() {
    return (ITree) super.getOwner();
  }

  @Override
  public void setOwnerInternal(IPropertyObserver owner) {
    if (owner == null || owner instanceof ITree) {
      super.setOwnerInternal(owner);
    }
    else {
      throw new IllegalArgumentException("menu owner must be an instance of ITree.");
    }
  }

  public void setMenuType(EnumSet<TreeMenuType> menuType) {
    propertySupport.setProperty(PROP_MENU_TYPE, menuType);
  }

  @SuppressWarnings("unchecked")
  @Override
  public EnumSet<TreeMenuType> getMenuType() {
    return (EnumSet<TreeMenuType>) propertySupport.getProperty(PROP_MENU_TYPE);
  }

}
