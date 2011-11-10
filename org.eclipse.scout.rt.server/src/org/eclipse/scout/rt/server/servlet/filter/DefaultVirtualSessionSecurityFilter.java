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
package org.eclipse.scout.rt.server.servlet.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.server.VirtualSessionIdPrincipal;
import org.eclipse.scout.rt.shared.servicetunnel.SoapHandlingUtility;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Filter used to authenticate soap headers containing virtual request (for ajax, rap)
 * <p>
 * Reads the soap wsse header and creates a subject with the user principal and an additional
 * {@link VirtualSessionIdPrincipal}
 */
@Priority(-1)
public class DefaultVirtualSessionSecurityFilter implements Filter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DefaultVirtualSessionSecurityFilter.class);
  private SAXParserFactory m_saxParserFactory;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    try {
      m_saxParserFactory = SoapHandlingUtility.createSaxParserFactory();
    }
    catch (Exception e) {
      throw new ServletException(e);
    }
  }

  @Override
  public void destroy() {
    m_saxParserFactory = null;
  }

  @Override
  public void doFilter(ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
    if (Subject.getSubject(AccessController.getContext()) != null) {
      chain.doFilter(request, response);
      return;
    }
    InputStream httpIn = request.getInputStream();
    ByteArrayOutputStream cacheOut = new ByteArrayOutputStream();
    final Subject subject;
    try {
      subject = negotiateSubject(httpIn, cacheOut);
    }
    catch (Throwable t) {
      LOG.warn("WS-Security check", t);
      ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    final InputStream cacheIn = new ByteArrayInputStream(cacheOut.toByteArray());
    cacheOut = null;
    //
    final HttpServletRequestWrapper wreq = new HttpServletRequestWrapper((HttpServletRequest) request) {
      @Override
      public ServletInputStream getInputStream() throws IOException {
        return new ServletInputStream() {
          @Override
          public int read() throws IOException {
            return cacheIn.read();
          }
        };
      }
    };
    try {
      Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
        @Override
        public Object run() throws Exception {
          chain.doFilter(wreq, response);
          return null;
        }
      });
    }
    catch (PrivilegedActionException e) {
      LOG.error("WS-Security delegate", e.getCause());
    }
  }

  protected Subject negotiateSubject(final InputStream httpIn, final ByteArrayOutputStream cacheOut) throws Exception {
    InputStream filterIn = new InputStream() {
      @Override
      public int read() throws IOException {
        int ch = httpIn.read();
        if (ch < 0) {
          return ch;
        }
        cacheOut.write(ch);
        return ch;
      }

      @Override
      public int read(byte[] b) throws IOException {
        int n = httpIn.read(b);
        if (n <= 0) {
          return n;
        }
        cacheOut.write(b, 0, n);
        return n;
      }

      @Override
      public int read(byte[] b, int off, int len) throws IOException {
        int n = httpIn.read(b, off, len);
        if (n <= 0) {
          return n;
        }
        cacheOut.write(b, off, n);
        return n;
      }
    };
    WSSEUserTokenHandler handler = new WSSEUserTokenHandler();
    SoapHandlingUtility.createSaxParser(m_saxParserFactory).parse(new InputSource(filterIn), handler);
    if (handler.user == null || handler.pass == null || handler.type == null) {
      throw new SecurityException("Username, password, or password type is undefined");
    }
    if (!SoapHandlingUtility.WSSE_PASSWORD_TYPE_FOR_SCOUT_VIRTUAL_SESSION_ID.equals(handler.type)) {
      throw new SecurityException("Password type is not '" + SoapHandlingUtility.WSSE_PASSWORD_TYPE_FOR_SCOUT_VIRTUAL_SESSION_ID + "'");
    }
    SimplePrincipal pUser = new SimplePrincipal(handler.user);
    VirtualSessionIdPrincipal pSession = new VirtualSessionIdPrincipal(handler.pass);
    Subject subject = new Subject();
    subject.getPrincipals().add(pUser);
    subject.getPrincipals().add(pSession);
    subject.setReadOnly();
    return subject;
  }

  /**
   * <pre>
   * <wsse:Security soapenv:mustUnderstand="1">
   *   <wsse:UsernameToken>
   *     <wsse:Username>user</wsse:Username>
   *     <wsse:Password Type="http://...#Virtual">QDgxVkqYnuqk...==</wsse:Password>
   *   </wsse:UsernameToken>
   * </wsse:Security>
   * </pre>
   */
  private static class WSSEUserTokenHandler extends DefaultHandler {
    public String user;
    public String type;
    public String pass;
    //
    private boolean insideEnvelope;
    private boolean insideHeader;
    private boolean insideSecurity;
    private boolean insideUsernameToken;
    private boolean insideUsername;
    private boolean insidePasswort;
    private boolean done;

    @Override
    public void startElement(String namespaceURI, String localName, String qNameText, Attributes attributes) throws SAXException {
      if (done) {
        return;
      }
      QName qname = new QName(namespaceURI, localName);
      if (SoapHandlingUtility.SOAPENV_ENVELOPE_ELEMENT.equals(qname)) {
        insideEnvelope = true;
        return;
      }
      if (SoapHandlingUtility.SOAPENV_HEADER_ELEMENT.equals(qname)) {
        insideHeader = true;
        return;
      }
      if (SoapHandlingUtility.WSSE_SECURITY_ELEMENT.equals(qname)) {
        insideSecurity = true;
        return;
      }
      if (SoapHandlingUtility.WSSE_USERNAME_TOKEN_ELEMENT.equals(qname)) {
        insideUsernameToken = true;
        return;
      }
      if (SoapHandlingUtility.WSSE_USERNAME_ELEMENT.equals(qname)) {
        insideUsername = true;
        return;
      }
      if (SoapHandlingUtility.WSSE_PASSWORD_ELEMENT.equals(qname)) {
        insidePasswort = true;
        type = attributes.getValue("", SoapHandlingUtility.WSSE_PASSWORD_TYPE_ATTRIBUTE);
        return;
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      if (done) {
        return;
      }
      if (insideEnvelope && insideHeader && insideSecurity && insideUsernameToken) {
        if (insideUsername) {
          user = new String(ch, start, length);
        }
        if (insidePasswort) {
          pass = new String(ch, start, length);
        }
      }

    }

    @Override
    public void endElement(String namespaceURI, String localName, String qNameText) throws SAXException {
      if (done) {
        return;
      }
      QName qname = new QName(namespaceURI, localName);
      if (SoapHandlingUtility.SOAPENV_ENVELOPE_ELEMENT.equals(qname)) {
        insideEnvelope = false;
        done = true;
        return;
      }
      if (SoapHandlingUtility.SOAPENV_HEADER_ELEMENT.equals(qname)) {
        insideHeader = false;
        return;
      }
      if (SoapHandlingUtility.WSSE_SECURITY_ELEMENT.equals(qname)) {
        insideSecurity = false;
        return;
      }
      if (SoapHandlingUtility.WSSE_USERNAME_TOKEN_ELEMENT.equals(qname)) {
        insideUsernameToken = false;
        return;
      }
      if (SoapHandlingUtility.WSSE_USERNAME_ELEMENT.equals(qname)) {
        insideUsername = false;
        return;
      }
      if (SoapHandlingUtility.WSSE_PASSWORD_ELEMENT.equals(qname)) {
        insidePasswort = false;
        return;
      }
    }
  }
}
