/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.menu.root.internal;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITreeContextMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * The invisible root menu node of any tree. (internal usage only)
 */
@ClassId("8af6de2d-6e4a-4008-821f-1830b6a360fd")
public class TreeContextMenu extends AbstractContextMenu<ITree> implements ITreeContextMenu {
  private Set<? extends ITreeNode> m_currentSelection;

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
    if (IWidget.PROP_ENABLED.equals(evt.getPropertyName())) {
      calculateEnabledState();
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
    calculateEnabledState();
  }

  protected boolean isSelectionEnabled() {
    return getContainer()
        .getSelectedNodes().stream()
        .allMatch(ITreeNode::isEnabled);
  }

  /**
   * called on selection change (selected tree nodes) or when the tree enabled state changes
   */
  protected void calculateEnabledState() {
    ActionUtility.updateContextMenuEnabledState(this, this::isSelectionEnabled, TreeMenuType.MultiSelection, TreeMenuType.SingleSelection);
  }

  protected Set<TreeMenuType> getMenuTypesForSelection(Set<? extends ITreeNode> selection) {
    if (CollectionUtility.isEmpty(selection)) {
      return CollectionUtility.hashSet(TreeMenuType.EmptySpace);
    }
    if (CollectionUtility.size(selection) == 1) {
      return CollectionUtility.hashSet(TreeMenuType.SingleSelection);
    }
    return CollectionUtility.hashSet(TreeMenuType.MultiSelection);
  }
}
