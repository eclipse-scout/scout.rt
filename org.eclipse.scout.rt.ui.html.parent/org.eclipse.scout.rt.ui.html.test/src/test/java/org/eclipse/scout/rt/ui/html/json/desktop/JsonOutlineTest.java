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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.NodePage;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.NodePageWithForm;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.Outline;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.OutlineWithOneNode;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.TablePage;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTreeTest;
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
public class JsonOutlineTest {
  private JsonSessionMock m_jsonSession;

  @Before
  public void setUp() {
    m_jsonSession = new JsonSessionMock();
  }

  /**
   * Tests whether the adapters for the detail forms get created
   */
  @Test
  public void testChildAdaptersCreated() throws ProcessingException {
    TablePage tablePage = new TablePage(1, new TablePage.NodePageWithFormFactory());
    NodePageWithForm nodePage = new NodePageWithForm();

    List<IPage<?>> pages = new ArrayList<IPage<?>>();
    pages.add(nodePage);
    pages.add(tablePage);
    IOutline outline = new Outline(pages);

    // Activate nodes (forms get created lazily on activation)
    outline.selectNode(nodePage);
    outline.selectNode(tablePage);

    IPage rowPage = (IPage) tablePage.getTreeNodeFor(tablePage.getTable().getRow(0));
    rowPage = (IPage) tablePage.resolveVirtualChildNode((rowPage));
    outline.selectNode(rowPage);

    JsonOutline<IOutline> jsonOutline = m_jsonSession.newJsonAdapter(outline, null, null);

    Assert.assertNotNull(jsonOutline.getAdapter(nodePage.getDetailForm()));
    Assert.assertNotNull(jsonOutline.getAdapter(rowPage.getDetailForm()));
  }

  @Test
  public void testPageDisposal() throws ProcessingException {
    TablePage tablePage = new TablePage(1, new TablePage.NodePageWithFormFactory());
    NodePage nodePage = new NodePage();

    List<IPage<?>> pages = new ArrayList<IPage<?>>();
    pages.add(nodePage);
    IOutline outline = new Outline(pages);

    outline.addChildNode(nodePage, tablePage);
    outline.selectNode(tablePage);

    JsonOutline<IOutline> jsonOutline = m_jsonSession.newJsonAdapter(outline, null, null);

    List<ITreeNode> allNodes = JsonTreeTest.getAllTreeNodes(outline);
    List<String> allNodeIds = new LinkedList<String>();

    for (ITreeNode node : allNodes) {
      String nodeId = JsonTreeTest.getOrCreateNodeId(jsonOutline, node);
      allNodeIds.add(nodeId);

      assertNotNull(nodeId);
      assertNotNull(JsonTreeTest.getNode(jsonOutline, nodeId));
    }

    outline.removeNode(nodePage);

    // Verify nodes get unregistered
    for (ITreeNode node : allNodes) {
      assertNull(JsonTreeTest.getNodeId(jsonOutline, node));
    }
    for (String nodeId : allNodeIds) {
      assertNull(JsonTreeTest.getNode(jsonOutline, nodeId));
    }

    // Verify table adapter gets unregistered
    m_jsonSession.flush();
    assertNull(m_jsonSession.getJsonAdapter(tablePage.getTable(), m_jsonSession.getRootJsonAdapter()));
  }

  /**
   * Node.detailTable must not be sent if node.tableVisible is set to false to reduce response size.
   */
  @Test
  public void testTableNotSentIfInvisible() throws ProcessingException, JSONException {
    NodePageWithForm nodePage = new NodePageWithForm();
    nodePage.setTableVisible(false);

    List<IPage<?>> pages = new ArrayList<IPage<?>>();
    pages.add(nodePage);
    IOutline outline = new Outline(pages);
    JsonOutline<IOutline> jsonOutline = m_jsonSession.newJsonAdapter(outline, null, null);

    JSONObject jsonNode = jsonOutline.toJson().getJSONArray("nodes").getJSONObject(0);
    Assert.assertNull(jsonNode.opt(IOutline.PROP_DETAIL_TABLE));

    nodePage.setTableVisible(true);
    jsonNode = jsonOutline.toJson().getJSONArray("nodes").getJSONObject(0);
    Assert.assertNotNull(jsonNode.opt(IOutline.PROP_DETAIL_TABLE));
  }

  @Test
  public void testDispose() {
    ITree tree = new OutlineWithOneNode();
    JsonTree<ITree> object = m_jsonSession.newJsonAdapter(tree, null, null);
    WeakReference<JsonTree> ref = new WeakReference<JsonTree>(object);

    object.dispose();
    m_jsonSession.flush();
    m_jsonSession = null;
    object = null;
    JsonTestUtility.assertGC(ref);
  }

  /**
   * Tests that no events are fired during page initialization
   */
  @Test
  public void testNoEventsFiredOnChildPageCreation() throws ProcessingException, JSONException {
    final Holder<Integer> initPageCounter = new Holder<>(Integer.class);
    initPageCounter.setValue(0);

    // Build an outline with two nodes, where the second node has a child node
    IPage page1 = new AbstractPageWithNodes() {
    };
    IPage page2 = new AbstractPageWithNodes() {
      @Override
      protected void execCreateChildPages(List<IPage<?>> pageList) throws ProcessingException {
        pageList.add(new AbstractPageWithNodes() {
          @Override
          protected void execInitPage() throws ProcessingException {
            // Change some properties (this would normally fire events, but we are inside execInitPage())
            setLeaf(!isLeaf());
            getCellForUpdate().setText("Test");
            initPageCounter.setValue(initPageCounter.getValue() + 1);
          }
        });
        super.execCreateChildPages(pageList);
      }
    };
    List<IPage<?>> pages = new ArrayList<IPage<?>>();
    pages.add(page2);
    IOutline outline = new Outline(pages);
    outline.selectNode(page1);

    // Outline to JsonOutline
    JsonOutline<IOutline> jsonOutline = m_jsonSession.newJsonAdapter(outline, null, null);
    jsonOutline.toJson(); // simulate "send to client"

    Assert.assertEquals(0, initPageCounter.getValue().intValue());

    // Simulate "select page2" event
    JsonEvent event = createNodeSelectionEvent(jsonOutline, page2);
    jsonOutline.handleUiEvent(event, new JsonResponse());

    assertEquals(1, initPageCounter.getValue().intValue());

    // Get all events for the outline (ignore table events)
    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(m_jsonSession.currentJsonResponse(), null, jsonOutline.getId());
    // Check that we got only one event (node insertion)
    assertEquals(1, responseEvents.size());
    assertEquals(JsonTree.EVENT_NODES_INSERTED, responseEvents.get(0).getType());
  }

  protected JsonEvent createNodeSelectionEvent(JsonOutline<IOutline> jsonOutline, IPage page) throws JSONException {
    JSONObject eventData = new JSONObject();
    JSONArray nodeIds = new JSONArray();
    nodeIds.put(JsonTreeTest.getNodeId(jsonOutline, page));
    eventData.put("nodeIds", nodeIds);
    JsonEvent event = new JsonEvent(jsonOutline.getId(), JsonTree.EVENT_NODES_SELECTED, eventData);
    return event;
  }
}
