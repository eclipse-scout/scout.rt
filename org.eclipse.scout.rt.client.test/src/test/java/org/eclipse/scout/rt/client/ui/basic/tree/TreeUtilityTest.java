package org.eclipse.scout.rt.client.ui.basic.tree;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

/**
 * @since 5.2
 */
public class TreeUtilityTest {

  @Test
  public void testCalculateCommonParentNodeNullAndEmpty() {
    assertNull(TreeUtility.calculateCommonParentNode(null));
    assertNull(TreeUtility.calculateCommonParentNode(Collections.<ITreeNode> emptySet()));
  }

  @Test
  public void testCalculateCommonParentNodeOneNodeWithoutParent() {
    ITreeNode nodeA = mockNode("a", null);
    assertNull(TreeUtility.calculateCommonParentNode(Collections.singleton(nodeA)));
  }

  @Test
  public void testCalculateCommonParentNodeOneNodeWithParent() {
    ITreeNode rootNode = mockNode("root", null);
    ITreeNode nodeA = mockNode("a", rootNode);
    assertSame(rootNode, TreeUtility.calculateCommonParentNode(Collections.singleton(nodeA)));
  }

  @Test
  public void testCalculateCommonParentNodeMultipleNodesHavingSameParent() {
    ITreeNode rootNode = mockNode("root", null);
    ITreeNode nodeA = mockNode("a", rootNode);
    ITreeNode nodeB = mockNode("b", rootNode);
    assertSame(rootNode, TreeUtility.calculateCommonParentNode(Arrays.asList(nodeA, nodeB)));
  }

  @Test
  public void testCalculateCommonParentNodeMultipleNodesHavingNullParent() {
    ITreeNode nodeA = mockNode("a", null);
    ITreeNode nodeB = mockNode("b", null);
    assertNull(TreeUtility.calculateCommonParentNode(Arrays.asList(nodeA, nodeB)));
  }

  @Test
  public void testCalculateCommonParentNodeMultipleNodesHavingDifferentParent() {
    ITreeNode parentA = mockNode("parentA", null);
    ITreeNode nodeA = mockNode("a", parentA);
    ITreeNode parentB = mockNode("parentB", null);
    ITreeNode nodeB = mockNode("b", parentB);
    assertNull(TreeUtility.calculateCommonParentNode(Arrays.asList(nodeA, nodeB)));
  }

  @Test
  public void testCalculateCommonParentNodeMultipleNodesOneHavingNullParent() {
    ITreeNode parentA = mockNode("parentA", null);
    ITreeNode nodeA = mockNode("a", parentA);
    ITreeNode nodeB = mockNode("b", null);
    assertNull(TreeUtility.calculateCommonParentNode(Arrays.asList(nodeA, nodeB)));
  }

  private ITreeNode mockNode(String nodeId, ITreeNode parentNode) {
    ITreeNode node = mock(ITreeNode.class, "MockNode[" + nodeId + "]");
    when(node.getNodeId()).thenReturn(nodeId);
    if (parentNode != null) {
      when(node.getParentNode()).thenReturn(parentNode);
    }
    return node;
  }
}
