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

import static org.eclipse.scout.rt.mail.MailHelperTest.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.resource.MimeType;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Test;

/**
 * JUnit tests for {@link MailHelper#createMimeMessage(MailMessage)}
 */
public class MailHelperCreateMimeMessageTest {

  protected static final String SAMPLE_PLAIN_TEXT = "plain text";
  protected static final String SAMPLE_HTML = "<html><body><h1>HTML<h1></body><html>";
  protected static final String SAMPLE_HTML_FOR_INLINE_ATTACHMENT = "<html><body><h1>HTML<h1><img src=\"cid:logo\" /></body><html>";

  protected static final byte[] SAMPLE_ATTACHMENT_DATA = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
  // use fix content type because content or filename might be resolved to different content types depending on the underlying platform
  protected static final MailAttachment SAMPLE_MAIL_ATTACHMENT = new MailAttachment(BinaryResources.create()
      .withFilename("attachment.dat")
      .withContentType(MimeType.APPLICATION_OCTET_STREAM.getType())
      .withContent(SAMPLE_ATTACHMENT_DATA)
      .build());

  protected static final MailAttachment SAMPLE_INLINE_MAIL_ATTACHMENT = new MailAttachment(BinaryResources.create()
      .withFilename("inline.png")
      .withContentType(MimeType.PNG.getType())
      .withContent(SAMPLE_ATTACHMENT_DATA).build(),
      "logo");

  @Test(expected = IllegalArgumentException.class)
  public void testCreateMimeMessageWithNull() {
    BEANS.get(MailHelper.class).createMimeMessage(null);
  }

  @Test
  public void testCreateMimeMessage() throws Exception {
    // no plain text or html body
    assertNull(BEANS.get(MailHelper.class).createMimeMessage(new MailMessage()));

    final String plainText = SAMPLE_PLAIN_TEXT;
    final String html = "<html><body><p>html</p></body></html>";

    MimeMessage plainTextMessage = BEANS.get(MailHelper.class).createMimeMessage(new MailMessage().withBodyPlainText(plainText));
    verifyMimeMessage(plainTextMessage, plainText, null);

    MimeMessage htmlMessage = BEANS.get(MailHelper.class).createMimeMessage(new MailMessage().withBodyHtml(html));
    verifyMimeMessage(htmlMessage, null, html);

    MimeMessage plainTextAndHtmlMessage = BEANS.get(MailHelper.class).createMimeMessage(new MailMessage().withBodyPlainText(plainText).withBodyHtml(html));
    verifyMimeMessage(plainTextAndHtmlMessage, plainText, html);

    MailMessage definition =
        new MailMessage().withSubject("Subject").withBodyPlainText(plainText).withBodyHtml(html).withSender(createMailParticipant("info@example.org")).addToRecipients(createMailParticipants(CollectionUtility.arrayList("to1@example.org")));

    final String attachmentContentId = "mycontentid";
    definition.withAttachment(new MailAttachment(BEANS.get(MailHelper.class).createDataSource(new ByteArrayInputStream(SAMPLE_ATTACHMENT_DATA), "sample1.dat", null), null, null, attachmentContentId));
    definition.addCcRecipient(createMailParticipant("cc1@example.org"));
    definition.addCcRecipient(createMailParticipant("cc2@example.org"));
    definition.addBccRecipient(createMailParticipant("bcc1@example.org"));
    definition.addBccRecipient(createMailParticipant("bcc2@example.org"));
    definition.addBccRecipient(createMailParticipant("bcc3@example.org"));
    MimeMessage msg = BEANS.get(MailHelper.class).createMimeMessage(definition);
    verifyMimeMessage(msg, plainText, html, "sample1.dat");
    // exactly one, already verified by verify method
    Part attachmentPart = CollectionUtility.firstElement(BEANS.get(MailHelper.class).getAttachmentParts(msg));
    assertTrue("Attachment part is of wrong type", attachmentPart instanceof MimeBodyPart);
    MimeBodyPart bodyPart = (MimeBodyPart) attachmentPart;
    assertEquals("Wrong content id", "<" + attachmentContentId + ">", bodyPart.getContentID());
    assertEquals("Wrong subject", "Subject", msg.getSubject());
    Address[] toRecipients = msg.getRecipients(Message.RecipientType.TO);
    Address[] ccRecipients = msg.getRecipients(Message.RecipientType.CC);
    Address[] bccRecipients = msg.getRecipients(Message.RecipientType.BCC);
    assertNotNull("No to recipients", toRecipients);
    assertNotNull("No cc recipients", ccRecipients);
    assertNotNull("No bcc recipients", bccRecipients);

    assertEquals("Number of to recipients is wrong", 1, toRecipients.length);
    assertEquals("Number of cc recipients is wrong", 2, ccRecipients.length);
    assertEquals("Number of bcc recipients is wrong", 3, bccRecipients.length);
  }

