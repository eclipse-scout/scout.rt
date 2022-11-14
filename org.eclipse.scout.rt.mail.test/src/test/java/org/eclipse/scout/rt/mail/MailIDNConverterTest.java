/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.mail;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

/**
 * JUnit tests for {@link MailIDNConverter}
 */
public class MailIDNConverterTest {

  @Test
  public void testAsciiConvertionWithoutSpecialChar() {
    // Arrange
    String emailAddress = "someone@example.com";

    // Act
    String convertedAddress = BEANS.get(MailIDNConverter.class).toASCII(emailAddress);

    // Assert
    assertEquals(emailAddress, convertedAddress);
  }

  @Test
  public void testAsciiConvertionWithSpecialCharInLocalPart() {
    // Arrange
    String emailAddress = "sömeöne@example.com";

    // Act
    String convertedAddress = BEANS.get(MailIDNConverter.class).toASCII(emailAddress);

    // Assert
    assertEquals(emailAddress, convertedAddress);
  }

  @Test
  public void testAsciiConvertionWithSpecialChar() {
    // Arrange
    String emailAddress = "sömeone@exämple.com";
    String punycodeAddress = "sömeone@xn--exmple-cua.com";

    // Act
    String convertedAddress = BEANS.get(MailIDNConverter.class).toASCII(emailAddress);

    // Assert
    assertEquals(punycodeAddress, convertedAddress);
  }

  @Test
  public void testUnicodeConvertionWithSpecialChar() {
    // Arrange
    String punycodeAddress = "sömeöne@xn--exmple-cua.com";
    String emailAddress = "sömeöne@exämple.com";

    // Act
    String convertedAddress = BEANS.get(MailIDNConverter.class).toUnicode(punycodeAddress);

    // Assert
    assertEquals(emailAddress, convertedAddress);
  }
}
