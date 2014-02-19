package org.eclipse.scout.rt.ui.json;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonOutline extends JsonTree<IOutline> {
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
      json.put("id", getId());
      json.put("type", "outline");

      JSONArray jsonPages = new JSONArray();
      if (getScoutObject().isRootNodeVisible()) {
        IPage rootPage = getScoutObject().getRootPage();
        jsonPages.put(pageToJson(rootPage));
      }
      else {
        for (IPage childPage : getScoutObject().getRootPage().getChildPages()) {
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
//        getJsonSession().currentUIResponse().addUpdateEvent(m_jsonDesktop.getId(), "nodesAdded", jsonPages);
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
    else {
      throw new IllegalArgumentException("unsupported event type");
    }
  }

  protected void handleUiDrillDownEvent(JsonRequest req, JsonResponse res) throws JsonUIException {
    try {
      final String nodeId = req.getEventData().getString("nodeId");
      final List<ITreeNode> childPages = new ArrayList<>();
      ITreeVisitor v = new ITreeVisitor() {
        @Override
        public boolean visit(ITreeNode node) {
          if (node.getNodeId().equals(nodeId)) {
            childPages.addAll(node.getChildNodes());
            return false;
          }
          return true;
        }
      };
      getScoutObject().visitNode(getScoutObject().getRootNode(), v);

      //TODO better return nodes event driven
      JSONArray jsonPages = new JSONArray();
      for (ITreeNode node : childPages) {
        JSONObject jsonPage = pageToJson((IPage) node);
        jsonPages.put(jsonPage);
      }
      getJsonSession().currentUIResponse().addUpdateEvent(m_jsonDesktop.getId(), "nodesAdded", jsonPages);
    }
    catch (JSONException e) {
      throw new JsonUIException(e.getMessage(), e);
    }

    //TODO
//    getScoutObject().getUIFacade().fireNodeClickFromUI(node);
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
  }

}