  @Test
  public void testCreateMimeMessageWithParticipantWithoutEmail() {
    MailParticipant mailParticipant = new MailParticipant().withName("name only");

    // sender, reply to, to, cc and bcc must work with mail participant that will result in null
    // when MailHelper.createInternetAddress(MailParticipant) is called because no email address is present.
    MailMessage mailMessage = new MailMessage()
        .withSubject("Subject")
        .withBodyPlainText(SAMPLE_PLAIN_TEXT)
        .withSender(mailParticipant)
        .addReplyTo(mailParticipant)
        .addToRecipient(mailParticipant)
        .addCcRecipient(mailParticipant)
        .addBccRecipient(mailParticipant);

    assertNotNull(BEANS.get(MailHelper.class).createMimeMessage(mailMessage));
  }

  @Test
  public void testCreateMimeMessagePlainTextOnlyStructure() throws IOException, MessagingException {
    CharsetSafeMimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(createSampleMailMessage()
        .withBodyPlainText(SAMPLE_PLAIN_TEXT));

    // Plain only
    // - Content-Type: text/plain; charset="UTF-8"
    verifyPlainTextPart(message);
  }

  @Test
  public void testCreateMimeMessageHtmlOnlyStructure() throws IOException, MessagingException {
    CharsetSafeMimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(createSampleMailMessage()
        .withBodyHtml(SAMPLE_HTML));

    // HTML only
    // - Content-Type: text/html; charset="UTF-8"
    verifyHtmlPart(message);
  }

  @Test
  public void testCreateMimeMessagePlainTextAndHtmlStructure() throws IOException, MessagingException {
    CharsetSafeMimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(createSampleMailMessage()
        .withBodyPlainText(SAMPLE_PLAIN_TEXT)
        .withBodyHtml(SAMPLE_HTML));

    // Plain & HTML
    // - Content-Type: multipart/alternative; boundary="AAAA"
    //    - Content-Type: text/plain; charset="UTF-8"
    //    - Content-Type: text/html; charset="UTF-8"
    assertTrue(message.getContent() instanceof Multipart);
    assertTrue(message.getContentType().startsWith("multipart/alternative;"));

    Multipart multipart = (Multipart) message.getContent();
    verifyPlainTextPart(multipart.getBodyPart(0));
    verifyHtmlPart(multipart.getBodyPart(1));
  }

  @Test
  public void testCreateMimeMessagePlainTextWithAttachmentStructure() throws IOException, MessagingException {
    CharsetSafeMimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(createSampleMailMessage()
        .withBodyPlainText(SAMPLE_PLAIN_TEXT)
        .withAttachment(SAMPLE_MAIL_ATTACHMENT));

    // Plain with attachment
    // - Content-Type: multipart/mixed; boundary="AAAA"
    //    - Content-Type: text/plain; charset="UTF-8"
    //    - Content-Type: application/octet-stream; name=attachment.dat
    assertTrue(message.getContent() instanceof Multipart);
    assertTrue(message.getContentType().startsWith("multipart/mixed;"));

    Multipart multipart = (Multipart) message.getContent();
    verifyPlainTextPart(multipart.getBodyPart(0));
    verifyAttachment(multipart.getBodyPart(1));
  }

  @Test
  public void testCreateMimeMessageHtmlWithAttachmentStructure() throws IOException, MessagingException {
    CharsetSafeMimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(createSampleMailMessage()
        .withBodyHtml(SAMPLE_HTML)
        .withAttachment(SAMPLE_MAIL_ATTACHMENT));

    // HTML with attachment
    // - Content-Type: multipart/mixed; boundary="AAAA"
    //    - Content-Type: text/html; charset="UTF-8"
    //    - Content-Type: application/octet-stream; name=attachment.dat
    assertTrue(message.getContent() instanceof Multipart);
    assertTrue(message.getContentType().startsWith("multipart/mixed;"));

    Multipart multipart = (Multipart) message.getContent();
    verifyHtmlPart(multipart.getBodyPart(0));
    verifyAttachment(multipart.getBodyPart(1));
  }

