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
package org.eclipse.scout.rt.ui.html.json.form.fields.graphfield;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphEdge;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphLineStyle;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphModel;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphNode;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphShape;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.json.JSONObject;

/**
 * @since 5.2
 */
public class JsonGraph implements IJsonObject {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonGraph.class);

  private GraphModel m_graphModel;

  public JsonGraph(GraphModel model) {
    m_graphModel = model;
  }

  @Override
  public Object toJson() {
    if (m_graphModel == null) {
      return null;
    }
    JSONObject json = JsonObjectUtility.newOrderedJSONObject();
    Map<GraphNode, String> graphNodeIds = new HashMap<>();
    int nodeCounter = 0;

    // 1. Nodes
    for (GraphNode node : m_graphModel.getNodes()) {
      String graphNodeId = (nodeCounter++) + ""; // Convert to string, then increment
      graphNodeIds.put(node, graphNodeId);

      JSONObject jsonNode = JsonObjectUtility.newOrderedJSONObject();
      jsonNode.put("id", graphNodeId);
      jsonNode.put("label", node.getLabel());
      jsonNode.put("shape", shapeAsString(node.getShape()));
      jsonNode.put("url", node.getUrl());
      jsonNode.put("cssClass", node.getCssClass());
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
    for (GraphEdge edge : m_graphModel.getEdges()) {
      JSONObject jsonEdge = JsonObjectUtility.newOrderedJSONObject();
      jsonEdge.put("source", graphNodeIds.get(edge.getSource()));
      jsonEdge.put("target", graphNodeIds.get(edge.getTarget()));
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

}
