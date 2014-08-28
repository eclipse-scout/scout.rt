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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.OutlineWithOneNode;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.rt.ui.html.json.tree.fixtures.Tree;
import org.eclipse.scout.rt.ui.html.json.tree.fixtures.TreeNode;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class JsonTreeTest {

  /**
   * Tests whether the model node gets correctly selected
   */
  @Test
  public void testSelectionEvent() throws ProcessingException, JSONException {
    ITree tree = new OutlineWithOneNode();
    ITreeNode node = tree.getRootNode().getChildNode(0);
    assertFalse(node.isSelectedNode());

    JsonTree jsonTree = createJsonTreeWithMocks(tree);

    JsonEvent event = createJsonSelectedEvent(jsonTree.getOrCreateNodeId(node));
    jsonTree.handleUiEvent(event, new JsonResponse());

    assertTrue(node.isSelectedNode());
  }

  /**
   * Response must not contain the selection event if the selection was triggered by the request
   */
  @Test
  public void testIgnorableSelectionEvent() throws ProcessingException, JSONException {
    ITree tree = new OutlineWithOneNode();
    ITreeNode node = tree.getRootNode().getChildNode(0);

    JsonTree jsonTree = createJsonTreeWithMocks(tree);

    JsonEvent event = createJsonSelectedEvent(jsonTree.getOrCreateNodeId(node));
    jsonTree.handleUiEvent(event, new JsonResponse());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        jsonTree.getJsonSession().currentJsonResponse(), JsonTree.EVENT_NODES_SELECTED);
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
//    ITree tree = new Tree(nodes) {
//
//      @Override
//      protected void execNodesSelected(TreeEvent e) throws ProcessingException {
//        if (e.getNode().equals(secondNode)) {
//          selectNode(firstNode);
//        }
//      }
//    };
//
//    JsonTree<ITree> jsonTree = createJsonTreeWithMocks(tree);
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
//        jsonTree.getJsonSession().currentJsonResponse(), JsonTree.EVENT_NODES_SELECTED);
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
    ITree tree = new OutlineWithOneNode();
    ITreeNode node = tree.getRootNode().getChildNode(0);

    JsonTree jsonTree = createJsonTreeWithMocks(tree);

    //Check expanded = true
    JsonEvent event = createJsonExpansionEvent(jsonTree.getOrCreateNodeId(node), true);
    jsonTree.handleUiEvent(event, new JsonResponse());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        jsonTree.getJsonSession().currentJsonResponse(), JsonTree.EVENT_NODE_EXPANDED);
    assertTrue(responseEvents.size() == 0);

    //Check expanded = false
    event = createJsonExpansionEvent(jsonTree.getOrCreateNodeId(node), false);
    jsonTree.handleUiEvent(event, new JsonResponse());

    responseEvents = JsonTestUtility.extractEventsFromResponse(
        jsonTree.getJsonSession().currentJsonResponse(), JsonTree.EVENT_NODE_EXPANDED);
    assertTrue(responseEvents.size() == 0);
  }

  @Test
  public void testDispose() {
    ITree tree = new OutlineWithOneNode();
    JsonTree object = createJsonTreeWithMocks(tree);
    WeakReference<JsonTree> ref = new WeakReference<JsonTree>(object);

    object.dispose();
    object.getJsonSession().flush();
    object = null;
    JsonTestUtility.assertGC(ref);
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
    ITree tree = new Tree(nodes);
    JsonTree jsonTree = createJsonTreeWithMocks(tree);
    IJsonSession session = jsonTree.getJsonSession();

    String node1Id = jsonTree.getOrCreateNodeId(nodes.get(1));
    tree.removeNode(nodes.get(1));

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(session.currentJsonResponse(), JsonTree.EVENT_NODES_DELETED);
    assertTrue(responseEvents.size() == 1);

    JsonEvent event = responseEvents.get(0);
    JSONArray nodeIds = event.getData().getJSONArray("nodeIds");

    assertTrue(nodeIds.length() == 1);
    assertTrue(nodeIds.get(0).equals(node1Id));
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
    ITree tree = new Tree(nodes);
    JsonTree jsonTree = createJsonTreeWithMocks(tree);
    IJsonSession session = jsonTree.getJsonSession();

    tree.removeChildNodes(tree.getRootNode(), tree.getRootNode().getChildNodes());

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        session.currentJsonResponse(), JsonTree.EVENT_ALL_NODES_DELETED);
    JsonEvent event = responseEvents.get(0);
    assertNull(event.getData().optJSONArray("nodeIds"));
  }

  /**
   * GetOrCreateNodeId must not create a nodeId if null is passed (may happen if someone calls getSelectedNode which may
   * return null).
   */
  @Test
  public void testGetOrCreateNodeIdWithNull() throws ProcessingException, JSONException {
    ITree tree = new OutlineWithOneNode();
    JsonTree jsonTree = createJsonTreeWithMocks(tree);
    Assert.assertNull(jsonTree.getOrCreateNodeId(null));
  }

  public static JsonTree<ITree> createJsonTreeWithMocks(ITree tree) {
    JsonSessionMock jsonSession = new JsonSessionMock();
    JsonTree<ITree> jsonTree = new JsonTree<ITree>(tree, jsonSession, jsonSession.createUniqueIdFor(null));
    jsonTree.attach();
    return jsonTree;
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
}
