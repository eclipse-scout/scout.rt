package org.eclipse.scout.rt.server.commons.authentication;

import java.util.Collection;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h3>{@link SessionCookieValidator}</h3>
 */
@ApplicationScoped
public class SessionCookieValidator {

  private static final Logger LOG = LoggerFactory.getLogger(SessionCookieValidator.class);

  // for IETF spec see http://tools.ietf.org/html/rfc6265#section-5.2.5
  protected static final Pattern HTTP_ONLY_FLAG_VALIDATION_PAT = Pattern.compile(";\\s*httponly", Pattern.CASE_INSENSITIVE);
  protected static final Pattern SECURE_FLAG_VALIDATION_PAT = Pattern.compile(";\\s*secure", Pattern.CASE_INSENSITIVE);

  private final boolean m_isSecureFlagCheckEnabled;

  public SessionCookieValidator() {
    m_isSecureFlagCheckEnabled = CONFIG.getPropertyValue(CheckSessionCookieSecureFlagProperty.class).booleanValue();
  }

  public void validate(HttpServletRequest req, HttpServletResponse resp) {
    Collection<String> setCookieHeaders = resp.getHeaders("Set-Cookie");
    if (setCookieHeaders == null || setCookieHeaders.isEmpty()) {
      return; // nothing to validate
    }

    for (String cookieValue : setCookieHeaders) {
      if (cookieValue != null && cookieValue.startsWith("JSESSIONID")) {
        validateJSessionId(req, resp, cookieValue);
        return;
      }
    }
  }

  protected boolean isSecureFlagCheckEnabled() {
    return m_isSecureFlagCheckEnabled;
  }

  protected boolean isSecureFlagPresent(String cookieValue) {
    return SECURE_FLAG_VALIDATION_PAT.matcher(cookieValue).find();
  }

  protected boolean isHttpOnlyFlagPresent(String cookieValue) {
    return HTTP_ONLY_FLAG_VALIDATION_PAT.matcher(cookieValue).find();
  }

  protected boolean handleHttpOnlyFlag(HttpServletRequest req, HttpServletResponse resp, String cookieValue) {
    boolean httpOnlyFlagPresent = isHttpOnlyFlagPresent(cookieValue);
    if (!httpOnlyFlagPresent) {
      LOG.error("'HttpOnly' flag has not been set on session cookie. Enable the flag in your web.xml (<session-config>...<cookie-config>...<http-only>true</http-only>...</cookie-config>...</session-config>)");
    }
    return httpOnlyFlagPresent;
  }

  protected boolean handleSecureFlag(HttpServletRequest req, HttpServletResponse resp, String cookieValue) {
    if (!isSecureFlagCheckEnabled()) {
      return true; // validation of secure flag is disabled (e.g. because there is no secure channel used)
    }

    if (isSecureFlagPresent(cookieValue)) {
      // secure flag is set. everything ok.
      return true;
    }

    // secure flag is missing: error!
    LOG.error("'Secure' flag has not been set on session cookie. Enable the flag in your web.xml "
        + "(<session-config>...<cookie-config>...<secure>true</secure>...</cookie-config>...</session-config>)"
        + " or disable the 'Secure' flag check using property '{}=false' if no encrypted channel (https) to the end user is used.", BEANS.get(CheckSessionCookieSecureFlagProperty.class).getKey());
    return false;
  }

  protected void validateJSessionId(HttpServletRequest req, HttpServletResponse resp, String cookieValue) {
    boolean httpOnlyFlagOk = handleHttpOnlyFlag(req, resp, cookieValue);
    boolean secureFlagOk = handleSecureFlag(req, resp, cookieValue);

    if (!httpOnlyFlagOk || !secureFlagOk) {
      HttpSession httpSession = req.getSession(false);
      if (httpSession != null) {
        httpSession.invalidate();
      }
    }
  }

  /**
   * Specifies if the {@link SessionCookieValidator} should check if the 'Secure' flag is set on the '<code>Set-Cookie:
   * JSESSIONID=abc;Secure</code>' http header.<br>
   * This header flag should be set for encrypted (https) channels to ensure the user agent only sends the cookie over
   * secured channels.<br>
   * Unfortunately it is not possible to detect if the request from the user is using a secured channel.
   * {@link ServletRequest#isSecure()} only detects if the request received by the container is secured. But the
   * container may be behind a proxy which forwards the requests without encryption but the request form the browser to
   * the proxy itself is encrypted.<br>
   * To handle these cases the check is executed by default even if the request is not secure. In those cases where
   * really no encrypted channel to the user agent is used (not recommended) this property should be set to
   * <code>false</code>.
   */
  public static class CheckSessionCookieSecureFlagProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.auth.cookie.session.validate.secure";
    }

    @Override
    protected Boolean getDefaultValue() {
      return Boolean.TRUE;
    }
  }
}
