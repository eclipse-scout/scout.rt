/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.client.proxy;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.client.Client;

import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.jackson.testing.DataObjectSerializationTestHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.client.proxy.api.ApiSignature;
import org.eclipse.scout.rt.rest.client.proxy.api.ApiSignatureDo;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test creates an inventory of all JAX-RS client classes and compares it with the one created at the time the
 * {@link RestClientProxyFactory} has been developed. Some JAX-RS classes are proxied in order to provide additional
 * features (currently generic exception handling for JAX-RS resources families returning a common error response type).
 * <p>
 * <b>If this test fails</b> verify whether new classes and methods are required to be proxied as well and whether
 * changed methods are still handled correctly.
 */
@RunWith(PlatformTestRunner.class)
public class RestClientApiFootprintTest {

  @Test
  public void verifyApiSignatures() throws IOException {
    ApiSignatureDo api = BEANS.get(ApiSignature.class)
        .classFilter(c -> c.getPackage() != null && c.getPackage().getName().startsWith("javax.ws.rs."))
        .methodFilter(m -> !"$jacocoInit".equals(m.getName())) // ignore methods created by code coverage
        .collect(Client.class)
        .build();

    ApiSignatureDo referenceApi;
    try (InputStream in = RestClientApiFootprintTest.class.getResourceAsStream("restApiSignature.json")) {
      referenceApi = BEANS.get(IDataObjectMapper.class).readValue(in, ApiSignatureDo.class);
    }

    BEANS.get(DataObjectSerializationTestHelper.class).assertJsonEquals(referenceApi, api);
  }
}
