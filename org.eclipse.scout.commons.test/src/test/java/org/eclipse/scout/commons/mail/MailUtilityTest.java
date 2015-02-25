/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.mail;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link MailUtility}
 */
@SuppressWarnings("restriction")
public class MailUtilityTest {

  /**
   * Message without sender can be created
   *
   * @throws ProcessingException
   *           ,MessagingException
   */
  @Test
  public void testMimeMessageWithoutSender() throws ProcessingException, MessagingException {
    MailMessage definition = new MailMessage("Body", null);
    MimeMessage message = MailUtility.createMimeMessage(definition);
    assertNotNull(message);

    definition = new MailMessage("Body", null);
    definition.setSubject("Subject");
    message = MailUtility.createMimeMessage(definition);
    assertNotNull(message);
  }

  @Test
  public void testCreateDataSource() throws Exception {
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    final String fileName = "test.file";
    File file = IOUtility.createTempFile(fileName, sampleData);
    DataSource ds = MailUtility.createDataSource(file);
    assertNotNull(ds);
    assertEquals(fileName, ds.getName());
    assertTrue(ds instanceof ByteArrayDataSource);
    ByteArrayDataSource bds = (ByteArrayDataSource) ds;
    assertEquals("application/octet-stream", bds.getContentType());
    byte[] data = IOUtility.getContent(bds.getInputStream());
    assertArrayEquals(sampleData, data);
  }

  @Test
  public void testDataSourceWithoutFileExtension() throws ProcessingException, IOException, MessagingException {
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    final String fileName = "test.file";

    DataSource ds = MailUtility.createDataSource(new ByteArrayInputStream(sampleData), fileName, null);
    assertNotNull(ds);
    assertEquals(fileName, ds.getName());
    assertTrue(ds instanceof ByteArrayDataSource);
    ByteArrayDataSource bds = (ByteArrayDataSource) ds;
    assertEquals("application/octet-stream", bds.getContentType());
    byte[] data = IOUtility.getContent(bds.getInputStream());
    assertArrayEquals(sampleData, data);

    MailMessage definition = new MailMessage("test", null);
    definition.addAttachment(new MailAttachment(ds));

    MimeMessage message = MailUtility.createMimeMessage(definition);
    message.writeTo(new ByteArrayOutputStream());
  }

