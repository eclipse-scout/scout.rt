/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.visitor.IDepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeTraversals;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;

/**
 * @since 3.8.0
 */
public final class TreeUtility {

  private TreeUtility() {
  }

  /**
   * If every given node has the same parent, that common parent node will be returned, else null.
   */
  public static ITreeNode calculateCommonParentNode(Collection<? extends ITreeNode> nodes) {
    if (CollectionUtility.isEmpty(nodes)) {
      return null;
    }
    Iterator<? extends ITreeNode> nodeIt = nodes.iterator();
    final ITreeNode commonParent = nodeIt.next().getParentNode();
    while (nodeIt.hasNext()) {
      if (nodeIt.next().getParentNode() != commonParent) {
        return null;
      }
    }
    return commonParent;
  }

  /**
   * Searches for the lowest common ancestor of the given node. Lowest means farthest away from root.
   */
  public static ITreeNode findLowestCommonAncestorNode(List<ITreeNode> nodes) {
    if (nodes == null || nodes.isEmpty()) {
      return null;
    }

    if (nodes.size() == 1) {
      return nodes.get(0).getParentNode();
    }

    ITreeNode commonParent = nodes.get(0).getParentNode();

    for (int i = 1; i < nodes.size(); i++) {
      ITreeNode parentNode = nodes.get(i).getParentNode();
      if (parentNode != commonParent) {
        commonParent = findLowestCommonAncestorNode(commonParent, parentNode);
      }
    }

    return commonParent;
  }

  public static ITreeNode findLowestCommonAncestorNode(ITreeNode firstNode, ITreeNode secondNode) {
    if (firstNode == null || secondNode == null) {
      return null;
    }

    if (firstNode.getTree() != secondNode.getTree()) {
      return null;
    }

    if (firstNode.getParentNode() == secondNode.getParentNode()) {
      return firstNode.getParentNode();
    }

    if (secondNode.getTreeLevel() > firstNode.getTreeLevel()) {
      secondNode = findAncestorNodeAtLevel(secondNode, firstNode.getTreeLevel());
    }
    else if (firstNode.getTreeLevel() > secondNode.getTreeLevel()) {
      firstNode = findAncestorNodeAtLevel(firstNode, secondNode.getTreeLevel());
    }

    while (firstNode.getParentNode() != secondNode.getParentNode()) {
      firstNode = firstNode.getParentNode();
      secondNode = secondNode.getParentNode();
    }

    return firstNode.getParentNode();
  }

  /**
   * Returns the ancestor of the given node which resides at the given level.
   *
   * @param level
   *     The level to look for the ancestor. The level has to be <= than node.getLevel(); Otherwise null will
   *     returned.
   */
  public static ITreeNode findAncestorNodeAtLevel(ITreeNode node, int level) {
    if (node == null) {
      return null;
    }

    if (node.getTreeLevel() < level) {
      return null;
    }

    while (node.getTreeLevel() > level) {
      node = node.getParentNode();
    }

    return node;
  }

  public static TreeVisitResult visitNodes(Collection<ITreeNode> nodes, IDepthFirstTreeVisitor<ITreeNode> v) {
    return visitNodes(nodes, v, ITreeNode::getChildNodes);
  }

  public static TreeVisitResult visitNodes(Collection<ITreeNode> nodes, IDepthFirstTreeVisitor<ITreeNode> v, Function<ITreeNode, Collection<? extends ITreeNode>> childrenSupplier) {
    if (CollectionUtility.isEmpty(nodes)) {
      return TreeVisitResult.CONTINUE;
    }

    for (ITreeNode node : nodes) {
      if (node == null) {
        continue;
      }
      TreeVisitResult result = visitNode(node, v, childrenSupplier);
      if (result == TreeVisitResult.TERMINATE || result == TreeVisitResult.SKIP_SIBLINGS) {
        return result;
      }
    }
    return TreeVisitResult.CONTINUE;
  }

  public static TreeVisitResult visitNode(ITreeNode node, IDepthFirstTreeVisitor<ITreeNode> visitor) {
    return visitNode(node, visitor, ITreeNode::getChildNodes);
  }

  public static TreeVisitResult visitNode(ITreeNode node, IDepthFirstTreeVisitor<ITreeNode> visitor, Function<ITreeNode, Collection<? extends ITreeNode>> childrenSupplier) {
    return TreeTraversals.create(visitor, childrenSupplier.andThen(TreeUtility::filterNodesWithoutTree)).traverse(node);
  }

  private static List<? extends ITreeNode> filterNodesWithoutTree(Collection<? extends ITreeNode> candidates) {
    List<ITreeNode> result = new ArrayList<>(candidates.size());
    for (ITreeNode childNode : candidates) {
      if (childNode == null) {
        continue;
      }
      if (childNode.getTree() == null) {
        // it might be that the visit of a node detached the node from the tree
        continue;
      }
      result.add(childNode);
    }
    return result;
  }
}
