/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mail;

import static org.junit.Assert.*;

import jakarta.mail.MessagingException;

import org.junit.Test;

public class CharsetSafeMimeMessageTest {

  @Test
  public void testDefaultMessageId() throws MessagingException {
    CharsetSafeMimeMessage mimeMessage1 = new CharsetSafeMimeMessage();
    mimeMessage1.saveChanges();
    String messageId1 = mimeMessage1.getMessageID();

    CharsetSafeMimeMessage mimeMessage2 = new CharsetSafeMimeMessage();
    mimeMessage2.saveChanges();
    String messageId2 = mimeMessage2.getMessageID();
    assertNotEquals(messageId1, messageId2);
  }

  @Test
  public void testCustomMessageId() throws MessagingException {
    CharsetSafeMimeMessage mimeMessage1 = new CharsetSafeMimeMessage();
    mimeMessage1.setCustomMessageId("<mymessageid@localhost>");
    mimeMessage1.saveChanges();
    String messageId1 = mimeMessage1.getMessageID();

    CharsetSafeMimeMessage mimeMessage2 = new CharsetSafeMimeMessage();
    mimeMessage2.setCustomMessageId("<mymessageid@localhost>");
    mimeMessage2.saveChanges();
    String messageId2 = mimeMessage2.getMessageID();

    assertEquals("<mymessageid@localhost>", messageId1);
    assertEquals("<mymessageid@localhost>", messageId2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidCustomMessageId() {
    CharsetSafeMimeMessage mimeMessage1 = new CharsetSafeMimeMessage();
    mimeMessage1.setCustomMessageId("mymessageid@localhost"); // <> is missing
  }
}
