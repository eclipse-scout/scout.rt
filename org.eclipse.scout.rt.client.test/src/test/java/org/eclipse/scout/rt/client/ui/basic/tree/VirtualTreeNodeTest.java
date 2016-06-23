package org.eclipse.scout.rt.client.ui.basic.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
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
  private Set<ITreeNode> m_nodeCollector;

  @Before
  public void before() {
    m_treeNode = new VirtualTreeNode();
    m_mockNodes = new HashMap<>();
    m_nodeA = mockNode("a");
    m_nodeCollector = new HashSet<>();
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
  public void testCollectChildNodesUnresolved() {
    m_treeNode.collectChildNodes(m_nodeCollector, false);
    assertEquals(Collections.emptySet(), m_nodeCollector);
  }

  @Test
  public void testCollectChildNodesResolved() {
    m_treeNode.setResolvedNode(m_nodeA);
    m_treeNode.collectChildNodes(m_nodeCollector, false);
    assertEquals(Collections.singleton(m_nodeA), m_nodeCollector);
  }

  @Test
  public void testCollectChildNodeCascadingResolved() {
    VirtualTreeNode intermediateVirtualNode = wrapByVirtualNode(m_nodeA);
    m_treeNode.setResolvedNode(intermediateVirtualNode);
    m_treeNode.collectChildNodes(m_nodeCollector, false);
    assertEquals(Collections.singleton(intermediateVirtualNode), m_nodeCollector);
  }

  @Test
  public void testCollectChildNodesRecursiveUnresolved() {
    m_treeNode.collectChildNodes(m_nodeCollector, true);
    assertEquals(Collections.emptySet(), m_nodeCollector);
  }

  @Test
  public void testCollectChildNodesRecursiveResolved() {
    m_treeNode.setResolvedNode(m_nodeA);
    m_treeNode.collectChildNodes(m_nodeCollector, true);
    assertEquals(Collections.singleton(m_nodeA), m_nodeCollector);
  }

  @Test
  public void testCollectChildNodeRecursiveCascadingResolved() {
    VirtualTreeNode intermediateVirtualNode = wrapByVirtualNode(m_nodeA);
    m_treeNode.setResolvedNode(intermediateVirtualNode);
    m_treeNode.collectChildNodes(m_nodeCollector, true);
    assertEquals(CollectionUtility.hashSet(intermediateVirtualNode, m_nodeA), m_nodeCollector);
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
