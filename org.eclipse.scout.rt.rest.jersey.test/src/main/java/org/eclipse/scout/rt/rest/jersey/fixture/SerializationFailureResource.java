/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.fixture;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.scout.rt.rest.IRestResource;

@Path("serializationFailure")
public class SerializationFailureResource implements IRestResource {

  /**
   * Returns a data object which cannot be serialized (attribute value does not match the declared attribute type).
   */
  @GET
  @Path("invalidResponse")
  @Produces(MediaType.APPLICATION_JSON)
  public FixtureDo invalidResponse() {
    FixtureDo fixture = new FixtureDo();
    fixture.put(fixture.uuid().getAttributeName(), "not-a-uuid");
    return fixture;
  }

  @POST
  @Path("echo")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public FixtureDo echo(FixtureDo request) {
    return request;
  }
}
