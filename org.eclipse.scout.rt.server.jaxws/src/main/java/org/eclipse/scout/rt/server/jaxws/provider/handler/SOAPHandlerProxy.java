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
package org.eclipse.scout.rt.server.jaxws.provider.handler;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.Handler;

/**
 * {@link HandlerProxy} to proxy a {@link SOAPHandler}.
 *
 * @see HandlerProxy
 * @since 5.1
 */
@Internal
public class SOAPHandlerProxy extends HandlerProxy<SOAPMessageContext> implements SOAPHandler<SOAPMessageContext> {

  public SOAPHandlerProxy(final Handler handlerAnnotation) {
    super(handlerAnnotation);
  }

  @Override
  public Set<QName> getHeaders() {
    return ((SOAPHandler<SOAPMessageContext>) getHandlerDelegate()).getHeaders();
  }
}
