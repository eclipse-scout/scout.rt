/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.mail;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.IDN;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link MailUtility}
 */
public class MailUtilityTest {

  /**
   * Very rare, unknown character set. Currently UTF-7, by default Oracle JDK does not support it.
   */
  private static final String RARE_UNKNOWN_CHARSET = "unicode-1-1-utf-7";

  /**
   * Message without sender can be created
   *
   * @throws ProcessingException
   *           ,MessagingException
   */
  @Test
  public void testMimeMessageWithoutSender() throws MessagingException {
    MailMessage definition = new MailMessage().withBodyPlainText("Body");
    MimeMessage message = MailUtility.createMimeMessage(definition);
    assertNotNull(message);

    definition = new MailMessage().withSubject("Subject").withBodyPlainText("Body");
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
    try (InputStream in = bds.getInputStream()) {
      byte[] data = IOUtility.readBytes(in);
      assertArrayEquals(sampleData, data);
    }
  }

  @Test
  public void testDataSourceWithoutFileExtension() throws IOException, MessagingException {
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    final String fileName = "test.file";

    DataSource ds = MailUtility.createDataSource(new ByteArrayInputStream(sampleData), fileName, null);
    assertNotNull(ds);
    assertEquals(fileName, ds.getName());
    assertTrue(ds instanceof ByteArrayDataSource);
    ByteArrayDataSource bds = (ByteArrayDataSource) ds;
    assertEquals("application/octet-stream", bds.getContentType());
    try (InputStream in = bds.getInputStream()) {
      byte[] data = IOUtility.readBytes(in);
      assertArrayEquals(sampleData, data);
    }

    new MailMessage().withBodyPlainText("test").withAttachment(new MailAttachment(ds));
    MimeMessage message = MailUtility.createMimeMessage(new MailMessage().withBodyPlainText("test"));
    message.writeTo(new ByteArrayOutputStream());
  }

