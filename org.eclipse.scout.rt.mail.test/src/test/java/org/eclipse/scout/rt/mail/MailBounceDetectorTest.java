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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Test;

/**
 * JUnit tests for {@link MailBounceDetector}.
 */
public class MailBounceDetectorTest {

  @Test(expected = AssertionException.class)
  public void testNoMessage() {
    BEANS.get(MailBounceDetector.class).test(null);
  }

  @Test
  public void testNoBounce() {
    MailMessage mailMessage = BEANS.get(MailMessage.class)
        .withSubject("lorem")
        .withSender(BEANS.get(MailParticipant.class).withEmail("sender@example.org"))
        .addToRecipient(BEANS.get(MailParticipant.class).withEmail("recipient@example.org"))
        .withBodyPlainText("Lorem ipsum");

    assertFalse(BEANS.get(MailBounceDetector.class).test(BEANS.get(MailHelper.class).createMimeMessage(mailMessage)));
  }

  @Test
  public void testNoNonStandardBounceSender() throws MessagingException {
    MailMessage mailMessage = BEANS.get(MailMessage.class)
        .withSubject("lorem")
        .withSender(BEANS.get(MailParticipant.class).withEmail("postmaster@example.org"))
        .addToRecipient(BEANS.get(MailParticipant.class).withEmail("recipient@example.org"))
        .withBodyPlainText("Lorem ipsum");

    // only sender criteria matches (still two missing)
    MimeMessage mimeMessage = BEANS.get(MailHelper.class).createMimeMessage(mailMessage);
    mimeMessage.addHeader("Return-Path", "sender@example.org");
    assertFalse(BEANS.get(MailBounceDetector.class).test(mimeMessage));
  }

  @Test
  public void testNoNonStandardBounceSubject() throws MessagingException {
    MailMessage mailMessage = BEANS.get(MailMessage.class)
        .withSubject("undeliverable")
        .withSender(BEANS.get(MailParticipant.class).withEmail("sender@example.org"))
        .addToRecipient(BEANS.get(MailParticipant.class).withEmail("recipient@example.org"))
        .withBodyPlainText("Lorem ipsum");

    // only subject criteria matches (still two missing)
    MimeMessage mimeMessage = BEANS.get(MailHelper.class).createMimeMessage(mailMessage);
    mimeMessage.addHeader("Return-Path", "sender@example.org");
    assertFalse(BEANS.get(MailBounceDetector.class).test(mimeMessage));
  }

  @Test
  public void testNoNonStandardBounceSubjectSender() throws MessagingException {
    MailMessage mailMessage = BEANS.get(MailMessage.class)
        .withSubject("undeliverable")
        .withSender(BEANS.get(MailParticipant.class).withEmail("postmaster@example.org"))
        .addToRecipient(BEANS.get(MailParticipant.class).withEmail("recipient@example.org"))
        .withBodyPlainText("Lorem ipsum");

    // only subject and sender criteria matches (still one missing)
    MimeMessage mimeMessage = BEANS.get(MailHelper.class).createMimeMessage(mailMessage);
    mimeMessage.addHeader("Return-Path", "sender@example.org");
    assertFalse(BEANS.get(MailBounceDetector.class).test(mimeMessage));
  }

  @Test
  public void testNonStandardBounceSubjectSenderReturnPath() {
    MailMessage mailMessage = BEANS.get(MailMessage.class)
        .withSubject("undeliverable")
        .withSender(BEANS.get(MailParticipant.class).withEmail("postmaster@example.org"))
        .addToRecipient(BEANS.get(MailParticipant.class).withEmail("recipient@example.org"))
        .withBodyPlainText("undeliverable");

    // sender, subject and return path criteria matches (reached 3 criterias to detect as bounce)
    assertTrue(BEANS.get(MailBounceDetector.class).test(BEANS.get(MailHelper.class).createMimeMessage(mailMessage)));
  }
}
