/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mail;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

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
