/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.servicetunnel;

import java.io.InputStream;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;

import org.eclipse.scout.rt.client.rest.ScoutBackendRestClientHelper;
import org.eclipse.scout.rt.client.servicetunnel.ServiceTunnelClientConfigProperties.CompressServiceTunnelRequestProperty;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.rest.client.IRestClientHelper;
import org.eclipse.scout.rt.rest.client.IRestResourceClient;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelConstants;

public class ProcessResourceClient implements IRestResourceClient {

  protected static final String RESOURCE_PATH = ServiceTunnelConstants.PROCESS_PATH;

  public Response call(InputStream inputStream) {
    WebTarget target = helper().target(RESOURCE_PATH);

    Builder builder = target
        .request()
        .accept(MediaType.APPLICATION_OCTET_STREAM);

    return builder.post(Entity.entity(
        inputStream,
        new Variant(
            MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM),
            (String) null,
            CONFIG.getPropertyValue(CompressServiceTunnelRequestProperty.class) ? "gzip" : null // enable request compression, see GzipEncoder for handling
        )
    ));
  }

  protected IRestClientHelper helper() {
    return BEANS.get(ScoutBackendRestClientHelper.class);
  }
}
