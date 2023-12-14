/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet.logging;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.HexUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * The HTTP session ID is a crucial factor for the security of an application. It should therefore never be made
 * available to a malicious third party, because knowledge of the session id can enable attackers to hijack the session
 * of an active user. Writing the id to a log file can therefore pose a security risk.
 * <p>
 * This helper provides a mechanism to convert the HTTP session ID to an obfuscated and truncated version that can be
 * safely written to a log file while still providing stable ids to support debugging. A
 * {@link HttpSessionIdLogModeProperty system property} is provided to change the output format.
 *
 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html#data-to-exclude">OWASP Logging
 *      Cheat Sheet</a>
 */
@ApplicationScoped
public class HttpSessionIdLogHelper {

  public static final String SESSION_ATTRIBUTE = HttpSessionIdLogHelper.class.getName();

  /**
   * Returns the ID of the given HTTP session in a format suitable for logging. See the top of the class for details.
   * <p>
   * For any given HTTP session, the resulting string for logging is computed only once and then cached in a session
   * attribute under the key {@link #SESSION_ATTRIBUTE}.
   *
   * @return either the full HTTP session ID, an obfuscated and truncated version of it, or {@code null}, depending on
   *         the value of the system property {@link HttpSessionIdLogModeProperty}.
   */
  public String getSessionIdForLogging(HttpSession session) {
    if (session == null) {
      return null;
    }
    String precomputedResult = (String) session.getAttribute(SESSION_ATTRIBUTE);
    if (precomputedResult != null) {
      return StringUtility.nullIfEmpty(precomputedResult);
    }
    String result = computeSessionIdForLogging(session.getId(), CONFIG.getPropertyValue(HttpSessionIdLogModeProperty.class));
    session.setAttribute(SESSION_ATTRIBUTE, StringUtility.emptyIfNull(result)); // store null as "", because null would remove the attribute
    return result;
  }

  protected String computeSessionIdForLogging(String sessionId, HttpSessionIdLogMode mode) {
    if (sessionId == null || mode == HttpSessionIdLogMode.FULL) {
      return sessionId;
    }
    if (mode == HttpSessionIdLogMode.SHORT) {
      try {
        return StringUtility.join("~",
            StringUtility.box("#", getShortHash(sessionId), ""),
            getSuffix(sessionId));
      }
      catch (Exception e) { // NOSONAR
        // something failed -> fall back to 'OFF' mode
      }
    }
    return null;
  }

  protected String getSuffix(String sessionId) {
    if (sessionId.length() < 30) {
      return null; // too short
    }
    // Exposing the last 4 characters of a 30 characters long ID should keep security reasonably intact.
    // See this discussion: https://owasp.org/www-community/vulnerabilities/Insufficient_Session-ID_Length
    final int suffixLength = 4;
    // The value of session.getId() is not necessarily identical to the JSESSIONID cookie.
    // Application servers sometimes seem to encode additional information in the session id, such as the
    // name of the target cluster node. Because the structure of session ids is not standardized, we have
    // to "guess" which part might be most helpful. From looking at some examples, we have concluded that
    // the different parts appear to be separated most commonly by a "dot" character:
    //
    // Examples:
    // - EF02AA0D087EA7F2E0DD9CF1B3EAE2A2.prod1002.example.com
    // - DED84B9F9C62A4EE158F825A45B1CF75.75d56f7d58a19e4b5bdc2d33347d025a45032ec6
    // - node014oww7syw3vwh8xe2re0s2aqy54.node0
    //
    // To minimize incorrect guesses when a dot appears too early in the string (perhaps a prefix that
    // identifies the cluster node?), dots are only considered after a reasonable distance from the
    // beginning. The number 15 was chosen simply because it is half of 30.
    final int dotIndex = sessionId.indexOf(".", 15);
    if (dotIndex >= 15) {
      return sessionId.substring(dotIndex - suffixLength, dotIndex);
    }
    return sessionId.substring(sessionId.length() - suffixLength);
  }

  /**
   * Returns a hash of the given string, shortened to 10 characters. The algorithm to generate the hash is unspecified,
   * but it will always return the same result for the same input. We deliberately don't emit the full hash for 2
   * reasons: a) minimize the risk of attacks related to the inversion of the hash function (rainbow tables etc.) and b)
   * reduce the size of log files.
   */
  protected String getShortHash(String sessionId) throws NoSuchAlgorithmException {
    return StringUtility.substring(getHash(sessionId), 0, 10);
  }

  protected String getHash(String sessionId) throws NoSuchAlgorithmException {
    MessageDigest messageDigest = null;
    messageDigest = MessageDigest.getInstance("MD5");
    return HexUtility.encode(messageDigest.digest(sessionId.getBytes(StandardCharsets.UTF_8)));
  }

  public enum HttpSessionIdLogMode {
    /**
     * No HTTP session ID is provided (always {@code null}).
     */
    OFF("off"),
    /**
     * Only a short and obfuscated version of the HTTP session is provided. The actual ID cannot be recovered from this.
     */
    SHORT("short"),
    /**
     * The full HTTP session ID, as reported by the application server, is provided.
     * <p>
     * CAUTION: Only use this mode if you can guarantee that no malicious third party has access to the log files.
     * Otherwise they might be able to hijack active sessions.
     */
    FULL("full");

    private final String m_id;

    HttpSessionIdLogMode(String id) {
      m_id = id;
    }

    public String getId() {
      return m_id;
    }
  }

  public static class HttpSessionIdLogModeProperty extends AbstractConfigProperty<HttpSessionIdLogMode, String> {

    @Override
    public String getKey() {
      return "scout.diagnostics.httpSessionIdLogMode";
    }

    @Override
    public HttpSessionIdLogMode getDefaultValue() {
      if (Platform.get().inDevelopmentMode()) {
        return HttpSessionIdLogMode.FULL;
      }
      return HttpSessionIdLogMode.SHORT;
    }

    @Override
    protected HttpSessionIdLogMode parse(String value) {
      String trimmedValue = StringUtility.trim(value);
      return Stream.of(HttpSessionIdLogMode.values())
          .filter(m -> m.getId().equalsIgnoreCase(trimmedValue))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Invalid value: '" + value + "'"));
    }

    @Override
    public String description() {
      return "Specifies in which form the HTTP session ID is provided as diagnostic context value.\n"
          + "Possible modes:\n"
          + "'" + HttpSessionIdLogMode.OFF.getId() + "': No HTTP session ID is provided.\n"
          + "'" + HttpSessionIdLogMode.SHORT.getId() + "': Only a short and obfuscated version of the HTTP session is provided. The actual ID cannot be recovered from this.\n"
          + "'" + HttpSessionIdLogMode.FULL.getId() + "': The full HTTP session ID is provided, as reported by the application server. "
          + "CAUTION: Only use this mode if you can guarantee that no malicious third party has access to the log files. Otherwise they might be able to hijack active sessions.\n"
          + "In development mode, the default value is '" + HttpSessionIdLogMode.FULL.getId() + "'. "
          + "Otherwise, the default value is '" + HttpSessionIdLogMode.SHORT.getId() + "'.";
    }
  }
}
