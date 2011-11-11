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
package org.eclipse.scout.jaxws216.internal.tube;

import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.security.auth.Subject;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.jaxws216.Activator;
import org.eclipse.scout.jaxws216.annotation.ScoutTransaction;
import org.eclipse.scout.jaxws216.annotation.ScoutWebService;
import org.eclipse.scout.jaxws216.handler.internal.ScoutTransactionLogicalHandlerWrapper;
import org.eclipse.scout.jaxws216.handler.internal.ScoutTransactionMessageHandlerWrapper;
import org.eclipse.scout.jaxws216.handler.internal.ScoutTransactionSOAPHandlerWrapper;
import org.eclipse.scout.jaxws216.security.provider.IAuthenticationHandler;
import org.eclipse.scout.jaxws216.security.provider.ICredentialValidationStrategy;
import org.eclipse.scout.jaxws216.session.IServerSessionFactory;
import org.eclipse.scout.rt.server.IServerSession;

import com.sun.xml.internal.ws.api.handler.MessageHandler;
import com.sun.xml.internal.ws.api.handler.MessageHandlerContext;
import com.sun.xml.internal.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.internal.ws.api.pipe.ServerTubeAssemblerContext;
import com.sun.xml.internal.ws.api.pipe.Tube;
import com.sun.xml.internal.ws.api.pipe.TubelineAssembler;

/**
 * Tube line assembler which installs a security handler at runtime.
 */
