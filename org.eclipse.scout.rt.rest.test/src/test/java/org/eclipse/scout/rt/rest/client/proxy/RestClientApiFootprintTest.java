/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.client.proxy;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.client.Client;

import org.eclipse.scout.rt.jackson.testing.DataObjectSerializationTestHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.rest.client.proxy.api.ApiSignature;
import org.eclipse.scout.rt.rest.client.proxy.api.ApiSignatureDo;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class RestClientApiFootprintTest {

  @Test
  public void collectApiSignatures() throws IOException {
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
