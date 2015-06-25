/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.commons.servlet.filter.authentication;

import java.io.IOException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.Bean;

/**
 * @since 5.0
 */
@Bean
public class ServletFilterHelper {
  public static final String SESSION_ATTRIBUTE_FOR_PRINCIPAL = Principal.class.getName();
  public static final String SESSION_ATTRIBUTE_FOR_PRINCIPAL_TIMESTAMP = SESSION_ATTRIBUTE_FOR_PRINCIPAL + ".timestamp";

  public Subject createSubject(Principal principal) {
    // create subject if necessary
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null || subject.isReadOnly()) {
      subject = new Subject();
    }
    subject.getPrincipals().add(principal);
    subject.setReadOnly();
    return subject;
  }

  public void continueChainAsSubject(final Principal principal, final HttpServletRequest req, final HttpServletResponse res, final FilterChain chain) throws IOException, ServletException {
    try {
      Subject.doAs(
          createSubject(principal),
          new PrivilegedExceptionAction<Object>() {
            @Override
            public Object run() throws Exception {
              HttpServletRequest secureReq = new SecureHttpServletRequestWrapper(req, principal);
              chain.doFilter(secureReq, res);
              return null;
            }
          }
          );
    }
    catch (PrivilegedActionException e) {
      Throwable t = e.getCause();
      if (t instanceof IOException) {
        throw (IOException) t;
      }
      else if (t instanceof ServletException) {
        throw (ServletException) t;
      }
      else {
        throw new ServletException(t);
      }
    }
  }

}
