/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.provider.handler;

import java.util.Set;

import javax.xml.namespace.QName;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

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
