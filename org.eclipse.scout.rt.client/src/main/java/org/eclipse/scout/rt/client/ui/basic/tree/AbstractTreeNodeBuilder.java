/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public abstract class AbstractTreeNodeBuilder<LOOKUP_ROW_TYPE> {

  public AbstractTreeNodeBuilder() {
  }

  protected abstract ITreeNode createEmptyTreeNode();

  public ITreeNode createTreeNode(LOOKUP_ROW_TYPE primaryKey, String text, int nodeStatus, boolean markChildrenLoaded) {
    return createTreeNode(new LookupRow<LOOKUP_ROW_TYPE>(primaryKey, text), nodeStatus, markChildrenLoaded);
  }

  public List<ITreeNode> createTreeNodes(List<? extends ILookupRow<LOOKUP_ROW_TYPE>> lookupRows, int nodeStatus, boolean markChildrenLoaded) {
    ArrayList<ITreeNode> rootNodes = new ArrayList<ITreeNode>();
    HashMap<Object, ITreeNode> nodeMap = new HashMap<Object, ITreeNode>();
    HashMap<Object, ArrayList<ITreeNode>> parentChildMap = new HashMap<Object, ArrayList<ITreeNode>>();
    if (lookupRows != null) {
      for (ILookupRow<LOOKUP_ROW_TYPE> row : lookupRows) {
        ITreeNode node = createTreeNode(row, nodeStatus, markChildrenLoaded);
        nodeMap.put(node.getPrimaryKey(), node);
        if (row.getParentKey() != null) {
          // child
          ArrayList<ITreeNode> list = parentChildMap.get(row.getParentKey());
          if (list == null) {
            list = new ArrayList<ITreeNode>();
            parentChildMap.put(row.getParentKey(), list);
          }
          list.add(node);
        }
        else {
          // root
          rootNodes.add(node);
        }
      }
    }
    for (Map.Entry<Object, ArrayList<ITreeNode>> e : parentChildMap.entrySet()) {
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
    treeNode.setEnabledInternal(lookupRow.isEnabled());
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
    cell.setBackgroundColor(lookupRow.getBackgroundColor());
    cell.setForegroundColor(lookupRow.getForegroundColor());
    cell.setFont(lookupRow.getFont());
    // hint for inactive codes
    if (!lookupRow.isActive()) {
      if (cell.getFont() == null) {
        cell.setFont(FontSpec.parse("italic"));
      }
      cell.setText(lookupRow.getText() + " (" + ScoutTexts.get("InactiveState") + ")");
    }
    return treeNode;
  }

}
