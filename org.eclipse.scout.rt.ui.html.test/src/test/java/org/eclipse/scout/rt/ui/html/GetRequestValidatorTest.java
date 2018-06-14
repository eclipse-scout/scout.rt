package org.eclipse.scout.rt.ui.html;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

/**
 * <h3>{@link GetRequestValidatorTest}</h3>
 */
public class GetRequestValidatorTest {

  @Test
  public void testValidate() {
    assertValid(null);
    assertValid("");
    assertValid("a.txt");

    assertValid("/./a.txt");
    assertValid("/./a..txt");
    assertValid("/./a...txt");
    assertValid("./a.txt");
    assertValid("\\.\\a.txt");
    assertValid(".\\a.txt");
    assertValid("a/test/b/dasdf/a.txt");
    assertValid("a\\test\\b\\dasdf\\a.txt");

    assertInvalid("/..");
    assertInvalid("../");
    assertInvalid("\\..");
    assertInvalid("..\\");
    assertInvalid("..");

    assertInvalid("/icon/../../../../../config.properties");
    assertInvalid("/icon/..\\..\\..\\..\\..\\config.properties");
  }

  private void assertValid(String pathInfo) {
    new GetRequestValidator().validate(createRequest(pathInfo));
  }

  private void assertInvalid(String path) {
    try {
      assertValid(path);
      fail("Path '" + path + "' should not be valid.");
    }
    catch (IllegalArgumentException expected) {
      assertNotNull(expected);
    }
  }

  private HttpServletRequest createRequest(String pathInfo) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getPathInfo()).thenReturn(pathInfo);
    return request;
  }
}
