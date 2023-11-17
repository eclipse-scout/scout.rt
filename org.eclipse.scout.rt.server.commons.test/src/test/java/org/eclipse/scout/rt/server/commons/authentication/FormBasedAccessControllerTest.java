/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.authentication;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.security.Principal;
import java.util.Arrays;
import java.util.function.Supplier;

import org.eclipse.scout.rt.platform.holders.IntegerHolder;
import org.eclipse.scout.rt.platform.security.ICredentialVerifier;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.authentication.FormBasedAccessController.FormBasedAuthConfig;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RunWith(PlatformTestRunner.class)
public class FormBasedAccessControllerTest {

  /**
   * Return value for successful verification with {@link #m_credentialVerifier}
   */
  private int m_success = ICredentialVerifier.AUTH_OK;

  /**
   * The credential verifier, allowing every username starting with foo using secret password bar.
   */
  private ICredentialVerifier m_credentialVerifier = (username, password) -> StringUtility.startsWith(username, "foo") && Arrays.equals(password, "bar".toCharArray()) ? m_success : ICredentialVerifier.AUTH_FORBIDDEN;

  public void reset() {
    m_success = ICredentialVerifier.AUTH_OK;
  }

  @Test
  public void testHandleAuthRequest() throws ServletException, IOException {
    FormBasedAccessController formBasedAccessController = new FormBasedAccessController().init(new FormBasedAuthConfig()
        .withCredentialVerifier(m_credentialVerifier)
        .withStatus403WaitMillis(0));

    // forbidden: no credentials provided
    testHandleAuthRequestInternal(formBasedAccessController, null, null, false, false);

    // forbidden: correct user/invalid password
    testHandleAuthRequestInternal(formBasedAccessController, () -> "foo", null, false, false);

    // forbidden: no user/correct password
    testHandleAuthRequestInternal(formBasedAccessController, null, () -> "bar", false, false);

    // forbidden: invalid user/correct password
    testHandleAuthRequestInternal(formBasedAccessController, () -> "baz", () -> "bar", false, false);

    // forbidden: correct user/invalid password
    testHandleAuthRequestInternal(formBasedAccessController, () -> "foo", () -> "baz", false, false);

    // successful
    testHandleAuthRequestInternal(formBasedAccessController, () -> "foo", () -> "bar", true, false);

    // successful: but two factor required, no second factor verifier is set
    m_success = ICredentialVerifier.AUTH_2FA_REQUIRED;
    testHandleAuthRequestInternal(formBasedAccessController, () -> "foo", () -> "bar", false, false);

    // other case: ICredentialVerifier.AUTH_CREDENTIALS_REQUIRED
    m_success = ICredentialVerifier.AUTH_CREDENTIALS_REQUIRED;
    testHandleAuthRequestInternal(formBasedAccessController, () -> "foo", () -> "bar", false, false);

    // other case: ICredentialVerifier.AUTH_FAILED
    m_success = ICredentialVerifier.AUTH_FAILED;
    testHandleAuthRequestInternal(formBasedAccessController, () -> "foo", () -> "bar", false, false);
  }

  protected void testHandleAuthRequestInternal(FormBasedAccessController formBasedAccessController, Supplier<String> usernameSupplier, Supplier<String> passwordSupplier, boolean successful, boolean requireSecondFactor)
      throws IOException, ServletException {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    HttpSession session = mock(HttpSession.class);
    doReturn(session).when(req).getSession();
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    doReturn(printWriter).when(resp).getWriter();

    if (usernameSupplier != null) {
      doReturn(usernameSupplier.get()).when(req).getParameter(eq("user"));
    }
    if (passwordSupplier != null) {
      doReturn(passwordSupplier.get()).when(req).getParameter(eq("password"));
    }

    formBasedAccessController.handleAuthRequest(req, resp);

    if (successful) {
      verify(resp, never()).sendError(anyInt());
      if (requireSecondFactor) {
        verify(session, times(1)).setAttribute(eq(ServletFilterHelper.SESSION_ATTRIBUTE_FOR_2FA_PRINCIPAL), notNull());
        assertEquals("{\"twoFactorRequired\":true}", stringWriter.toString());
      }
      else {
        verify(session, times(1)).setAttribute(eq(ServletFilterHelper.SESSION_ATTRIBUTE_FOR_PRINCIPAL), notNull());
        assertEquals("", stringWriter.toString());
      }
    }
    else {
      verify(resp, times(1)).sendError(HttpURLConnection.HTTP_FORBIDDEN);
      verify(session, never()).setAttribute(eq(ServletFilterHelper.SESSION_ATTRIBUTE_FOR_PRINCIPAL), notNull());
      assertEquals("", stringWriter.toString());
    }
  }

