package org.eclipse.scout.rt.client.ui.basic.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 5.2
 */
public class VirtualTreeNodeTest {

  private static final String TEST_TEXT = "text";
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
  public void testSetEnabled() {
    assertTrue(m_treeNode.isEnabled());
    m_treeNode.setEnabled(false);
    assertFalse(m_treeNode.isEnabled());
    m_treeNode.setEnabled(true);
    assertTrue(m_treeNode.isEnabled());
  }

  @Test
  public void testCellSetText() {
    ITree tree = mock(ITree.class);
    m_treeNode.setTreeInternal(tree, false);
    Cell cell = m_treeNode.getCellForUpdate();
    assertNull(cell.getText());
    cell.setText(TEST_TEXT);
    verify(tree).fireNodeChanged(m_treeNode);
  }

  @Test
  public void testCellSetTextNoChange() {
    Cell cell = m_treeNode.getCellForUpdate();
    cell.setText(TEST_TEXT);
    assertEquals(TEST_TEXT, cell.getText());

    ITree tree = mock(ITree.class);
    m_treeNode.setTreeInternal(tree, false);
    cell.setText(TEST_TEXT);
    verifyZeroInteractions(tree);
  }

  @Test
  public void testCellSetTextWithoutTree() {
    Cell cell = m_treeNode.getCellForUpdate();
    assertNull(cell.getText());
    cell.setText(TEST_TEXT);
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