public class ScoutTubelineAssembler implements TubelineAssembler {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScoutTubelineAssembler.class);

  @Override
  public Tube createClient(ClientTubeAssemblerContext context) {
    // wrap handlers with transactional context
    wrapScoutTransactionHandlers(context.getBinding());

    // assemble tubes
    Tube head = context.createTransportTube();
    head = context.createSecurityTube(head);
    head = context.createWsaTube(head);
    head = context.createClientMUTube(head);
    head = context.createValidationTube(head);
    return context.createHandlerTube(head);
  }

  @Override
  public Tube createServer(ServerTubeAssemblerContext context) {
    // Installs the authentication handler
    // This must precede wrapping handlers with a transactional context
    installAuthenticationHandler(context);

    // wrap handlers with transactional context
    wrapScoutTransactionHandlers(context.getEndpoint().getBinding());

    // assemble tubes
    Tube head = context.getTerminalTube();
    head = context.createValidationTube(head);
    head = context.createHandlerTube(head);
    head = context.createMonitoringTube(head);
    head = context.createServerMUTube(head);
    head = context.createWsaTube(head);
    head = context.createSecurityTube(head);
    return head;
  }

  private void wrapScoutTransactionHandlers(Binding binding) {
    List<Handler> handlerChain = new ArrayList<Handler>();

    for (Handler handler : binding.getHandlerChain()) {
      if (handler instanceof LogicalHandler ||
          handler instanceof SOAPHandler ||
          handler instanceof MessageHandler) {

        ScoutTransaction annotation = getAnnotation(handler.getClass(), ScoutTransaction.class);
        if (annotation != null) {
          handler = createScoutTransactionHandlerWrapper(handler, annotation);
        }
      }
      handlerChain.add(handler);
    }
    binding.setHandlerChain(handlerChain);
  }

  @SuppressWarnings("unchecked")
  private Handler createScoutTransactionHandlerWrapper(final Handler handler, final ScoutTransaction scoutTransaction) {
    if (scoutTransaction == null || handler == null) {
      return handler;
    }

    Subject subject = null;
    try {
      subject = Subject.getSubject(AccessController.getContext());
    }
    catch (Exception e) {
      LOG.error("Failed to get subject of calling acess context", e);
    }
    // in case of server tube, subject typically is null because assembler is called in bootstrap of JAX-WS
    if (subject == null) {
      String principalName = Activator.getDefault().getBundle().getBundleContext().getProperty(Activator.PROP_DEFAULT_PRINCIPAL);
      if (!StringUtility.hasText(principalName)) {
        LOG.warn("No subject found in calling AccessContext which is the expected behavor. That is why the principal 'anonymous' is registered with a new subject to create sessions for transactional handlers. This can be changed by configuring the prinicipal in '" + Activator.PROP_DEFAULT_PRINCIPAL + "' in config.ini.");
        principalName = "anonymous";
      }
      subject = new Subject();
      subject.getPrincipals().add(new SimplePrincipal(principalName));
      subject.setReadOnly();
    }

    // create session on behalf of the created subject
    IServerSession serverSession = null;
    try {
      serverSession = Subject.doAs(subject, new PrivilegedExceptionAction<IServerSession>() {
        @Override
        public IServerSession run() throws Exception {
          IServerSessionFactory sessionFactory = scoutTransaction.sessionFactory().newInstance();
          if (sessionFactory == null) {
            LOG.error("No session factory registered on hander '" + handler.getClass().getName() + "'. Handler is not run in transactional scope.");
            return null;
          }
          return sessionFactory.create();
        }
      });
    }
    catch (PrivilegedActionException e) {
      LOG.error("Failed to create session factory / session for hander '" + handler.getClass().getName() + "'. Handler is not run in transactional scope.", e.getException());
    }
    if (serverSession == null) {
      LOG.error("Session for handler '" + handler.getClass().getName() + "' nust not be null. Handler is not run in transactional scope.");
    }

    if (handler instanceof LogicalHandler) {
      return new ScoutTransactionLogicalHandlerWrapper<LogicalMessageContext>((LogicalHandler<LogicalMessageContext>) handler, serverSession);
    }
    else if (handler instanceof SOAPHandler) {
      return new ScoutTransactionSOAPHandlerWrapper<SOAPMessageContext>((SOAPHandler) handler, serverSession);
    }
    else if (handler instanceof MessageHandler) {
      return new ScoutTransactionMessageHandlerWrapper<MessageHandlerContext>((MessageHandler<MessageHandlerContext>) handler, serverSession);
    }
    else {
      LOG.warn("Unsupported handler type  '" + handler.getClass().getName() + "' for Scout transaction.");
      return handler;
    }
  }

  private void installAuthenticationHandler(ServerTubeAssemblerContext context) {
    IAuthenticationHandler authenticationHandler = createAuthenticationHandler(context);
    if (authenticationHandler == null) {
      return;
    }

    List<Handler> handlerChain = new LinkedList<Handler>();

    // add existing handlers to chain
    handlerChain.addAll(context.getEndpoint().getBinding().getHandlerChain());

    // add Scout security handler to chain
    handlerChain.add(authenticationHandler);

    // set handler chain
    context.getEndpoint().getBinding().setHandlerChain(handlerChain);
  }

  private IAuthenticationHandler createAuthenticationHandler(ServerTubeAssemblerContext context) {
    Class<?> wsImplClazz = context.getEndpoint().getImplementationClass();
    if (wsImplClazz == null) {
      return null;
    }

    ScoutWebService annotation = getAnnotation(wsImplClazz, ScoutWebService.class);
    if (annotation == null) {
      return null;
    }

    Class<? extends IAuthenticationHandler> authenticationHandlerClazz = annotation.authenticationHandler();
    if (authenticationHandlerClazz == null || authenticationHandlerClazz == IAuthenticationHandler.NONE.class) {
      return null;
    }

    IAuthenticationHandler authenticationHandler = null;
    try {
      authenticationHandler = authenticationHandlerClazz.newInstance();
    }
    catch (Throwable e) {
      LOG.error("Failed to create authentication handler '" + authenticationHandlerClazz.getName() + "'. No authentication is applied.", e);
      return null;
    }

    // inject credential validation strategy
    Class<? extends ICredentialValidationStrategy> strategyClazz = annotation.credentialValidationStrategy();
    if (strategyClazz == null) {
      return authenticationHandler;
    }

    ICredentialValidationStrategy strategy = null;
    try {
      strategy = strategyClazz.newInstance();
    }
    catch (Throwable e) {
      LOG.error("Failed to create credential validation strategy '" + strategyClazz.getName() + "' for authentication handler '" + authenticationHandler.getClass().getName() + "'.", e);
      return authenticationHandler;
    }

    // inject credential validation strategy
    try {
      authenticationHandler.injectCredentialValidationStrategy(strategy);
    }
    catch (Throwable e) {
      LOG.error("Failed to inject credential validation strategy to authentication handler '" + authenticationHandler.getClass().getName() + "'.", e);
      return authenticationHandler;
    }

    return authenticationHandler;
  }

  private <A extends Annotation> A getAnnotation(Class<?> type, Class<A> annotationClazz) {
    A annotation = type.getAnnotation(annotationClazz);
    if (annotation == null && type != Object.class) {
      return getAnnotation(type.getSuperclass(), annotationClazz);
    }
    return annotation;
  }
}
