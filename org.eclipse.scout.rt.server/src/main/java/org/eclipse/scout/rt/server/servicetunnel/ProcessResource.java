/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.servicetunnel;

import java.io.InputStream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.IRestResource;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelConstants;

@Path(ServiceTunnelConstants.PROCESS_PATH)
public class ProcessResource implements IRestResource {

  @POST
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response process(InputStream in) {
    StreamingOutput stream = out -> BEANS.get(ServiceTunnelService.class).incomingRequest(in, out);
    return Response.ok(stream).build();
  }
}
