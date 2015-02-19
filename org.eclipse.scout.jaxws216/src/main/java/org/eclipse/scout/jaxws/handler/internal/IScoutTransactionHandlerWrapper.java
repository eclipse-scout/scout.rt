/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.handler.internal;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

public interface IScoutTransactionHandlerWrapper<T extends MessageContext> {

  Handler<T> getHandler();
}
