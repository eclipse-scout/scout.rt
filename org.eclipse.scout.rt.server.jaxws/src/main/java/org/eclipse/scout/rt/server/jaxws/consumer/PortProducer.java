/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.consumer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.MessageContext;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContextProducer;
import org.eclipse.scout.rt.platform.context.RunWithRunContext;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * Factory for new Port objects to interact with a webservice endpoint.
 *
 * @since 5.1
 */
public class PortProducer<SERVICE extends Service, PORT> implements IPortProvider<PORT> {

  protected static final Set<Method> PROXIED_HANDLER_METHODS = CollectionUtility.hashSet(Handler.class.getDeclaredMethods()); // only methods declared directly on the handler are proxied.

  protected final Class<SERVICE> m_serviceClazz;
  protected final Class<PORT> m_portTypeClazz;
  protected final String m_serviceName;
  protected final URL m_wsdlLocation;
  protected final String m_targetNamespace;
  protected final IPortInitializer m_initializer;

  public PortProducer(final Class<SERVICE> serviceClazz, final Class<PORT> portTypeClazz, final String serviceName, final URL wsdlLocation, final String targetNamespace, final IPortInitializer initializer) {
    m_serviceClazz = serviceClazz;
    m_portTypeClazz = portTypeClazz;
    m_serviceName = serviceName;
    m_wsdlLocation = wsdlLocation;
    m_targetNamespace = targetNamespace;
    m_initializer = initializer;
  }

  /**
   * Creates a new Port to interact with the webservice endpoint.
   */
  @Override
  public PORT provide() {
    try {
      // Create the service
      final Constructor<? extends Service> constructor = m_serviceClazz.getConstructor(URL.class, QName.class);
      @SuppressWarnings("unchecked")
      final SERVICE service = (SERVICE) constructor.newInstance(m_wsdlLocation, new QName(m_targetNamespace, m_serviceName));

      // Install the handler chain
      service.setHandlerResolver(portInfo -> {
        final List<Handler<? extends MessageContext>> handlerChain = new ArrayList<>();
        m_initializer.initHandlers(handlerChain);

        for (int i = 0; i < handlerChain.size(); i++) {
          handlerChain.set(i, proxyHandler(handlerChain.get(i)));
        }

        @SuppressWarnings("unchecked")
        final List<Handler> handlers = TypeCastUtility.castValue(handlerChain, List.class);
        return handlers;
      });

      // Install implementor specific webservice features
      final List<WebServiceFeature> webServiceFeatures = new ArrayList<>();
      m_initializer.initWebServiceFeatures(webServiceFeatures);

      // Create the port
      return service.getPort(m_portTypeClazz, CollectionUtility.toArray(webServiceFeatures, WebServiceFeature.class));
    }
    catch (final ReflectiveOperationException e) {
      throw new WebServiceException("Failed to instantiate webservice stub.", e);
    }
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
    return (Handler<?>) Proxy.newProxyInstance(handler.getClass().getClassLoader(), handler.getClass().getInterfaces(), (proxy, method, args) -> {
      if (PROXIED_HANDLER_METHODS.contains(method)) {
        return runContextProducer.produce(Subject.getSubject(AccessController.getContext())).call(() -> method.invoke(handler, args), DefaultExceptionTranslator.class);
      }
      else {
        return method.invoke(handler, args);
      }
    });
  }
}
