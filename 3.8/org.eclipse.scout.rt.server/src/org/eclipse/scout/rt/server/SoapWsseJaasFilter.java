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
package org.eclipse.scout.rt.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.AccessController;
import java.security.Principal;
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

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.EncryptionUtility;
import org.eclipse.scout.commons.SoapHandlingUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.http.servletfilter.FilterConfigInjection;
import org.eclipse.scout.http.servletfilter.security.SecureHttpServletRequestWrapper;
import org.eclipse.scout.rt.server.internal.Activator;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Transformation filter used to create a subject based on the soap headers containing a virtual request (from ajax,
 * rap)
 * <p>
 * If there is already a subject set as {@link Subject#getSubject(java.security.AccessControlContext)} then the filter
 * is transparent.
 * <p>
 * Reads the soap wsse header and transforms it to a subject with the user principal. The password is the triple-des
 * encoding of "${timestamp}:${username}" using the config.ini parameter <code>scout.ajax.token.key</code>
 * <p>
 * Normally this filters the alias /ajax
 * <p>
 * This filter is registered in the scout server plugin.xml as /ajax by default with order 1'000'010 and has the active
 * flag set to true
 */
public class SoapWsseJaasFilter implements Filter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SoapWsseJaasFilter.class);

  private static final byte[] tripleDesKey;
  static {
    String key = Activator.getDefault().getBundle().getBundleContext().getProperty("scout.ajax.token.key");
    if (key == null) {
      tripleDesKey = null;
    }
    else {
      tripleDesKey = new byte[24];
      byte[] keyBytes;
      try {
        keyBytes = key.getBytes("UTF-8");
        System.arraycopy(keyBytes, 0, tripleDesKey, 0, Math.min(keyBytes.length, tripleDesKey.length));
      }
      catch (UnsupportedEncodingException e) {
        LOG.error("reading property 'scout.ajax.token.key'", e);
      }
    }
  }

  private SAXParserFactory m_saxParserFactory;
  private FilterConfigInjection m_injection;

  @Override
  public void init(FilterConfig config0) throws ServletException {
    m_injection = new FilterConfigInjection(config0, getClass());
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
    m_injection = null;
  }

  @Override
  public void doFilter(ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
    if (isSubjectSet()) {
      chain.doFilter(request, response);
      return;
    }
    FilterConfigInjection.FilterConfig config = m_injection.getConfig(request);
    if (!config.isActive()) {
      chain.doFilter(request, response);
      return;
    }

    InputStream httpIn = request.getInputStream();
    ByteArrayOutputStream cacheOut = new ByteArrayOutputStream();
    Subject subject;
    try {
      subject = parseSubject(httpIn, cacheOut);
    }
    catch (Throwable t) {
      LOG.warn("WS-Security check", t);
      ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    final InputStream cacheIn = new ByteArrayInputStream(cacheOut.toByteArray());
    cacheOut = null;
    //
    final HttpServletRequestWrapper replayRequest = new HttpServletRequestWrapper((HttpServletRequest) request) {
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
    continueChainWithPrincipal(subject, replayRequest, (HttpServletResponse) response, chain);
  }

  private void continueChainWithPrincipal(Subject subject, final HttpServletRequest req, final HttpServletResponse res, final FilterChain chain) throws IOException, ServletException {
    try {
      Subject.doAs(
          subject,
          new PrivilegedExceptionAction<Object>() {
            @Override
            public Object run() throws Exception {
              Principal principal = Subject.getSubject(AccessController.getContext()).getPrincipals().iterator().next();
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

  protected Subject parseSubject(final InputStream httpIn, final ByteArrayOutputStream cacheOut) throws Exception {
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
    return createSubject(cleanString(handler.user), cleanString(handler.tokenRaw), cleanString(handler.tokenEncoding));
  }

  /**
   * override this method to do additional checks on credentials
   */
  protected Subject createSubject(String user, String tokenRaw, String tokenEncoding) throws Exception {
    if (user == null || tokenRaw == null) {
      LOG.error("Ajax back-end call contains no ws-security token. Check if the config.ini of the /rap and the /ajax webapp contains the property 'scout.ajax.token.key'.");
      throw new SecurityException("SOAP header contains no ws-security token");
    }
    byte[] token = Base64Utility.decode(tokenRaw);
    String msg = new String(EncryptionUtility.decrypt(token, tripleDesKey), "UTF-8");
    String[] tupel = msg.split(":", 2);
    long timestamp = Long.parseLong(tupel[0]);
    String userRef = tupel[1];
    if (timestamp < 0L || userRef == null || !userRef.equals(user)) {
      throw new SecurityException("SOAP header contains no ws-security token");
    }
    Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal(user));
    subject.setReadOnly();
    return subject;
  }

  private boolean isSubjectSet() {
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null) {
      return false;
    }
    if (subject.getPrincipals().size() == 0) {
      return false;
    }
    String name = subject.getPrincipals().iterator().next().getName();
    if (name == null || name.trim().length() == 0) {
      return false;
    }
    return true;
  }

  private String cleanString(String s) {
    if (s == null) {
      return null;
    }
    s = s.trim();
    if (s.length() == 0) {
      return null;
    }
    if (s.equalsIgnoreCase("null")) {
      return null;
    }
    return s;
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
    public String tokenEncoding;
    public String tokenRaw;
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
        tokenEncoding = attributes.getValue("", SoapHandlingUtility.WSSE_PASSWORD_TYPE_ATTRIBUTE);
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
          tokenRaw = new String(ch, start, length);
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