  @Test
  public void testGetParts() throws IOException, MessagingException {
    final String plainText = "plain text";
    final String htmlText = "<html><body><p>plain text</p></body></html>";
    MailMessage definition = new MailMessage().withBodyPlainText(plainText).withBodyHtml(htmlText);

    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    definition.withAttachment(new MailAttachment(MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sample1.dat", null)));
    definition.withAttachment(new MailAttachment(MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sample2.dat", null)));
    definition.withAttachment(new MailAttachment(MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sämple3.dat", null)));

    MimeMessage message = MailUtility.createMimeMessage(definition);

    verifyMimeMessage(message, plainText, htmlText, "sample1.dat", "sample2.dat", "sämple3.dat");

    // validate that the method call does not throw an exception
    MailUtility.collectMailParts(null, null, null, null);
    MailUtility.collectMailParts(message, null, null, null);

    List<Part> bodyCollector = new ArrayList<Part>();
    List<Part> attachmentCollector = new ArrayList<Part>();
    List<Part> inlineAttachmentCollector = new ArrayList<Part>();
    MailUtility.collectMailParts(message, bodyCollector, attachmentCollector, inlineAttachmentCollector);
    assertEquals("body parts size is wrong", 2, bodyCollector.size());
    assertEquals("attachments parts size is wrong", 3, attachmentCollector.size());
    assertEquals("inline attachments parts size is wrong", 0, inlineAttachmentCollector.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateMimeMessageWithNull() throws Exception {
    MailUtility.createMimeMessage(null);
  }

  @Test
  public void testCreateMimeMessage() throws Exception {
    // no plain text or html body
    assertNull(MailUtility.createMimeMessage(new MailMessage()));

    final String plainText = "plain text";
    final String html = "<html><body><p>html</p></body></html>";

    MimeMessage plainTextMessage = MailUtility.createMimeMessage(new MailMessage().withBodyPlainText(plainText));
    verifyMimeMessage(plainTextMessage, plainText, null);

    MimeMessage htmlMessage = MailUtility.createMimeMessage(new MailMessage().withBodyHtml(html));
    verifyMimeMessage(htmlMessage, null, html);

    MimeMessage plainTextAndHtmlMessage = MailUtility.createMimeMessage(new MailMessage().withBodyPlainText(plainText).withBodyHtml(html));
    verifyMimeMessage(plainTextAndHtmlMessage, plainText, html);

    MailMessage definition =
        new MailMessage().withSubject("Subject").withBodyPlainText(plainText).withBodyHtml(html).withSender(createMailParticipant("info@example.org")).addToRecipients(createMailParticipants(CollectionUtility.arrayList("to1@example.org")));
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    final String attachmentContentId = "mycontentid";
    definition.withAttachment(new MailAttachment(MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sample1.dat", null), null, null, attachmentContentId));
    definition.addCcRecipient(createMailParticipant("cc1@example.org"));
    definition.addCcRecipient(createMailParticipant("cc2@example.org"));
    definition.addBccRecipient(createMailParticipant("bcc1@example.org"));
    definition.addBccRecipient(createMailParticipant("bcc2@example.org"));
    definition.addBccRecipient(createMailParticipant("bcc3@example.org"));
    MimeMessage msg = MailUtility.createMimeMessage(definition);
    verifyMimeMessage(msg, plainText, html, "sample1.dat");
    // exactly one, already verified by verify method
    Part attachmentPart = CollectionUtility.firstElement(MailUtility.getAttachmentParts(msg));
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
  public void testInlineAttachmentCollector() throws MessagingException {
    CharsetSafeMimeMessage message = new CharsetSafeMimeMessage();
    MimeMultipart multiPart = new MimeMultipart();
    message.setContent(multiPart);

    MimeBodyPart bodyPart = new MimeBodyPart();
    bodyPart.setText("plain text", StandardCharsets.UTF_8.name());
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
    assertEquals("body parts size is wrong", 1, bodyCollector.size());
    assertEquals("attachments parts size is wrong", 0, attachmentCollector.size());
    assertEquals("inline attachments parts size is wrong", 1, inlineAttachmentCollector.size());
  }

  @Test
  public void testAddAttachmentsToMimeMessage() throws IOException, MessagingException {
    // create html mime message without attachments
    final String plainText = "plain text";
    final String html = "<html><body><p>plain text</p></html>";
    MailMessage definition = new MailMessage().withBodyPlainText(plainText).withBodyHtml(html);
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
    verifyMimeMessage(message, plainText, html, "sample1.dat", "sample2.dat", "sample3_öüä.dat");

    // store and recreate mime message (byte[])
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    message.writeTo(bos);
    message = MailUtility.createMessageFromBytes(bos.toByteArray());

    // verify new instance
    verifyMimeMessage(message, plainText, html, "sample1.dat", "sample2.dat", "sample3_öüä.dat");
  }

  @Test
  public void testAddResourcesAsAttachments() throws IOException, MessagingException {
    // create html mime message without attachments
    final String plainText = "plain text";
    final String html = "<html><body><p>plain text</p></html>";
    MailMessage definition = new MailMessage().withBodyPlainText(plainText).withBodyHtml(html);
    MimeMessage message = MailUtility.createMimeMessage(definition);
    verifyMimeMessage(message, plainText, html /* no attachments*/);

    // add no attachments
    MailUtility.addAttachmentsToMimeMessage(message, null);
    verifyMimeMessage(message, plainText, html);
    MailUtility.addAttachmentsToMimeMessage(message, new ArrayList<File>());
    verifyMimeMessage(message, plainText, html);

    // add 3 attachments to mime message
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    List<BinaryResource> attachments = new ArrayList<BinaryResource>();
    attachments.add(new BinaryResource("sample1.dat", sampleData));
    attachments.add(new BinaryResource("sample2.dat", sampleData));
    attachments.add(new BinaryResource("sample3_öüä.dat", sampleData));
    MailUtility.addResourcesAsAttachments(message, attachments);

    // verify added attachments in java instance
    verifyMimeMessage(message, plainText, html, "sample1.dat", "sample2.dat", "sample3_öüä.dat");

    // store and recreate mime message (byte[])
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    message.writeTo(bos);
    message = MailUtility.createMessageFromBytes(bos.toByteArray());

    // verify new instance
    verifyMimeMessage(message, plainText, html, "sample1.dat", "sample2.dat", "sample3_öüä.dat");
  }

  @Test
  public void testMimeMessageDefinitionRecipients() {
    MailMessage definition = new MailMessage();
    definition.addToRecipient(createMailParticipant("to@example.org"));
    definition.addCcRecipient(createMailParticipant("cc@example.org"));
    definition.addBccRecipient(createMailParticipant("bcc@example.org"));

    assertEquals("Number of TO recipients is wrong", 1, definition.getToRecipients().size());
    assertEquals("Number of CC recipients is wrong", 1, definition.getCcRecipients().size());
    assertEquals("Number of BCC recipients is wrong", 1, definition.getBccRecipients().size());

    assertEquals("TO recipient is wrong", "to@example.org", CollectionUtility.firstElement(definition.getToRecipients()).toString());
    assertEquals("CC recipient is wrong", "cc@example.org", CollectionUtility.firstElement(definition.getCcRecipients()).toString());
    assertEquals("BCC recipient is wrong", "bcc@example.org", CollectionUtility.firstElement(definition.getBccRecipients()).toString());

    definition.clearToRecipients();
    assertEquals("Number of TO recipients is wrong", 0, definition.getToRecipients().size());
    assertEquals("Number of CC recipients is wrong", 1, definition.getCcRecipients().size());
    assertEquals("Number of BCC recipients is wrong", 1, definition.getBccRecipients().size());

    definition.clearCcRecipients();
    assertEquals("Number of TO recipients is wrong", 0, definition.getToRecipients().size());
    assertEquals("Number of CC recipients is wrong", 0, definition.getCcRecipients().size());
    assertEquals("Number of BCC recipients is wrong", 1, definition.getBccRecipients().size());

    definition.clearBccRecipients();
    assertEquals("Number of TO recipients is wrong", 0, definition.getToRecipients().size());
    assertEquals("Number of CC recipients is wrong", 0, definition.getCcRecipients().size());
    assertEquals("Number of BCC recipients is wrong", 0, definition.getBccRecipients().size());

    definition.addToRecipients(createMailParticipants(CollectionUtility.arrayList("to1@exapmle.org", "to2@example.org")));
    definition.addCcRecipients(createMailParticipants(CollectionUtility.arrayList("cc1@exapmle.org", "cc2@example.org", "cc3@example.org")));
    definition.addBccRecipients(createMailParticipants(CollectionUtility.arrayList("bcc1@exapmle.org", "bcc2@example.org", "bcc3@example.org", "bcc4@example.org")));

    assertEquals("Number of TO recipients is wrong", 2, definition.getToRecipients().size());
    assertEquals("Number of CC recipients is wrong", 3, definition.getCcRecipients().size());
    assertEquals("Number of BCC recipients is wrong", 4, definition.getBccRecipients().size());
  }

  @Test
  public void testMimeMessageDefinitionAttachments() throws Exception {
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};

    MailMessage definition = new MailMessage();
    definition.withAttachment(new MailAttachment(MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sample1.dat", null)));

    assertEquals("Number of attachments is wrong", 1, definition.getAttachments().size());

    definition.clearAttachments();

    assertEquals("Number of attachments is wrong", 0, definition.getAttachments().size());

    definition.withAttachments(CollectionUtility.arrayList(
        new MailAttachment(MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sample1.dat", null)),
        new MailAttachment(MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sample2.dat", null))));

    assertEquals("Number of attachments is wrong", 2, definition.getAttachments().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMimeMessageAttachmentWithNullDataSource1() {
    new MailAttachment((DataSource) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMimeMessageAttachmentWithNullDataSource2() {
    new MailAttachment(null, null, null, "mycontentid");
  }

  @Test
  public void testInternetAddress1() throws Exception {
    MailParticipant participant = new MailParticipant()
        .withEmail("test@gugus.com")
        .withName("André Böller");
    InternetAddress address = MailUtility.createInternetAddress(participant);
    String addressToString = address.toString();
    String addressPersonal = address.getPersonal();
    CharsetSafeMimeMessage msg = new CharsetSafeMimeMessage();
    msg.addRecipient(RecipientType.TO, address);

    InternetAddress address2 = (InternetAddress) msg.getRecipients(RecipientType.TO)[0];
    assertEquals(addressToString, address2.toString());
    assertEquals(addressPersonal, address2.getPersonal());
  }

  @Test
  public void testInternetAddress2() throws Exception {
    InternetAddress address = new InternetAddress("test@gugus.com", "André Böller", StandardCharsets.UTF_8.name());
    String addressToString = address.toString();
    String addressPersonal = address.getPersonal();
    CharsetSafeMimeMessage msg = new CharsetSafeMimeMessage();
    msg.addRecipient(RecipientType.TO, address);

    InternetAddress address2 = (InternetAddress) msg.getRecipients(RecipientType.TO)[0];
    assertEquals(addressToString, address2.toString());
    assertEquals(addressPersonal, address2.getPersonal());
  }

  @Test
  public void testInternetAddressIDN() throws Exception {
    final String email = "test@gügüs.com"; // internationalized domain name
    InternetAddress address = MailUtility.createInternetAddress(email, "André Böller");
    String addressToString = address.toString();
    String addressPersonal = address.getPersonal();
    CharsetSafeMimeMessage msg = new CharsetSafeMimeMessage();
    msg.addRecipient(RecipientType.TO, address);

    InternetAddress address2 = (InternetAddress) msg.getRecipients(RecipientType.TO)[0];
    assertEquals(addressToString, address2.toString());
    assertEquals(addressPersonal, address2.getPersonal());

    assertEquals(IDN.toASCII(email), address2.getAddress());
    assertEquals(email, IDN.toUnicode(address2.getAddress()));

    assertEquals("xn--peter@mller-zhb.de", MailUtility.createInternetAddress("peter@müller.de").getAddress());
  }

  @Test
  public void testInternetAddressEmpty() throws Exception {
    assertNull(MailUtility.createInternetAddress((String) null));
    assertNull(MailUtility.createInternetAddress((MailParticipant) null));
    assertNull(MailUtility.createInternetAddress(""));
  }

  @Test(expected = ProcessingException.class)
  public void testInternetAddressInvalid() throws Exception {
    assertNull(MailUtility.createInternetAddress("foo@bar@foo.de"));
  }

  @Test
  public void testParseInternetAddressListEmpty() {
    InternetAddress[] addresses = MailUtility.parseInternetAddressList(null);
    assertEquals(0, addresses.length);

    addresses = MailUtility.parseInternetAddressList("");
    assertEquals(0, addresses.length);
  }

  @Test
  public void testParseInternetAddressList() {
    runTestParseInternetAddressList("foo@bar.de");
    runTestParseInternetAddressList("foo@bür.de");
    runTestParseInternetAddressList("abc@abc.com", "abc@def.com", "ghi@abc.com");
    runTestParseInternetAddressList("abc@äöü.com", "abc@äöü.com", "ghi@äöü.com");
    runTestParseInternetAddressList("abc@foo.com", "abc@みんな.com", "ghi@äöü.com");
  }

  protected void runTestParseInternetAddressList(String... inputAddresses) {
    InternetAddress[] addresses = MailUtility.parseInternetAddressList(StringUtility.join(",", inputAddresses));
    assertEquals(inputAddresses.length, addresses.length);
    for (int i = 0; i < inputAddresses.length; i++) {
      assertEquals(IDN.toASCII(inputAddresses[i]), addresses[i].getAddress());
      assertEquals(inputAddresses[i], IDN.toUnicode(addresses[i].getAddress()));
    }
  }

  @Test(expected = ProcessingException.class)
  public void testParseInternetAddressListInvalid() {
    runTestParseInternetAddressList("foo@bar@de.de");
  }

  @Test(expected = ProcessingException.class)
  public void testParseInternetAddressListInvalid2() {
    runTestParseInternetAddressList("foo@bar.de", "foo@bar@de.de");
  }

  @Test(expected = UnsupportedCharsetException.class)
  public void testUnknownCharsetIsStillUnknown() {
    // pre-condition, charset is really (still) unknown
    Assert.assertEquals(null, Charset.forName(RARE_UNKNOWN_CHARSET));
  }

  protected MimeMessage createMimeMessageUsingUnknownEncoding() throws IOException, MessagingException {
    MailMessage definition = new MailMessage().withBodyPlainText("a");
    MimeMessage mimeMessage = MailUtility.createMimeMessage(definition);
    Object multipart0 = mimeMessage.getContent();
    Assert.assertTrue(multipart0 instanceof Multipart);
    Multipart multipart = (Multipart) multipart0;
    BodyPart plaintextPart = multipart.getBodyPart(0);
    plaintextPart.setHeader(MailUtility.CONTENT_TYPE_ID, "text/plain; charset=\"" + RARE_UNKNOWN_CHARSET + "\"");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    mimeMessage.writeTo(bos);
    mimeMessage = new MimeMessage(null, new ByteArrayInputStream(bos.toByteArray()));
    return mimeMessage;
  }

  @Test(expected = UnsupportedEncodingException.class)
  public void testReadingContentWithUnknownCharsetFails() throws IOException, MessagingException {
    MimeMessage mimeMessage = createMimeMessageUsingUnknownEncoding();
    Object multipart0 = mimeMessage.getContent();
    Assert.assertTrue(multipart0 instanceof Multipart);
    Multipart multipart = (Multipart) multipart0;
    BodyPart plaintextPart = multipart.getBodyPart(0);
    plaintextPart.getContent();
  }

  @Test
  public void testFallbackForUnknownEncoding() throws IOException, MessagingException {
    MimeMessage mimeMessage = createMimeMessageUsingUnknownEncoding();
    Assert.assertEquals("a", MailUtility.getPlainText(mimeMessage));
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
   * @throws IOException
   * @throws MessagingException
   */
  private void verifyMimeMessage(MimeMessage message, String plainText, String htmlText, String... attachmentFilenames) throws IOException, MessagingException {
    if (plainText != null) {
      assertEquals("wrong plain text", plainText, MailUtility.getPlainText(message));
    }
    else {
      assertNull("wrong plain text", MailUtility.getPlainText(message));
    }

    int bodyPartCount = 0;
    bodyPartCount += plainText == null ? 0 : 1;
    bodyPartCount += htmlText == null ? 0 : 1;

    List<Part> bodyParts = MailUtility.getBodyParts(message);
    assertEquals("body parts size is wrong", bodyPartCount, bodyParts.size());

    Part plainTextPart = MailUtility.getPlainTextPart(bodyParts);
    if (plainText != null) {
      assertNotNull("no plain text part found", plainTextPart);
      assertTrue("plain text part content is not string", plainTextPart.getContent() instanceof String);
      assertEquals("wrong plain text", plainText, (String) plainTextPart.getContent());
    }
    else {
      assertNull("plain text part found", plainTextPart);
    }

    Part htmlPart = MailUtility.getHtmlPart(bodyParts);
    if (htmlText != null) {
      assertNotNull("no html part found", htmlPart);
      assertTrue("html part content is not string", htmlPart.getContent() instanceof String);
      assertEquals("wrong html text", htmlText, (String) htmlPart.getContent());
    }
    else {
      assertNull("html part found", htmlPart);
    }

    List<Part> attachmentParts = MailUtility.getAttachmentParts(message);
    assertEquals("attachments parts size is wrong", attachmentFilenames.length, attachmentParts.size());
    Set<String> attachmentFilenamesSet = new HashSet<String>();
    for (Part part : attachmentParts) {
      attachmentFilenamesSet.add(MimeUtility.decodeText(part.getFileName()));
    }
    assertEquals("attachments filenames size is wrong", attachmentFilenames.length, attachmentFilenamesSet.size());
    for (String attachmentFilename : attachmentFilenames) {
      assertTrue("attachment filename " + attachmentFilename + " is missing", attachmentFilenamesSet.contains(attachmentFilename));
    }
  }

  /**
   * Helper method for test.
   */
  protected static MailParticipant createMailParticipant(String email) {
    return new MailParticipant().withEmail(email);
  }

  /**
   * Helper method for test.
   */
  protected static List<MailParticipant> createMailParticipants(List<String> emails) {
    List<MailParticipant> participants = new ArrayList<MailParticipant>();
    for (String email : emails) {
      participants.add(new MailParticipant().withEmail(email));
    }
    return participants;
  }
}
