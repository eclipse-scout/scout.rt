/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.http;

import java.net.URI;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;

/**
 * The original {@link LaxRedirectStrategy} basically supports redirecting POST requests but it lacks to re-send another
 * POST request and instead sends a GET request.
 * <p>
 * This enhanced version re-sends the original POST request to the redirected URL.
 *
 * @since 7.0
 */
public class EnhancedLaxRedirectStrategy extends LaxRedirectStrategy {

  public static final EnhancedLaxRedirectStrategy INSTANCE = new EnhancedLaxRedirectStrategy();

  @Override
  public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
    final String method = request.getRequestLine().getMethod();
    if (method.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
      //This line as a side-effect checks for circular redirects and must not be called twice. Therefore it is inside this if block.
      final URI uri = getLocationURI(request, response, context);
      return RequestBuilder.copy(request).setUri(uri).build();
    }
    return super.getRedirect(request, response, context);
  }
}
