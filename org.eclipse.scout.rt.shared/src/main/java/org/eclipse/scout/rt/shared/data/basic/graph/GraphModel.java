/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.shared.data.basic.graph;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class GraphModel implements Serializable {
  private static final long serialVersionUID = 1L;

  private final Set<GraphNode> m_nodes = new HashSet<>();
  private final Set<GraphEdge> m_edges = new HashSet<>();

  protected GraphModel() {
  }

  public static GraphModel create() {
    return new GraphModel();
  }

  public static GraphModel create(Set<GraphNode> nodes, Set<GraphEdge> edges) {
    return create()
        .withNodes(nodes)
        .withEdges(edges);
  }

  public GraphModel withNodes(GraphNode... nodes) {
    if (nodes != null) {
      getNodes().clear();
      for (GraphNode node : nodes) {
        addNode(node);
      }
    }
    return this;
  }

  public GraphModel withNodes(Set<GraphNode> nodes) {
    if (nodes != null) {
      getNodes().clear();
      for (GraphNode node : nodes) {
        addNode(node);
      }
    }
    return this;
  }

  public GraphModel withEdges(GraphEdge... edges) {
    if (edges != null) {
      getEdges().clear();
      for (GraphEdge edge : edges) {
        addEdge(edge);
      }
    }
    return this;
  }

  public GraphModel withEdges(Set<GraphEdge> edges) {
    if (edges != null) {
      getEdges().clear();
      for (GraphEdge edge : edges) {
        addEdge(edge);
      }
    }
    return this;
  }

  public Set<GraphNode> getNodes() {
    return m_nodes;
  }

  public Set<GraphEdge> getEdges() {
    return m_edges;
  }

  public void addNode(GraphNode node) {
    m_nodes.add(node);
  }

  public void addEdge(GraphEdge edge) {
    m_edges.add(edge);
  }
}
