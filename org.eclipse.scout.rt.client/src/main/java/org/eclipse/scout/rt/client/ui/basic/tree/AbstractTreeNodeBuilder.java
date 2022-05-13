/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.basic.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.shared.dimension.IDimensions;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public abstract class AbstractTreeNodeBuilder<LOOKUP_ROW_TYPE> {

  protected abstract ITreeNode createEmptyTreeNode();

  public ITreeNode createTreeNode(LOOKUP_ROW_TYPE primaryKey, String text, int nodeStatus, boolean markChildrenLoaded) {
    return createTreeNode(new LookupRow<>(primaryKey, text), nodeStatus, markChildrenLoaded);
  }

  public List<ITreeNode> createTreeNodes(List<? extends ILookupRow<LOOKUP_ROW_TYPE>> lookupRows, int nodeStatus, boolean markChildrenLoaded) {
    List<ITreeNode> rootNodes = new ArrayList<>();
    Map<Object, ITreeNode> nodeMap = new HashMap<>();
    Map<LOOKUP_ROW_TYPE, ArrayList<ITreeNode>> parentChildMap = new HashMap<>();
    if (lookupRows != null) {
      for (ILookupRow<LOOKUP_ROW_TYPE> row : lookupRows) {
        ITreeNode node = createTreeNode(row, nodeStatus, markChildrenLoaded);
        nodeMap.put(node.getPrimaryKey(), node);
        if (row.getParentKey() != null) {
          // child
          List<ITreeNode> list = parentChildMap.computeIfAbsent(row.getParentKey(), k -> new ArrayList<>());
          list.add(node);
        }
        else {
          // root
          rootNodes.add(node);
        }
      }
    }
    for (Entry<LOOKUP_ROW_TYPE, ArrayList<ITreeNode>> e : parentChildMap.entrySet()) {
      Object parentKey = e.getKey();
      ITreeNode parentNode = nodeMap.get(parentKey);
      if (parentNode instanceof AbstractTreeNode) {
        ((AbstractTreeNode) parentNode).addChildNodesInternal(parentNode.getChildNodeCount(), e.getValue(), true);
      }
      else {
        rootNodes.addAll(e.getValue());
      }
    }
    return rootNodes;
  }

  public ITreeNode createTreeNode(ILookupRow<LOOKUP_ROW_TYPE> lookupRow, int nodeStatus, boolean markChildrenLoaded) {
    ITreeNode treeNode = createEmptyTreeNode();
    // fill values to treeNode
    treeNode.setPrimaryKey(lookupRow.getKey());
    treeNode.setEnabled(lookupRow.isEnabled(), IDimensions.ENABLED);
    treeNode.setStatusInternal(nodeStatus);
    if (markChildrenLoaded) {
      treeNode.setChildrenLoaded(markChildrenLoaded);
    }
    Cell cell = treeNode.getCellForUpdate();
    cell.setValue(lookupRow);
    cell.setText(lookupRow.getText());
    cell.setTooltipText(lookupRow.getTooltipText());
    if (cell.getIconId() == null) {
      cell.setIconId(lookupRow.getIconId());
    }
    cell.setCssClass(lookupRow.getCssClass());
    cell.setBackgroundColor(lookupRow.getBackgroundColor());
    cell.setForegroundColor(lookupRow.getForegroundColor());
    cell.setFont(lookupRow.getFont());
    // hint for inactive codes
    if (!lookupRow.isActive()) {
      cell.addCssClass("inactive");
    }
    return treeNode;
  }

}
