/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.context;

import java.util.UUID;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Provides the current node's identification.
 *
 * @see 6.1
 */
@ApplicationScoped
public class NodeIdentifier {

  private String m_nodeId;

  @PostConstruct
  public void postConstruct() {
    m_nodeId = compute();
  }

  /**
   * Returns the identifier of the current node.
   *
   * @deprecated Use typed {@link org.eclipse.scout.rt.dataobject.id.NodeId} instead
   * @see org.eclipse.scout.rt.dataobject.id.NodeId#current()
   */
  @Deprecated
  public String get() {
    return m_nodeId;
  }

  /**
   * Computes the current node identifier.
   */
  protected String compute() {
    // Check system property defined node id
    String nodeId = CONFIG.getPropertyValue(NodeIdProperty.class);
    if (StringUtility.hasText(nodeId)) {
      return nodeId;
    }

    // Check for WebLogic name
    nodeId = System.getProperty("weblogic.Name");
    if (StringUtility.hasText(nodeId)) {
      return nodeId;
    }

    // Check for JBoss node name
    nodeId = System.getProperty("jboss.node.name");
    if (StringUtility.hasText(nodeId)) {
      return nodeId;
    }

    // Generate random ID
    return UUID.randomUUID().toString();
  }

  public static class NodeIdProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.nodeId";
    }

    @Override
    public String description() {
      return "Specifies the cluster node name. If not specified a default id is computed.";
    }
  }
}
