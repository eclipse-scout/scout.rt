/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.tree;

import static org.junit.Assert.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.visitor.DepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.IDepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.ui.html.UiException;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonOutline;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.Outline;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.TablePage;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonAdapterMock;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.menu.IJsonContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.fixtures.Menu;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.rt.ui.html.json.tree.fixtures.Tree;
import org.eclipse.scout.rt.ui.html.json.tree.fixtures.TreeNode;
import org.eclipse.scout.rt.ui.html.json.tree.fixtures.TreeWith3Levels;
import org.eclipse.scout.rt.ui.html.json.tree.fixtures.TreeWithOneNode;
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
  private UiSessionMock m_uiSession;

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
  }

  /**
   * Tests whether the model node gets correctly selected
   */
  @Test
  public void testSelectionEvent() throws JSONException {
    ITree tree = createTreeWithOneNode();
    ITreeNode node = tree.getRootNode().getChildNode(0);
    assertFalse(node.isSelectedNode());

    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());

    JsonEvent event = createJsonSelectedEvent(jsonTree.getOrCreateNodeId(node));
    jsonTree.handleUiEvent(event);

    assertTrue(node.isSelectedNode());
  }

  /**
   * Response must not contain the selection event if the selection was triggered by the request
   */
  @Test
  public void testIgnorableSelectionEvent() throws JSONException {
    ITree tree = createTreeWithOneNode();
    ITreeNode node = tree.getRootNode().getChildNode(0);

    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());

    JsonEvent event = createJsonSelectedEvent(jsonTree.getOrCreateNodeId(node));
    jsonTree.handleUiEvent(event);

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTree.EVENT_NODES_SELECTED);
    assertEquals(0, responseEvents.size());
  }

  /*
   * If the selection event triggers the selection of another node, the selection event must not be ignored.
   */
  //TODO [7.0] cgu: Test fails due to scout model bug: selectNode puts first selection event AFTER this new selection event -> gets filtered in processEventBuffers. With table it works though.
