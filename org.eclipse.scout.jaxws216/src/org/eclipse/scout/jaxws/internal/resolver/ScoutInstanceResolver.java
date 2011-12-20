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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.ObjectHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.jaxws.annotation.ScoutWebService;
import org.eclipse.scout.jaxws.internal.ContextHelper;
import org.eclipse.scout.jaxws.internal.SessionHelper;
import org.eclipse.scout.jaxws.session.IServerSessionFactory;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJob;

import com.sun.xml.internal.ws.api.message.Packet;
import com.sun.xml.internal.ws.api.server.InstanceResolver;
import com.sun.xml.internal.ws.api.server.Invoker;
import com.sun.xml.internal.ws.api.server.WSEndpoint;
import com.sun.xml.internal.ws.api.server.WSWebServiceContext;

/**
 * This resolver intercepts webservice requests prior to being propagated to the webservice implementation in order to
 * bind the call to a context.
 */
public class ScoutInstanceResolver extends InstanceResolver<Object> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScoutInstanceResolver.class);

  private Class<?> m_portTypeClass;
  private IServerSessionFactory m_sessionFactory;

  public ScoutInstanceResolver(Class portTypeClass) {
    m_portTypeClass = portTypeClass;
    if (m_portTypeClass == null) {
      throw new WebServiceException("No port type class configured in sun-jaxws.xml");
    }

    ScoutWebService annotation = m_portTypeClass.getAnnotation(ScoutWebService.class);
    if (annotation == null) {
      return;
    }
    try {
      m_sessionFactory = annotation.sessionFactory().newInstance();
    }
    catch (Exception e) {
      LOG.error("Error occured while creating session factory.", e);
    }
  }

  @Override
  public Object resolve(Packet packet) {
    try {
      return m_portTypeClass.newInstance();
    }
    catch (Exception e) {
      LOG.error("Could not create instance of port type '" + m_portTypeClass.getName() + "'");
    }
    return null;
  }

  @Override
  public Invoker createInvoker() {
    return new P_Invoker();
  }

  private class P_Invoker extends Invoker {

    private WSWebServiceContext m_serviceContext;

    @Override
    public void start(WSWebServiceContext serviceContext, WSEndpoint endpoint) {
      m_serviceContext = serviceContext;
    }

    @Override
    public Object invoke(final Packet packet, final Method method, final Object... aobj) throws InvocationTargetException, IllegalAccessException {
      final Object portType = resolve(packet);
      if (portType == null) {
        throw new WebServiceException("No port type found");
      }

      Subject subject = null;
      try {
        subject = Subject.getSubject(AccessController.getContext());
      }
      catch (Exception e) {
        LOG.error("Failed to get subject of calling access context", e);
      }
      if (subject == null) {
        throw new WebServiceException("Webservice request was NOT dispatched due to security reasons: request must run on behalf of a subject context.");
      }

      IServerSession serverSession = null;
      if (m_sessionFactory != null) {
        MessageContext context = m_serviceContext.getMessageContext();
        // Prefer cached session over creating a new one.
        // However, the session is only considered if created by the same type of factory.
        // This is to ensure a proper session context which is what the user is expecting.
        IServerSession contextSession = ContextHelper.getContextSession(context);
        Class<? extends IServerSessionFactory> contextSessionFactory = ContextHelper.getContextSessionFactoryClass(context);
        if (contextSession == null || !CompareUtility.equals(m_sessionFactory.getClass(), contextSessionFactory)) {
          // create a new session
          serverSession = SessionHelper.createNewServerSession(m_sessionFactory);
        }
        else {
          // cached session
          serverSession = contextSession;
        }
      }
      if (serverSession == null) {
        LOG.warn("Webservice request is not run in a session context as no server session is configured.");
        return method.invoke(portType, aobj);
      }

      try {
        final ObjectHolder resultHolder = new ObjectHolder();
        final Holder<InvocationTargetException> invocationTargetExceptionHolder = new Holder<InvocationTargetException>(InvocationTargetException.class);
        final Holder<IllegalAccessException> illegalAccessExceptionHolder = new Holder<IllegalAccessException>(IllegalAccessException.class);
        final Holder<RuntimeException> runtimeExceptionHolder = new Holder<RuntimeException>(RuntimeException.class);
        // run server job
        ServerJob serverJob = new ServerJob("Server Job", serverSession, subject) {

          @Override
          protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
            try {
              resultHolder.setValue(method.invoke(portType, aobj));
            }
            catch (InvocationTargetException e) {
              LOG.error("Failed to dispatch webservice request.", e);
              invocationTargetExceptionHolder.setValue(e);
            }
            catch (IllegalAccessException e) {
              LOG.error("Illegal access exception occured while dispatching webservice request. This might be caused because of Java security settings.", e);
              illegalAccessExceptionHolder.setValue(e);
            }
            catch (RuntimeException e) {
              LOG.warn("webservice processing exception occured. Please handle faults by respective SOAP faults.", e);
              runtimeExceptionHolder.setValue(e);
            }

            return Status.OK_STATUS;
          }
        };
        serverJob.setSystem(true);
        serverJob.runNow(new NullProgressMonitor());
        if (invocationTargetExceptionHolder.getValue() != null) {
          throw invocationTargetExceptionHolder.getValue();
        }
        if (illegalAccessExceptionHolder.getValue() != null) {
          throw illegalAccessExceptionHolder.getValue();
        }
        if (runtimeExceptionHolder.getValue() != null) {
          throw runtimeExceptionHolder.getValue();
        }
        return resultHolder.getValue();
      }
      finally {
        postInvoke(packet, portType);
      }
    }
  }
}
