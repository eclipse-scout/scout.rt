/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.jms.internal;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.server.services.common.jms.AbstractJmsConsumerService;

@Priority(-1)
public class ExampleJmsConsumer extends AbstractJmsConsumerService {

  @Override
  protected String getConfiguredConnectionFactoryJndiName() {
    return "ConnectionFactory";
  }

  @Override
  protected String getConfiguredJndiName() {
    return "queue2";
  }

  @Override
  protected String getConfiguredProviderUrl() {
    return "tcp://10.0.2.128:3035";
  }

  @Override
  protected String getConfiguredContextFactory() {
    return "org.exolab.jms.jndi.InitialContextFactory";
  }
}
