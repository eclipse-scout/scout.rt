/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop.outline.pages.js;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.eclipse.scout.rt.api.data.page.IPageParamDo;
import org.eclipse.scout.rt.api.data.page.IdPageParamDo;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridEvent;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridManager;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.js.AbstractJsPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.js.IJsPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.js.LoadChildPagesHybridActionDo;
import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.IIdSignatureDataObjectMapper;
import org.eclipse.scout.rt.dataobject.fixture.FixtureIntegerId;
import org.eclipse.scout.rt.dataobject.id.IdCodec;
import org.eclipse.scout.rt.dataobject.id.IdCodec.IdCodecFlag;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSession;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.desktop.JsPageChildPageToJsonContributor;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonOutline;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.Outline;
import org.eclipse.scout.rt.ui.html.json.desktop.hybrid.JsonHybridManager;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonLoadChildPagesHybridActionTest {

  private HybridManager m_oldHybridManager;

  private UiSessionMock m_uiSession;
  private HybridManager m_hybridManager;
  private JsonHybridManager<HybridManager> m_jsonHybridManager;
  private JsonOutline<IOutline> m_jsonOutline;
  private IOutline m_outline;

  private static List<IBean<?>> s_beans;

  @BeforeClass
  public static void beforeClass() {
    s_beans = BeanTestingHelper.get().registerBeans(new BeanMetaData(P_IdCodec.class).withReplace(true));
  }

  @AfterClass
  public static void afterClass() {
    BeanTestingHelper.get().unregisterBeans(s_beans);
    s_beans = null;
  }

  @Before
  public void setUp() {
    m_oldHybridManager = HybridManager.get();
    IDesktop.CURRENT.get().removeAddOn(m_oldHybridManager);

    m_uiSession = new UiSessionMock();

    m_hybridManager = BEANS.get(HybridManager.class);
    IDesktop.CURRENT.get().addAddOn(getHybridManager());
    m_jsonHybridManager = UiSessionTestUtility.newJsonAdapter(getUiSession(), getHybridManager());

    m_outline = new Outline(List.of());
    m_jsonOutline = UiSessionTestUtility.newJsonAdapter(getUiSession(), getOutline());
  }

  @After
  public void tearDown() {
    UiSessionTestUtility.getJsonAdapterRegistry(getUiSession()).disposeAdapters();

    IDesktop.CURRENT.get().removeAddOn(getHybridManager());
    IDesktop.CURRENT.get().addAddOn(m_oldHybridManager);
  }

  private UiSession getUiSession() {
    return m_uiSession;
  }

  private HybridManager getHybridManager() {
    return m_hybridManager;
  }

  private JsonHybridManager<HybridManager> getJsonHybridManager() {
    return m_jsonHybridManager;
  }

  private IOutline getOutline() {
    return m_outline;
  }

  private JsonOutline<IOutline> getJsonOutline() {
    return m_jsonOutline;
  }

  private String createId() {
    return UUID.randomUUID().toString();
  }

  private String callLoadChildPages(String pageId, boolean replace, FixtureIntegerId... fictureIntegerIds) {
    var id = createId();
    var data = new JSONObject(BEANS.get(IIdSignatureDataObjectMapper.class).writeValue(BEANS.get(DoEntityBuilder.class)
        .put("id", id)
        .put("actionType", "scout.LoadChildPages")
        .put("data", BEANS.get(LoadChildPagesHybridActionDo.class)
            .withPageParams(
                Arrays.stream(fictureIntegerIds).map(fictureIntegerId -> BEANS.get(IdPageParamDo.class).withId(fictureIntegerId)).toList()
            )
            .withReplace(replace))
        .put("contextElements", BEANS.get(DoEntityBuilder.class)
            .put("page", List.of(BEANS.get(DoEntityBuilder.class)
                .put("widget", getJsonOutline().getId())
                .put("element", pageId)
                .build()))
            .build())
        .build()));

    getJsonHybridManager().handleUiEvent(new JsonEvent(getJsonHybridManager().getId(), "hybridAction", data));

    return id;
  }

  @Test
  public void testHybridAction() {
    var myJsTablePage = new MyJsTablePage();
    getOutline().addChildNode(getOutline().getRootPage(), myJsTablePage);

    getUiSession().currentJsonResponse().fireProcessBufferedEvents();
    JsonTestUtility.endRequest(m_uiSession);

    var myJsTablePageId = getJsonOutline().getOrCreateNodeId(myJsTablePage);

    // initially insert pages 7 and 13

    var create7And13Id = callLoadChildPages(myJsTablePageId, true, FixtureIntegerId.of(7), FixtureIntegerId.of(13));

    assertEquals(2, myJsTablePage.getChildNodeCount());
    var createJsPage7 = myJsTablePage.getChildNode(0);
    var createJsPage13 = myJsTablePage.getChildNode(1);

    assertEquals(BEANS.get(IdPageParamDo.class).withId(FixtureIntegerId.of(7)), createJsPage7.getPrimaryKey());
    assertEquals(BEANS.get(IdPageParamDo.class).withId(FixtureIntegerId.of(13)), createJsPage13.getPrimaryKey());

    var response = getUiSession().currentJsonResponse();
    response.fireProcessBufferedEvents();

    var createJsPage7Id = getJsonOutline().getOrCreateNodeId(createJsPage7);
    var createJsPage13Id = getJsonOutline().getOrCreateNodeId(createJsPage13);

    // corresponding hybridActionEnd-event
    assertTrue(response.getEventList().stream().anyMatch(hybridActionEndPredicate(create7And13Id)));
    // nodesInserted-event
    assertTrue(response.getEventList().stream().anyMatch(nodesInsertedPredicate(
        myJsTablePageId,
        new NodesInsertedChildPageInfo(createJsPage7Id, 0, "MyJsPage", FixtureIntegerId.of(7)),
        new NodesInsertedChildPageInfo(createJsPage13Id, 1, "MyJsPage", FixtureIntegerId.of(13))
    )));

    JsonTestUtility.endRequest(m_uiSession);

    // replace with pages 13 and 42

    var replace13And42Id = callLoadChildPages(myJsTablePageId, true, FixtureIntegerId.of(13), FixtureIntegerId.of(42));

    assertEquals(2, myJsTablePage.getChildNodeCount());
    var replaceJsPage13 = myJsTablePage.getChildNode(0);
    var replaceJsPage42 = myJsTablePage.getChildNode(1);

    // page 13 was replaced
    assertNotEquals(createJsPage13, replaceJsPage13);

    assertEquals(BEANS.get(IdPageParamDo.class).withId(FixtureIntegerId.of(13)), replaceJsPage13.getPrimaryKey());
    assertEquals(BEANS.get(IdPageParamDo.class).withId(FixtureIntegerId.of(42)), replaceJsPage42.getPrimaryKey());

    response = getUiSession().currentJsonResponse();
    response.fireProcessBufferedEvents();

    var replaceJsPage13Id = getJsonOutline().getOrCreateNodeId(replaceJsPage13);
    var replaceJsPage42Id = getJsonOutline().getOrCreateNodeId(replaceJsPage42);

    // corresponding hybridActionEnd-event
    assertTrue(response.getEventList().stream().anyMatch(hybridActionEndPredicate(replace13And42Id)));
    // allChildNodesDeleted-event
    assertTrue(response.getEventList().stream().anyMatch(allChildNodesDeletedPredicate(myJsTablePageId)));
    // nodesInserted-event
    assertTrue(response.getEventList().stream().anyMatch(nodesInsertedPredicate(
        myJsTablePageId,
        new NodesInsertedChildPageInfo(replaceJsPage13Id, 0, "MyJsPage", FixtureIntegerId.of(13)),
        new NodesInsertedChildPageInfo(replaceJsPage42Id, 1, "MyJsPage", FixtureIntegerId.of(42))
    )));

    JsonTestUtility.endRequest(m_uiSession);

    // create 7 and 42 without replace

    var noReplace7And42Id = callLoadChildPages(myJsTablePageId, false, FixtureIntegerId.of(7), FixtureIntegerId.of(42));

    assertEquals(2, myJsTablePage.getChildNodeCount());
    var noReplaceJsPage7 = myJsTablePage.getChildNode(0);
    var noReplaceJsPage42 = myJsTablePage.getChildNode(1);

    // page 42 was NOT replaced
    assertSame(replaceJsPage42, noReplaceJsPage42);

    assertEquals(BEANS.get(IdPageParamDo.class).withId(FixtureIntegerId.of(7)), noReplaceJsPage7.getPrimaryKey());
    assertEquals(BEANS.get(IdPageParamDo.class).withId(FixtureIntegerId.of(42)), noReplaceJsPage42.getPrimaryKey());

    response = getUiSession().currentJsonResponse();
    response.fireProcessBufferedEvents();

    var noReplaceJsPage7Id = getJsonOutline().getOrCreateNodeId(noReplaceJsPage7);
    var noReplaceJsPage42Id = getJsonOutline().getOrCreateNodeId(noReplaceJsPage42);

    // corresponding hybridActionEnd-event
    assertTrue(response.getEventList().stream().anyMatch(hybridActionEndPredicate(noReplace7And42Id)));
    // nodesDeleted-event
    assertTrue(response.getEventList().stream().anyMatch(nodesDeletedPredicate(myJsTablePageId, replaceJsPage13Id)));
    // nodesInserted-event
    assertTrue(response.getEventList().stream().anyMatch(nodesInsertedPredicate(myJsTablePageId, new NodesInsertedChildPageInfo(noReplaceJsPage7Id, 0, "MyJsPage", FixtureIntegerId.of(7)))));
    // childNodeOrderChanged-event
    assertTrue(response.getEventList().stream().anyMatch(childNodeOrderChangedPredicate(myJsTablePageId, noReplaceJsPage7Id, noReplaceJsPage42Id)));
  }

  private Predicate<JsonEvent> hybridActionEndPredicate(String id) {
    return event -> getJsonHybridManager().getId().equals(event.getTarget()) &&
        "hybridEvent".equals(event.getType()) &&
        event.getData() instanceof JSONObject data &&
        id.equals(data.getString("id")) &&
        HybridEvent.HYBRID_ACTION_END.equals(data.getString("eventType"));
  }

  private Predicate<JsonEvent> nodesInsertedPredicate(String pageId, NodesInsertedChildPageInfo... childPages) {
    return event -> getJsonOutline().getId().equals(event.getTarget()) &&
        JsonTree.EVENT_NODES_INSERTED.equals(event.getType()) &&
        event.getData() instanceof JSONObject data &&
        pageId.equals(data.getString(JsonTree.PROP_COMMON_PARENT_NODE_ID)) &&
        data.getJSONArray("nodes") instanceof JSONArray nodes &&
        childPages.length == nodes.length() &&
        IntStream.range(0, childPages.length).allMatch(i -> nodes.getJSONObject(i) instanceof JSONObject node &&
            childPages[i].id().equals(node.getString("id")) &&
            childPages[i].childNodeIndex() == node.getInt("childNodeIndex") &&
            childPages[i].jsPageObjectType().equals(node.getString(IJsPage.PROP_JS_PAGE_OBJECT_TYPE)) &&
            node.getJSONObject(IJsPage.PROP_JS_PAGE_MODEL).getJSONObject(JsPageChildPageToJsonContributor.PROP_JS_PAGE_CHILD_PAGE_PARAM) instanceof JSONObject childPageParam &&
            BEANS.get(IdCodec.class).toQualified(childPages[i].fixtureIntegerId(), IdCodecFlag.SIGNATURE).equals(childPageParam.get("id"))
        );
  }

  private Predicate<JsonEvent> nodesDeletedPredicate(String pageId, String... childPageIds) {
    return event -> getJsonOutline().getId().equals(event.getTarget()) &&
        JsonTree.EVENT_NODES_DELETED.equals(event.getType()) &&
        event.getData() instanceof JSONObject data &&
        pageId.equals(data.getString(JsonTree.PROP_COMMON_PARENT_NODE_ID)) &&
        data.getJSONArray(JsonTree.PROP_NODE_IDS) instanceof JSONArray nodeIds &&
        childPageIds.length == nodeIds.length() &&
        IntStream.range(0, childPageIds.length).allMatch(i -> childPageIds[i].equals(nodeIds.getString(i)));
  }

  private Predicate<JsonEvent> allChildNodesDeletedPredicate(String pageId) {
    return event -> getJsonOutline().getId().equals(event.getTarget()) &&
        JsonTree.EVENT_ALL_CHILD_NODES_DELETED.equals(event.getType()) &&
        event.getData() instanceof JSONObject data &&
        pageId.equals(data.getString(JsonTree.PROP_COMMON_PARENT_NODE_ID));
  }

  private Predicate<JsonEvent> childNodeOrderChangedPredicate(String pageId, String... childPageIds) {
    return event -> getJsonOutline().getId().equals(event.getTarget()) &&
        JsonTree.EVENT_CHILD_NODE_ORDER_CHANGED.equals(event.getType()) &&
        event.getData() instanceof JSONObject data &&
        pageId.equals(data.getString("parentNodeId")) &&
        data.getJSONArray("childNodeIds") instanceof JSONArray childNodeIds &&
        childPageIds.length == childNodeIds.length() &&
        IntStream.range(0, childPageIds.length).allMatch(i -> childPageIds[i].equals(childNodeIds.getString(i)));
  }

  private static class MyJsTablePage extends AbstractJsPage {

    @Override
    public String getConfiguredJsPageObjectType() {
      return "MyJsTablePage";
    }

    @Override
    protected IPage<?> createChildPage(IPageParamDo pageParam) {
      return new MyJsPage();
    }
  }

  private static class MyJsPage extends AbstractJsPage {

    @Override
    public String getConfiguredJsPageObjectType() {
      return "MyJsPage";
    }

    @Override
    protected IPage<?> createChildPage(IPageParamDo pageParam) {
      return new MyJsTablePage();
    }
  }

  private static class P_IdCodec extends IdCodec {

    @Override
    protected byte[] getIdSignaturePassword() {
      return "42".getBytes(StandardCharsets.UTF_8);
    }
  }

  private record NodesInsertedChildPageInfo(String id, int childNodeIndex, String jsPageObjectType, FixtureIntegerId fixtureIntegerId) {
  }
}
