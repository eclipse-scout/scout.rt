package org.eclipse.scout.rt.client.ui.basic.tree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * @since 5.2
 */
public class TreeNodeTest {

  private P_TreeNode m_rootNode;
  private Map<String, ITreeNode> m_mockNodes;

  @Before
  public void before() {
    m_rootNode = new P_TreeNode();
    m_mockNodes = new HashMap<>();
  }

  @Test
  public void testContainsChildNodeNoChildNodes() {
    ITreeNode nodeA = mockNode("a");
    assertFalse(m_rootNode.containsChildNode(nodeA, false));
    assertFalse(m_rootNode.containsChildNode(nodeA, true));
  }

  @Test
  public void testContainsChildNodeOneChildNodes() {
    ITreeNode nodeA = mockNode("a");
    m_rootNode.addChildNodesInternal(0, Collections.singletonList(nodeA), false);
    assertTrue(m_rootNode.containsChildNode(nodeA, false));
    assertTrue(m_rootNode.containsChildNode(nodeA, true));
  }

  @Test
  public void testContainsChildNodeMultipleChildNodes() {
    ITreeNode nodeA = mockNode("a");
    ITreeNode nodeB = mockNode("b");
    m_rootNode.addChildNodesInternal(0, Arrays.asList(nodeA, nodeB), false);
    assertTrue(m_rootNode.containsChildNode(nodeA, false));
    assertTrue(m_rootNode.containsChildNode(nodeA, true));
    assertTrue(m_rootNode.containsChildNode(nodeB, false));
    assertTrue(m_rootNode.containsChildNode(nodeB, true));
  }

  @Test
  public void testContainsChildNodeHierarchicalChildNodes() {
    P_TreeNode nodeA = new P_TreeNode();
    m_rootNode.addChildNodesInternal(0, Collections.singletonList(nodeA), true);
    ITreeNode nodeB = mockNode("b");
    nodeA.addChildNodesInternal(0, Collections.singletonList(nodeB), false);

    assertTrue(m_rootNode.containsChildNode(nodeA, false));
    assertTrue(m_rootNode.containsChildNode(nodeA, true));

    assertFalse(m_rootNode.containsChildNode(nodeB, false));
    assertTrue(m_rootNode.containsChildNode(nodeB, true));

    assertTrue(nodeA.containsChildNode(nodeB, false));
    assertTrue(nodeA.containsChildNode(nodeB, true));
  }

  @Test
  public void testContainsChildNodeMultipleHierarchicalChildNodes() {
    P_TreeNode nodeA = new P_TreeNode();
    P_TreeNode nodeB = new P_TreeNode();
    m_rootNode.addChildNodesInternal(0, Arrays.asList(nodeA, nodeB), false);

    ITreeNode nodeX = mockNode("x");
    nodeA.addChildNodesInternal(0, Collections.singletonList(nodeX), false);

    ITreeNode nodeY = mockNode("y");
    nodeB.addChildNodesInternal(0, Collections.singletonList(nodeY), false);

    assertTrue(m_rootNode.containsChildNode(nodeA, false));
    assertTrue(m_rootNode.containsChildNode(nodeA, true));

    assertTrue(m_rootNode.containsChildNode(nodeB, false));
    assertTrue(m_rootNode.containsChildNode(nodeB, true));

    assertFalse(m_rootNode.containsChildNode(nodeX, false));
    assertTrue(m_rootNode.containsChildNode(nodeX, true));
    assertTrue(nodeA.containsChildNode(nodeX, false));
    assertTrue(nodeA.containsChildNode(nodeX, true));
    assertFalse(nodeB.containsChildNode(nodeX, false));
    assertFalse(nodeB.containsChildNode(nodeX, true));

    assertFalse(m_rootNode.containsChildNode(nodeY, false));
    assertTrue(m_rootNode.containsChildNode(nodeY, true));
    assertFalse(nodeA.containsChildNode(nodeY, false));
    assertFalse(nodeA.containsChildNode(nodeY, true));
    assertTrue(nodeB.containsChildNode(nodeY, false));
    assertTrue(nodeB.containsChildNode(nodeY, true));
  }

  @Test
  public void testContainsChildNodeResolvedVirtualNode() {
    ITreeNode nodeA = mockNode("a");
    m_rootNode.addChildNodesInternal(0, Collections.singletonList(wrapByVirtualNode(nodeA)), false);

    assertTrue(m_rootNode.containsChildNode(nodeA, false));
    assertTrue(m_rootNode.containsChildNode(nodeA, true));
  }

  @Test
  public void testContainsChildNodeRecursiveResolvedVirtualNode() {
    P_TreeNode nodeA = new P_TreeNode();
    m_rootNode.addChildNodesInternal(0, Collections.singletonList(wrapByVirtualNode(nodeA)), false);

    ITreeNode nodeB = mockNode("b");
    nodeA.addChildNodesInternal(0, Collections.singletonList(wrapByVirtualNode(nodeB)), false);

    assertTrue(m_rootNode.containsChildNode(nodeA, false));
    assertTrue(m_rootNode.containsChildNode(nodeA, true));

    assertFalse(m_rootNode.containsChildNode(nodeB, false));
    assertTrue(m_rootNode.containsChildNode(nodeB, true));

    assertTrue(nodeA.containsChildNode(nodeB, false));
    assertTrue(nodeA.containsChildNode(nodeB, true));
  }

  private VirtualTreeNode wrapByVirtualNode(ITreeNode nodeA) {
    VirtualTreeNode virtualNodaA = new VirtualTreeNode();
    virtualNodaA.setResolvedNode(nodeA);
    return virtualNodaA;
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
