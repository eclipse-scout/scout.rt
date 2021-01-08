/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.jersey;

import java.util.Locale;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;

import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.nls.NlsLocale;

/**
 * Filter setting the following HTTP headers:
 * <li>
 * <li>{@link HttpHeaders#ACCEPT_LANGUAGE} (according to the current {@link NlsLocale})
 * <li>{@link CorrelationId#HTTP_HEADER_NAME} (according the the current {@link CorrelationId})
 * </ul>
 */
public class LanguageAndCorrelationIdRestRequestFilter implements ClientRequestFilter {

  @Override
  public void filter(ClientRequestContext requestContext) {
    putLocale(requestContext);
    putCorrelationId(requestContext);
  }

  protected void putLocale(ClientRequestContext requestContext) {
    Locale locale = NlsLocale.get();
    if (locale != null) {
      requestContext.getHeaders().putSingle(HttpHeaders.ACCEPT_LANGUAGE, locale);
    }
  }

  protected void putCorrelationId(ClientRequestContext requestContext) {
    final String cid = CorrelationId.CURRENT.get();
    if (cid != null) {
      requestContext.getHeaders().putSingle(CorrelationId.HTTP_HEADER_NAME, cid);
    }
  }
}
