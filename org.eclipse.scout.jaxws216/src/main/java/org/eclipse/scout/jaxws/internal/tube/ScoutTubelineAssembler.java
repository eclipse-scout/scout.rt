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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ReflectionUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.ICallable;
import org.eclipse.scout.jaxws.annotation.ScoutTransaction;
import org.eclipse.scout.jaxws.annotation.ScoutWebService;
import org.eclipse.scout.jaxws.internal.JaxWsHelper;
import org.eclipse.scout.jaxws.security.provider.IAuthenticationHandler;
import org.eclipse.scout.jaxws.security.provider.IAuthenticator;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.job.IServerJobManager;
import org.eclipse.scout.rt.server.job.ServerJobInput;

import com.sun.xml.internal.ws.api.WSBinding;
import com.sun.xml.internal.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.internal.ws.api.pipe.ServerTubeAssemblerContext;
import com.sun.xml.internal.ws.api.pipe.Tube;
import com.sun.xml.internal.ws.api.pipe.TubelineAssembler;
import com.sun.xml.internal.ws.api.server.WSEndpoint;

/**
 * Tube line assembler which installs an authentication handler and proxies transactional handlers to run in a server
 * job.
 */
@SuppressWarnings("restriction")
public class ScoutTubelineAssembler implements TubelineAssembler {

  private static final Set<Method> TRANSACTIONAL_HANDLER_METHODS = CollectionUtility.hashSet(javax.xml.ws.handler.Handler.class.getDeclaredMethods());

  @Override
  public Tube createClient(final ClientTubeAssemblerContext context) {
    // proxy transactional handler to run in a separate transaction.
    final WSBinding binding = context.getBinding();
    binding.setHandlerChain(interceptHandlers(binding.getHandlerChain()));

    // assemble tubes
    Tube head = context.createTransportTube();
    head = context.createSecurityTube(head);
    head = context.createWsaTube(head);
    head = context.createClientMUTube(head);
    head = context.createValidationTube(head);
    return context.createHandlerTube(head);
  }

  @Override
  public Tube createServer(final ServerTubeAssemblerContext context) {
    // install authentication handler
    installAuthenticationHandler(context);

    // proxy transactional handler to run in a separate transaction.
    final WSBinding binding = context.getEndpoint().getBinding();
    binding.setHandlerChain(interceptHandlers(binding.getHandlerChain()));

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

  /**
   * Installs the authentication handler as very first handler.
   */
  @Internal
  protected void installAuthenticationHandler(final ServerTubeAssemblerContext context) {
    final WSEndpoint endpoint = context.getEndpoint();

    final List<Handler> handlerChain = new LinkedList<Handler>();

    // install authentication handler as very first handler.
    handlerChain.add(createAuthenticationHandler(endpoint.getImplementationClass()));

    // install existing handlers.
    handlerChain.addAll(endpoint.getBinding().getHandlerChain());

    // register the handler chain.
    endpoint.getBinding().setHandlerChain(handlerChain);
  }

  /**
   * Method invoked to change the handlers to be installed.<br/>
   * The default implementation proxies transactional handlers to run on behalf of a new server job.
   */
  @Internal
  protected List<Handler> interceptHandlers(final List<Handler> handlers) {
    for (int i = 0; i < handlers.size(); i++) {
      final Handler handler = handlers.get(i);
      final ScoutTransaction transactionalAnnotation = handler.getClass().getAnnotation(ScoutTransaction.class);
      if (transactionalAnnotation != null) {
        handlers.set(i, proxyTransactionalHandler(handler));
      }
    }
    return handlers;
  }

  /**
   * Method invoked to create a transactional proxy for the given {@link Handler}.
   */
  @Internal
  protected Handler proxyTransactionalHandler(final Handler handler) {
    return (Handler) Proxy.newProxyInstance(handler.getClass().getClassLoader(), ReflectionUtility.getInterfaces(handler.getClass()), new InvocationHandler() {

      @Override
      public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (TRANSACTIONAL_HANDLER_METHODS.contains(method)) {
          Assertions.assertTrue(args.length == 1 && args[0] instanceof MessageContext, "wrong method signature: %s argument expected. [handler=%s, method=%s]", MessageContext.class.getSimpleName(), handler.getClass().getName(), method.getName());

          final MessageContext messageContext = Assertions.assertNotNull((MessageContext) args[0], "message context must not be null");
          final IServerSession serverSession = Assertions.assertNotNull(JaxWsHelper.getContextSession(messageContext), "Missig server-session on message context [messageContext=%s]", messageContext);

          return ScoutTubelineAssembler.this.invokeInServerJob(ServerJobInput.defaults().name("JAX-WS TX-Handler").session(serverSession), handler, method, args);
        }
        else {
          return method.invoke(handler, args);
        }
      }
    });
  }

  /**
   * Method invoked to create the {@link IAuthenticationHandler} to be installed.
   *
   * @return {@link IAuthenticationHandler}; must not be <code>null</code>.
   */
  @Internal
  protected IAuthenticationHandler createAuthenticationHandler(final Class<?> portTypeClasss) {
    final Class<?> portTypeClass = Assertions.assertNotNull(portTypeClasss, "port-type class not found");

    // Find the 'ScoutWebService' annotation on the port type.
    final ScoutWebService annotation = portTypeClass.getAnnotation(ScoutWebService.class);
    if (annotation == null) {
      return IAuthenticationHandler.None.INSTANCE;
    }

    // Instantiate configured authentication handler with its authenticator.
    final Class<? extends IAuthenticationHandler> authHandlerClass = annotation.authenticationHandler();
    if (authHandlerClass == null || authHandlerClass == IAuthenticationHandler.None.class) {
      return IAuthenticationHandler.None.INSTANCE;
    }

    try {
      final Constructor<?> constructor = ReflectionUtility.getConstructor(authHandlerClass, new Class<?>[]{IAuthenticator.class});
      if (constructor != null) {
        return (IAuthenticationHandler) constructor.newInstance(createAuthenticator(annotation.authenticator()));
      }
      else {
        return authHandlerClass.newInstance();
      }
    }
    catch (final ReflectiveOperationException e) {
      throw new WebServiceException(String.format("Failed to instantiate authentication handler [handler=%s, annotation=%s]", authHandlerClass.getName(), annotation), e);
    }
  }

  /**
   * Method invoked to create the {@link IAuthenticator} to be installed.
   *
   * @return {@link IAuthenticator}; must not be <code>null</code>.
   */
  @Internal
  protected IAuthenticator createAuthenticator(final Class<? extends IAuthenticator> authenticatorClass) {
    if (authenticatorClass == null || authenticatorClass == IAuthenticator.AcceptAnyAuthenticator.class) {
      return IAuthenticator.AcceptAnyAuthenticator.INSTANCE;
    }
    else {
      return OBJ.one(authenticatorClass);
    }
  }

  /**
   * Method invoked to run the given method on behalf of a new server job.
   */
  @Internal
  protected Object invokeInServerJob(final ServerJobInput input, final Object object, final Method method, final Object[] args) throws Throwable {
    try {
      return OBJ.one(IServerJobManager.class).runNow(new ICallable<Object>() {

        @Override
        public Object call() throws Exception {
          try {
            return method.invoke(object, args); // InvocationTargetException is unpacked in server-job.
          }
          catch (ReflectiveOperationException e) {
            throw new WebServiceException("Failed to invoke proxy method", e);
          }
        }
      }, input);
    }
    catch (final ProcessingException e) {
      throw e.getCause(); // propagate the real cause.
    }
  }
}
