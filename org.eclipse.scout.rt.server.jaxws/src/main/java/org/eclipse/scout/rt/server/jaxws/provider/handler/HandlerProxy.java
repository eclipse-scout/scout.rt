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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Resource;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContextProducer;
import org.eclipse.scout.rt.platform.context.RunWithRunContext;
import org.eclipse.scout.rt.platform.exception.ExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsHandlerSubjectProperty;
import org.eclipse.scout.rt.server.jaxws.MessageContexts;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.ClazzUtil;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.InitParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class proxies a JAX-WS handler to be installed on provider side, and adds the following functionality:
 * <ul>
 * <li>Adds support for 'init-params' to initialize the handler (not supported by JAX-WS specification).</li>
 * <li>Runs the handler's methods on behalf of a {@link RunContext} if annotated with {@link RunWithRunContext}
 * annotation.</li>
 * <li>Obtains the concrete handler instance from {@link IBeanManager}.</li>
 * </ul>
 *
 * @since 5.1
 */
public class HandlerProxy<CONTEXT extends MessageContext> implements Handler<CONTEXT> {

  private static final Logger LOG = LoggerFactory.getLogger(HandlerProxy.class);

  private static final Subject HANDLER_SUBJECT = CONFIG.getPropertyValue(JaxWsHandlerSubjectProperty.class);

  private final Handler<CONTEXT> m_handler;

  private final RunContextProducer m_handlerRunContextProducer;

  @SuppressWarnings("unchecked")
  public HandlerProxy(final org.eclipse.scout.rt.server.jaxws.provider.annotation.Handler handlerAnnotation) {
    m_handler = BEANS.get(ClazzUtil.resolve(handlerAnnotation.value(), Handler.class, "@Handler.value"));

    final RunWithRunContext runHandleWithRunContext = m_handler.getClass().getAnnotation(RunWithRunContext.class);
    m_handlerRunContextProducer = (runHandleWithRunContext != null ? BEANS.get(runHandleWithRunContext.value()) : null);

    injectInitParams(m_handler, toInitParamMap(handlerAnnotation.initParams()));
  }

  @Override
  public boolean handleMessage(final CONTEXT messageContext) {
    return handle(messageContext, "handleMessage", new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return m_handler.handleMessage(messageContext);
      }
    });
  }

  @Override
  public boolean handleFault(final CONTEXT messageContext) {
    return handle(messageContext, "handleFault", new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return m_handler.handleFault(messageContext);
      }
    });
  }

  @Override
  public void close(final MessageContext messageContext) {
    handle(messageContext, "close", new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        m_handler.close(messageContext);
        return null;
      }
    });
  }

  /**
   * Method invoked to optionally run the given {@link Callable} on behalf of a {@link RunContext}.
   */
  @Internal
  protected <T> T handle(final MessageContext messageContext, final String method, final Callable<T> callable) {
    try {
      if (m_handlerRunContextProducer == null) {
        return callable.call();
      }
      else {
        final Subject subject = MessageContexts.getSubject(messageContext, HANDLER_SUBJECT);
        return m_handlerRunContextProducer.produce(subject).call(new Callable<T>() {
          @Override
          public T call() throws Exception {
            return callable.call();
          }
        }, BEANS.get(ExceptionTranslator.class));
      }
    }
    catch (final Exception e) {
      LOG.error("Failed to handle message [handler={}, method={}, inbound={}]", m_handler.getClass().getName(), method, MessageContexts.isInboundMessage(messageContext), e);
      throw new HTTPException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // do not send cause to the client
    }
  }

  /**
   * @return original handler this handler delegates to.
   */
  protected Handler<CONTEXT> getHandlerDelegate() {
    return m_handler;
  }

  /**
   * Injects the given 'init-params' into the given handler.
   */
  @Internal
  protected void injectInitParams(final Handler<CONTEXT> handler, final Map<String, String> initParams) {
    for (final Field field : handler.getClass().getDeclaredFields()) {
      if (field.getAnnotation(Resource.class) != null && field.getType().isAssignableFrom(initParams.getClass())) {
        try {
          LOG.info("Inject 'initParams' to JAX-WS handler [path={}#{}]", handler.getClass().getName(), field.getName());
          field.setAccessible(true);
          field.set(handler, initParams);
          return;
        }
        catch (final ReflectiveOperationException e) {
          throw new PlatformException(String.format("Failed to inject 'InitParams' for handler '%s'", handler.getClass().getName()), e);
        }
      }
    }

    if (!initParams.isEmpty()) {
      LOG.warn("'InitParams' could not be injected into handler because no field found that is of type Map<String, String> and annotated with @Resource [handler={}, initParams={}]", handler.getClass().getName(), initParams);
    }
  }

  @Internal
  protected Map<String, String> toInitParamMap(final InitParam[] initParams) {
    final Map<String, String> map = new HashMap<>(initParams.length);
    for (final InitParam initParam : initParams) {
      map.put(initParam.key(), initParam.value());
    }
    return map;
  }
}
