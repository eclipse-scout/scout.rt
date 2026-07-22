/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import org.eclipse.scout.rt.dataobject.id.IIds;
import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxyRequestContext;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxyRequestOptions;
import org.eclipse.scout.rt.server.commons.servlet.IHttpProxyRequestOptionsModifier;

/**
 * Adds the {@value NodeId#HTTP_HEADER_NAME} header to the proxied request.
 */
public class ClientNodeIdHttpProxyRequestOptionsModifier implements IHttpProxyRequestOptionsModifier {

  @Override
  public void modify(HttpProxyRequestOptions options, HttpProxyRequestContext context) {
    options.withCustomRequestHeader(NodeId.HTTP_HEADER_NAME, IIds.toString(NodeId.current()));
  }
}
