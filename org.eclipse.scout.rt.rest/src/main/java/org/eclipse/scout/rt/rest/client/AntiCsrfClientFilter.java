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
package org.eclipse.scout.rt.rest.client;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.rest.csrf.AntiCsrfHelper;

/**
 * This filter prevents CSRF attacks on REST services.
 *
 * @see AntiCsrfHelper
 */
public class AntiCsrfClientFilter implements ClientRequestFilter {

  private final LazyValue<AntiCsrfHelper> m_requestWithHelper = new LazyValue<>(AntiCsrfHelper.class);

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    m_requestWithHelper.get().prepareRequest(requestContext);
  }
}
