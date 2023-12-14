/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.client;

import java.io.IOException;

import jakarta.ws.rs.client.ClientRequestContext;

import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.rest.csrf.AntiCsrfHelper;

/**
 * This filter prevents CSRF attacks on REST services.
 *
 * @see AntiCsrfHelper
 */
public class AntiCsrfClientFilter implements IGlobalRestRequestFilter {

  private final LazyValue<AntiCsrfHelper> m_requestWithHelper = new LazyValue<>(AntiCsrfHelper.class);

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    m_requestWithHelper.get().prepareRequest(requestContext);
  }
}
