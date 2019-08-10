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
package org.eclipse.scout.rt.mom.jms;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.eclipse.scout.rt.mom.jms.internal.JmsConnectionWrapper;

/**
 * Lambda function for a late binding JMS {@link Connection} used in {@link JmsConnectionWrapper}
 *
 * @since 6.1
 */
@FunctionalInterface
public interface ICreateJmsConnection {
  Connection create() throws JMSException;
}
