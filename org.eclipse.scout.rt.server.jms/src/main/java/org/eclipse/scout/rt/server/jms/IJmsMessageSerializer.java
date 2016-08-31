/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jms;

import javax.jms.Message;
import javax.jms.Session;

import org.eclipse.scout.rt.mom.api.MOM;

/**
 * @deprecated will be removed in 7.1.x; use {@link MOM} instead.
 */
@Deprecated
public interface IJmsMessageSerializer<T> {

  Message createMessage(T message, Session session);

  T extractMessage(Message jmsMessage);

}