//  @Test
//  public void testIgnorableSelectionEvent2() throws JSONException {
//    List<ITreeNode> nodes = new LinkedList<>();
//    final TreeNode firstNode = new TreeNode();
//    final TreeNode secondNode = new TreeNode();
//
//    nodes.add(firstNode);
//    nodes.add(secondNode);
//    ITree tree = new Tree(nodes) {
//
//      @Override
//      protected void execNodesSelected(TreeEvent e) {
//        if (e.getNode().equals(secondNode)) {
//          selectNode(firstNode);
//        }
//      }
//    };
//    tree.initTree();
//
//    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());
//    JsonEvent event = createJsonSelectedEvent(jsonTree.getOrCreateNodeId(secondNode));
//
//    assertFalse(firstNode.isSelectedNode());
//    assertFalse(secondNode.isSelectedNode());
//
//    jsonTree.handleUiEvent(event);
//
//    assertTrue(firstNode.isSelectedNode());
//    assertFalse(secondNode.isSelectedNode());
//
//    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
//        m_uiSession.currentJsonResponse(), JsonTree.EVENT_NODES_SELECTED);
//    assertTrue(responseEvents.size() == 1);
//
//    List<ITreeNode> treeNodes = jsonTree.extractTreeNodes(responseEvents.get(0).getData());
//    assertEquals(firstNode, treeNodes.get(0));
//  }

  /**
   * Selection must not be cleared if nodeIds cannot be resolved.
   */
  @Test
  public void testIgnorableSelectionEventInconsistentState() throws JSONException {
    TreeNode node0 = new TreeNode();
    TreeNode node1 = new TreeNode();
    TreeNode node2 = new TreeNode();
    TreeNode child0 = new TreeNode();
    TreeNode child1 = new TreeNode();

    List<ITreeNode> nodes = new ArrayList<>(Arrays.asList(node0, node1, node2));
    ITree tree = createTree(nodes);
    tree.addChildNode(node0, child0);
    tree.addChildNode(node0, child1);
    tree.selectNode(node0);

    JsonTree<ITree> jsonTree = UiSessionTestUtility.newJsonAdapter(m_uiSession, tree);
    jsonTree.toJson();

    assertTrue(node0.isSelectedNode());
    assertFalse(node1.isSelectedNode());

    // ----------

    // Model selection MUST NOT be cleared when an invalid selection is sent from the UI

    JsonEvent event = createJsonSelectedEvent("not-existing-id");
    jsonTree.handleUiEvent(event);
    jsonTree.cleanUpEventFilters();

    assertTrue(node0.isSelectedNode());
    assertFalse(node1.isSelectedNode());

    // No reply (we assume that the UI state is correct and only the event was wrong, e.g. due to caching)
    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTree.EVENT_NODES_SELECTED);
    assertEquals(0, responseEvents.size());
    JsonTestUtility.endRequest(m_uiSession);

    // ----------

    // Model selection MUST be cleared when an empty selection is sent from the UI

    event = createJsonSelectedEvent(null);
    jsonTree.handleUiEvent(event);
    jsonTree.cleanUpEventFilters();

    assertFalse(node0.isSelectedNode());
    assertFalse(node1.isSelectedNode());

    // No reply (states should be equal)
    responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTree.EVENT_NODES_SELECTED);
    assertEquals(0, responseEvents.size());
    JsonTestUtility.endRequest(m_uiSession);

    // ----------

    // Model selection MUST be updated when a partially invalid selection is sent from the UI

    event = createJsonSelectedEvent("not-existing-id");
    event.getData().getJSONArray(JsonTree.PROP_NODE_IDS).put(jsonTree.getNodeId(node1));
    jsonTree.handleUiEvent(event);
    jsonTree.cleanUpEventFilters();

    assertFalse(node0.isSelectedNode());
    assertTrue(node1.isSelectedNode());

    // Inform the UI about the change
    responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTree.EVENT_NODES_SELECTED);
    assertEquals(1, responseEvents.size());
    List<ITreeNode> treeNodes = jsonTree.extractTreeNodes(responseEvents.get(0).getData());
    assertEquals(node1, treeNodes.get(0));
    JsonTestUtility.endRequest(m_uiSession);
  }

  /**
   * Response must not contain the expansion event if the expansion was triggered by the request
   */
  @Test
  public void testIgnorableExpansionEvent() throws JSONException {
    ITree tree = createTreeWithOneNode();
    ITreeNode node = tree.getRootNode().getChildNode(0);

    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());

    //Check expanded = true
    JsonEvent event = createJsonExpansionEvent(jsonTree.getOrCreateNodeId(node), true);
    jsonTree.handleUiEvent(event);

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTree.EVENT_NODE_EXPANDED);
    assertEquals(0, responseEvents.size());

    //Check expanded = false
    event = createJsonExpansionEvent(jsonTree.getOrCreateNodeId(node), false);
    jsonTree.handleUiEvent(event);

    responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTree.EVENT_NODE_EXPANDED);
    assertEquals(0, responseEvents.size());
  }

  @Test
  public void testDispose() {
    ITree tree = new TreeWith3Levels();
    tree.init();
    JsonTree<ITree> object = UiSessionTestUtility.newJsonAdapter(m_uiSession, tree);
    WeakReference<JsonTree> ref = new WeakReference<>(object);

    object.dispose();
    m_uiSession = null;
    object = null;
    TestingUtility.assertGC(ref);
  }

  @Test
  public void testMenuDisposalOnPropertyChange() throws JSONException {
    ITree tree = createTreeWithOneNode();
    ITreeNode node = tree.getRootNode().getChildNode(0);
    assertFalse(node.isSelectedNode());

    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());

    Menu menu1 = new Menu();
    tree.getContextMenu().addChildAction(menu1);
    assertNotNull(jsonTree.getAdapter(menu1));
    assertTrue(jsonTree.getAdapter(menu1).isInitialized());

    tree.getContextMenu().removeChildAction(menu1);
    assertNull(jsonTree.getAdapter(menu1));
  }

  @Test
  public void testMultipleMenuDisposallOnPropertyChange() throws JSONException {
    ITree tree = createTreeWithOneNode();
    ITreeNode node = tree.getRootNode().getChildNode(0);
    assertFalse(node.isSelectedNode());

    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());

    Menu menu1 = new Menu();
    Menu menu2 = new Menu();
    tree.getContextMenu().addChildAction(menu1);
    tree.getContextMenu().addChildAction(menu2);
    assertNotNull(jsonTree.getAdapter(menu1));
    assertTrue(jsonTree.getAdapter(menu1).isInitialized());
    assertNotNull(jsonTree.getAdapter(menu2));
    assertTrue(jsonTree.getAdapter(menu2).isInitialized());

    tree.getContextMenu().removeChildAction(menu1);
    assertNull(jsonTree.getAdapter(menu1));
    assertNotNull(jsonTree.getAdapter(menu2));
    assertTrue(jsonTree.getAdapter(menu2).isInitialized());
  }

  /**
   * Tests whether a menus property change event gets sent for the json tree if the context menu changes
   */
  @Test
  public void testMenusChangedEvent() throws JSONException {
    ITree tree = createTreeWithOneNode();
    ITreeNode node = tree.getRootNode().getChildNode(0);
    assertFalse(node.isSelectedNode());

    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());

    Menu menu1 = new Menu();
    tree.getContextMenu().addChildAction(menu1);
    IJsonAdapter<?> jsonMenu1 = jsonTree.getAdapter(menu1);
    assertNotNull(jsonMenu1);
    assertTrue(jsonMenu1.isInitialized());

    JSONArray jsonMenus = JsonTestUtility.extractProperty(
        m_uiSession.currentJsonResponse(), jsonTree.getId(), IJsonContextMenuOwner.PROP_MENUS);

    assertNotNull(jsonMenus);
    assertEquals(1, jsonMenus.length());
    assertEquals(jsonMenu1.getId(), jsonMenus.get(0));
  }

  /**
   * Tests whether a deletion event with correct node id gets sent whenever a node gets deleted.
   */
  @Test
  public void testNodesDeletedEvent() throws JSONException {
    List<ITreeNode> nodes = new ArrayList<>();
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    ITree tree = createTree(nodes);
    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());

    String node1Id = jsonTree.getOrCreateNodeId(nodes.get(1));
    tree.removeNode(nodes.get(1));

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTree.EVENT_NODES_DELETED);
    assertEquals(1, responseEvents.size());
    assertEventTypeAndNodeIds(responseEvents.get(0), "nodesDeleted", node1Id);
  }

  /**
   * Tests whether the node gets removed from the maps after deletion (m_treeNodes, m_treeNodeIds)
   */
  @Test
  public void testNodeDisposal() throws JSONException {
    List<ITreeNode> nodes = new ArrayList<>();
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    ITree tree = createTree(nodes);
    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());

    String node0Id = jsonTree.getOrCreateNodeId(nodes.get(0));
    assertNotNull(node0Id);
    assertNotNull(jsonTree.optTreeNodeForNodeId(node0Id));

    tree.removeNode(nodes.get(0));

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertNull(jsonTree.optNodeId(nodes.get(0)));
    assertNull(jsonTree.optTreeNodeForNodeId(node0Id));
    assertNull(jsonTree.getParentNode(nodes.get(0)));
    assertEquals(0, jsonTree.getChildNodes(nodes.get(0)).size());
  }

  @Test
  public void testChildNodeDisposal() throws JSONException {
    List<ITreeNode> nodes = new ArrayList<>();
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    TreeNode child0 = new TreeNode();
    TreeNode child1 = new TreeNode();
    ITree tree = createTree(nodes);
    tree.addChildNode(nodes.get(0), child0);
    tree.addChildNode(nodes.get(0), child1);
    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());

    String child0Id = jsonTree.getOrCreateNodeId(child0);
    assertNotNull(child0Id);
    assertNotNull(jsonTree.optTreeNodeForNodeId(child0Id));
    assertEquals(nodes.get(0), jsonTree.getParentNode(child0));
    assertEquals(2, jsonTree.getChildNodes(nodes.get(0)).size());

    tree.removeNode(child0);

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertNull(jsonTree.optNodeId(child0));
    assertNull(jsonTree.optTreeNodeForNodeId(child0Id));
    assertNull(jsonTree.getParentNode(child0));
    assertEquals(1, jsonTree.getChildNodes(nodes.get(0)).size());
  }

  @Test
  public void testNodeFilter() throws JSONException {
    TreeNode nodeToFilter = new TreeNode();
    nodeToFilter.setEnabled(false);
    List<ITreeNode> nodes = new ArrayList<>();
    nodes.add(nodeToFilter);
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    ITree tree = createTree(nodes);
    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());

    String node0Id = jsonTree.getOrCreateNodeId(nodes.get(0));
    assertNotNull(node0Id);
    assertNotNull(jsonTree.optTreeNodeForNodeId(node0Id));

    tree.addNodeFilter((node, level) -> node.isEnabled());

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertNull(jsonTree.optNodeId(nodes.get(0)));
    assertNull(jsonTree.optTreeNodeForNodeId(node0Id));
  }

  @Test
  public void testGetVsOpt() {
    List<ITreeNode> nodes = new ArrayList<>();
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    ITree tree = createTree(nodes);
    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());

    String node0Id = jsonTree.getOrCreateNodeId(nodes.get(0));
    assertNotNull(node0Id);
    assertEquals(node0Id, jsonTree.optNodeId(nodes.get(0)));
    assertEquals(node0Id, jsonTree.getNodeId(nodes.get(0)));
    assertNotNull(jsonTree.optTreeNodeForNodeId(node0Id));
    assertNotNull(jsonTree.getTreeNodeForNodeId(node0Id));

    String nonExistingNodeId = "bla";
    ITreeNode nonExistingTreeNode = new TreeNode();
    assertNull(jsonTree.optNodeId(nonExistingTreeNode));
    try {
      jsonTree.getNodeId(nonExistingTreeNode);
      fail("Expected UiException");
    }
    catch (UiException e) {
      // ok
    }
    assertNull(jsonTree.optTreeNodeForNodeId(nonExistingNodeId));
    try {
      jsonTree.getTreeNodeForNodeId(nonExistingNodeId);
      fail("Expected UiException");
    }
    catch (UiException e) {
      // ok
    }
  }

  @Test
  public void testNodeFilter_events() throws JSONException {
    TreeNode nodeToFilter = new TreeNode();
    nodeToFilter.setEnabled(false);
    List<ITreeNode> nodes = new ArrayList<>();
    nodes.add(nodeToFilter);
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    ITree tree = createTree(nodes);
    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());
    jsonTree.toJson();

    String node0Id = jsonTree.getOrCreateNodeId(nodes.get(0));
    assertNotNull(node0Id);
    assertNotNull(jsonTree.optTreeNodeForNodeId(node0Id));

    tree.addNodeFilter((node, level) -> node.isEnabled());

    JsonTestUtility.processBufferedEvents(m_uiSession);
    assertNull(jsonTree.optNodeId(nodes.get(0)));
    assertNull(jsonTree.optTreeNodeForNodeId(node0Id));

    List<JsonEvent> events = m_uiSession.currentJsonResponse().getEventList();
    assertEquals(1, events.size());
    assertEventTypeAndNodeIds(events.get(0), "nodesDeleted", node0Id);
  }

