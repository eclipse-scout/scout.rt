/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.fixture;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.IRestResource;

@Path("serialization")
public class SerializationResource implements IRestResource {

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public SerializationResponse serialize(SerializationRequest request) {
    return BEANS.get(SerializationResponse.class)
        .withOriginal(request.getBody())
        .withSerialized(BEANS.get(IDataObjectMapper.class).writeValue(request.getBody()));
  }
}
