/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
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

@SuppressWarnings("restriction")
public interface IJmsMessageSerializer<T> {

  Message createMessage(T message, Session session) throws Exception;

  T extractMessage(Message jmsMessage) throws Exception;

}