//  @Test
//  public void testNodeAndUserFilter() throws JSONException {
//    TreeNode node0 = new TreeNode();
//    TreeNode node1 = new TreeNode();
//    TreeNode node2 = new TreeNode();
//    List<ITreeNode> nodes = new ArrayList<ITreeNode>();
//    nodes.add(node0);
//    nodes.add(node1);
//    nodes.add(node2);
//    ITree tree = createTree(nodes);
//    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());
//    jsonTree.toJson();
//
//    String node0Id = jsonTree.getOrCreateNodeId(nodes.get(0));
//    assertNotNull(node0Id);
//    assertNotNull(jsonTree.getNode(node0Id));
//
//    node0.setEnabled(false);
//    tree.addNodeFilter(new ITreeNodeFilter() {
//
//      @Override
//      public boolean accept(ITreeNode node, int level) {
//        return node.isEnabled();
//      }
//    });
//
//    JsonEvent event = createJsonRowsFilteredEvent(row0Id, row2Id);
//    jsonTree.handleUiEvent(event);
//
//    JsonTestUtility.processBufferedEvents(m_uiSession);
//    assertNull(jsonTree.getNodeId(nodes.get(0)));
//    assertNull(jsonTree.getNode(node0Id));
//
//    List<JsonEvent> events = m_uiSession.currentJsonResponse().getEventList();
//    assertEquals(1, events.size());
//    assertEventTypeAndNodeIds(events.get(0), "nodesDeleted", node0Id);
//  }

  /**
   * Tests whether the child nodes gets removed from the maps after deletion (m_treeNodes, m_treeNodeIds)
   */
  @Test
  public void testNodeDisposalRec() throws JSONException {
    ITree tree = new TreeWith3Levels();
    tree.init();

    List<ITreeNode> allNodes = getAllTreeNodes(tree);
    List<String> allNodeIds = new LinkedList<>();

    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());

    for (ITreeNode node : allNodes) {
      String nodeId = jsonTree.getOrCreateNodeId(node);
      allNodeIds.add(nodeId);

      assertNotNull(nodeId);
      assertNotNull(jsonTree.optTreeNodeForNodeId(nodeId));
      assertNotNull(jsonTree.getParentNode(node));
    }

    tree.removeNode(allNodes.get(0));

    JsonTestUtility.processBufferedEvents(m_uiSession);
    for (ITreeNode node : allNodes) {
      assertNull(jsonTree.optNodeId(node));
      assertNull(jsonTree.getParentNode(node));
      assertEquals(0, jsonTree.getChildNodes(node).size());
    }
    for (String nodeId : allNodeIds) {
      assertNull(jsonTree.optTreeNodeForNodeId(nodeId));
    }
  }

  /**
   * Tests whether an all nodes deleted event gets sent whenever all children of a node get deleted.
   */
  @Test
  public void testAllNodesDeletedEvent() throws JSONException {
    List<ITreeNode> nodes = new ArrayList<>();
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    ITree tree = createTree(nodes);
    m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());

    tree.removeChildNodes(tree.getRootNode(), tree.getRootNode().getChildNodes());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonTree.EVENT_ALL_CHILD_NODES_DELETED);
    JsonEvent event = responseEvents.get(0);
    assertNull(event.getData().optJSONArray("nodeIds"));
  }

  /**
   * GetOrCreateNodeId must not create a nodeId if null is passed (may happen if someone calls getSelectedNode which may
   * return null).
   */
  @Test
  public void testGetOrCreateNodeIdWithNull() throws JSONException {
    ITree tree = createTreeWithOneNode();
    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());
    Assert.assertNull(jsonTree.getOrCreateNodeId(null));
  }

  /**
   * Expected: adding two nodes to a common parent node and actively expanding one node should result in just one event.
   */
  @Test
  public void testInsertionOrderWithExpandEvent() {
    ITree tree = createTreeWithOneNode();
    tree.getRootNode().setExpanded(true);
    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());
    tree.setTreeChanging(true);
    TreeNode child = new TreeNode();
    tree.addChildNode(tree.getRootNode(), child);
    child.setExpanded(true);
    tree.addChildNode(0, tree.getRootNode(), new TreeNode());
    tree.setTreeChanging(false);
    assertEquals(1, jsonTree.eventBuffer().consumeAndCoalesceEvents().size());
  }

  @Test
  public void testTreeExpandedRecursive() {
    // (root)
    //   +-(node)
    //   |   +-(node)
    //   |   |   +-(node)
    //   |   |   +-(node)
    //   |   +-(node)
    //   +-(node)
    //   |   +-(node)
    //   +-(node)
    List<ITreeNode> nodes = new ArrayList<>();
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    ITree tree = createTree(nodes);
    tree.addChildNode(tree.getRootNode().getChildNode(0), new TreeNode());
    tree.addChildNode(tree.getRootNode().getChildNode(0), new TreeNode());
    tree.addChildNode(tree.getRootNode().getChildNode(0).getChildNode(0), new TreeNode());
    tree.addChildNode(tree.getRootNode().getChildNode(0).getChildNode(0), new TreeNode());
    tree.addChildNode(tree.getRootNode().getChildNode(1), new TreeNode());

    IJsonAdapter<? super ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());
    m_uiSession.currentJsonResponse().addAdapter(jsonTree);
    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    System.out.println("Response #1: " + response);
    JsonTestUtility.endRequest(m_uiSession);

    // --------------------------------------

    tree.expandAll(tree.getRootNode());
    response = m_uiSession.currentJsonResponse().toJson();
    System.out.println("Response #2: " + response);

    List<JsonEvent> events = m_uiSession.currentJsonResponse().getEventList();
    assertEquals(3, events.size());
    assertEquals("nodeExpanded", events.get(0).getType());
    assertTrue(events.get(0).getData().optBoolean("expanded"));
    assertTrue(events.get(0).getData().optBoolean("recursive"));
    assertEquals("nodeExpanded", events.get(1).getType());
    assertTrue(events.get(1).getData().optBoolean("expanded"));
    assertTrue(events.get(1).getData().optBoolean("recursive"));
    assertEquals("nodeExpanded", events.get(2).getType());
    assertTrue(events.get(2).getData().optBoolean("expanded"));
    assertTrue(events.get(2).getData().optBoolean("recursive"));

    JsonTestUtility.endRequest(m_uiSession);

    // --------------------------------------

    tree.collapseAll(tree.getRootNode().getChildNode(0));
    response = m_uiSession.currentJsonResponse().toJson();
    System.out.println("Response #3: " + response);

    events = m_uiSession.currentJsonResponse().getEventList();
    assertEquals(1, events.size());
    assertEquals("nodeExpanded", events.get(0).getType());
    assertFalse(events.get(0).getData().optBoolean("expanded"));
    assertTrue(events.get(0).getData().optBoolean("recursive"));
  }

  @Test
  public void testMultipleFilterChanged() {
    // (root)
    //   +-(node)
    //   |   +-(node)
    //   |   |   +-(node)
    //   |   |   +-(node)
    //   |   +-(node)
    //   +-(node)
    //   |   +-(node)
    //   +-(node)
    List<ITreeNode> nodes = new ArrayList<>();
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    ITree tree = createTree(nodes);
    tree.addChildNode(tree.getRootNode().getChildNode(0), new TreeNode());
    tree.addChildNode(tree.getRootNode().getChildNode(0), new TreeNode());
    TreeNode node0000 = new TreeNode();
    node0000.setEnabled(false);
    tree.addChildNode(tree.getRootNode().getChildNode(0).getChildNode(0), node0000);
    tree.addChildNode(tree.getRootNode().getChildNode(0).getChildNode(0), new TreeNode());
    tree.addChildNode(tree.getRootNode().getChildNode(1), new TreeNode());

    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());
    m_uiSession.currentJsonResponse().addAdapter(jsonTree);
    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    System.out.println("Response #1: " + response);
    String node0000Id = jsonTree.getNodeId(node0000);
    JsonTestUtility.endRequest(m_uiSession);

    // --------------------------------------

    // 3 events: filterChanged, nodeChanged, filterChanged
    // filterChanged events are converted to nodesDeleted event in JsonTree.
    // Because of coalesce the nodeChanged event will be removed (it is obsolete, because nodes are deleted and re-inserted later).
    tree.addNodeFilter((node, level) -> node.isEnabled());
    node0000.getCellForUpdate().setText("Test-Text");
    tree.applyNodeFilters();

    response = m_uiSession.currentJsonResponse().toJson();
    System.out.println("Response #2: " + response);

    List<JsonEvent> events = m_uiSession.currentJsonResponse().getEventList();
    assertEquals(1, events.size());
    assertEventTypeAndNodeIds(events.get(0), "nodesDeleted", node0000Id);
  }

  /**
   * Tests that events are ignored when nodes are not yet inserted.
   */
  @Test
  public void testWrongEventOrder() throws JSONException {
    List<ITreeNode> nodes = new ArrayList<>();
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    nodes.add(new TreeNode());
    ITree tree = createTree(nodes);
    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());

    TreeNode newNode = new TreeNode();
    jsonTree.handleModelTreeEvent(new TreeEvent(tree, TreeEvent.TYPE_NODE_EXPANDED, newNode));
    jsonTree.handleModelTreeEvent(new TreeEvent(tree, TreeEvent.TYPE_NODES_INSERTED, nodes.get(0), Collections.singletonList(newNode)));

    JsonTestUtility.processBufferedEvents(m_uiSession);
    List<JsonEvent> events = m_uiSession.currentJsonResponse().getEventList();
    assertEquals(1, events.size());
    assertEquals("nodesInserted", events.get(0).getType());
  }

  protected void assertEventTypeAndNodeIds(JsonEvent event, String expectedType, String... expectedNodeIds) {
    assertEquals(expectedType, event.getType());

    JSONArray nodeIds = event.getData().getJSONArray("nodeIds");
    assertEquals(nodeIds.length(), expectedNodeIds.length);
    for (int i = 0; i < expectedNodeIds.length; i++) {
      assertEquals(nodeIds.get(i), expectedNodeIds[i]);
    }
  }

  @Test
  public void testOptTreeNodeForNodeId() {
    ITree tree = createTree(Collections.emptyList());
    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());
    assertNull(jsonTree.optTreeNodeForNodeId("foo"));
  }

  @Test(expected = UiException.class)
  public void testGetTreeNodeForNodeId() {
    ITree tree = createTree(Collections.emptyList());
    JsonTree<ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());
    jsonTree.getTreeNodeForNodeId("foo");
  }

  @Test
  public void testInsertAndDeleteInSameRequest() {
    // Note: A test for the same problem (but without a real tree) can be found here:
    // org.eclipse.scout.rt.client.ui.basic.tree.TreeEventBufferTest.testInsertAndRemoveInSameRequest()

    ITree tree = new Tree();
    tree.setRootNode(new TreeNode("Root"));

    final List<TreeEvent> treeEventCollector = new ArrayList<>();
    tree.addTreeListener(new TreeAdapter() {
      @Override
      public void treeChanged(TreeEvent e) {
        treeEventCollector.add(e);
      }
    });

    IJsonAdapter<? super ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());
    m_uiSession.currentJsonResponse().addAdapter(jsonTree);
    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    System.out.println("Response #1: " + response);
    JsonTestUtility.endRequest(m_uiSession);

    // ----------------

    // (root)
    //   +-[A]
    //      +-[B]
    ITreeNode nodeA = new TreeNode("A");
    ITreeNode nodeB = new TreeNode("B");

    // Insert A and B in one "tree changing" batch
    // -> TreeEventBuffer should remove the second event (because B is a sub-node of A)
    tree.setTreeChanging(true);
    tree.addChildNode(tree.getRootNode(), nodeA);
    tree.addChildNode(nodeA, nodeB);
    tree.setTreeChanging(false);
    assertEquals(1, treeEventCollector.size());
    treeEventCollector.clear();

    // Remove B, then A (in two separate calls)
    // -> TreeEventBuffer should remove the second event (because B is a sub-node of A), altough
    // only an insertion event for A exists (and A.getChildNodes() returns nothing)
    tree.removeAllChildNodes(nodeA);
    tree.removeAllChildNodes(tree.getRootNode());
    assertEquals(2, treeEventCollector.size());
    treeEventCollector.clear();
    assertEquals(0, nodeA.getChildNodeCount());
    assertEquals(0, tree.getRootNode().getChildNodeCount());

    // Process the buffer
    // -> TreeEventBuffer should remove all events
    JsonTestUtility.processBufferedEvents(m_uiSession);
    List<JsonEvent> events = m_uiSession.currentJsonResponse().getEventList();
    assertEquals(0, events.size());

    response = m_uiSession.currentJsonResponse().toJson();
    System.out.println("Response #2: " + response);
    JsonTestUtility.endRequest(m_uiSession);
  }

  @Test
  public void testInsertAndDeleteChildrenInSameRequest() {
    ITree tree = new Tree();
    tree.setRootNode(new TreeNode("Root"));

    IJsonAdapter<? super ITree> jsonTree = m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());
    m_uiSession.currentJsonResponse().addAdapter(jsonTree);
    JSONObject response = m_uiSession.currentJsonResponse().toJson();
    System.out.println("Response #1: " + response);
    JsonTestUtility.endRequest(m_uiSession);

    // ----------------

    // (root)
    //   +-[A]
    //      +-[B]
    //        +-[C]
    ITreeNode nodeA = new TreeNode("A");
    ITreeNode nodeB = new TreeNode("B");
    ITreeNode nodeC = new TreeNode("C");

    // Insert A, B and C in one "tree changing" batch
    tree.setTreeChanging(true);
    tree.addChildNode(tree.getRootNode(), nodeA);
    tree.addChildNode(nodeA, nodeB);
    tree.addChildNode(nodeB, nodeC);
    tree.setTreeChanging(false);

    // Remove all child nodes of B (-> C)
    tree.removeAllChildNodes(nodeB);
    assertEquals(0, nodeB.getChildNodeCount());

    // Process the buffer
    // -> TreeEventBuffer should remove delete event
    JsonTestUtility.processBufferedEvents(m_uiSession);
    List<JsonEvent> events = m_uiSession.currentJsonResponse().getEventList();
    assertEquals(1, events.size());
    assertEquals("nodesInserted", events.get(0).getType());

    response = m_uiSession.currentJsonResponse().toJson();
    System.out.println("Response #2: " + response);
    JsonTestUtility.endRequest(m_uiSession);
  }


  @Test
  public void testDeletionOfAllChildrenOfUnknownNode() {
    IOutline outline = new Outline(new ArrayList<>());

    ITreeNode parent = new TablePage(0);
    ITreeNode node1 = new TablePage(0);
    node1.setParentNodeInternal(parent);
    ITreeNode node2 = new TablePage(0);
    node2.setParentNodeInternal(parent);
    ITreeNode node3 = new TablePage(0);
    node3.setParentNodeInternal(parent);

    outline.addChildNode(outline.getRootNode(), parent);
    outline.addChildNode(parent, node1);
    outline.addChildNode(parent, node2);
    outline.addChildNode(parent, node3);

    JsonOutline<IOutline> jsonOutline = m_uiSession.createJsonAdapter(outline, null);

    List<ITreeNode> allChildren = CollectionUtility.arrayList(node1, node2, node3);

    jsonOutline.bufferModelEvent(new TreeEvent(outline, TreeEvent.TYPE_ALL_CHILD_NODES_DELETED, parent, allChildren));
    try {
      jsonOutline.processBufferedEvents();
    }
    catch (UiException e) {
      fail("Regression of ticket 210096: Tree does not contain node whose children are to be deleted.");
    }
  }

  /**
   * Test for ticket 218516. When autoCheckChildNodes is set to true the JSON layer should send the state of all child
   * nodes for a given parent node.
   */
  @Test
  public void testAllNodesUnchecked() {
    ITree tree = new Tree();
    tree.setRootNode(new TreeNode("Root"));
    tree.setAutoCheckChildNodes(true);
    tree.setCheckable(true);

    ITreeNode parent = new TreeNode("Parent");
    parent.setParentNodeInternal(tree.getRootNode());
    ITreeNode child1 = new TreeNode("Child1");
    child1.setParentNodeInternal(parent);
    ITreeNode child2 = new TreeNode("Child2");
    child2.setParentNodeInternal(parent);

    tree.addChildNode(tree.getRootNode(), parent);
    tree.addChildNode(parent, child1);
    tree.addChildNode(parent, child2);

    // set tree state before we start with the test case
    m_uiSession.createJsonAdapter(tree, new JsonAdapterMock());
    tree.setNodeChecked(parent, true);
    assertTrue(child1.isChecked());
    assertTrue(child2.isChecked());
    JsonTestUtility.endRequest(m_uiSession);

    tree.setNodeChecked(parent, false); // <-- test case

    JsonTestUtility.processBufferedEvents(m_uiSession);
    List<JsonEvent> events = m_uiSession.currentJsonResponse().getEventList();
    assertEquals(1, events.size());
    JsonEvent event = events.get(0);
    assertEquals("nodesChecked", event.getType());
    JSONArray nodes = event.getData().getJSONArray("nodes");
    assertEquals(3, nodes.length());
    for (int i = 0; i < nodes.length(); i++) {
      JSONObject jsonNode = (JSONObject) nodes.get(i);
      assertFalse(jsonNode.getBoolean("checked"));
    }
  }

  public static JsonEvent createJsonSelectedEvent(String nodeId) throws JSONException {
    String desktopId = "x"; // never used
    JSONObject data = new JSONObject();
    JSONArray nodeIds = new JSONArray();
    if (nodeId != null) {
      nodeIds.put(nodeId);
    }
    data.put(JsonTree.PROP_NODE_IDS, nodeIds);
    return new JsonEvent(desktopId, JsonTree.EVENT_NODES_SELECTED, data);
  }

  public static JsonEvent createJsonExpansionEvent(String nodeId, boolean expanded) throws JSONException {
    JSONObject data = new JSONObject();
    data.put(JsonTree.PROP_NODE_ID, nodeId);
    data.put("expanded", expanded);
    data.put("expandedLazy", false);
    return new JsonEvent("", JsonTree.EVENT_NODE_EXPANDED, data);
  }

  protected TreeWithOneNode createTreeWithOneNode() {
    TreeWithOneNode tree = new TreeWithOneNode();
    tree.init();
    return tree;
  }

  protected Tree createTree(List<ITreeNode> nodes) {
    Tree tree = new Tree(nodes);
    tree.init();
    return tree;
  }

  public static String getOrCreateNodeId(JsonTree tree, ITreeNode node) {
    return tree.getOrCreateNodeId(node);
  }

  public static String getNodeId(JsonTree tree, ITreeNode node) {
    return tree.getNodeId(node);
  }

  public static String optNodeId(JsonTree tree, ITreeNode node) {
    return tree.optNodeId(node);
  }

  public static ITreeNode getNode(JsonTree tree, String nodeId) {
    return tree.optTreeNodeForNodeId(nodeId);
  }

  public static List<ITreeNode> getAllTreeNodes(final ITree tree) {
    final List<ITreeNode> nodes = new LinkedList<>();
    IDepthFirstTreeVisitor<ITreeNode> v = new DepthFirstTreeVisitor<>() {
      @Override
      public TreeVisitResult preVisit(ITreeNode node, int level, int index) {
        if (!tree.isRootNodeVisible() && tree.getRootNode() == node) {
          return TreeVisitResult.CONTINUE;
        }
        nodes.add(node);
        return TreeVisitResult.CONTINUE;
      }
    };
    tree.visitTree(v);
    return nodes;
  }
}
