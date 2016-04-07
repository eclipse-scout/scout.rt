package org.eclipse.scout.rt.server.commons.authentication;

import org.eclipse.scout.rt.server.commons.authentication.SessionCookieValidator;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <h3>{@link SessionCookieValidatorTest}</h3>
 */
@RunWith(PlatformTestRunner.class)
public class SessionCookieValidatorTest {

  private static final String HEADER_PREFIX = "JSESSIONID=abc";

  @Test
  public void testAllSetNoSpaces() {
    doTest(true, HEADER_PREFIX + ";Secure;HttpOnly", true, true);
  }

  @Test
  public void testAllSetNoSpacesLower() {
    doTest(true, HEADER_PREFIX + ";secure;httponly", true, true);
  }

  @Test
  public void testAllSetSpaces() {
    doTest(true, HEADER_PREFIX + " ; Secure ; HttpOnly ", true, true);
  }

  @Test
  public void testAllSetSpacesLower() {
    doTest(true, HEADER_PREFIX + " ; secure ; httponly ", true, true);
  }

  @Test
  public void testMissingSecure() {
    doTest(true, HEADER_PREFIX + ";HttpOnly", true, false);
  }

  @Test
  public void testMissingHttpOnly() {
    doTest(true, HEADER_PREFIX + ";Secure", false, true);
  }

  @Test
  public void testMissingBoth() {
    doTest(true, HEADER_PREFIX, false, false);
  }

  @Test
  public void testSecureTestDisabled() {
    doTest(false, HEADER_PREFIX, false, true);
  }

  @Test
  public void testSecureTestDisabled2() {
    doTest(false, HEADER_PREFIX + ";HttpOnly", true, true);
  }

  protected void doTest(final boolean secureCheckEnabled, String cookieHeaderVal, boolean expectedHttpOnlyOk, boolean expectedSecureOk) {
    SessionCookieValidator validator = new SessionCookieValidator() {
      @Override
      protected boolean isSecureFlagCheckEnabled() {
        return secureCheckEnabled;
      }
    };
    boolean httpOnlyOk = validator.handleHttpOnlyFlag(null, null, cookieHeaderVal);
    Assert.assertEquals(expectedHttpOnlyOk, httpOnlyOk);

    boolean secureOk = validator.handleSecureFlag(null, null, cookieHeaderVal);
    Assert.assertEquals(expectedSecureOk, secureOk);
  }
}
