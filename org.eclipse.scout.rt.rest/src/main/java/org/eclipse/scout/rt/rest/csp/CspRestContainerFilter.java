/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.csp;

import java.io.IOException;
import java.util.regex.Pattern;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.rest.container.IRestContainerResponseFilter;
import org.eclipse.scout.rt.security.csp.BlockAllContentSecurityPolicy;
import org.eclipse.scout.rt.security.csp.ContentSecurityPolicy;

public class CspRestContainerFilter implements IRestContainerResponseFilter {

  private final LazyValue<String> m_blockAllCsp = new LazyValue<>(() -> createRestPolicy().toToken());

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    if (isCspHeaderPresent(responseContext)) {
      return; // policy already set. Do not add default policy
    }
    if (isAttachment(responseContext)) {
      return; // attachments are downloaded and not rendered by the browser: no need to add a policy
    }
    // append a block-all default CSP header (in case a REST response is text/html, image/svg or text/javascript which might be interpreted by the browser)
    responseContext.getHeaders().putSingle(ContentSecurityPolicy.HTTP_HEADER, m_blockAllCsp.get());
  }

  protected boolean isCspHeaderPresent(ContainerResponseContext responseContext) {
    return responseContext.containsHeaderString(ContentSecurityPolicy.HTTP_HEADER, v -> true /* accept any value */);
  }

  protected boolean isAttachment(ContainerResponseContext responseContext) {
    String disposition = responseContext.getHeaderString("Content-Disposition");
    if (!StringUtility.hasText(disposition)) {
      return false;
    }
    return Pattern.compile(";").splitAsStream(disposition)
        .anyMatch(s -> "attachment".equalsIgnoreCase(StringUtility.trim(s)));
  }

  protected ContentSecurityPolicy createRestPolicy() {
    return BEANS.get(BlockAllContentSecurityPolicy.class);
  }
}
