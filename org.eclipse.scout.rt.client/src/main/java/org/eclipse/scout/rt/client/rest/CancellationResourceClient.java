/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.client.IRestClientHelper;
import org.eclipse.scout.rt.rest.client.IRestResourceClient;

/**
 * {@link IRestResourceClient} used to cancel ongoing REST requests using a separate cancellation call.
 */
public class CancellationResourceClient implements IRestResourceClient {

  protected static final String RESOURCE_PATH = "cancellation";

  public void cancel(String requestId) {
    WebTarget target = helper().target(RESOURCE_PATH)
        .path("{requestId}")
        .resolveTemplate("requestId", requestId);

    target.request().put(Entity.json("")).close();
  }

  protected IRestClientHelper helper() {
    return BEANS.get(ScoutBackendRestClientHelper.class);
  }
}
