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
package org.eclipse.scout.jaxws.security.consumer;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.jaxws.annotation.ScoutWebServiceClient;

/**
 * Handler that represents an authentication strategy to include user's credentials in a webservice request.
 */
public interface IAuthenticationHandler extends SOAPHandler<SOAPMessageContext> {

  void setUsername(String username);

  void setPassword(String password);

  /**
   * Used in {@link ScoutWebServiceClient#authenticationHandler()} to
   * signal that authentication handler is not used.
   */
  static final class NONE implements IAuthenticationHandler {

    @Override
    public void setPassword(String password) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setUsername(String username) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Set<QName> getHeaders() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void close(MessageContext context) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
      throw new UnsupportedOperationException();
    }
  }
}
