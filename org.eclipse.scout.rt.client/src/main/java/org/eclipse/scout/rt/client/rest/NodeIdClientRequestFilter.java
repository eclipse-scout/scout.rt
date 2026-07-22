/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.rest;

import java.io.IOException;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

import org.eclipse.scout.rt.dataobject.id.IIds;
import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.ApplicationScoped;

@ApplicationScoped
public class NodeIdClientRequestFilter implements ClientRequestFilter {

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    addNodeIdHeader(requestContext);
  }

  protected void addNodeIdHeader(ClientRequestContext requestContext) {
    NodeId nodeId = NodeId.current();
    if (nodeId != null) {
      requestContext.getHeaders().add(NodeId.HTTP_HEADER_NAME, IIds.toString(nodeId));
    }
  }
}
