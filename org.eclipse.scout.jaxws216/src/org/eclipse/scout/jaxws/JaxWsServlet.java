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
package org.eclipse.scout.jaxws;

import java.io.IOException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.scout.http.servletfilter.ServletFilterDelegate;
import org.eclipse.scout.jaxws.internal.servlet.EndpointServlet;
import org.eclipse.scout.jaxws.security.provider.IAuthenticationHandler;

/**
 * Runs the request in a Subject context.
 * Soap handlers may add {@link Principal}'s to {@link Subject#getSubject(java.security.AccessControlContext)}
 * 
 * @see {@link Subject#doAs(Subject, java.security.PrivilegedAction)}.
 * @see {@link IAuthenticationHandler}.
 */
public class JaxWsServlet extends EndpointServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public final void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
    new ServletFilterDelegate().delegateServiceMethod(req, res, new ServletFilterDelegate.IServiceCallback() {
      @Override
      public void service(final ServletRequest reqInner, final ServletResponse resInner) throws ServletException, IOException {
        /*
         * The request can be authenticated in two ways:
         * 1. By the application server itself or a previous filter.
         *    In such situation, we are already running in a respective doAs call which implies that the subject obtained
         *    is not null and contains one principal at minimum.
         * 2. By a subsequent {@link IAuthenticationHandler} handler.
         *    This is if the subject is null or there is no principal associated yet.
         *    If the current subject is null or readonly, a new one is created.
         */
        Subject subject = Subject.getSubject(AccessController.getContext());
        if (subject == null || subject.getPrincipals().size() == 0) {
          // request is not authenticated yet
          if (subject == null || subject.isReadOnly()) {
            subject = new Subject(); // precondition that subject is set
          }
          try {
            Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
              @Override
              public Object run() throws Exception {
                JaxWsServlet.super.service(reqInner, resInner);
                return null;
              }
            });
          }
          catch (PrivilegedActionException e) {
            if (e.getCause() instanceof ServletException) {
              throw (ServletException) e.getCause();
            }
            else if (e.getCause() instanceof IOException) {
              throw (IOException) e.getCause();
            }
            else {
              throw new IOException("Wrapped", e);
            }
          }
        }
        else {
          // request is already authenticated
          JaxWsServlet.super.service(reqInner, resInner);
        }
      }

      @Override
      public ServletContext getServletContext() {
        return JaxWsServlet.this.getServletContext();
      }
    });
  }
}
