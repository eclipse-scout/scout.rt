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

import org.eclipse.scout.rt.server.jaxws.provider.annotation.Handler;

/**
 * Like {@link HandlerDelegate}, but to be used for {@link SOAPHandler}.
 *
 * @see HandlerDelegate
 * @since 5.1
 */
public class SOAPHandlerDelegate extends HandlerDelegate<SOAPMessageContext> implements SOAPHandler<SOAPMessageContext> {

  public SOAPHandlerDelegate(final Handler handlerAnnotation) {
    super(handlerAnnotation);
  }

  @Override
  public Set<QName> getHeaders() {
    return ((SOAPHandler<SOAPMessageContext>) getHandlerDelegate()).getHeaders();
  }
}
