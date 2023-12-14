/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.jms;

import jakarta.jms.JMSException;
import jakarta.jms.Session;

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
