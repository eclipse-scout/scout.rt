/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.server;

import static org.junit.Assert.assertEquals;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.jersey.JerseyTestApplication;
import org.eclipse.scout.rt.rest.jersey.JerseyTestRestClientHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * HTTP status of serialization/deserialization failures of data objects in REST resources.
 */
@RunWith(PlatformTestRunner.class)
public class SerializationFailureResourceTest {

  @BeforeClass
  public static void beforeClass() {
    BEANS.get(JerseyTestApplication.class).ensureStarted();
  }

  /**
   * Jackson 3 does not wrap exceptions thrown while serializing the response anymore (Jackson 2 wrapped them into a
   * JsonMappingException, which was mapped to 400). The raw exception is handled by the DefaultExceptionMapper.
   */
  @Test
  public void testInvalidResponse() {
    try (Response response = target("invalidResponse").request().get()) {
      assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }
  }

  /**
   * Exceptions thrown while deserializing the request are still wrapped into a databind exception and mapped to 400.
   */
  @Test
  public void testInvalidRequest() {
    String json = "{\"_type\" : \"scout.Fixture\", \"dates\" : \"not-a-list\"}";
    try (Response response = target("echo").request().post(Entity.entity(json, MediaType.APPLICATION_JSON))) {
      assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }
  }

  protected WebTarget target(String path) {
    JerseyTestRestClientHelper helper = BEANS.get(JerseyTestRestClientHelper.class);
    // raw client: error responses are not transformed into exceptions
    return helper.rawClient().target(helper.getBaseUri()).path("api/serializationFailure").path(path);
  }
}
