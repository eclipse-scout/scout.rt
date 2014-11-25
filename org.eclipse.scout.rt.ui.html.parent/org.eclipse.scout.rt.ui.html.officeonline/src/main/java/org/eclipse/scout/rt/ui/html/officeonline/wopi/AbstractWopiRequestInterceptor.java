/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.officeonline.wopi;

import java.io.IOException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.TTLCache;
import org.eclipse.scout.rt.ui.html.json.servlet.AbstractJsonServlet;
import org.eclipse.scout.rt.ui.html.json.servlet.IServletRequestInterceptor;
import org.eclipse.scout.service.AbstractService;

/**
 * Handler inside the {@link AbstractJsonServlet} for the urls /wopi/files/* according to the MS WOPI protocol (not
 * supporting Cobalt)
 * <p>
 * The url /wopi/files/* should be open and is protected by the wopi-token in the url
 * <p>
 * Typical URLs have the form
 * 
 * <pre>
 * http://localhost:8080/.../wopi/files/fileid123?access_token=1&access_token_ttl=0
 * http://localhost:8080/.../wopi/files/fileid123/contents?access_token=1&access_token_ttl=0
 * </pre>
 */
public abstract class AbstractWopiRequestInterceptor extends AbstractService implements IServletRequestInterceptor, IWopiContentProvider {
  private static final long serialVersionUID = 1L;

  //locks must expire after 30 minutes
  private final TTLCache<String/*fileId*/, String/*lockId*/> m_fileLocks = new TTLCache<String, String>(30L * 60L * 1000L);

  public AbstractWopiRequestInterceptor() {
  }

  @Override
  public boolean interceptGet(AbstractJsonServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    return serviceWopiFile(servlet, req, resp);
  }

  @Override
  public boolean interceptPost(AbstractJsonServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    return serviceWopiFile(servlet, req, resp);
  }

  protected boolean serviceWopiFile(final AbstractJsonServlet servlet, final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    String pathInfo = req.getPathInfo();
    if (pathInfo == null) {
      return false;
    }
    if (!pathInfo.startsWith("/wopi/files/")) {
      return false;
    }

    final String[] tokens = pathInfo.split("/");
    if (tokens.length < 4) {
      return false;
    }
    final String fileId = tokens[3];
    FileInfo fileInfo = getFileInfo(fileId);
    if (fileInfo == null) {
      return false;
    }
    try {
      final Subject subject = createSubject(req.getParameter("access_token"));
      final WopiRoundtrip handler = createWopiRoundtrip(req, resp, tokens, fileInfo);
      Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
        @Override
        public Object run() throws ServletException, IOException {
          handler.handle();
          return null;
        }
      });
      return true;
    }
    catch (PrivilegedActionException e) {
      Throwable t = e.getCause();
      if (t instanceof ServletException) {
        throw (ServletException) t;
      }
      if (t instanceof IOException) {
        throw (IOException) t;
      }
      throw new ServletException("Unexpected", t);
    }
  }

  protected TTLCache<String, String> getFileLocks() {
    return m_fileLocks;
  }

  protected Subject createSubject(String accessToken) {
    Subject subject = new Subject();
    if (accessToken != null) {
      subject.getPrincipals().add(new WopiPrincipal(accessToken));
    }
    return subject;
  }

  protected WopiRoundtrip createWopiRoundtrip(HttpServletRequest req, HttpServletResponse resp, String[] tokens, FileInfo fileInfo) {
    return new WopiRoundtrip(getFileLocks(), req, resp, tokens, this, fileInfo);
  }

}
