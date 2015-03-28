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

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.jaxws.internal.JaxWsHelper;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;

import com.sun.xml.internal.ws.api.message.Packet;
import com.sun.xml.internal.ws.api.server.Invoker;
import com.sun.xml.internal.ws.api.server.WSEndpoint;
import com.sun.xml.internal.ws.api.server.WSWebServiceContext;
import com.sun.xml.internal.ws.server.AbstractMultiInstanceResolver;

/**
 * This resolver intercepts webservice requests to be run in a server job.
 */
@SuppressWarnings("restriction")
public class ScoutInstanceResolver<T> extends AbstractMultiInstanceResolver<T> {

  public ScoutInstanceResolver(Class<T> portTypeClass) {
    super(Assertions.assertNotNull(portTypeClass, "No port type class configured in sun-jaxws.xml"));
  }

  @Override
  public T resolve(Packet packet) {
    return super.create(); // creates a new port type instance, injects the @{WebServiceContext} and invokes the method annotated with @{link PostConstruct}
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
      final T portType = Assertions.assertNotNull(ScoutInstanceResolver.this.resolve(packet), "port-type not found");
      final Subject subject = Assertions.assertNotNull(Subject.getSubject(AccessController.getContext()), "subject must not be null.");
      final IServerSession serverSession = Assertions.assertNotNull(JaxWsHelper.getContextSession(m_context.getMessageContext()), "server-session must not be null");

      try {
        return invokePortTypeMethod(ServerRunContexts.copyCurrent().session(serverSession).subject(subject), portType, method, args);
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
          throw new WebServiceException("Unexpected error while processing webservice request.", cause);
        }
      }
      finally {
        postInvoke(packet, portType);
      }
    }

    /**
     * Method invoked to invoke the port-type method on behalf of the given <code>RunContext</code>.
     */
    protected Object invokePortTypeMethod(ServerRunContext runContext, final T portType, final Method method, final Object... args) throws ProcessingException {
      return runContext.call(new ICallable<Object>() {

        @Override
        public Object call() throws Exception {
          return method.invoke(portType, args);
        }
      });
    }
  }
}
