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
package org.eclipse.scout.rt.ui.html.json.tree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNodeFilter;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.menu.fixtures.Menu;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.rt.ui.html.json.tree.fixtures.Tree;
import org.eclipse.scout.rt.ui.html.json.tree.fixtures.TreeNode;
import org.eclipse.scout.rt.ui.html.json.tree.fixtures.TreeWith3Levels;
import org.eclipse.scout.rt.ui.html.json.tree.fixtures.TreeWithOneNode;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonTreeTest {
  private JsonSessionMock m_jsonSession;

  @Before
  public void setUp() {
    m_jsonSession = new JsonSessionMock();
  }

  /**
   * Tests whether the model node gets correctly selected
   */
  @Test
  public void testSelectionEvent() throws ProcessingException, JSONException {
    ITree tree = createTreeWithOneNode();
    ITreeNode node = tree.getRootNode().getChildNode(0);
    assertFalse(node.isSelectedNode());

    JsonTree<ITree> jsonTree = m_jsonSession.createJsonAdapter(tree, null);

    JsonEvent event = createJsonSelectedEvent(jsonTree.getOrCreateNodeId(node));
    jsonTree.handleUiEvent(event, new JsonResponse());

    assertTrue(node.isSelectedNode());
  }

  /**
   * Response must not contain the selection event if the selection was triggered by the request
   */
  @Test
  public void testIgnorableSelectionEvent() throws ProcessingException, JSONException {
    ITree tree = createTreeWithOneNode();
    ITreeNode node = tree.getRootNode().getChildNode(0);

    JsonTree<ITree> jsonTree = m_jsonSession.createJsonAdapter(tree, null);

    JsonEvent event = createJsonSelectedEvent(jsonTree.getOrCreateNodeId(node));
    jsonTree.handleUiEvent(event, new JsonResponse());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_jsonSession.currentJsonResponse(), JsonTree.EVENT_NODES_SELECTED);
    assertTrue(responseEvents.size() == 0);
  }

  /**
   * If the selection event triggers the selection of another node, the selection event must not be ignored.
   */
  //FIXME CGU Test fails due to scout model bug: selectNode puts first selection event AFTER this new selection event -> gets filtered in processEventBuffers. With table it works though.
