/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.tree;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 5.2
 */
public class TreeNodeTest {

  private P_TreeNode m_rootNode;
  private Map<String, ITreeNode> m_mockNodes;
  private Set<ITreeNode> m_nodesCollector;

  @Before
  public void before() {
    m_rootNode = new P_TreeNode();
    m_mockNodes = new HashMap<>();
    m_nodesCollector = new HashSet<>();
  }

  @Test
  public void testCollectChildNodesNoChildNodes() {
    m_rootNode.collectChildNodes(m_nodesCollector, false);
    assertEquals(Collections.emptySet(), m_nodesCollector);
  }

  @Test
  public void testCollectChildNodesRecursiveNoChildNodes() {
    m_rootNode.collectChildNodes(m_nodesCollector, true);
    assertEquals(Collections.emptySet(), m_nodesCollector);
  }

  @Test
  public void testCollectChildNodesOneChildNodes() {
    ITreeNode nodeA = mockNode("a");
    m_rootNode.addChildNodesInternal(0, Collections.singletonList(nodeA), false);

    m_rootNode.collectChildNodes(m_nodesCollector, false);
    assertEquals(Collections.singleton(nodeA), m_nodesCollector);
  }

  @Test
  public void testCollectChildNodesRecursiveOneChildNodes() {
    ITreeNode nodeA = mockNode("a");
    m_rootNode.addChildNodesInternal(0, Collections.singletonList(nodeA), false);

    m_rootNode.collectChildNodes(m_nodesCollector, true);
    assertEquals(Collections.singleton(nodeA), m_nodesCollector);
  }

  @Test
  public void testCollectChildNodesMultipleChildNodes() {
    ITreeNode nodeA = mockNode("a");
    ITreeNode nodeB = mockNode("b");
    m_rootNode.addChildNodesInternal(0, Arrays.asList(nodeA, nodeB), false);

    m_rootNode.collectChildNodes(m_nodesCollector, false);
    assertEquals(CollectionUtility.hashSet(nodeA, nodeB), m_nodesCollector);
  }

  @Test
  public void testCollectChildNodesRecursiveMultipleChildNodes() {
    ITreeNode nodeA = mockNode("a");
    ITreeNode nodeB = mockNode("b");
    m_rootNode.addChildNodesInternal(0, Arrays.asList(nodeA, nodeB), false);

    m_rootNode.collectChildNodes(m_nodesCollector, true);
    assertEquals(CollectionUtility.hashSet(nodeA, nodeB), m_nodesCollector);
  }

  @Test
  public void testCollectChildNodesHierarchicalChildNodes() {
    P_TreeNode nodeA = new P_TreeNode();
    m_rootNode.addChildNodesInternal(0, Collections.singletonList(nodeA), true);
    ITreeNode nodeB = mockNode("b");
    nodeA.addChildNodesInternal(0, Collections.singletonList(nodeB), false);

    m_rootNode.collectChildNodes(m_nodesCollector, false);
    assertEquals(Collections.singleton(nodeA), m_nodesCollector);
    m_nodesCollector.clear();

    m_rootNode.collectChildNodes(m_nodesCollector, true);
    assertEquals(CollectionUtility.hashSet(nodeA, nodeB), m_nodesCollector);
    m_nodesCollector.clear();

    nodeA.collectChildNodes(m_nodesCollector, false);
    assertEquals(Collections.singleton(nodeB), m_nodesCollector);
    m_nodesCollector.clear();

    nodeA.collectChildNodes(m_nodesCollector, true);
    assertEquals(Collections.singleton(nodeB), m_nodesCollector);
  }

  @Test
  public void testCollectChildNodesMultipleHierarchicalChildNodes() {
    P_TreeNode nodeA = new P_TreeNode();
    P_TreeNode nodeB = new P_TreeNode();
    m_rootNode.addChildNodesInternal(0, Arrays.asList(nodeA, nodeB), false);

    ITreeNode nodeX = mockNode("x");
    nodeA.addChildNodesInternal(0, Collections.singletonList(nodeX), false);

    ITreeNode nodeY = mockNode("y");
    nodeB.addChildNodesInternal(0, Collections.singletonList(nodeY), false);

    m_rootNode.collectChildNodes(m_nodesCollector, false);
    assertEquals(CollectionUtility.hashSet(nodeA, nodeB), m_nodesCollector);
    m_nodesCollector.clear();

    m_rootNode.collectChildNodes(m_nodesCollector, true);
    assertEquals(CollectionUtility.hashSet(nodeA, nodeB, nodeX, nodeY), m_nodesCollector);
    m_nodesCollector.clear();

    nodeA.collectChildNodes(m_nodesCollector, false);
    assertEquals(Collections.singleton(nodeX), m_nodesCollector);
    m_nodesCollector.clear();

    nodeA.collectChildNodes(m_nodesCollector, true);
    assertEquals(Collections.singleton(nodeX), m_nodesCollector);
    m_nodesCollector.clear();

    nodeB.collectChildNodes(m_nodesCollector, false);
    assertEquals(Collections.singleton(nodeY), m_nodesCollector);
    m_nodesCollector.clear();

    nodeB.collectChildNodes(m_nodesCollector, true);
    assertEquals(Collections.singleton(nodeY), m_nodesCollector);
    m_nodesCollector.clear();

    nodeX.collectChildNodes(m_nodesCollector, false);
    assertEquals(Collections.emptySet(), m_nodesCollector);

    nodeX.collectChildNodes(m_nodesCollector, true);
    assertEquals(Collections.emptySet(), m_nodesCollector);

    nodeY.collectChildNodes(m_nodesCollector, false);
    assertEquals(Collections.emptySet(), m_nodesCollector);

    nodeY.collectChildNodes(m_nodesCollector, true);
    assertEquals(Collections.emptySet(), m_nodesCollector);
  }

  private ITreeNode mockNode(String nodeId) {
    ITreeNode node = m_mockNodes.get(nodeId);
    if (node != null) {
      return node;
    }
    // Create a new
    node = mock(ITreeNode.class, "MockNode[" + nodeId + "]");
    when(node.getNodeId()).thenReturn(nodeId);
    m_mockNodes.put(nodeId, node);
    return node;
  }

  private static class P_TreeNode extends AbstractTreeNode {
  }
}
