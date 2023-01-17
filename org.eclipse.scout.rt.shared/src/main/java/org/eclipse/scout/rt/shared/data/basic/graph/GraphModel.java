/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.basic.graph;

import java.io.Serializable;
import java.util.Collection;
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

  public static GraphModel create(Collection<GraphNode> nodes, Collection<GraphEdge> edges) {
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

  public GraphModel withNodes(Collection<GraphNode> nodes) {
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

  public GraphModel withEdges(Collection<GraphEdge> edges) {
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
