/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;
import org.mockito.Mockito;

public class HttpProxyTest {

  @Test
  public void testRewriteUrl() {
    HttpProxy proxy = BEANS.get(HttpProxy.class)
        .withRemoteBaseUrl("http://internal.example.com:1234/api");

    HttpProxyRequestOptions options = new HttpProxyRequestOptions()
        .withRewriteRule(new SimpleRegexRewriteRule("/my-api/", "/"));

    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    Mockito.when(req.getPathInfo()).thenReturn("/my-api/templates/a20a1264-2c56-4c71-a1fd-a1edb675a8ee/preview");

    String url = proxy.rewriteUrl(req, options);
    assertEquals("http://internal.example.com:1234/api/templates/a20a1264-2c56-4c71-a1fd-a1edb675a8ee/preview", url);
  }
}
