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
