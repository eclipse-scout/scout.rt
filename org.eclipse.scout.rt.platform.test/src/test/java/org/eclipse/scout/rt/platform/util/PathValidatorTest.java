package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * <h3>{@link PathValidatorTest}</h3>
 */
public class PathValidatorTest {

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
    new PathValidator().validate(pathInfo);
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
}