  @Test
  public void testCreateMimeMessagePlainTextAndHtmlWithAttachmentStructure() throws IOException, MessagingException {
    CharsetSafeMimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(createSampleMailMessage()
        .withBodyPlainText(SAMPLE_PLAIN_TEXT)
        .withBodyHtml(SAMPLE_HTML)
        .withAttachment(SAMPLE_MAIL_ATTACHMENT));

    // Plain & HTML with attachment
    // - Content-Type: multipart/mixed; boundary="AAAA"
    //    - Content-Type: multipart/alternative; boundary="BBBB"
    //       - Content-Type: text/plain; charset="UTF-8"
    //       - Content-Type: text/html; charset="UTF-8"
    //    - Content-Type: application/octet-stream; name=attachment.dat
    assertTrue(message.getContent() instanceof Multipart);
    assertTrue(message.getContentType().startsWith("multipart/mixed;"));

    Multipart multipart = (Multipart) message.getContent();
    BodyPart alternativeBodyPart = multipart.getBodyPart(0);
    BodyPart attachmentPart = multipart.getBodyPart(1);

    assertTrue(alternativeBodyPart.getContent() instanceof Multipart);
    assertTrue(alternativeBodyPart.getContentType().startsWith("multipart/alternative;"));

    Multipart alternativeMultiPart = (Multipart) alternativeBodyPart.getContent();
    verifyPlainTextPart(alternativeMultiPart.getBodyPart(0));
    verifyHtmlPart(alternativeMultiPart.getBodyPart(1));

    verifyAttachment(attachmentPart);
  }

  @Test
  public void testCreateMimeMessageHtmlWithInlineAttachmentStructure() throws IOException, MessagingException {
    CharsetSafeMimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(createSampleMailMessage()
        .withBodyHtml(SAMPLE_HTML_FOR_INLINE_ATTACHMENT)
        .withInlineAttachment(SAMPLE_INLINE_MAIL_ATTACHMENT));

    // HTML with inline attachment
    // - Content-Type: multipart/related; boundary="AAAA"
    //    - Content-Type: text/html; charset="UTF-8"
    //    - Content-Type: image/png; name="inline.png"
    assertTrue(message.getContent() instanceof Multipart);
    assertTrue(message.getContentType().startsWith("multipart/related;"));

    Multipart relatedMultiPart = (Multipart) message.getContent();
    verifyHtmlPartWithInlineImage(relatedMultiPart.getBodyPart(0));
    verifyInlineAttachment(relatedMultiPart.getBodyPart(1));
  }

  @Test
  public void testCreateMimeMessagePlainTextAndHtmlWithInlineAttachmentStructure() throws IOException, MessagingException {
    CharsetSafeMimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(createSampleMailMessage()
        .withBodyPlainText(SAMPLE_PLAIN_TEXT)
        .withBodyHtml(SAMPLE_HTML_FOR_INLINE_ATTACHMENT)
        .withInlineAttachment(SAMPLE_INLINE_MAIL_ATTACHMENT));

    // Plain & HTML with inline attachment
    // - Content-Type: multipart/related; boundary="AAAA"
    //    - Content-Type: multipart/alternative; boundary="BBBB"
    //       - Content-Type: text/plain; charset="UTF-8"
    //       - Content-Type: text/html; charset="UTF-8"
    //    - Content-Type: image/png; name="inline.png"
    assertTrue(message.getContent() instanceof Multipart);
    assertTrue(message.getContentType().startsWith("multipart/related;"));

    Multipart relatedMultiPart = (Multipart) message.getContent();
    BodyPart alternativeBodyPart = relatedMultiPart.getBodyPart(0);
    BodyPart inlineAttachmentPart = relatedMultiPart.getBodyPart(1);

    assertTrue(alternativeBodyPart.getContent() instanceof Multipart);
    assertTrue(alternativeBodyPart.getContentType().startsWith("multipart/alternative;"));

    Multipart alternativeMultiPart = (Multipart) alternativeBodyPart.getContent();
    verifyPlainTextPart(alternativeMultiPart.getBodyPart(0));
    verifyHtmlPartWithInlineImage(alternativeMultiPart.getBodyPart(1));

    verifyInlineAttachment(inlineAttachmentPart);
  }

