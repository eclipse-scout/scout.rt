package org.eclipse.scout.rt.client.ui.basic.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * @since 5.2
 */
public class TreeEventTest {

  private Map<String, ITreeNode> m_mockNodes;

  @Before
  public void setup() {
    m_mockNodes = new HashMap<>();
  }

  @Test
  public void testGetNodeNoNodes() {
    ITree tree = mock(ITree.class);
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION);
    assertNull(event.getNode());
  }

  @Test
  public void testGetNodeSingleNode() {
    ITree tree = mock(ITree.class);
    ITreeNode root = mockNode("root");
    ITreeNode node = mockNode("a");
    when(node.getParentNode()).thenReturn(root);
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, node);
    assertSame(node, event.getNode());
  }

  @Test
  public void testGetNodeMultipleNode() {
    ITree tree = mock(ITree.class);
    ITreeNode root = mockNode("root");
    ITreeNode nodeA = mockNode("a");
    ITreeNode nodeB = mockNode("b");
    when(nodeA.getParentNode()).thenReturn(root);
    when(nodeB.getParentNode()).thenReturn(root);
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, Arrays.asList(nodeB, nodeA));
    assertSame(nodeB, event.getNode());
  }

  @Test
  public void testGetNodesNoNodes() {
    ITree tree = mock(ITree.class);
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION);
    assertEquals(Collections.emptyList(), event.getNodes());
  }

  @Test
  public void testGetNodesSingleNode() {
    ITree tree = mock(ITree.class);
    ITreeNode node = mockNode("a");
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, node);
    assertEquals(Collections.singletonList(node), event.getNodes());
  }

  @Test
  public void testGetNodesMultipleNode() {
    ITree tree = mock(ITree.class);
    ITreeNode nodeA = mockNode("a");
    ITreeNode nodeB = mockNode("b");
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, Arrays.asList(nodeB, nodeA));
    assertEquals(Arrays.asList(nodeB, nodeA), event.getNodes());
  }

  @Test
  public void testHasNodesNoNodes() {
    ITree tree = mock(ITree.class);
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION);
    assertFalse(event.hasNodes());
  }

  @Test
  public void testHasNodesSingleNode() {
    ITree tree = mock(ITree.class);
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, mockNode("a"));
    assertTrue(event.hasNodes());
  }

  @Test
  public void testHasNodesMultipleNode() {
    ITree tree = mock(ITree.class);
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, mockNodes("a", "b"));
    assertTrue(event.hasNodes());
  }

  @Test
  public void testGetNodeCountNoNodes() {
    ITree tree = mock(ITree.class);
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION);
    assertEquals(0, event.getNodeCount());
  }

  @Test
  public void testGetNodeCountSingleNode() {
    ITree tree = mock(ITree.class);
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, mockNode("a"));
    assertEquals(1, event.getNodeCount());
  }

  @Test
  public void testGetNodeCountMultipleNode() {
    ITree tree = mock(ITree.class);
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, mockNodes("a", "b"));
    assertEquals(2, event.getNodeCount());
  }

  @Test
  public void testContainsNodeNoNodes() {
    ITree tree = mock(ITree.class);
    ITreeNode nodeA = mockNode("a");
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION);
    assertFalse(event.containsNode(nodeA));
  }

  @Test
  public void testContainsNodeSingleNode() {
    ITree tree = mock(ITree.class);
    ITreeNode nodeA = mockNode("a");
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, nodeA);
    assertTrue(event.containsNode(nodeA));
  }

  @Test
  public void testContainsNodeMultipleNode() {
    ITree tree = mock(ITree.class);
    ITreeNode nodeA = mockNode("a");
    ITreeNode nodeB = mockNode("b");
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, Arrays.asList(nodeA, nodeB));
    assertTrue(event.containsNode(nodeA));
    assertTrue(event.containsNode(nodeB));
  }

  @Test
  public void testContainsNodeSingleVirtualResolvedNode() {
    ITree tree = mock(ITree.class);
    ITreeNode nodeA = mockNode("a");
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, wrapByVirtualNode(nodeA));
    assertTrue(event.containsNode(nodeA));
  }

  @Test
  public void testContainsNodeMultipleVirtualResolvedNode() {
    ITree tree = mock(ITree.class);
    ITreeNode nodeA = mockNode("a");
    ITreeNode nodeB = mockNode("b");
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, Arrays.asList(wrapByVirtualNode(nodeA), wrapByVirtualNode(nodeB)));
    assertTrue(event.containsNode(nodeA));
    assertTrue(event.containsNode(nodeB));
  }

  @Test
  public void testContainsNodeSingleVirtualUnresolvedNode() {
    ITree tree = mock(ITree.class);
    ITreeNode nodeA = mockNode("a");
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, new VirtualTreeNode());
    assertFalse(event.containsNode(nodeA));
  }

  @Test
  public void testContainsNodeMultipleVirtualUnrResolvedNode() {
    ITree tree = mock(ITree.class);
    ITreeNode nodeA = mockNode("a");
    ITreeNode nodeB = mockNode("b");
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, Arrays.asList(new VirtualTreeNode(), new VirtualTreeNode()));
    assertFalse(event.containsNode(nodeA));
    assertFalse(event.containsNode(nodeB));
  }

  /**
   * Currently it is not supported that a {@link VirtualTreeNode} references another {@link VirtualTreeNode}.
   */
  @Test
  public void testContainsNodeSingleRecursiveVirtualResolvedNode() {
    ITree tree = mock(ITree.class);
    ITreeNode nodeA = mockNode("a");
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, wrapByVirtualNode(wrapByVirtualNode(wrapByVirtualNode(nodeA))));
    assertFalse(event.containsNode(nodeA));
  }

  @Test
  public void testSetNodesSameParent() {
    ITree tree = mock(ITree.class);
    ITreeNode parent = mockNode("parent");
    ITreeNode nodeA = mockNode("a", parent);
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, nodeA);

    assertEquals(Arrays.asList(nodeA), event.getNodes());
    assertEquals(parent, event.getCommonParentNode());

    ITreeNode nodeB = mockNode("b", parent);
    event.setNodes(Arrays.asList(nodeA, nodeB));
    assertEquals(Arrays.asList(nodeA, nodeB), event.getNodes());
    assertEquals(parent, event.getCommonParentNode());
  }

  @Test
  public void testSetNodesDifferentParents() {
    ITree tree = mock(ITree.class);
    ITreeNode parentA = mockNode("parentA");
    ITreeNode nodeA = mockNode("a", parentA);
    TreeEvent event = new TreeEvent(tree, TreeEvent.TYPE_NODE_ACTION, nodeA);

    assertEquals(Arrays.asList(nodeA), event.getNodes());
    assertEquals(parentA, event.getCommonParentNode());

    ITreeNode parentB = mockNode("parentB");
    ITreeNode nodeB = mockNode("b", parentB);

    event.setNodes(Arrays.asList(nodeA, nodeB));

    assertEquals(Arrays.asList(nodeA, nodeB), event.getNodes());
    assertNull(event.getCommonParentNode());
  }

  private VirtualTreeNode wrapByVirtualNode(ITreeNode nodeA) {
    VirtualTreeNode virtualNodaA = new VirtualTreeNode();
    virtualNodaA.setResolvedNode(nodeA);
    return virtualNodaA;
  }

  private List<ITreeNode> mockNodes(String... nodeIds) {
    if (nodeIds == null) {
      return null;
    }
    List<ITreeNode> rows = new ArrayList<>();
    for (String nodeId : nodeIds) {
      rows.add(mockNode(nodeId));
    }
    return rows;
  }

  private ITreeNode mockNode(String nodeId) {
    return mockNode(nodeId, null);
  }

  private ITreeNode mockNode(String nodeId, ITreeNode parentNode) {
    ITreeNode node = m_mockNodes.get(nodeId);
    if (node != null) {
      return node;
    }
    // Create a new
    node = mock(ITreeNode.class, "MockNode[" + nodeId + "]");
    when(node.getNodeId()).thenReturn(nodeId);
    if (parentNode != null) {
      when(node.getParentNode()).thenReturn(parentNode);
    }
    m_mockNodes.put(nodeId, node);
    return node;
  }
}
