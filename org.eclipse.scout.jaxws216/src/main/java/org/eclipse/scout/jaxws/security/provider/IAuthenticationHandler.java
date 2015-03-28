/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.scout.jaxws.security.provider;

import java.security.AccessController;
import java.util.Collections;
import java.util.Set;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.jaxws.internal.JaxWsConstants;
import org.eclipse.scout.jaxws.internal.JaxWsHelper;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;

/**
 * Handler to enable authentication on a webservice provider.<br/>
 * Use {@link IAuthenticationHandler.None} to disable authentication. If the handler declares a public, single-argument
 * constructor with the parameter type {@link IAuthenticator}, the handler gets the {@link IAuthenticator} injected as
 * configured on the port type. The authenticator is used to authenticate a user's credentials against a datasource like
 * config.ini or database.
 */
public interface IAuthenticationHandler extends SOAPHandler<SOAPMessageContext> {

  /**
   * @return
   *         The name of this authentication mechanism; is used in the webservice overview-page.
   */
  String getName();

  /**
   * This handler indicates that no authentication is used.
   */
  static final class None implements IAuthenticationHandler {

    public static final IAuthenticationHandler INSTANCE = new None();

    private None() {
    }

    @Override
    public Set<QName> getHeaders() {
      return Collections.emptySet();
    }

    @Override
    public void close(final MessageContext context) {
      // NOOP
    }

    @Override
    public boolean handleFault(final SOAPMessageContext context) {
      return true;
    }

    @Override
    public boolean handleMessage(final SOAPMessageContext context) {
      if (JaxWsHelper.isOutboundMessage(context)) {
        return true;
      }
      else if (JaxWsHelper.isAuthenticated()) {
        return true;
      }
      else {
        try {
          final Subject subject = Subject.getSubject(AccessController.getContext());
          subject.getPrincipals().add(new SimplePrincipal(JaxWsConstants.USER_ANONYMOUS));
          subject.setReadOnly();

          final IServerSession serverSession = OBJ.get(ServerSessionProviderWithCache.class).provide(ServerRunContexts.copyCurrent().subject(subject));
          JaxWsHelper.setContextSession(context, serverSession);
          return true;
        }
        catch (final ProcessingException e) {
          return JaxWsHelper.reject500(context, e);
        }
      }
    }

    @Override
    public String getName() {
      return JaxWsConstants.AUTH_NONE_NAME;
    }
  }
}
