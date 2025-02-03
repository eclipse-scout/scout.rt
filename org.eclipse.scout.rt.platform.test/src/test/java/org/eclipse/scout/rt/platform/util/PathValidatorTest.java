/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.*;

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