//  @Test
//  public void testIgnorableSelectionEvent2() throws ProcessingException, JSONException {
//    List<ITreeNode> nodes = new LinkedList<>();
//    final TreeNode firstNode = new TreeNode();
//    final TreeNode secondNode = new TreeNode();
//
//    nodes.add(firstNode);
//    nodes.add(secondNode);
//    ITree tree = createTree(nodes) {
//
//      @Override
//      protected void execNodesSelected(TreeEvent e) throws ProcessingException {
//        if (e.getNode().equals(secondNode)) {
//          selectNode(firstNode);
//        }
//      }
//    };
//
//    JsonTree<ITree> jsonTree = m_jsonSession.createJsonAdapter(tree, null);
//    JsonEvent event = createJsonSelectedEvent(jsonTree.getOrCreateNodeId(secondNode));
//
//    assertFalse(firstNode.isSelectedNode());
//    assertFalse(secondNode.isSelectedNode());
//
//    jsonTree.handleUiEvent(event, new JsonResponse());
//
//    assertTrue(firstNode.isSelectedNode());
//    assertFalse(secondNode.isSelectedNode());
//
//    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
//        m_jsonSession.currentJsonResponse(), JsonTree.EVENT_NODES_SELECTED);
//    assertTrue(responseEvents.size() == 1);
//
//    List<ITreeNode> treeNodes = jsonTree.extractTreeNodes(responseEvents.get(0).getData());
//    assertEquals(firstNode, treeNodes.get(0));
//  }

  /**
   * Response must not contain the expansion event if the expansion was triggered by the request
   */
  @Test
  public void testIgnorableExpansionEvent() throws ProcessingException, JSONException {
    ITree tree = createTreeWithOneNode();
    ITreeNode node = tree.getRootNode().getChildNode(0);

    JsonTree<ITree> jsonTree = m_jsonSession.createJsonAdapter(tree, null);

    //Check expanded = true
    JsonEvent event = createJsonExpansionEvent(jsonTree.getOrCreateNodeId(node), true);
    jsonTree.handleUiEvent(event, new JsonResponse());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_jsonSession.currentJsonResponse(), JsonTree.EVENT_NODE_EXPANDED);
    assertTrue(responseEvents.size() == 0);

    //Check expanded = false
    event = createJsonExpansionEvent(jsonTree.getOrCreateNodeId(node), false);
    jsonTree.handleUiEvent(event, new JsonResponse());

    responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_jsonSession.currentJsonResponse(), JsonTree.EVENT_NODE_EXPANDED);
    assertTrue(responseEvents.size() == 0);
  }

  @Test
  public void testDispose() throws ProcessingException {
    ITree tree = new TreeWith3Levels();
    tree.initTree();
    JsonTree<ITree> object = m_jsonSession.newJsonAdapter(tree, null, null);
    WeakReference<JsonTree> ref = new WeakReference<JsonTree>(object);

    object.dispose();
    m_jsonSession.flush();
    m_jsonSession = null;
    object = null;
    JsonTestUtility.assertGC(ref);
  }

  @Test
  public void testMenuDisposalOnPropertyChange() throws ProcessingException, JSONException {
    ITree tree = createTreeWithOneNode();
    ITreeNode node = tree.getRootNode().getChildNode(0);
    assertFalse(node.isSelectedNode());

    JsonTree<ITree> jsonTree = m_jsonSession.createJsonAdapter(tree, null);
    IJsonAdapter<?> contextMenu = jsonTree.getAdapter(tree.getContextMenu());

    Menu menu1 = new Menu();
    tree.getContextMenu().addChildAction(menu1);
    assertNotNull(contextMenu.getAdapter(menu1));
    assertTrue(contextMenu.getAdapter(menu1).isAttached());

    tree.getContextMenu().removeChildAction(menu1);
    m_jsonSession.flush();
    assertNull(contextMenu.getAdapter(menu1));
  }

  @Test
  public void testMultipleMenuDisposallOnPropertyChange() throws ProcessingException, JSONException {
    ITree tree = createTreeWithOneNode();
    ITreeNode node = tree.getRootNode().getChildNode(0);
    assertFalse(node.isSelectedNode());

    JsonTree<ITree> jsonTree = m_jsonSession.createJsonAdapter(tree, null);
    IJsonAdapter<?> contextMenu = jsonTree.getAdapter(tree.getContextMenu());

    Menu menu1 = new Menu();
    Menu menu2 = new Menu();
    tree.getContextMenu().addChildAction(menu1);
    tree.getContextMenu().addChildAction(menu2);
    assertNotNull(contextMenu.getAdapter(menu1));
    assertTrue(contextMenu.getAdapter(menu1).isAttached());
    assertNotNull(contextMenu.getAdapter(menu2));
    assertTrue(contextMenu.getAdapter(menu2).isAttached());

    tree.getContextMenu().removeChildAction(menu1);
    m_jsonSession.flush();
    assertNull(contextMenu.getAdapter(menu1));
    assertNotNull(contextMenu.getAdapter(menu2));
    assertTrue(contextMenu.getAdapter(menu2).isAttached());
  }

  /**
   * Tests whether a deletion event with correct node id gets sent whenever a node gets deleted.
   */
  @Test
  public void testNodesDeletedEvent() throws ProcessingException, JSONException {
    List<ITreeNode> nodes = new ArrayList<ITreeNode>();
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    ITree tree = createTree(nodes);
    JsonTree<ITree> jsonTree = m_jsonSession.createJsonAdapter(tree, null);

    String node1Id = jsonTree.getOrCreateNodeId(nodes.get(1));
    tree.removeNode(nodes.get(1));

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_jsonSession.currentJsonResponse(), JsonTree.EVENT_NODES_DELETED);
    assertTrue(responseEvents.size() == 1);

    JsonEvent event = responseEvents.get(0);
    JSONArray nodeIds = event.getData().getJSONArray("nodeIds");

    assertTrue(nodeIds.length() == 1);
    assertTrue(nodeIds.get(0).equals(node1Id));
  }

  /**
   * Tests whether the node gets removed from the maps after deletion (m_treeNodes, m_treeNodeIds)
   */
  @Test
  public void testNodeDisposal() throws ProcessingException, JSONException {
    List<ITreeNode> nodes = new ArrayList<ITreeNode>();
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    ITree tree = createTree(nodes);
    JsonTree<ITree> jsonTree = m_jsonSession.createJsonAdapter(tree, null);

    String node0Id = jsonTree.getOrCreateNodeId(nodes.get(0));
    assertNotNull(node0Id);
    assertNotNull(jsonTree.getNode(node0Id));

    tree.removeNode(nodes.get(0));

    assertNull(jsonTree.getNodeId(nodes.get(0)));
    assertNull(jsonTree.getNode(node0Id));
  }

  @Test
  public void testNodeFilter() throws ProcessingException, JSONException {
    TreeNode nodeToFilter = new TreeNode();
    nodeToFilter.setEnabled(false);
    List<ITreeNode> nodes = new ArrayList<ITreeNode>();
    nodes.add(nodeToFilter);
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    ITree tree = createTree(nodes);
    JsonTree<ITree> jsonTree = m_jsonSession.createJsonAdapter(tree, null);

    String node0Id = jsonTree.getOrCreateNodeId(nodes.get(0));
    assertNotNull(node0Id);
    assertNotNull(jsonTree.getNode(node0Id));

    tree.addNodeFilter(new ITreeNodeFilter() {

      @Override
      public boolean accept(ITreeNode node, int level) {
        return node.isEnabled();
      }
    });

    assertNull(jsonTree.getNodeId(nodes.get(0)));
    assertNull(jsonTree.getNode(node0Id));
  }

  /**
   * Tests whether the child nodes gets removed from the maps after deletion (m_treeNodes, m_treeNodeIds)
   */
  @Test
  public void testNodeDisposalRec() throws ProcessingException, JSONException {
    ITree tree = new TreeWith3Levels();
    tree.initTree();

    List<ITreeNode> allNodes = getAllTreeNodes(tree);
    List<String> allNodeIds = new LinkedList<String>();

    JsonTree<ITree> jsonTree = m_jsonSession.createJsonAdapter(tree, null);

    for (ITreeNode node : allNodes) {
      String nodeId = jsonTree.getOrCreateNodeId(node);
      allNodeIds.add(nodeId);

      assertNotNull(nodeId);
      assertNotNull(jsonTree.getNode(nodeId));
    }

    tree.removeNode(allNodes.get(0));

    for (ITreeNode node : allNodes) {
      assertNull(jsonTree.getNodeId(node));
    }
    for (String nodeId : allNodeIds) {
      assertNull(jsonTree.getNode(nodeId));
    }
  }

  /**
   * Tests whether an all nodes deleted event gets sent whenever all children of a node get deleted.
   */
  @Test
  public void testAllNodesDeletedEvent() throws ProcessingException, JSONException {
    List<ITreeNode> nodes = new ArrayList<ITreeNode>();
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    ITree tree = createTree(nodes);
    m_jsonSession.createJsonAdapter(tree, null);

    tree.removeChildNodes(tree.getRootNode(), tree.getRootNode().getChildNodes());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_jsonSession.currentJsonResponse(), JsonTree.EVENT_ALL_NODES_DELETED);
    JsonEvent event = responseEvents.get(0);
    assertNull(event.getData().optJSONArray("nodeIds"));
  }

  /**
   * GetOrCreateNodeId must not create a nodeId if null is passed (may happen if someone calls getSelectedNode which may
   * return null).
   */
  @Test
  public void testGetOrCreateNodeIdWithNull() throws ProcessingException, JSONException {
    ITree tree = createTreeWithOneNode();
    JsonTree<ITree> jsonTree = m_jsonSession.createJsonAdapter(tree, null);
    Assert.assertNull(jsonTree.getOrCreateNodeId(null));
  }

  public static JsonEvent createJsonSelectedEvent(String nodeId) throws JSONException {
    String desktopId = "x"; // never used
    JSONObject data = new JSONObject();
    JSONArray nodeIds = new JSONArray();
    nodeIds.put(nodeId);
    data.put(JsonTree.PROP_NODE_IDS, nodeIds);
    return new JsonEvent(desktopId, JsonTree.EVENT_NODES_SELECTED, data);
  }

  public static JsonEvent createJsonExpansionEvent(String nodeId, boolean expanded) throws JSONException {
    JSONObject data = new JSONObject();
    data.put(JsonTree.PROP_NODE_ID, nodeId);
    data.put("expanded", expanded);
    return new JsonEvent("", JsonTree.EVENT_NODE_EXPANDED, data);
  }

  protected TreeWithOneNode createTreeWithOneNode() throws ProcessingException {
    TreeWithOneNode tree = new TreeWithOneNode();
    tree.initTree();
    return tree;
  }

  protected Tree createTree(List<ITreeNode> nodes) throws ProcessingException {
    Tree tree = new Tree(nodes);
    tree.initTree();
    return tree;
  }

  public static String getOrCreateNodeId(JsonTree tree, ITreeNode node) {
    return tree.getOrCreateNodeId(node);
  }

  public static String getNodeId(JsonTree tree, ITreeNode node) {
    return tree.getNodeId(node);
  }

  public static ITreeNode getNode(JsonTree tree, String nodeId) {
    return tree.getNode(nodeId);
  }

  public static List<ITreeNode> getAllTreeNodes(final ITree tree) {
    final List<ITreeNode> nodes = new LinkedList<ITreeNode>();
    tree.visitTree(new ITreeVisitor() {

      @Override
      public boolean visit(ITreeNode node) {
        if (!tree.isRootNodeVisible() && tree.getRootNode() == node) {
          return true;
        }
        nodes.add(node);
        return true;
      }
    });
    return nodes;
  }
}
