package org.eclipse.scout.rt.ui.html.json.form.fields.graphfield;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.graphfield.IGraphField;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphEdge;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphLineStyle;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphModel;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphNode;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphShape;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONObject;

public class JsonGraphField<T extends IGraphField> extends JsonValueField<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonGraphField.class);

  public JsonGraphField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "GraphField";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IGraphField>(IValueField.PROP_VALUE, model) {
      @Override
      protected Object modelValue() {
        return getModel().getValue();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return graphModelToJson((GraphModel) value);
      }
    });
  }

  protected Object graphModelToJson(GraphModel graphModel) {
    if (graphModel == null) {
      return null;
    }
    JSONObject json = JsonObjectUtility.newOrderedJSONObject();
    Map<GraphNode, String> graphNodeIds = new HashMap<>();
    int nodeCounter = 0;

    // 1. Nodes
    for (GraphNode node : graphModel.getNodes()) {
      String graphNodeId = (nodeCounter++) + ""; // Convert to string, then increment
      graphNodeIds.put(node, graphNodeId);

      JSONObject jsonNode = JsonObjectUtility.newOrderedJSONObject();
      jsonNode.put("id", graphNodeId);
      jsonNode.put("label", node.getLabel());
      jsonNode.put("shape", shapeAsString(node.getShape()));
      jsonNode.put("url", node.getUrl());
      if (node.getColor() != null) {
        jsonNode.put("foregroundColor", node.getColor().getForeground());
        jsonNode.put("backgroundColor", node.getColor().getBackground());
      }
      if (node.getLocation() != null) {
        jsonNode.put("locationX", node.getLocation().getX());
        jsonNode.put("locationY", node.getLocation().getY());
      }
      jsonNode.put("order", node.getOrder());

      json.append("nodes", jsonNode);
    }

    // 2. Edges
    for (GraphEdge edge : graphModel.getEdges()) {
      JSONObject jsonEdge = JsonObjectUtility.newOrderedJSONObject();
      jsonEdge.put("startNodeId", graphNodeIds.get(edge.getStart()));
      jsonEdge.put("endNodeId", graphNodeIds.get(edge.getEnd()));
      jsonEdge.put("label", edge.getLabel());
      jsonEdge.put("style", lineStyleAsString(edge.getStyle()));
      jsonEdge.put("directed", edge.isDirected());
      json.append("edges", jsonEdge);
    }

    return json;
  }

  protected String shapeAsString(GraphShape shape) {
    if (shape == null) {
      return null;
    }
    switch (shape) {
      case DIAMOND:
        return "diamond";
      case ELLIPSE:
        return "ellipse";
      case HEXAGON:
        return "hexagon";
      case OCTAGON:
        return "octagon";
      case PARALLELOGRAM:
        return "parallelogram";
      case RECTANGLE:
        return "rectangle";
      case ROUNDED_RECTANGLE:
        return "roundedRectangle";
      case TRAPEZOID:
        return "trapezoid";
      case TRIANGLE:
        return "triangle";
      default:
        LOG.warn("Cannot map unknown graph node shape: " + shape);
        return null;
    }
  }

  protected String lineStyleAsString(GraphLineStyle lineStyle) {
    if (lineStyle == null) {
      return null;
    }
    switch (lineStyle) {
      case LIGHT_LABEL_HORIZONTAL:
        return "light-label-horizontal";
      case LIGHT_LABEL_ROTATED:
        return "light-label-rotated";
      default:
        LOG.warn("Cannot map unknown graph line style: " + lineStyle);
        return null;
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (JsonEventType.APP_LINK_ACTION.matches(event.getType())) {
      handleUiAppLinkAction(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiAppLinkAction(JsonEvent event) {
    String ref = event.getData().getString("ref");
    getModel().getUIFacade().fireAppLinkActionFromUI(ref);
  }
}
