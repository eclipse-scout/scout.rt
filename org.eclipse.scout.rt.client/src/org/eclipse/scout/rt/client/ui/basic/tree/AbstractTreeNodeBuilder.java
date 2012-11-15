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
package org.eclipse.scout.rt.client.ui.basic.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public abstract class AbstractTreeNodeBuilder {

  public AbstractTreeNodeBuilder() {
  }

  protected abstract ITreeNode createEmptyTreeNode() throws ProcessingException;

  public ITreeNode createTreeNode(Object primaryKey, String text, int nodeStatus, boolean markChildrenLoaded) throws ProcessingException {
    return createTreeNode(new LookupRow(primaryKey, text), nodeStatus, markChildrenLoaded);
  }

  public ITreeNode[] createTreeNodes(LookupRow[] lookupRows, int nodeStatus, boolean markChildrenLoaded) throws ProcessingException {
    ArrayList<ITreeNode> rootNodes = new ArrayList<ITreeNode>();
    HashMap<Object, ITreeNode> nodeMap = new HashMap<Object, ITreeNode>();
    HashMap<Object, ArrayList<ITreeNode>> parentChildMap = new HashMap<Object, ArrayList<ITreeNode>>();
    for (LookupRow row : lookupRows) {
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
    for (Map.Entry<Object, ArrayList<ITreeNode>> e : parentChildMap.entrySet()) {
      Object parentKey = e.getKey();
      ITreeNode[] childNodes = e.getValue().toArray(new ITreeNode[0]);
      ITreeNode parentNode = nodeMap.get(parentKey);
      if (parentNode instanceof AbstractTreeNode) {
        ((AbstractTreeNode) parentNode).addChildNodesInternal(parentNode.getChildNodeCount(), childNodes, true);
      }
      else {
        rootNodes.addAll(e.getValue());
      }
    }
    return rootNodes.toArray(new ITreeNode[0]);
  }

  public ITreeNode createTreeNode(LookupRow lookupRow, int nodeStatus, boolean markChildrenLoaded) throws ProcessingException {
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