  @Test
  public void testHandleSecondFactorRequest() throws ServletException, IOException {
    IntegerHolder secondFactorVerifierFailStateHolder = new IntegerHolder(ICredentialVerifier.AUTH_FORBIDDEN);
    FormBasedAccessController formBasedAccessController = new FormBasedAccessController().init(new FormBasedAuthConfig()
        .withCredentialVerifier(m_credentialVerifier)
        // for second factor verifier username has to be foo (no other usernames allowed)
        .withSecondFactorVerifier((username, token) -> "foo".equals(username) && Arrays.equals("42".toCharArray(), token) ? ICredentialVerifier.AUTH_OK : secondFactorVerifierFailStateHolder.getValue())
        .withStatus403WaitMillis(0));

    // successful: but two factor required, request second factor
    m_success = ICredentialVerifier.AUTH_2FA_REQUIRED;
    testHandleAuthRequestInternal(formBasedAccessController, () -> "foo", () -> "bar", true, true);
    testHandleAuthRequestInternal(formBasedAccessController, () -> "foo_", () -> "bar", true, true);

    // forbidden: no credentials provided (actually this case would not even end up in handleSecondFactorRequest)
    testHandleSecondFactorRequestInternal(formBasedAccessController, null, null, true, false);

    // forbidden: no username provided
    testHandleSecondFactorRequestInternal(formBasedAccessController, null, () -> "42", true, false);

    // forbidden: no token provided (actually this case would not even end up in handleSecondFactorRequest)
    testHandleSecondFactorRequestInternal(formBasedAccessController, () -> "foo", null, true, false);

    // forbidden: invalid user/correct token (even though user passed auth above, token is not valid for this user)
    testHandleSecondFactorRequestInternal(formBasedAccessController, () -> "foo_", () -> "42", true, false);

    // forbidden: correct user/invalid token
    testHandleSecondFactorRequestInternal(formBasedAccessController, () -> "foo", () -> "13", true, false);

    // forbidden: correct user/correct token, but first factor authentication has not been completed (no session attribute set)
    testHandleSecondFactorRequestInternal(formBasedAccessController, () -> "foo", () -> "42", false, false);

    // successful
    testHandleSecondFactorRequestInternal(formBasedAccessController, () -> "foo", () -> "42", true, true);
  }

  protected void testHandleSecondFactorRequestInternal(FormBasedAccessController formBasedAccessController, Supplier<String> usernameSupplier, Supplier<String> tokenSupplier, boolean firstChallengeCompleted, boolean successful) throws ServletException, IOException {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    HttpSession session = mock(HttpSession.class);
    doReturn(session).when(req).getSession();
    Principal principal = null;
    if (firstChallengeCompleted) {
      principal = mock(Principal.class);
      doReturn(principal).when(session).getAttribute(eq(ServletFilterHelper.SESSION_ATTRIBUTE_FOR_2FA_PRINCIPAL));
    }

    if (usernameSupplier != null && principal != null) {
      doReturn(usernameSupplier.get()).when(principal).getName();
    }
    if (tokenSupplier != null) {
      doReturn(tokenSupplier.get()).when(req).getParameter(eq("token"));
    }

    formBasedAccessController.handleAuthRequest(req, resp);

    if (successful) {
      verify(resp, never()).sendError(anyInt());
      verify(session, times(1)).setAttribute(eq(ServletFilterHelper.SESSION_ATTRIBUTE_FOR_PRINCIPAL), eq(principal));
    }
    else {
      verify(resp, times(1)).sendError(HttpURLConnection.HTTP_FORBIDDEN);
      verify(session, never()).setAttribute(eq(ServletFilterHelper.SESSION_ATTRIBUTE_FOR_PRINCIPAL), notNull());
    }
  }
}
