/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
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

import org.junit.Test;
import org.mockito.Mockito;

public class HttpProxyTest {

  @Test
  public void testRewriteUrl() {
    HttpProxy proxy = new HttpProxy();
    proxy.setRemoteUrl("http://localhost:8085/api");
    HttpProxyOptions options = new HttpProxyOptions()
        .withRewriteRule("/studio-api/")
        .withRewriteReplacement("/");
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    Mockito.when(req.getPathInfo()).thenReturn("/studio-api/config/media/templates/a20a1264-2c56-4c71-a1fd-a1edb675a8ee/preview-image");
    String url = proxy.rewriteUrl(req, options);
    assertEquals("http://localhost:8085/api/config/media/templates/a20a1264-2c56-4c71-a1fd-a1edb675a8ee/preview-image", url);
  }

}