  @Test
  public void testCreateMimeMessagePlainTextAndHtmlWithInlineAttachmentAndAttachmentStructure() throws IOException, MessagingException {
    CharsetSafeMimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(createSampleMailMessage()
        .withBodyPlainText(SAMPLE_PLAIN_TEXT)
        .withBodyHtml(SAMPLE_HTML_FOR_INLINE_ATTACHMENT)
        .withInlineAttachment(SAMPLE_INLINE_MAIL_ATTACHMENT)
        .withAttachment(SAMPLE_MAIL_ATTACHMENT));

    // Plain & HTML with inline and non-inline attachments
    // - Content-Type: multipart/mixed; boundary="AAAA"
    //    - Content-Type: multipart/related; boundary="BBBB"
    //       - Content-Type: multipart/alternative; boundary="CCCC"
    //          - Content-Type: text/plain; charset="UTF-8"
    //          - Content-Type: text/html; charset="UTF-8"
    //       - Content-Type: image/png; name=inline.png
    //    - Content-Type: application/octet-stream; name=attachment.dat
    assertTrue(message.getContent() instanceof Multipart);
    assertTrue(message.getContentType().startsWith("multipart/mixed;"));

    Multipart multipart = (Multipart) message.getContent();
    BodyPart relatedBodyPart = multipart.getBodyPart(0);
    BodyPart attachmentPart = multipart.getBodyPart(1);

    assertTrue(relatedBodyPart.getContent() instanceof Multipart);
    assertTrue(relatedBodyPart.getContentType().startsWith("multipart/related;"));

    Multipart relatedMultiPart = (Multipart) relatedBodyPart.getContent();
    BodyPart alternativeBodyPart = relatedMultiPart.getBodyPart(0);
    BodyPart inlineAttachmentPart = relatedMultiPart.getBodyPart(1);

    assertTrue(alternativeBodyPart.getContent() instanceof Multipart);
    assertTrue(alternativeBodyPart.getContentType().startsWith("multipart/alternative;"));

    Multipart alternativeMultiPart = (Multipart) alternativeBodyPart.getContent();
    verifyPlainTextPart(alternativeMultiPart.getBodyPart(0));
    verifyHtmlPartWithInlineImage(alternativeMultiPart.getBodyPart(1));

    verifyInlineAttachment(inlineAttachmentPart);
    verifyAttachment(attachmentPart);
  }

  /**
   * Creates a sample message from 'lorem@example.org' to 'ipsum@example.org' with subject 'dolor'.
   */
  protected MailMessage createSampleMailMessage() {
    return BEANS.get(MailMessage.class)
        .withSubject("dolor")
        .withSender(BEANS.get(MailParticipant.class).withEmail("lorem@example.org"))
        .addToRecipient(BEANS.get(MailParticipant.class).withEmail("ipsum@example.org"));
  }

  protected void verifyPlainTextPart(Part plainTextPart) throws IOException, MessagingException {
    assertTrue(plainTextPart.getContent() instanceof String);
    assertEquals(SAMPLE_PLAIN_TEXT, (String) plainTextPart.getContent());
    assertEquals("text/plain; charset=\"UTF-8\"", plainTextPart.getContentType());
  }

  protected void verifyHtmlPart(Part htmlPart) throws IOException, MessagingException {
    assertTrue(htmlPart.getContent() instanceof String);
    assertEquals(SAMPLE_HTML, (String) htmlPart.getContent());
    assertEquals("text/html; charset=\"UTF-8\"", htmlPart.getContentType());
  }

  protected void verifyHtmlPartWithInlineImage(Part htmlPart) throws IOException, MessagingException {
    assertTrue(htmlPart.getContent() instanceof String);
    assertEquals(SAMPLE_HTML_FOR_INLINE_ATTACHMENT, (String) htmlPart.getContent());
    assertEquals("text/html; charset=\"UTF-8\"", htmlPart.getContentType());
  }

  protected void verifyInlineAttachment(BodyPart inlineAttachmentPart) throws IOException, MessagingException {
    assertNotNull(inlineAttachmentPart.getContent());
    assertEquals("image/png; name=inline.png", inlineAttachmentPart.getContentType());
    assertEquals(BodyPart.INLINE, inlineAttachmentPart.getDisposition());
  }

  protected void verifyAttachment(BodyPart attachmentPart) throws IOException, MessagingException {
    assertNotNull(attachmentPart.getContent());
    assertEquals("application/octet-stream; name=attachment.dat", attachmentPart.getContentType());
    assertEquals(BodyPart.ATTACHMENT, attachmentPart.getDisposition());
  }
}
