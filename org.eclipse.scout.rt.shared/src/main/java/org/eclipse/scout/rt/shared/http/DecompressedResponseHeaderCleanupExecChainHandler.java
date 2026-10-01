/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http;

import java.io.IOException;

import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;

/**
 * Removes response headers describing the encoded payload once HttpClient has transparently decompressed the entity.
 * HttpClient &lt; 5.6 did this itself, see HTTPCLIENT-2409.
 */
public class DecompressedResponseHeaderCleanupExecChainHandler implements ExecChainHandler {

  public static final String NAME = DecompressedResponseHeaderCleanupExecChainHandler.class.getSimpleName();

  public static void install(HttpClientBuilder builder) {
    builder.addExecInterceptorBefore(ChainElement.COMPRESS.name(), NAME, new DecompressedResponseHeaderCleanupExecChainHandler());
  }

  @Override
  public ClassicHttpResponse execute(ClassicHttpRequest request, ExecChain.Scope scope, ExecChain chain) throws IOException, HttpException {
    ClassicHttpResponse response = chain.proceed(request, scope);
    HttpEntity entity = response.getEntity();
    if (entity != null && entity.getContentEncoding() == null && response.containsHeader(HttpHeaders.CONTENT_ENCODING)) {
      response.removeHeaders(HttpHeaders.CONTENT_ENCODING);
      response.removeHeaders(HttpHeaders.CONTENT_LENGTH);
      response.removeHeaders(HttpHeaders.CONTENT_MD5);
    }
    return response;
  }
}
