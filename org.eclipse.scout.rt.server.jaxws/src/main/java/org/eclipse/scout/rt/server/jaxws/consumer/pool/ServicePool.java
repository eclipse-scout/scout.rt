/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.consumer.pool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.PortInfo;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContextProducer;
import org.eclipse.scout.rt.platform.context.RunWithRunContext;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.server.jaxws.consumer.IPortProvider.IPortInitializer;

/**
 * Non-blocking, unlimited pool of JAX-WS service instances (which are used in turn for creating JAX-WS ports). Pooled
 * entries are discarded 12 hours after they have been created.
 *
 * @since 6.0.300
 */
public class ServicePool<SERVICE extends Service> extends AbstractNonBlockingPool<SERVICE> {

  protected static final Set<Method> PROXIED_HANDLER_METHODS = CollectionUtility.hashSet(Handler.class.getDeclaredMethods()); // only methods declared directly on the handler are proxied.

  protected final Class<SERVICE> m_serviceClazz;
  protected final String m_serviceName;
  protected final URL m_wsdlLocation;
  protected final String m_targetNamespace;
  protected final IPortInitializer m_initializer;

  public ServicePool(final Class<SERVICE> serviceClazz, final String serviceName, final URL wsdlLocation, final String targetNamespace, final IPortInitializer initializer) {
    super(12, TimeUnit.HOURS);
    m_serviceClazz = serviceClazz;
    m_serviceName = serviceName;
    m_wsdlLocation = wsdlLocation;
    m_targetNamespace = targetNamespace;
    m_initializer = initializer;
  }

  @Override
  protected SERVICE createElement() {
    try {
      // Create the service
      final Constructor<? extends Service> constructor = m_serviceClazz.getConstructor(URL.class, QName.class);
      @SuppressWarnings("unchecked")
      final SERVICE service = (SERVICE) constructor.newInstance(m_wsdlLocation, new QName(m_targetNamespace, m_serviceName));

      // Install the handler chain
      service.setHandlerResolver(new HandlerResolver() {

        @Override
        public List<Handler> getHandlerChain(final PortInfo portInfo) {
          final List<Handler<? extends MessageContext>> handlerChain = new ArrayList<>();
          m_initializer.initHandlers(handlerChain);

          for (int i = 0; i < handlerChain.size(); i++) {
            handlerChain.set(i, proxyHandler(handlerChain.get(i)));
          }

          @SuppressWarnings("unchecked")
          final List<Handler> handlers = TypeCastUtility.castValue(handlerChain, List.class);
          return handlers;
        }
      });

      return service;
    }
    catch (ReflectiveOperationException e) {
      throw new WebServiceException("Failed to instantiate webservice stub.", e);
    }
  }

  @Override
  protected boolean resetElement(SERVICE element) {
    return true;
  }

  /**
   * Proxies the given {@link Handler} to run on behalf of a {@link RunContext}, if the handler is annotated with
   * {@link RunWithRunContext}.
   */
  protected Handler<? extends MessageContext> proxyHandler(final Handler<? extends MessageContext> handler) {
    final RunWithRunContext handleWithRunContext = handler.getClass().getAnnotation(RunWithRunContext.class);
    if (handleWithRunContext == null) {
      return handler;
    }

    final RunContextProducer runContextProducer = BEANS.get(handleWithRunContext.value());
    return (Handler<?>) Proxy.newProxyInstance(handler.getClass().getClassLoader(), handler.getClass().getInterfaces(), new InvocationHandler() {

      @Override
      public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (PROXIED_HANDLER_METHODS.contains(method)) {
          return runContextProducer.produce(Subject.getSubject(AccessController.getContext())).call(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
              return method.invoke(handler, args);
            }
          }, DefaultExceptionTranslator.class);
        }
        else {
          return method.invoke(handler, args);
        }
      }
    });
  }
}
