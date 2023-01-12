/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.consumer;

import org.eclipse.scout.rt.server.AbstractServerSession;

/**
 * Server session used in JAX-WS Consumer tests.
 *
 * @since 6.0.300
 */
public class JaxWsConsumerTestServerSession extends AbstractServerSession {

  private static final long serialVersionUID = 1L;

  public JaxWsConsumerTestServerSession() {
    super(true);
  }
}
