/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mail.smtp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.eclipse.scout.rt.mail.MailHelper;
import org.eclipse.scout.rt.mail.MailMessage;
import org.eclipse.scout.rt.mail.MailParticipant;
import org.eclipse.scout.rt.mail.smtp.SmtpHelper.SmtpDebugReceiverEmailProperty;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for {@link SmtpHelper}
 */
public class SmtpHelperTest {

  private List<IBean<?>> m_replacedBeans = new ArrayList<>();

  @After
  public void after() {
    BeanTestingHelper.get().unregisterBeans(m_replacedBeans);
    m_replacedBeans.clear();
  }

  @Test
  public void testGetAllRecipients() throws MessagingException {
    MailMessage mailMessage = BEANS.get(MailMessage.class)
        .withBodyPlainText("lorem")
        .addToRecipient(BEANS.get(MailParticipant.class).withEmail("ipsum@example.org"))
        .addToRecipient(BEANS.get(MailParticipant.class).withEmail("dolor@example.org"))
        .addCcRecipient(BEANS.get(MailParticipant.class).withEmail("sit@example.org"))
        .addBccRecipient(BEANS.get(MailParticipant.class).withEmail("amet@example.org"));

    MimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(mailMessage);

    // No debug recipient
    Address[] allRecipients = BEANS.get(SmtpHelper.class).getAllRecipients(message);

    assertNotNull("No recipients", allRecipients);
    assertEquals("Number of recipients is wrong", 4, allRecipients.length);
    assertEquals("Wrong recipient 1", "ipsum@example.org", ((InternetAddress) allRecipients[0]).getAddress());
    assertEquals("Wrong recipient 2", "dolor@example.org", ((InternetAddress) allRecipients[1]).getAddress());
    assertEquals("Wrong recipient 3", "sit@example.org", ((InternetAddress) allRecipients[2]).getAddress());
    assertEquals("Wrong recipient 4", "amet@example.org", ((InternetAddress) allRecipients[3]).getAddress());

    // With Debug recipient
    m_replacedBeans.add(BeanTestingHelper.get().mockConfigProperty(SmtpDebugReceiverEmailProperty.class, "debug@example.org"));

    allRecipients = BEANS.get(SmtpHelper.class).getAllRecipients(message);

    assertNotNull("No recipients", allRecipients);
    assertEquals("Number of recipients is wrong", 1, allRecipients.length);
    assertEquals("Wrong recipient", "debug@example.org", ((InternetAddress) allRecipients[0]).getAddress());
  }

  /**
   * This test method provides an easy way to test an SMTP server access by sending an email. Remove the {@link Ignore}
   * annotation before executing this test.
   * <p>
   * Provide the required configuration for your SMTP server and an appropriate recipient email address (see marker
   * CHANGE_HERE)
   */
  @Ignore
  @Test
  public void testSendMessage() {
    // <CHANGE_HERE>
    String senderEmailAddress = "my-email-address@example.org";
    String password = "password";
    String smtpServerHost = "smtp.example.org";
    String recipientEmailAddress = "recipient-address@example.org";
    // </CHANGE_HERE>

    MailMessage mailMessage = BEANS.get(MailMessage.class)
        .withSubject("Lorem ipsum dolor")
        .withBodyPlainText("Lorem ipsum dolor (plain text)")
        .withBodyHtml(HTML.html(null, HTML.fragment(
            HTML.h1("lorem"),
            HTML.h2("ipsum"),
            HTML.h3("dolor"),
            HTML.p("html"))).toHtml())
        .withSender(BEANS.get(MailParticipant.class).withName("Sender name").withEmail(senderEmailAddress))
        .addToRecipient(BEANS.get(MailParticipant.class).withName("Recipient name").withEmail(recipientEmailAddress));

    SmtpServerConfig config = BEANS.get(SmtpServerConfig.class)
        .withHost(smtpServerHost)
        .withUsername(senderEmailAddress)
        .withPassword(password)
        .withPort(465)
        .withUseAuthentication(true)
        .withUseSmtps(true);

    MimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(mailMessage);
    BEANS.get(SmtpHelper.class).sendMessage(config, message);
  }
}
