package org.eclipse.scout.rt.client.ui.basic.tree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * @since 5.2
 */
public class VirtualTreeNodeTest {

  private VirtualTreeNode m_treeNode;
  private Map<String, ITreeNode> m_mockNodes;
  private ITreeNode m_nodeA;

  @Before
  public void before() {
    m_treeNode = new VirtualTreeNode();
    m_mockNodes = new HashMap<>();
    m_nodeA = mockNode("a");
  }

  @Test
  public void testContainsChildNodeNullNode() {
    assertFalse(m_treeNode.containsChildNode(null, false));
    assertFalse(m_treeNode.containsChildNode(null, true));
  }

  @Test
  public void testContainsChildNodeUnresolved() {
    assertFalse(m_treeNode.containsChildNode(m_nodeA, false));
    assertFalse(m_treeNode.containsChildNode(m_nodeA, true));
  }

  @Test
  public void testContainsChildNodeResolved() {
    m_treeNode.setResolvedNode(m_nodeA);
    assertTrue(m_treeNode.containsChildNode(m_nodeA, false));
    assertTrue(m_treeNode.containsChildNode(m_nodeA, true));
  }

  @Test
  public void testContainsChildNodeCascadingResolved() {
    m_treeNode.setResolvedNode(wrapByVirtualNode(m_nodeA));
    assertFalse(m_treeNode.containsChildNode(m_nodeA, false));
    assertTrue(m_treeNode.containsChildNode(m_nodeA, true));
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
}
