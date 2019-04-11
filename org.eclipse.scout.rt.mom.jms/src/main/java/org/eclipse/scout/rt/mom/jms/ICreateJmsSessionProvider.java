/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.jms;

import javax.jms.JMSException;
import javax.jms.Session;

import org.eclipse.scout.rt.mom.jms.internal.JmsSessionProviderWrapper;

/**
 * Lambda function for a late binding JMS {@link IJmsSessionProvider} used in {@link JmsSessionProviderWrapper}
 *
 * @since 6.1
 */
@FunctionalInterface
public interface ICreateJmsSessionProvider {
  IJmsSessionProvider create(Session session) throws JMSException;
}
