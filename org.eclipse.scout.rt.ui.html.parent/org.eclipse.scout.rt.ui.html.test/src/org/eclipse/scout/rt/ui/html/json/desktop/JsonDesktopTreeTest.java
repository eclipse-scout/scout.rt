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
package org.eclipse.scout.rt.ui.html.json.desktop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.NodePage;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.Outline;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.OutlineWithOneNode;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class JsonDesktopTreeTest {

  /**
   * Tests whether the model node gets correctly selected
   */
  @Test
  public void testSelectionEvent() throws ProcessingException, JSONException {
    IOutline outline = new OutlineWithOneNode();
    ITreeNode node = outline.getRootNode().getChildNode(0);
    assertFalse(node.isSelectedNode());

    JsonDesktopTree jsonDesktopTree = createJsonDesktopTreeWithMocks(outline);

    JsonEvent event = createJsonSelectedEvent(jsonDesktopTree.getOrCreatedNodeId(node));
    jsonDesktopTree.handleUiEvent(event, new JsonResponse());

    assertTrue(node.isSelectedNode());
  }

  /**
   * Response must not contain the selection event if the selection was triggered by the request
   */
  @Test
  public void testIgnorableSelectionEvent() throws ProcessingException, JSONException {
    IOutline outline = new OutlineWithOneNode();
    ITreeNode node = outline.getRootNode().getChildNode(0);

    JsonDesktopTree jsonDesktopTree = createJsonDesktopTreeWithMocks(outline);

    JsonEvent event = createJsonSelectedEvent(jsonDesktopTree.getOrCreatedNodeId(node));
    jsonDesktopTree.handleUiEvent(event, new JsonResponse());

    List<JSONObject> responseEvents = JsonTestUtility.extractEventsFromResponse(jsonDesktopTree.getJsonSession().currentJsonResponse(), JsonDesktopTree.EVENT_NODES_SELECTED);
    assertTrue(responseEvents.size() == 0);
  }

  /**
   * If the selection event triggers the selection of another node, the selection event must not be ignored.
   */
  @Test
  public void testIgnorableSelectionEvent2() throws ProcessingException, JSONException {
    List<IPage> pages = new LinkedList<>();
    final NodePage firstNode = new NodePage();
    NodePage secondNode = new NodePage() {
      @Override
      protected void execPageActivated() throws ProcessingException {
        getOutline().selectNode(firstNode);
      }
    };

    pages.add(firstNode);
    pages.add(secondNode);
    IOutline outline = new Outline(pages);

    JsonDesktopTree jsonDesktopTree = createJsonDesktopTreeWithMocks(outline);
    JsonEvent event = createJsonSelectedEvent(jsonDesktopTree.getOrCreatedNodeId(secondNode));

    assertFalse(firstNode.isSelectedNode());
    assertFalse(secondNode.isSelectedNode());

    jsonDesktopTree.handleUiEvent(event, new JsonResponse());

    assertTrue(firstNode.isSelectedNode());
    assertFalse(secondNode.isSelectedNode());

    List<JSONObject> responseEvents = JsonTestUtility.extractEventsFromResponse(jsonDesktopTree.getJsonSession().currentJsonResponse(), JsonDesktopTree.EVENT_NODES_SELECTED);
    assertTrue(responseEvents.size() == 1);

    List<ITreeNode> treeNodes = jsonDesktopTree.extractTreeNodes(responseEvents.get(0));
    assertEquals(firstNode, treeNodes.get(0));
  }

  @Test
  public void testDispose() {
    IOutline outline = new OutlineWithOneNode();
    JsonDesktopTree object = createJsonDesktopTreeWithMocks(outline);
    WeakReference<JsonDesktopTree> ref = new WeakReference<JsonDesktopTree>(object);

    object.dispose();
    object = null;
    JsonTestUtility.assertGC(ref);
  }

  /**
   * Tests whether a deletion event with correct node id gets sent whenever a node gets deleted.
   */
  @Test
  public void testNodesDeletedEvent() throws ProcessingException, JSONException {
    List<IPage> pages = new ArrayList<IPage>();
    pages.add(new NodePage());
    pages.add(new NodePage());
    pages.add(new NodePage());
    IOutline outline = new Outline(pages);
    JsonDesktopTree jsonDesktopTree = createJsonDesktopTreeWithMocks(outline);
    IJsonSession session = jsonDesktopTree.getJsonSession();

    String node1Id = jsonDesktopTree.getOrCreatedNodeId(pages.get(1));
    outline.removeNode(pages.get(1));

    List<JSONObject> responseEvents = JsonTestUtility.extractEventsFromResponse(session.currentJsonResponse(), JsonDesktopTree.EVENT_NODES_DELETED);
    assertTrue(responseEvents.size() == 1);

    JSONObject event = responseEvents.get(0);
    JSONArray nodeIds = event.getJSONArray("nodeIds");

    assertTrue(nodeIds.length() == 1);
    assertTrue(nodeIds.get(0).equals(node1Id));
  }

  /**
   * Tests whether an all nodes deleted event gets sent whenever all children of a node get deleted.
   */
  @Test
  public void testAllNodesDeletedEvent() throws ProcessingException, JSONException {
    List<IPage> pages = new ArrayList<IPage>();
    pages.add(new NodePage());
    pages.add(new NodePage());
    pages.add(new NodePage());
    IOutline outline = new Outline(pages);
    JsonDesktopTree jsonDesktopTree = createJsonDesktopTreeWithMocks(outline);
    IJsonSession session = jsonDesktopTree.getJsonSession();

    outline.removeChildNodes(outline.getRootNode(), outline.getRootNode().getChildNodes());

    List<JSONObject> responseEvents = JsonTestUtility.extractEventsFromResponse(session.currentJsonResponse(), JsonDesktopTree.EVENT_ALL_NODES_DELETED);
    JSONObject event = responseEvents.get(0);
    assertNull(event.optJSONArray("nodeIds"));
  }

  public static JsonDesktopTree createJsonDesktopTreeWithMocks(IOutline outline) {
    JsonSessionMock jsonSession = new JsonSessionMock();
    JsonDesktopTree jsonDesktopTree = new JsonDesktopTree(outline, jsonSession, jsonSession.createUniqueIdFor(null));
    jsonDesktopTree.attach();

    // init treeNode map
    jsonDesktopTree.toJson();
    return jsonDesktopTree;
  }

  public static JsonEvent createJsonSelectedEvent(String nodeId) throws JSONException {
    JsonEvent event = JsonTestUtility.createJsonEvent(JsonDesktopTree.EVENT_NODES_SELECTED);
    JSONArray nodeIds = new JSONArray();
    nodeIds.put(nodeId);
    event.getJsonObject().put(JsonDesktopTree.PROP_NODE_IDS, nodeIds);
    return event;
  }
}
