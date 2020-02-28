/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.action.menu.root.internal;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITreeContextMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

@ClassId("8af6de2d-6e4a-4008-821f-1830b6a360fd")
public class TreeContextMenu extends AbstractContextMenu<ITree> implements ITreeContextMenu {
  private Set<? extends ITreeNode> m_currentSelection;

  /**
   * @param owner
   */
  public TreeContextMenu(ITree owner, List<? extends IMenu> initialChildMenus) {
    super(owner, initialChildMenus);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    ITree container = getContainer();
    container.addTreeListener(
        e -> {
          switch (e.getType()) {
            case TreeEvent.TYPE_NODES_SELECTED: {
              handleOwnerValueChanged();
              break;
            }
            case TreeEvent.TYPE_NODES_UPDATED: {
              if (CollectionUtility.containsAny(e.getNodes(), m_currentSelection)) {
                handleOwnerValueChanged();
              }
              break;
            }
          }
        },
        TreeEvent.TYPE_NODES_SELECTED,
        TreeEvent.TYPE_NODES_UPDATED);
    // init current menu types
    setCurrentMenuTypes(getMenuTypesForSelection(container.getSelectedNodes()));
    calculateLocalVisibility();
  }

  @Override
  public void callOwnerValueChanged() {
    handleOwnerValueChanged();
  }

  @Override
  protected boolean isOwnerPropertyChangedListenerRequired() {
    return true;
  }

  @Override
  protected void handleOwnerPropertyChanged(PropertyChangeEvent evt) {
    super.handleOwnerPropertyChanged(evt);
    if (ITree.PROP_ENABLED.equals(evt.getPropertyName())) {
      calculateEnableState();
    }
  }

  protected void handleOwnerValueChanged() {
    ITree container = getContainer();
    if (container == null) {
      return;
    }

    final Set<ITreeNode> ownerSelection = container.getSelectedNodes();
    m_currentSelection = CollectionUtility.hashSet(ownerSelection);
    setCurrentMenuTypes(getMenuTypesForSelection(ownerSelection));
    visit(new MenuOwnerChangedVisitor(ownerSelection, getCurrentMenuTypes()), IMenu.class);
    // update menu types
    calculateLocalVisibility();
    calculateEnableState();
  }

  protected boolean isTreeAndSelectionEnabled() {
    ITree container = getContainer();
    boolean enabled = container.isEnabled();
    if (!enabled) {
      return false;
    }

    final Set<ITreeNode> containerSelection = container.getSelectedNodes();
    for (ITreeNode node : containerSelection) {
      if (!node.isEnabled()) {
        return false;
      }
    }
    return true;
  }

  protected void calculateEnableState() {
    setEnabled(isTreeAndSelectionEnabled());
  }

  protected Set<TreeMenuType> getMenuTypesForSelection(Set<? extends ITreeNode> selection) {
    if (CollectionUtility.isEmpty(selection)) {
      return CollectionUtility.hashSet(TreeMenuType.EmptySpace);
    }
    else if (CollectionUtility.size(selection) == 1) {
      return CollectionUtility.hashSet(TreeMenuType.SingleSelection);
    }
    else {
      return CollectionUtility.hashSet(TreeMenuType.MultiSelection);
    }
  }
}
