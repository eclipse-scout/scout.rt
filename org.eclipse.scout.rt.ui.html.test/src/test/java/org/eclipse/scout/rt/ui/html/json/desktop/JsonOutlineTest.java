/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import static org.junit.Assert.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.NodePage;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.NodePageWithForm;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.Outline;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.OutlineWithOneNode;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.TablePage;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.menu.JsonMenu;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTreeTest;
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
  private UiSessionMock m_uiSession;

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
  }

  /**
   * Tests whether the adapters for the detail forms get created
   */
  @Test
  public void testChildAdaptersCreated() {
    TablePage tablePage = new TablePage(1, new TablePage.NodePageWithFormFactory());
    NodePageWithForm nodePage = new NodePageWithForm();

    List<IPage<?>> pages = new ArrayList<>();
    pages.add(nodePage);
    pages.add(tablePage);
    IOutline outline = new Outline(pages);

    // Activate nodes (forms get created lazily on activation)
    outline.selectNode(nodePage);
    outline.selectNode(tablePage);

    IPage rowPage = (IPage) tablePage.getTreeNodeFor(tablePage.getTable().getRow(0));
    outline.selectNode(rowPage);

    JsonOutline<IOutline> jsonOutline = UiSessionTestUtility.newJsonAdapter(m_uiSession, outline);

    Assert.assertNotNull(jsonOutline.getAdapter(nodePage.getDetailForm()));
    Assert.assertNotNull(jsonOutline.getAdapter(rowPage.getDetailForm()));
  }

  @Test
  public void testPageDisposal() {
    TablePage tablePage = new TablePage(1, new TablePage.NodePageWithFormFactory());
    NodePage nodePage = new NodePage();

    List<IPage<?>> pages = new ArrayList<>();
    pages.add(nodePage);
    IOutline outline = new Outline(pages);

    outline.addChildNode(nodePage, tablePage);
    outline.selectNode(tablePage);

    JsonOutline<IOutline> jsonOutline = UiSessionTestUtility.newJsonAdapter(m_uiSession, outline);

    List<ITreeNode> allNodes = JsonTreeTest.getAllTreeNodes(outline);
    List<String> allNodeIds = new LinkedList<>();

    for (ITreeNode node : allNodes) {
      String nodeId = JsonTreeTest.getOrCreateNodeId(jsonOutline, node);
      allNodeIds.add(nodeId);

      assertNotNull(nodeId);
      assertNotNull(JsonTreeTest.getNode(jsonOutline, nodeId));
    }

    outline.removeNode(nodePage);
    JsonTestUtility.processBufferedEvents(m_uiSession);

    // Verify nodes get unregistered
    for (ITreeNode node : allNodes) {
      assertNull(JsonTreeTest.optNodeId(jsonOutline, node));
    }
    for (String nodeId : allNodeIds) {
      assertNull(JsonTreeTest.getNode(jsonOutline, nodeId));
    }

    // Verify table adapter gets unregistered
    assertNull(m_uiSession.getJsonAdapter(tablePage.getTable(), m_uiSession.getRootJsonAdapter()));
  }

  @Test
  public void testPageDisposalOnRemoveAll() throws JSONException {
    NodePage nodePage = new NodePage();
    nodePage.getTable(true); // trigger table creation

    NodePage childPage = new NodePage();
    childPage.getTable(true); // trigger table creation

    List<IPage<?>> pages = new ArrayList<>();
    pages.add(nodePage);
    IOutline outline = new Outline(pages);
    outline.addChildNode(nodePage, childPage);
    JsonOutline<IOutline> jsonOutline = UiSessionTestUtility.newJsonAdapter(m_uiSession, outline);

    // Assert that pages have an id
    String nodeId = JsonTreeTest.getOrCreateNodeId(jsonOutline, nodePage);
    assertNotNull(nodeId);
    assertNotNull(JsonTreeTest.getNode(jsonOutline, nodeId));
    String childId = JsonTreeTest.getOrCreateNodeId(jsonOutline, childPage);
    assertNotNull(childId);
    assertNotNull(JsonTreeTest.getNode(jsonOutline, childId));

    // Assert that adapters for table have been created
    assertNotNull(m_uiSession.getJsonAdapter(nodePage.getTable(), m_uiSession.getRootJsonAdapter()));
    assertNotNull(m_uiSession.getJsonAdapter(childPage.getTable(), m_uiSession.getRootJsonAdapter()));

    // Remove all child nodes of the root page
    outline.removeAllChildNodes(outline.getRootNode());

    JsonTestUtility.processBufferedEvents(m_uiSession);

    // Assert that pages have been disposed
    assertNull(JsonTreeTest.getNode(jsonOutline, nodeId));
    assertNull(JsonTreeTest.getNode(jsonOutline, childId));

    // Assert that tables have been disposed
    assertNull(m_uiSession.getJsonAdapter(nodePage.getTable(), m_uiSession.getRootJsonAdapter()));
    assertNull(m_uiSession.getJsonAdapter(childPage.getTable(), m_uiSession.getRootJsonAdapter()));
  }

  @Test
  public void testPageDisposalOnRemoveAllNested() throws JSONException {
    NodePage nodePage = new NodePage();
    nodePage.getCellForUpdate().setText("node");
    nodePage.getTable(true); // trigger table creation

    NodePage childPage = new NodePage();
    childPage.getCellForUpdate().setText("child");
    childPage.getTable(true); // trigger table creation

    List<IPage<?>> pages = new ArrayList<>();
    pages.add(nodePage);
    IOutline outline = new Outline(pages);
    outline.addChildNode(nodePage, childPage);
    JsonOutline<IOutline> jsonOutline = UiSessionTestUtility.newJsonAdapter(m_uiSession, outline);

    // Assert that pages have an id
    String nodeId = JsonTreeTest.getOrCreateNodeId(jsonOutline, nodePage);
    assertNotNull(nodeId);
    assertNotNull(JsonTreeTest.getNode(jsonOutline, nodeId));
    String childId = JsonTreeTest.getOrCreateNodeId(jsonOutline, childPage);
    assertNotNull(childId);
    assertNotNull(JsonTreeTest.getNode(jsonOutline, childId));

    // Assert that adapters for table have been created
    assertNotNull(m_uiSession.getJsonAdapter(nodePage.getTable(), m_uiSession.getRootJsonAdapter()));
    assertNotNull(m_uiSession.getJsonAdapter(childPage.getTable(), m_uiSession.getRootJsonAdapter()));

    outline.setTreeChanging(true);
    // First remove all child nodes of the node page
    outline.removeAllChildNodes(nodePage);
    // Then remove all child nodes of the root page
    outline.removeAllChildNodes(outline.getRootNode());
    // Finally fire the events -> event buffer will remove the first event, but JsonOutline must dispose the children of nodePage anyway. That is why JsonTree keeps track of the child/parent link by itself.
    outline.setTreeChanging(false);

    JsonTestUtility.processBufferedEvents(m_uiSession);

    // Assert that pages have been disposed
    assertNull(JsonTreeTest.getNode(jsonOutline, nodeId));
    assertNull(JsonTreeTest.getNode(jsonOutline, childId));

    // Assert that tables have been disposed
    assertNull(m_uiSession.getJsonAdapter(nodePage.getTable(), m_uiSession.getRootJsonAdapter()));
    assertNull(m_uiSession.getJsonAdapter(childPage.getTable(), m_uiSession.getRootJsonAdapter()));
  }

  /**
   * Node.detailTable must not be sent if node.tableVisible is set to false to reduce response size.
   */
  @Test
  public void testTableNotSentIfInvisible() throws JSONException {
    NodePageWithForm nodePage = new NodePageWithForm();
    nodePage.getTable(true); // trigger table creation
    nodePage.setTableVisible(false);

    List<IPage<?>> pages = new ArrayList<>();
    pages.add(nodePage);
    IOutline outline = new Outline(pages);
    JsonOutline<IOutline> jsonOutline = UiSessionTestUtility.newJsonAdapter(m_uiSession, outline);

    JSONObject jsonNode = jsonOutline.toJson().getJSONArray("nodes").getJSONObject(0);
    Assert.assertNull(jsonNode.opt(IOutline.PROP_DETAIL_TABLE));

    nodePage.setTableVisible(true);
    JsonTestUtility.processBufferedEvents(m_uiSession);
    jsonNode = jsonOutline.toJson().getJSONArray("nodes").getJSONObject(0);
    Assert.assertNotNull(jsonNode.opt(IOutline.PROP_DETAIL_TABLE));
  }

  @Test
  public void testDispose() {
    ITree tree = new OutlineWithOneNode();
    JsonTree<ITree> object = UiSessionTestUtility.newJsonAdapter(m_uiSession, tree);
    WeakReference<JsonTree> ref = new WeakReference<>(object);

    object.dispose();
    m_uiSession = null;
    object = null;
    TestingUtility.assertGC(ref);
  }

  /**
   * Tests that no events are fired during page initialization
   */
  @Test
  public void testNoEventsFiredOnChildPageCreation() throws JSONException {
    final Holder<Integer> initPageCounter = new Holder<>(Integer.class);
    initPageCounter.setValue(0);

    // Build an outline with two nodes, where the second node has a child node
    IPage page1 = new AbstractPageWithNodes() {
    };
    IPage page2 = new AbstractPageWithNodes() {
      @Override
      protected void execCreateChildPages(List<IPage<?>> pageList) {
        pageList.add(new AbstractPageWithNodes() {
          @Override
          protected void execInitPage() {
            // Change some properties (this would normally fire events, but we are inside execInitPage())
            setLeaf(!isLeaf());
            getCellForUpdate().setText("Test");
            initPageCounter.setValue(initPageCounter.getValue() + 1);
          }
        });
        super.execCreateChildPages(pageList);
      }
    };
    List<IPage<?>> pages = new ArrayList<>();
    pages.add(page2);
    IOutline outline = new Outline(pages);
    outline.selectNode(page1);

    // Outline to JsonOutline
    JsonOutline<IOutline> jsonOutline = UiSessionTestUtility.newJsonAdapter(m_uiSession, outline, m_uiSession.getRootJsonAdapter());
    jsonOutline.toJson(); // simulate "send to client"

    Assert.assertEquals(0, initPageCounter.getValue().intValue());

    // Simulate "select page2" event
    JsonEvent event = createNodeSelectionEvent(jsonOutline, page2);
    jsonOutline.handleUiEvent(event);

    assertEquals(1, initPageCounter.getValue().intValue());

    // Get all events for the outline (ignore table events)
    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(m_uiSession.currentJsonResponse(), null, jsonOutline.getId());
    // Check that we got only two events: page-changed (because the table was created), node insertion
    assertEquals(2, responseEvents.size());
    assertEquals("pageChanged", responseEvents.get(0).getType());
    assertEquals(JsonTree.EVENT_NODES_INSERTED, responseEvents.get(1).getType());
  }

  /**
   * Tests whether only header menus are sent.
   * <p>
   * Other menus are never displayed, no need to send them.
   */
  @Test
  public void testDontSendNonDisplayableMenus() {
    List<IPage<?>> pages = new ArrayList<>();
    IOutline outline = new Outline(pages);
    IMenu headerMenu = new HeaderMenu();
    headerMenu.init();
    IMenu nonHeaderMenu = new NonHeaderMenu();
    nonHeaderMenu.init();
    outline.getContextMenu().addChildAction(headerMenu);
    outline.getContextMenu().addChildAction(nonHeaderMenu);
    JsonOutline<IOutline> jsonOutline = UiSessionTestUtility.newJsonAdapter(m_uiSession, outline);

    // ----------

    JsonMenu<IMenu> jsonHeaderMenu = jsonOutline.getAdapter(headerMenu);
    JsonMenu<IMenu> jsonNonHeaderMenu = jsonOutline.getAdapter(nonHeaderMenu);

    // Adapter for NonHeaderMenu must not exist
    assertNull(jsonNonHeaderMenu);

    // Json response must not contain NonHeaderMenu
    JSONObject json = jsonOutline.toJson();
    JSONArray jsonMenus = json.getJSONArray("menus");
    assertEquals(1, jsonMenus.length());
    assertEquals(jsonHeaderMenu.getId(), jsonMenus.get(0));
  }

  protected JsonEvent createNodeSelectionEvent(JsonOutline<IOutline> jsonOutline, IPage page) throws JSONException {
    JSONObject eventData = new JSONObject();
    JSONArray nodeIds = new JSONArray();
    nodeIds.put(JsonTreeTest.getNodeId(jsonOutline, page));
    eventData.put("nodeIds", nodeIds);
    return new JsonEvent(jsonOutline.getId(), JsonTree.EVENT_NODES_SELECTED, eventData);
  }

  @ClassId("c2e40347-ac02-4576-a9ea-889d7f6dda2f")
  private static class HeaderMenu extends AbstractMenu {

    @Override
    protected Set<? extends IMenuType> getConfiguredMenuTypes() {
      return CollectionUtility.<IMenuType> hashSet(
          TreeMenuType.Header);
    }
  }

  @ClassId("afea6ef3-592f-4023-9968-09d5c175161c")
  private class NonHeaderMenu extends AbstractMenu {
    @Override
    protected Set<? extends IMenuType> getConfiguredMenuTypes() {
      return CollectionUtility.<IMenuType> hashSet(
          TreeMenuType.SingleSelection);
    }
  }
}
