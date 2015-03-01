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
package org.eclipse.scout.jaxws.internal.resolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;

import javax.security.auth.Subject;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.ICallable;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.jaxws.annotation.ScoutWebService;
import org.eclipse.scout.jaxws.internal.ContextHelper;
import org.eclipse.scout.jaxws.internal.SessionHelper;
import org.eclipse.scout.jaxws.session.IServerSessionFactory;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.job.internal.ServerJobManager;

import com.sun.xml.internal.ws.api.message.Packet;
import com.sun.xml.internal.ws.api.server.Invoker;
import com.sun.xml.internal.ws.api.server.WSEndpoint;
import com.sun.xml.internal.ws.api.server.WSWebServiceContext;
import com.sun.xml.internal.ws.server.AbstractMultiInstanceResolver;

/**
 * This resolver intercepts webservice requests prior to being propagated to the port type in order to
 * bind the call to a {@link IServerSession} context.
 */
@SuppressWarnings("restriction")
public class ScoutInstanceResolver<T> extends AbstractMultiInstanceResolver<T> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScoutInstanceResolver.class);

  private IServerSessionFactory m_sessionFactory;

  public ScoutInstanceResolver(Class<T> portTypeClass) {
    super(portTypeClass);
    if (portTypeClass == null) {
      throw new WebServiceException("No port type class configured in sun-jaxws.xml");
    }
  }

  @Override
  public void start(WSWebServiceContext context, WSEndpoint endpoint) {
    m_sessionFactory = createSessionFactory(clazz);
    super.start(context, endpoint);
  }

  @Override
  public T resolve(Packet packet) {
    return super.create(); // creates a new port type instance, injects the @{WebServiceContext} and invokes the method annotated with @{link PostConstruct}
  }

  protected IServerSessionFactory createSessionFactory(Class<?> portTypeClass) {
    ScoutWebService annotation = portTypeClass.getAnnotation(ScoutWebService.class);
    if (annotation == null) {
      return null;
    }
    try {
      return annotation.sessionFactory().newInstance();
    }
    catch (Exception e) {
      LOG.error("Error occured while creating session factory.", e);
    }
    return null;
  }

  protected IServerSession getSession(MessageContext context) {
    if (m_sessionFactory == null) {
      return null;
    }
    // Prefer cached session over creating a new one.
    // However, the session is only considered if created by the same type of factory.
    // This is to ensure a proper session context which is what the user is expecting.
    IServerSession contextSession = ContextHelper.getContextSession(context);
    Class<? extends IServerSessionFactory> contextSessionFactory = ContextHelper.getContextSessionFactoryClass(context);
    if (contextSession == null || !CompareUtility.equals(m_sessionFactory.getClass(), contextSessionFactory)) {
      // create a new session
      return SessionHelper.createNewServerSession(m_sessionFactory);
    }
    // cached session
    return contextSession;
  }

  @Override
  public Invoker createInvoker() {
    return new P_Invoker();
  }

  protected class P_Invoker extends Invoker {

    protected WSWebServiceContext m_context;

    @Override
    public void start(final WSWebServiceContext context, final WSEndpoint endpoint) {
      m_context = context;
      ScoutInstanceResolver.this.start(context, endpoint);
    }

    @Override
    public void dispose() {
      m_context = null;
      ScoutInstanceResolver.this.dispose();
    }

    @Override
    public Object invoke(final Packet packet, final Method method, final Object... args) throws InvocationTargetException, IllegalAccessException {
      T portType = Assertions.assertNotNull(ScoutInstanceResolver.this.resolve(packet), "no port type found");
      Subject subject = Assertions.assertNotNull(Subject.getSubject(AccessController.getContext()), "Webservice request was not dispatched due to security reasons: request must run on behalf of a subject context.");
      IServerSession session = getSession(m_context.getMessageContext());

      try {
        return invokePortTypeMethodInServerJob(subject, session, portType, method, args);
      }
      catch (ProcessingException e) {
        Throwable cause = e.getCause();

        if (cause instanceof InvocationTargetException) {
          throw (InvocationTargetException) cause;
        }
        else if (cause instanceof IllegalAccessException) {
          throw (IllegalAccessException) cause;
        }
        else if (cause instanceof RuntimeException) {
          throw (RuntimeException) cause;
        }
        else {
          throw new WebServiceException("Unexpected error while dispatching webservice request.", cause);
        }
      }
      finally {
        postInvoke(packet, portType);
      }
    }

    protected Object invokePortTypeMethodInServerJob(Subject subject, final IServerSession session, final T portType, final Method method, final Object... args) throws ProcessingException {
      if (session == null) {
        LOG.warn("Webservice request is not run in a session context as no server session is configured.");
      }

      return ServerJobManager.DEFAULT.runNow(new ICallable<Object>() {

        @Override
        public Object call() throws Exception {
          return method.invoke(portType, args);
        }
      }, ServerJobInput.defaults().session(session).sessionRequired(false).subject(subject));
    }
  }
}
