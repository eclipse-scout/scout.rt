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
package org.eclipse.scout.jaxws.internal.tube;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.jaxws.annotation.ScoutTransaction;
import org.eclipse.scout.jaxws.annotation.ScoutWebService;
import org.eclipse.scout.jaxws.handler.internal.ScoutTransactionLogicalHandlerWrapper;
import org.eclipse.scout.jaxws.handler.internal.ScoutTransactionMessageHandlerWrapper;
import org.eclipse.scout.jaxws.handler.internal.ScoutTransactionSOAPHandlerWrapper;
import org.eclipse.scout.jaxws.internal.ContextHelper;
import org.eclipse.scout.jaxws.security.provider.IAuthenticationHandler;
import org.eclipse.scout.jaxws.security.provider.ICredentialValidationStrategy;
import org.eclipse.scout.jaxws.session.IServerSessionFactory;

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
    installServerAuthenticationHandler(context);

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

    if (handler instanceof LogicalHandler) {
      return new ScoutTransactionLogicalHandlerWrapper<LogicalMessageContext>((LogicalHandler<LogicalMessageContext>) handler, scoutTransaction);
    }
    else if (handler instanceof SOAPHandler) {
      return new ScoutTransactionSOAPHandlerWrapper<SOAPMessageContext>((SOAPHandler) handler, scoutTransaction);
    }
    else if (handler instanceof MessageHandler) {
      return new ScoutTransactionMessageHandlerWrapper<MessageHandlerContext>((MessageHandler<MessageHandlerContext>) handler, scoutTransaction);
    }
    else {
      LOG.warn("Unsupported handler type  '" + handler.getClass().getName() + "' for Scout transaction.");
      return handler;
    }
  }

  private void installServerAuthenticationHandler(ServerTubeAssemblerContext context) {
    List<Handler> handlerChain = new LinkedList<Handler>();

    // install existing handlers
    handlerChain.addAll(context.getEndpoint().getBinding().getHandlerChain());

    // install authentication handler
    IAuthenticationHandler authenticationHandler = createAuthenticationHandler(context);
    if (authenticationHandler != null) {
      handlerChain.add(authenticationHandler);
    }

    // install handler to put the session factory configured on the port type into the runnning context.
    // This handler must be installed prior to authentication handlers
    Class<?> portTypeClass = context.getEndpoint().getImplementationClass();
    try {
      if (portTypeClass != null) {
        ScoutWebService scoutWebService = portTypeClass.getAnnotation(ScoutWebService.class);
        if (scoutWebService != null) {
          handlerChain.add(new P_PortTypeSessionFactoryRegistrationHandler(scoutWebService));
        }
      }
    }
    catch (Exception e) {
      LOG.error("failed to install handler to register configured port type factory in running context", e);
    }

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

  /**
   * Handler used to store the session factory configured on the port type in the calling context.
   * This must be the first handler installed.
   */
  private class P_PortTypeSessionFactoryRegistrationHandler implements SOAPHandler<SOAPMessageContext> {

    private ScoutWebService m_scoutWebServiceAnnotation;

    private P_PortTypeSessionFactoryRegistrationHandler(ScoutWebService scoutWebServiceAnnotation) {
      m_scoutWebServiceAnnotation = scoutWebServiceAnnotation;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
      boolean outbound = TypeCastUtility.castValue(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY), boolean.class);
      if (outbound) {
        return true; // only inbound messages are of interest
      }
      try {
        // get session factory configured on port type
        IServerSessionFactory portTypeSessionFactory = m_scoutWebServiceAnnotation.sessionFactory().newInstance();
        // store session factory in running context
        ContextHelper.setPortTypeSessionFactory(context, portTypeSessionFactory);
      }
      catch (Exception e) {
        LOG.error("Failed to put port type session factory into the running context", e);
      }
      return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
      return true;
    }

    @Override
    public void close(MessageContext context) {
    }

    @Override
    public Set<QName> getHeaders() {
      return new HashSet<QName>();
    }
  }
}
