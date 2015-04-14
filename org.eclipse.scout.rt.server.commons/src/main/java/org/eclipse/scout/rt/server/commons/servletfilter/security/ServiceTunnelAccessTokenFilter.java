package org.eclipse.scout.rt.server.commons.servletfilter.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.SecurityUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.commons.BufferedServletRequestWrapper;
import org.eclipse.scout.rt.shared.servicetunnel.http.AbstractHttpServiceTunnel;

/**
 * Security filter detecting service tunnel access token based on
 * {@link AbstractHttpServiceTunnel#TOKEN_AUTH_HTTP_HEADER} and {@link AbstractHttpServiceTunnel#TOKEN_USER_ID_HEADER}
 */
public class ServiceTunnelAccessTokenFilter implements Filter {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServiceTunnelAccessTokenFilter.class);
  public static final String PROP_PUBLIC_KEY = "org.eclipse.scout.rt.servicetunnel.signature.publickey";

  private byte[] m_publicKey;

  @Override
  public void init(FilterConfig config0) throws ServletException {
    String publicKeyForSignatureValidation = ConfigIniUtility.getProperty(PROP_PUBLIC_KEY);
    if (StringUtility.hasText(publicKeyForSignatureValidation)) {
      m_publicKey = Base64Utility.decode(publicKeyForSignatureValidation);
      if (m_publicKey == null || m_publicKey.length < 1) {
        throw new ServletException("Invalid digital signature public key.");
      }
    }
  }

  @Override
  public void destroy() {
    m_publicKey = null;
  }

  @Override
  public void doFilter(ServletRequest in, ServletResponse out, final FilterChain chain) throws IOException, ServletException {
    BufferedServletRequestWrapper req = new BufferedServletRequestWrapper((HttpServletRequest) in);

    if (!isSignatureValid(req)) {
      HttpServletResponse res = (HttpServletResponse) out;
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    chain.doFilter(req, out);
  }

  protected boolean isSignatureValid(BufferedServletRequestWrapper req) {
    if (m_publicKey == null) {
      return false;
    }

    String token = req.getHeader(AbstractHttpServiceTunnel.TOKEN_AUTH_HTTP_HEADER);
    if (StringUtility.isNullOrEmpty(token)) {
      return false;
    }

    byte[] signature = Base64Utility.decode(token);
    if (signature == null || signature.length < 1) {
      return false;
    }

    try {
      return SecurityUtility.verifySignature(m_publicKey, req.getData(), signature);
    }
    catch (ProcessingException e) {
      LOG.error("Unable to verify digital signature.", e);
      return false;
    }
  }
}
