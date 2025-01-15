/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.platform.util.visitor.DepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.IDepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

/**
 * Builder to create a tree from a search result. The result may consist of child nodes without their parents.
 * <p>
 * In order to create the complete result, parent nodes may need to be loaded. Optimized in such a way that as few
 * lookups as possible need to be done.
 * <p>
 * Assumes values of type LOOKUP_ROW_TYPE in tree cells
 */
public class IncrementalTreeBuilder<LOOKUP_KEY> {

  private final ILookupRowByKeyProvider<LOOKUP_KEY> m_provider;

  private final Map<LOOKUP_KEY, ILookupRow<LOOKUP_KEY>> m_keyCache = new HashMap<>();

  public IncrementalTreeBuilder(ILookupRowByKeyProvider<LOOKUP_KEY> provider) {
    m_provider = provider;
  }

  /**
   * @param lookupRows
   * @param selectedKey
   *     selected key or null
   * @param existingTree
   * @return
   */
  public List<ILookupRow<LOOKUP_KEY>> getRowsWithParents(List<ILookupRow<LOOKUP_KEY>> lookupRows, LOOKUP_KEY parent, ITree existingTree) {
    List<ILookupRow<LOOKUP_KEY>> res = new ArrayList<>();

    cacheKeys(lookupRows);
    Set<LOOKUP_KEY> allRows = new HashSet<>();

    List<List<ILookupRow<LOOKUP_KEY>>> paths = createPaths(lookupRows, existingTree);
    if (parent == null) {
      //ensure, the path to the root is included for every node
      for (List<ILookupRow<LOOKUP_KEY>> path : paths) {
        for (ILookupRow<LOOKUP_KEY> row : path) {
          if (!allRows.contains(row.getKey())) {
            allRows.add(row.getKey());
            res.add(row);
          }
        }
      }
    }
    else {
      //ensure that rows other then children of the parent are included
      for (List<ILookupRow<LOOKUP_KEY>> path : paths) {
        if (contains(parent, path)) {
          ILookupRow<LOOKUP_KEY> leaf = path.get(path.size() - 1);
          if (!allRows.contains(leaf.getKey())) {
            allRows.add(leaf.getKey());
            res.add(leaf);
          }
        }
      }
    }
    return res;
  }

  private boolean contains(LOOKUP_KEY key, List<ILookupRow<LOOKUP_KEY>> path) {
    for (ILookupRow<LOOKUP_KEY> row : path) {
      if (key.equals(row.getKey())) {
        return true;
      }
    }
    return false;
  }

  private ILookupRow<LOOKUP_KEY> getLookupRow(LOOKUP_KEY key) {
    if (!m_keyCache.containsKey(key)) {
      ILookupRow<LOOKUP_KEY> row = m_provider.getLookupRow(key);
      m_keyCache.put(key, row);
      return row;
    }
    return m_keyCache.get(key);
  }

  /**
   * Collects the path to the root for each node using the existing tree to lookup parent nodes, if possible. Each path
   * starts with the root node.
   */
  public List<List<ILookupRow<LOOKUP_KEY>>> createPaths(Collection<? extends ILookupRow<LOOKUP_KEY>> lookupRows, ITree existingTree) {
    Map<LOOKUP_KEY, ILookupRow<LOOKUP_KEY>> parentMap = createParentMap(existingTree);
    List<List<ILookupRow<LOOKUP_KEY>>> paths = new ArrayList<>();
    for (ILookupRow<LOOKUP_KEY> row : lookupRows) {
      // build path to root for this row
      List<ILookupRow<LOOKUP_KEY>> path = new ArrayList<>();
      ILookupRow<LOOKUP_KEY> r = row;
      while (r != null) {
        path.add(0, r);
        LOOKUP_KEY parentKey = r.getParentKey();
        if (parentKey == null) {
          // no parent
          break;
        }
        if (!parentMap.containsKey(r.getKey())) {
          ILookupRow<LOOKUP_KEY> parentRow = getLookupRow(parentKey);
          parentMap.put(r.getKey(), parentRow);
        }
        r = parentMap.get(r.getKey());
      }
      paths.add(path);
    }
    return paths;
  }

  private void cacheKeys(Collection<? extends ILookupRow<LOOKUP_KEY>> lookupRows) {
    for (ILookupRow<LOOKUP_KEY> row : lookupRows) {
      m_keyCache.put(row.getKey(), row);
    }
  }

  /**
   * Assumes values of type LOOKUP_ROW_TYPE in tree cells
   */
  @SuppressWarnings("unchecked")
  private ILookupRow<LOOKUP_KEY> getLookupRow(ITreeNode treeNode) {
    return (ILookupRow<LOOKUP_KEY>) treeNode.getCell().getValue();
  }

  /**
   * Creates a map containing every key in the tree and its parent tree node
   */
  public Map<LOOKUP_KEY, ILookupRow<LOOKUP_KEY>> createParentMap(ITree tree) {
    final Map<LOOKUP_KEY, ILookupRow<LOOKUP_KEY>> map = new HashMap<>();
    IDepthFirstTreeVisitor<ITreeNode> v = new DepthFirstTreeVisitor<ITreeNode>() {
      @Override
      public TreeVisitResult preVisit(ITreeNode node, int level, int index) {
        ITreeNode parent = node.getParentNode();
        ILookupRow<LOOKUP_KEY> row = getLookupRow(node);
        if (row != null) {
          LOOKUP_KEY key = row.getKey();
          m_keyCache.put(key, row);
          map.put(key, getLookupRow(parent));
        }
        return TreeVisitResult.CONTINUE;
      }
    };

    tree.visitTree(v);
    return map;
  }
}
