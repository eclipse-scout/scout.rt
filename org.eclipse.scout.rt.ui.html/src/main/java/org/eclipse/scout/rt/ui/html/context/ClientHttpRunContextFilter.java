/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.context;

import jakarta.servlet.Filter;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.commons.context.HttpRunContextFilter;
import org.eclipse.scout.rt.server.commons.context.HttpRunContextProducer;

/**
 * Default servlet {@link Filter} implementation providing {@link ClientHttpRunContextProducer} creating HTTP run context instances for client.
 */
public class ClientHttpRunContextFilter extends HttpRunContextFilter {

  @Override
  protected HttpRunContextProducer createRunContextProducer() {
    return BEANS.get(ClientHttpRunContextProducer.class);
  }
}