  @Test
  public void testGetParts() throws ProcessingException, IOException, MessagingException {
    final String plainText = "plain text";
    final String htmlText = "<html><body><p>plain text</p></body></html>";
    MailMessage definition = new MailMessage(plainText, htmlText);

    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    definition.addAttachment(new MailAttachment(MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sample1.dat", null)));
    definition.addAttachment(new MailAttachment(MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sample2.dat", null)));
    definition.addAttachment(new MailAttachment(MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sample3.dat", null)));

    MimeMessage message = MailUtility.createMimeMessage(definition);

    verifyMimeMessage(message, plainText, htmlText, "sample1.dat", "sample2.dat", "sample3.dat");

    // validate that the method call does not throw an exception
    MailUtility.collectMailParts(null, null, null, null);
    MailUtility.collectMailParts(message, null, null, null);

    List<Part> bodyCollector = new ArrayList<Part>();
    List<Part> attachmentCollector = new ArrayList<Part>();
    List<Part> inlineAttachmentCollector = new ArrayList<Part>();
    MailUtility.collectMailParts(message, bodyCollector, attachmentCollector, inlineAttachmentCollector);
    Assert.assertEquals("body parts size is wrong", 2, bodyCollector.size());
    Assert.assertEquals("attachments parts size is wrong", 3, attachmentCollector.size());
    Assert.assertEquals("inline attachments parts size is wrong", 0, inlineAttachmentCollector.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateMimeMessageWithNull() throws Exception {
    MailUtility.createMimeMessage(null);
  }

  @Test
  public void testCreateMimeMessage() throws Exception {
    // no plain text or html body
    Assert.assertNull(MailUtility.createMimeMessage(new MailMessage(null, null)));

    final String plainText = "plain text";
    final String html = "<html><body><p>html</p></body></html>";

    MimeMessage plainTextMessage = MailUtility.createMimeMessage(new MailMessage(plainText, null));
    verifyMimeMessage(plainTextMessage, plainText, null);

    MimeMessage htmlMessage = MailUtility.createMimeMessage(new MailMessage(null, html));
    verifyMimeMessage(htmlMessage, null, html);

    MimeMessage plainTextAndHtmlMessage = MailUtility.createMimeMessage(new MailMessage(plainText, html));
    verifyMimeMessage(plainTextAndHtmlMessage, plainText, html);

    MailMessage definition = new MailMessage(plainText, html, "Subject", "info@example.org", CollectionUtility.arrayList("to1@example.org"));
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    final String attachmentContentId = "mycontentid";
    definition.addAttachment(new MailAttachment(MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sample1.dat", null), attachmentContentId));
    definition.addCcRecipient("cc1@example.org");
    definition.addCcRecipient("cc2@example.org");
    definition.addBccRecipient("bcc1@example.org");
    definition.addBccRecipient("bcc2@example.org");
    definition.addBccRecipient("bcc3@example.org");
    MimeMessage msg = MailUtility.createMimeMessage(definition);
    verifyMimeMessage(msg, plainText, html, "sample1.dat");
    // exactly one, already verified by verify method
    Part attachmentPart = CollectionUtility.firstElement(MailUtility.getAttachmentParts(msg));
    Assert.assertTrue("Attachment part is of wrong type", attachmentPart instanceof MimeBodyPart);
    MimeBodyPart bodyPart = (MimeBodyPart) attachmentPart;
    Assert.assertEquals("Wrong content id", "<" + attachmentContentId + ">", bodyPart.getContentID());
    Assert.assertEquals("Wrong subject", "Subject", msg.getSubject());
    Address[] toRecipients = msg.getRecipients(Message.RecipientType.TO);
    Address[] ccRecipients = msg.getRecipients(Message.RecipientType.CC);
    Address[] bccRecipients = msg.getRecipients(Message.RecipientType.BCC);
    Assert.assertNotNull("No to recipients", toRecipients);
    Assert.assertNotNull("No cc recipients", ccRecipients);
    Assert.assertNotNull("No bcc recipients", bccRecipients);

    Assert.assertEquals("Number of to recipients is wrong", 1, toRecipients.length);
    Assert.assertEquals("Number of cc recipients is wrong", 2, ccRecipients.length);
    Assert.assertEquals("Number of bcc recipients is wrong", 3, bccRecipients.length);
  }

  @Test
  public void testInlineAttachmentCollector() throws MessagingException, ProcessingException {
    CharsetSafeMimeMessage message = new CharsetSafeMimeMessage();
    MimeMultipart multiPart = new MimeMultipart();
    message.setContent(multiPart);

    MimeBodyPart bodyPart = new MimeBodyPart();
    bodyPart.setText("plain text", "UTF-8");
    bodyPart.addHeader(MailUtility.CONTENT_TYPE_ID, MailUtility.CONTENT_TYPE_TEXT_PLAIN);
    multiPart.addBodyPart(bodyPart);

    BodyPart inlineAttachmentPart = new MimeBodyPart();
    inlineAttachmentPart.setContent("base-64-encoded-image-content", "image/gif");
    inlineAttachmentPart.addHeader(MailUtility.CONTENT_TYPE_ID, "image/gif; name=\"graycol.gif\"");
    inlineAttachmentPart.addHeader("Content-ID", "<5__=4EBBF65EEFABF58A8f9e8a9@example.org>");
    inlineAttachmentPart.addHeader("Content-Disposition", Part.INLINE);
    multiPart.addBodyPart(inlineAttachmentPart);

    List<Part> bodyCollector = new ArrayList<Part>();
    List<Part> attachmentCollector = new ArrayList<Part>();
    List<Part> inlineAttachmentCollector = new ArrayList<Part>();
    MailUtility.collectMailParts(message, bodyCollector, attachmentCollector, inlineAttachmentCollector);
    Assert.assertEquals("body parts size is wrong", 1, bodyCollector.size());
    Assert.assertEquals("attachments parts size is wrong", 0, attachmentCollector.size());
    Assert.assertEquals("inline attachments parts size is wrong", 1, inlineAttachmentCollector.size());
  }

  @Test
  public void testAddAttachmentsToMimeMessage() throws ProcessingException, IOException, MessagingException {
    // create html mime message without attachments
    final String plainText = "plain text";
    final String html = "<html><body><p>plain text</p></html>";
    MailMessage definition = new MailMessage(plainText, html);
    MimeMessage message = MailUtility.createMimeMessage(definition);
    verifyMimeMessage(message, plainText, html /* no attachments*/);

    // add no attachments
    MailUtility.addAttachmentsToMimeMessage(message, null);
    verifyMimeMessage(message, plainText, html);
    MailUtility.addAttachmentsToMimeMessage(message, new ArrayList<File>());
    verifyMimeMessage(message, plainText, html);

    // add 3 attachments to mime message
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    List<File> attachments = new ArrayList<File>();
    attachments.add(IOUtility.createTempFile("sample1.dat", sampleData));
    attachments.add(IOUtility.createTempFile("sample2.dat", sampleData));
    attachments.add(IOUtility.createTempFile("sample3_öüä.dat", sampleData));
    MailUtility.addAttachmentsToMimeMessage(message, attachments);

    // verify added attachments in java instance
    verifyMimeMessage(message, plainText, html, "sample1.dat", "sample2.dat", MimeUtility.encodeText("sample3_öüä.dat", "UTF-8", null));

    // store and recreate mime message (byte[])
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    message.writeTo(bos);
    message = MailUtility.createMessageFromBytes(bos.toByteArray());

    // verify new instance
    verifyMimeMessage(message, plainText, html, "sample1.dat", "sample2.dat", MimeUtility.encodeText("sample3_öüä.dat", "UTF-8", null));
  }

  @Test
  public void testMimeMessageDefinitionRecipients() {
    MailMessage definition = new MailMessage();
    definition.addToRecipient("to@example.org");
    definition.addCcRecipient("cc@example.org");
    definition.addBccRecipient("bcc@example.org");

    Assert.assertEquals("Number of TO recipients is wrong", 1, definition.getToRecipients().size());
    Assert.assertEquals("Number of CC recipients is wrong", 1, definition.getCcRecipients().size());
    Assert.assertEquals("Number of BCC recipients is wrong", 1, definition.getBccRecipients().size());

    Assert.assertEquals("TO recipient is wrong", "to@example.org", CollectionUtility.firstElement(definition.getToRecipients()));
    Assert.assertEquals("CC recipient is wrong", "cc@example.org", CollectionUtility.firstElement(definition.getCcRecipients()));
    Assert.assertEquals("BCC recipient is wrong", "bcc@example.org", CollectionUtility.firstElement(definition.getBccRecipients()));

    definition.clearToRecipients();
    Assert.assertEquals("Number of TO recipients is wrong", 0, definition.getToRecipients().size());
    Assert.assertEquals("Number of CC recipients is wrong", 1, definition.getCcRecipients().size());
    Assert.assertEquals("Number of BCC recipients is wrong", 1, definition.getBccRecipients().size());

    definition.clearCcRecipients();
    Assert.assertEquals("Number of TO recipients is wrong", 0, definition.getToRecipients().size());
    Assert.assertEquals("Number of CC recipients is wrong", 0, definition.getCcRecipients().size());
    Assert.assertEquals("Number of BCC recipients is wrong", 1, definition.getBccRecipients().size());

    definition.clearBccRecipients();
    Assert.assertEquals("Number of TO recipients is wrong", 0, definition.getToRecipients().size());
    Assert.assertEquals("Number of CC recipients is wrong", 0, definition.getCcRecipients().size());
    Assert.assertEquals("Number of BCC recipients is wrong", 0, definition.getBccRecipients().size());

    definition.addToRecipients(CollectionUtility.arrayList("to1@exapmle.org", "to2@example.org"));
    definition.addCcRecipients(CollectionUtility.arrayList("cc1@exapmle.org", "cc2@example.org", "cc3@example.org"));
    definition.addBccRecipients(CollectionUtility.arrayList("bcc1@exapmle.org", "bcc2@example.org", "bcc3@example.org", "bcc4@example.org"));

    Assert.assertEquals("Number of TO recipients is wrong", 2, definition.getToRecipients().size());
    Assert.assertEquals("Number of CC recipients is wrong", 3, definition.getCcRecipients().size());
    Assert.assertEquals("Number of BCC recipients is wrong", 4, definition.getBccRecipients().size());
  }

  @Test
  public void testMimeMessageDefinitionAttachments() throws Exception {
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};

    MailMessage definition = new MailMessage();
    definition.addAttachment(new MailAttachment(MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sample1.dat", null)));

    Assert.assertEquals("Number of attachments is wrong", 1, definition.getAttachments().size());

    definition.clearAttachments();

    Assert.assertEquals("Number of attachments is wrong", 0, definition.getAttachments().size());

    definition.addAttachments(CollectionUtility.arrayList(
        new MailAttachment(MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sample1.dat", null)),
        new MailAttachment(MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sample2.dat", null))
        ));

    Assert.assertEquals("Number of attachments is wrong", 2, definition.getAttachments().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMimeMessageAttachmentWithNullDataSource1() {
    new MailAttachment(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMimeMessageAttachmentWithNullDataSource2() {
    new MailAttachment(null, "mycontentid");
  }

  /**
   * Verifies the following properties of the mime message:
   * <ul>
   * <li>Plain text part</li>
   * <li>Html part</li>
   * <li>Number of filename of attachments</li>
   * </ul>
   *
   * @param message
   *          Mime message to verify
   * @param plainText
   *          Plain text (null of no plain text is available)
   * @param htmlText
   *          HTML (null of no html is available)
   * @param attachmentFilenames
   *          Filenames of attachments (use {@link MimeUtility#encodeText(String)} in case the filename has
   *          non-supported chars)
   * @throws ProcessingException
   * @throws IOException
   * @throws MessagingException
   */
  private void verifyMimeMessage(MimeMessage message, String plainText, String htmlText, String... attachmentFilenames) throws ProcessingException, IOException, MessagingException {
    if (plainText != null) {
      Assert.assertEquals("wrong plain text", plainText, MailUtility.getPlainText(message));
    }
    else {
      Assert.assertNull("wrong plain text", MailUtility.getPlainText(message));
    }

    int bodyPartCount = 0;
    bodyPartCount += plainText == null ? 0 : 1;
    bodyPartCount += htmlText == null ? 0 : 1;

    List<Part> bodyParts = MailUtility.getBodyParts(message);
    Assert.assertEquals("body parts size is wrong", bodyPartCount, bodyParts.size());

    Part plainTextPart = MailUtility.getPlainTextPart(bodyParts);
    if (plainText != null) {
      Assert.assertNotNull("no plain text part found", plainTextPart);
      Assert.assertTrue("plain text part content is not string", plainTextPart.getContent() instanceof String);
      Assert.assertEquals("wrong plain text", plainText, (String) plainTextPart.getContent());
    }
    else {
      Assert.assertNull("plain text part found", plainTextPart);
    }

    Part htmlPart = MailUtility.getHtmlPart(bodyParts);
    if (htmlText != null) {
      Assert.assertNotNull("no html part found", htmlPart);
      Assert.assertTrue("html part content is not string", htmlPart.getContent() instanceof String);
      Assert.assertEquals("wrong html text", htmlText, (String) htmlPart.getContent());
    }
    else {
      Assert.assertNull("html part found", htmlPart);
    }

    List<Part> attachmentParts = MailUtility.getAttachmentParts(message);
    Assert.assertEquals("attachments parts size is wrong", attachmentFilenames.length, attachmentParts.size());
    Set<String> attachmentFilenamesSet = new HashSet<String>();
    for (Part part : attachmentParts) {
      attachmentFilenamesSet.add(part.getFileName());
    }
    Assert.assertEquals("attachments filenames size is wrong", attachmentFilenames.length, attachmentFilenamesSet.size());
    for (String attachmentFilename : attachmentFilenames) {
      Assert.assertTrue("attachment filename " + attachmentFilename + " is missing", attachmentFilenamesSet.contains(attachmentFilename));
    }
  }
}
