package org.eclipse.scout.rt.ui.json;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonOutline extends JsonDesktopTree<IOutline> {
  private JsonDesktop m_jsonDesktop;

  public JsonOutline(JsonDesktop jsonDesktop, IOutline modelObject, IJsonSession jsonSession) {
    super(modelObject, jsonSession);
    m_jsonDesktop = jsonDesktop;
  }

  @Override
  protected void attachModel() throws JsonUIException {
    super.attachModel();
  }

  @Override
  public JSONObject toJson() throws JsonUIException {
    try {
      JSONObject json = new JSONObject();
      json.put("objectType", "Outline");
      json.put("id", getId());

      JSONArray jsonPages = new JSONArray();
      if (getModelObject().isRootNodeVisible()) {
        IPage rootPage = getModelObject().getRootPage();
        jsonPages.put(pageToJson(rootPage));
      }
      else {
        for (IPage childPage : getModelObject().getRootPage().getChildPages()) {
          jsonPages.put(pageToJson(childPage));
        }
      }
      json.put("pages", jsonPages);
      return json;
    }
    catch (JSONException e) {
      throw new JsonUIException(e.getMessage(), e);
    }
  }

  @Override
  protected void handleModelTreeEvent(TreeEvent event) {
    //TODO move to super class
//    switch (event.getType()) {
//      case TreeEvent.TYPE_NODES_INSERTED: {
//        JSONArray jsonPages = new JSONArray();
//        for (ITreeNode node : event.getNodes()) {
//          JSONObject jsonPage = pageToJson((IPage) node);
//          jsonPages.put(jsonPage);
//        }
//        //TODO currently sent as desktop event
//        getJsonSession().currentJsonResponse().addUpdateEvent(m_jsonDesktop.getId(), "nodesAdded", jsonPages);
//        break;
//      }
//    }
  }

  @Override
  protected void handleModelTreeEventBatch(List<? extends TreeEvent> events) {
    for (TreeEvent event : events) {
      handleModelTreeEvent(event);
    }
  }

  protected JSONObject pageToJson(IPage page) throws JsonUIException {
    try {
      JSONObject json = new JSONObject();
      json.put("id", page.getNodeId());
      json.put("parentNodeId", page.getParentPage().getNodeId());
      json.put("text", page.getCell().getText());
      json.put("expanded", page.isExpanded());
      json.put("leaf", page.isLeaf());
      JSONArray jsonChildPages = new JSONArray();
      if (page.getChildNodeCount() > 0) {
        for (IPage childPage : page.getChildPages()) {
          jsonChildPages.put(pageToJson(childPage));
        }
      }
      json.put("childPages", jsonChildPages);

      //TODO bench
      JSONObject bench = new JSONObject();
      bench.put("type", "table");
      bench.put("columns", new JSONArray());
      bench.put("data", "load");
      bench.put("graph", "load");
      bench.put("map", "load");
      json.put("bench", bench);

      return json;
    }
    catch (JSONException e) {
      throw new JsonUIException(e.getMessage(), e);
    }
  }

  @Override
  public void handleUiEvent(JsonRequest req, JsonResponse res) throws JsonUIException {
    if ("drilldown".equals(req.getEventType())) {
      handleUiDrillDownEvent(req, res);
    }
    else if ("drilldown_menu".equals(req.getEventType())) {
      handleUiDrillDownMenuEvent(req, res);
    }
    else {
      throw new IllegalArgumentException("unsupported event type");
    }
  }

  protected void handleUiDrillDownEvent(JsonRequest req, JsonResponse res) throws JsonUIException {
    try {
      final String nodeId = req.getEventObject().getString("nodeId");
      final ITreeNode node = findTreeNode(nodeId);
      if (node == null) {
        throw new JsonUIException("No node found for id " + nodeId);
      }

      new ClientSyncJob("Node click", getJsonSession().getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          getModelObject().getUIFacade().fireNodeClickFromUI(node);
          getModelObject().getUIFacade().setNodesSelectedFromUI(CollectionUtility.arrayList(node));
        }
      }.runNow(new NullProgressMonitor());

      //TODO better return nodes event driven
      JSONArray jsonPages = new JSONArray();
      for (ITreeNode childNode : node.getChildNodes()) {
        JSONObject jsonPage = pageToJson((IPage) childNode);
        jsonPages.put(jsonPage);
      }
      JSONObject event = new JSONObject();
      event.put("nodes", jsonPages);
      getJsonSession().currentJsonResponse().addActionEvent("nodesAdded", m_jsonDesktop.getId(), event);
    }
    catch (JSONException e) {
      throw new JsonUIException(e.getMessage(), e);
    }
  }

  protected void handleUiDrillDownMenuEvent(JsonRequest req, JsonResponse res) throws JsonUIException {
    try {
      List<IMenu> menus = collectMenus(false, true);
      JSONArray jsonMenus = new JSONArray();
      for (IMenu menu : menus) {
        JsonMenu jsonMenu = new JsonMenu(menu, getJsonSession());
        jsonMenu.init();
        jsonMenus.put(jsonMenu.toJson());
      }
      JSONObject event = new JSONObject();
      event.put("menus", jsonMenus);
      getJsonSession().currentJsonResponse().addActionEvent("menuPopup", getId(), event);
    }
    catch (JSONException e) {
      throw new JsonUIException(e.getMessage(), e);
    }
  }

  protected List<IMenu> collectMenus(final boolean emptySpaceActions, final boolean nodeActions) {
    final List<IMenu> menuList = new LinkedList<IMenu>();
    new ClientSyncJob("Menu popup", getJsonSession().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        if (emptySpaceActions) {
          menuList.addAll(getModelObject().getUIFacade().fireEmptySpacePopupFromUI());
        }
        if (nodeActions) {
          menuList.addAll(getModelObject().getUIFacade().fireNodePopupFromUI());
        }
      }
    }.runNow(new NullProgressMonitor());

    return Collections.unmodifiableList(menuList);
  }

  protected ITreeNode findTreeNode(final String nodeId) {
    final Holder<ITreeNode> nodeHolder = new Holder<>(ITreeNode.class);
    ITreeVisitor v = new ITreeVisitor() {
      @Override
      public boolean visit(ITreeNode node) {
        if (node.getNodeId().equals(nodeId)) {
          nodeHolder.setValue(node);
          return false;
        }
        return true;
      }
    };
    getModelObject().visitNode(getModelObject().getRootNode(), v);
    return nodeHolder.getValue();
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
  }

}
