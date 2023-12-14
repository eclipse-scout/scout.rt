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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import jakarta.activation.DataSource;
import jakarta.mail.BodyPart;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.util.ByteArrayDataSource;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link MailHelper}
 */
public class MailHelperTest {

  /**
   * Very rare, unknown character set. Currently, UTF-7, by default Oracle JDK does not support it.
   */
  @SuppressWarnings("InjectedReferences")
  private static final String RARE_UNKNOWN_CHARSET = "unicode-1-1-utf-7";

  /**
   * Message without sender can be created
   *
   * @throws ProcessingException
   *           ,MessagingException
   */
  @Test
  public void testMimeMessageWithoutSender() {
    MailMessage definition = new MailMessage().withBodyPlainText("Body");
    MimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(definition);
    assertNotNull(message);

    definition = new MailMessage().withSubject("Subject").withBodyPlainText("Body");
    message = BEANS.get(MailHelper.class).createMimeMessage(definition);
    assertNotNull(message);
  }

  @Test
  public void testCreateDataSource() throws Exception {
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    final String fileName = "test.file";
    File file = IOUtility.createTempFile(fileName, sampleData);
    DataSource ds = BEANS.get(MailHelper.class).createDataSource(file);
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

    DataSource ds = BEANS.get(MailHelper.class).createDataSource(new ByteArrayInputStream(sampleData), fileName, null);
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
    MimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(new MailMessage().withBodyPlainText("test"));
    message.writeTo(new ByteArrayOutputStream());
  }

  @Test
  public void testGetParts() throws IOException, MessagingException {
    final String plainText = "plain text";
    final String htmlText = "<html><body><p>plain text</p></body></html>";
    MailMessage definition = new MailMessage().withBodyPlainText(plainText).withBodyHtml(htmlText);

    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    definition.withAttachment(new MailAttachment(BEANS.get(MailHelper.class).createDataSource(new ByteArrayInputStream(sampleData), "sample1.dat", null)));
    definition.withAttachment(new MailAttachment(BEANS.get(MailHelper.class).createDataSource(new ByteArrayInputStream(sampleData), "sample2.dat", null)));
    definition.withAttachment(new MailAttachment(BEANS.get(MailHelper.class).createDataSource(new ByteArrayInputStream(sampleData), "sämple3.dat", null)));

    MimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(definition);

    verifyMimeMessage(message, plainText, htmlText, "sample1.dat", "sample2.dat", "sämple3.dat");

    // validate that the method call does not throw an exception
    BEANS.get(MailHelper.class).collectMailParts(null, null, null, null);
    BEANS.get(MailHelper.class).collectMailParts(message, null, null, null);

    List<Part> bodyCollector = new ArrayList<>();
    List<Part> attachmentCollector = new ArrayList<>();
    List<Part> inlineAttachmentCollector = new ArrayList<>();
    BEANS.get(MailHelper.class).collectMailParts(message, bodyCollector, attachmentCollector, inlineAttachmentCollector);
    assertEquals("body parts size is wrong", 2, bodyCollector.size());
    assertEquals("attachments parts size is wrong", 3, attachmentCollector.size());
    assertEquals("inline attachments parts size is wrong", 0, inlineAttachmentCollector.size());
  }

  @Test
  public void testInlineAttachmentCollector() throws MessagingException {
    CharsetSafeMimeMessage message = new CharsetSafeMimeMessage();
    MimeMultipart multiPart = new MimeMultipart();
    message.setContent(multiPart);

    MimeBodyPart bodyPart = new MimeBodyPart();
    bodyPart.setText("plain text", StandardCharsets.UTF_8.name());
    bodyPart.addHeader(MailHelper.CONTENT_TYPE_ID, MailHelper.CONTENT_TYPE_TEXT_PLAIN);
    multiPart.addBodyPart(bodyPart);

    BodyPart inlineAttachmentPart = new MimeBodyPart();
    inlineAttachmentPart.setContent("base-64-encoded-image-content", "image/gif");
    inlineAttachmentPart.addHeader(MailHelper.CONTENT_TYPE_ID, "image/gif; name=\"graycol.gif\"");
    inlineAttachmentPart.addHeader("Content-ID", "<5__=4EBBF65EEFABF58A8f9e8a9@example.org>");
    inlineAttachmentPart.addHeader("Content-Disposition", Part.INLINE);
    multiPart.addBodyPart(inlineAttachmentPart);

    List<Part> bodyCollector = new ArrayList<>();
    List<Part> attachmentCollector = new ArrayList<>();
    List<Part> inlineAttachmentCollector = new ArrayList<>();
    BEANS.get(MailHelper.class).collectMailParts(message, bodyCollector, attachmentCollector, inlineAttachmentCollector);
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
    MimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(definition);
    verifyMimeMessage(message, plainText, html /* no attachments*/);

    // add no attachments
    BEANS.get(MailHelper.class).addAttachmentsToMimeMessage(message, null);
    verifyMimeMessage(message, plainText, html);
    BEANS.get(MailHelper.class).addAttachmentsToMimeMessage(message, new ArrayList<>());
    verifyMimeMessage(message, plainText, html);

    // add 3 attachments to mime message
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    List<File> attachments = new ArrayList<>();
    attachments.add(IOUtility.createTempFile("sample1.dat", sampleData));
    attachments.add(IOUtility.createTempFile("sample2.dat", sampleData));
    attachments.add(IOUtility.createTempFile("sample3_öüä.dat", sampleData));
    BEANS.get(MailHelper.class).addAttachmentsToMimeMessage(message, attachments);

    // verify added attachments in java instance
    verifyMimeMessage(message, plainText, html, "sample1.dat", "sample2.dat", "sample3_öüä.dat");

    // store and recreate mime message (byte[])
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    message.writeTo(bos);
    message = BEANS.get(MailHelper.class).createMessageFromBytes(bos.toByteArray());

    // verify new instance
    verifyMimeMessage(message, plainText, html, "sample1.dat", "sample2.dat", "sample3_öüä.dat");
  }

  @Test
  public void testAddResourcesAsAttachments() throws IOException, MessagingException {
    // create html mime message without attachments
    final String plainText = "plain text";
    final String html = "<html><body><p>plain text</p></html>";
    MailMessage definition = new MailMessage().withBodyPlainText(plainText).withBodyHtml(html);
    MimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(definition);
    verifyMimeMessage(message, plainText, html /* no attachments*/);

    // add no attachments
    BEANS.get(MailHelper.class).addAttachmentsToMimeMessage(message, null);
    verifyMimeMessage(message, plainText, html);
    BEANS.get(MailHelper.class).addAttachmentsToMimeMessage(message, new ArrayList<>());
    verifyMimeMessage(message, plainText, html);

    // add 3 attachments to mime message
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    List<BinaryResource> attachments = new ArrayList<>();
    attachments.add(new BinaryResource("sample1.dat", sampleData));
    attachments.add(new BinaryResource("sample2.dat", sampleData));
    attachments.add(new BinaryResource("sample3_öüä.dat", sampleData));
    BEANS.get(MailHelper.class).addResourcesAsAttachments(message, attachments);

    // verify added attachments in java instance
    verifyMimeMessage(message, plainText, html, "sample1.dat", "sample2.dat", "sample3_öüä.dat");

    // store and recreate mime message (byte[])
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    message.writeTo(bos);
    message = BEANS.get(MailHelper.class).createMessageFromBytes(bos.toByteArray());

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
  public void testMimeMessageDefinitionAttachments() {
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};

    MailMessage definition = new MailMessage();
    definition.withAttachment(new MailAttachment(BEANS.get(MailHelper.class).createDataSource(new ByteArrayInputStream(sampleData), "sample1.dat", null)));

    assertEquals("Inline attachments should not be changed", 0, definition.getInlineAttachments().size());
    assertEquals("Number of attachments is wrong", 1, definition.getAttachments().size());

    definition.clearAttachments();

    assertEquals("Number of attachments is wrong", 0, definition.getAttachments().size());

    definition.withAttachments(CollectionUtility.arrayList(
        new MailAttachment(BEANS.get(MailHelper.class).createDataSource(new ByteArrayInputStream(sampleData), "sample1.dat", null)),
        new MailAttachment(BEANS.get(MailHelper.class).createDataSource(new ByteArrayInputStream(sampleData), "sample2.dat", null))));

    assertEquals("Number of attachments is wrong", 2, definition.getAttachments().size());
  }

  @Test
  public void testMimeMessageDefinitionInlineAttachments() {
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};

    MailMessage definition = new MailMessage();
    definition.withInlineAttachment(new MailAttachment(BEANS.get(MailHelper.class).createDataSource(new ByteArrayInputStream(sampleData), "sample1.dat", null)));

    assertEquals("Attachments should not be changed", 0, definition.getAttachments().size());
    assertEquals("Number of inline attachments is wrong", 1, definition.getInlineAttachments().size());

    definition.clearInlineAttachments();

    assertEquals("Number of inline attachments is wrong", 0, definition.getInlineAttachments().size());

    definition.withInlineAttachments(CollectionUtility.arrayList(
        new MailAttachment(BEANS.get(MailHelper.class).createDataSource(new ByteArrayInputStream(sampleData), "sample1.dat", null)),
        new MailAttachment(BEANS.get(MailHelper.class).createDataSource(new ByteArrayInputStream(sampleData), "sample2.dat", null))));

    assertEquals("Number of inline attachments is wrong", 2, definition.getInlineAttachments().size());
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
    InternetAddress address = BEANS.get(MailHelper.class).createInternetAddress(participant);
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

  @Test(expected = ProcessingException.class)
  public void testInternetAddress3() {
    // test an invalid address, expect a ProcessingException
    MailParticipant participant = new MailParticipant()
        .withEmail("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@example.com")
        .withName("Invalid address");
    BEANS.get(MailHelper.class).createInternetAddress(participant);
  }

  @Test
  public void testInternetAddressIDN() throws Exception {
    final String email = "test@gügüs.com"; // internationalized domain name
    InternetAddress address = BEANS.get(MailHelper.class).createInternetAddress(email, "André Böller");
    String addressToString = address.toString();
    String addressPersonal = address.getPersonal();
    CharsetSafeMimeMessage msg = new CharsetSafeMimeMessage();
    msg.addRecipient(RecipientType.TO, address);

    InternetAddress address2 = (InternetAddress) msg.getRecipients(RecipientType.TO)[0];
    assertEquals(addressToString, address2.toString());
    assertEquals(addressPersonal, address2.getPersonal());

    assertEquals(BEANS.get(MailIDNConverter.class).toASCII(email), address2.getAddress());
    assertEquals(email, BEANS.get(MailIDNConverter.class).toUnicode(address2.getAddress()));

    assertEquals("peter@xn--mller-kva.de", BEANS.get(MailHelper.class).createInternetAddress("peter@müller.de").getAddress());
  }

  @Test
  public void testInternetAddressEmpty() {
    assertNull(BEANS.get(MailHelper.class).createInternetAddress((String) null));
    assertNull(BEANS.get(MailHelper.class).createInternetAddress((MailParticipant) null));
    assertNull(BEANS.get(MailHelper.class).createInternetAddress(""));
  }

  @Test(expected = ProcessingException.class)
  public void testInternetAddressInvalid() {
    assertNull(BEANS.get(MailHelper.class).createInternetAddress("foo@bar@foo.de"));
  }

  @Test
  public void testParseInternetAddressListEmpty() {
    InternetAddress[] addresses = BEANS.get(MailHelper.class).parseInternetAddressList(null);
    assertEquals(0, addresses.length);

    addresses = BEANS.get(MailHelper.class).parseInternetAddressList("");
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
    InternetAddress[] addresses = BEANS.get(MailHelper.class).parseInternetAddressList(StringUtility.join(",", inputAddresses));
    assertEquals(inputAddresses.length, addresses.length);
    for (int i = 0; i < inputAddresses.length; i++) {
      assertEquals(BEANS.get(MailIDNConverter.class).toASCII(inputAddresses[i]), addresses[i].getAddress());
      assertEquals(inputAddresses[i], BEANS.get(MailIDNConverter.class).toUnicode(addresses[i].getAddress()));
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
    assertNull(Charset.forName(RARE_UNKNOWN_CHARSET));
  }

  protected MimeMessage createMimeMessageUsingUnknownEncoding() throws IOException, MessagingException {
    MailMessage definition = new MailMessage().withBodyPlainText("a");
    MimeMessage mimeMessage = BEANS.get(MailHelper.class).createMimeMessage(definition);
    mimeMessage.setHeader(MailHelper.CONTENT_TYPE_ID, "text/plain; charset=\"" + RARE_UNKNOWN_CHARSET + "\"");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    mimeMessage.writeTo(bos);
    mimeMessage = new MimeMessage(null, new ByteArrayInputStream(bos.toByteArray()));
    return mimeMessage;
  }

  @Test(expected = UnsupportedEncodingException.class)
  public void testReadingContentWithUnknownCharsetFails() throws IOException, MessagingException {
    MimeMessage mimeMessage = createMimeMessageUsingUnknownEncoding();
    mimeMessage.getContent();
  }

  @Test
  public void testFallbackForUnknownEncoding() throws IOException, MessagingException {
    MimeMessage mimeMessage = createMimeMessageUsingUnknownEncoding();
    Assert.assertEquals("a", BEANS.get(MailHelper.class).getPlainText(mimeMessage));
  }

  @Test
  public void testIsEmailAddressValid() {
    MailHelper mailHelper = BEANS.get(MailHelper.class);
    assertTrue(mailHelper.isEmailAddressValid("foo@bär.de"));
    assertTrue(mailHelper.isEmailAddressValid("foo@domaintest.みんな"));
    assertTrue(mailHelper.isEmailAddressValid("füü@bär.de"));

    assertFalse(mailHelper.isEmailAddressValid("foo@bär@de.de"));
    assertFalse(mailHelper.isEmailAddressValid("foo@domain/test.みんな"));
    assertFalse(mailHelper.isEmailAddressValid("foo@domain\test.みんな"));
    assertFalse(mailHelper.isEmailAddressValid("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@example.com"));
  }

  @Test
  public void testEnsureFromAddressWithoutFromAndDefaultFrom() throws MessagingException {
    MailMessage mailMessage = BEANS.get(MailMessage.class)
        .withBodyPlainText("lorem");

    MimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(mailMessage);
    BEANS.get(MailHelper.class).ensureFromAddress(message, null);

    assertNull("No 'from'", message.getFrom());
  }

  @Test
  public void testEnsureFromAddressWithoutFromButDefaultFrom() throws MessagingException {
    MailMessage mailMessage = BEANS.get(MailMessage.class)
        .withBodyPlainText("lorem");

    MimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(mailMessage);
    BEANS.get(MailHelper.class).ensureFromAddress(message, "default@example.org");

    assertNotNull("No 'from'", message.getFrom());
    assertEquals("Number of 'from' is wrong", 1, message.getFrom().length);
    assertEquals("Wrong 'from'", "default@example.org", ((InternetAddress) message.getFrom()[0]).getAddress());
  }

  @Test
  public void testEnsureFromAddressWithFrom() throws MessagingException {
    MailMessage mailMessage = BEANS.get(MailMessage.class)
        .withBodyPlainText("lorem")
        .withSender(BEANS.get(MailParticipant.class).withEmail("sender@example.org"));

    MimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(mailMessage);
    BEANS.get(MailHelper.class).ensureFromAddress(message, "default@example.org");

    assertNotNull("No 'from'", message.getFrom());
    assertEquals("Number of 'from' is wrong", 1, message.getFrom().length);
    assertEquals("Wrong 'from'", "sender@example.org", ((InternetAddress) message.getFrom()[0]).getAddress());
  }

  @Test
  public void testAddPrefixToSubject() throws MessagingException {
    BEANS.get(MailHelper.class).addPrefixToSubject(null, null);
    verifyAddPrefixToSubject(null, null, null);
    verifyAddPrefixToSubject("subject", null, "subject");
    verifyAddPrefixToSubject(null, "prefix", "prefix");
    verifyAddPrefixToSubject("subject", "prefix", "prefixsubject");
    verifyAddPrefixToSubject("lorem", "lorem", "lorem"); // no adding of prefix is subject already starts with prefix
  }

  @Test
  public void testExtractInReplyMessageIdsWithoutMessageId() throws MessagingException {
    assertEquals(0, BEANS.get(MailHelper.class).extractInReplyMessageIds(null).size());

    MailMessage mailMessage = BEANS.get(MailMessage.class)
        .withBodyPlainText("lorem")
        .addToRecipient(BEANS.get(MailParticipant.class).withEmail("recipient@example.org"))
        .withSender(BEANS.get(MailParticipant.class).withEmail("sender@example.org"));

    MimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(mailMessage);
    message.saveChanges();
    assertEquals(0, BEANS.get(MailHelper.class).extractInReplyMessageIds(message).size());
  }

  @Test
  public void testExtractInReplyMessageIds() throws MessagingException {
    MailMessage mailMessage = BEANS.get(MailMessage.class)
        .withBodyPlainText("lorem")
        .addToRecipient(BEANS.get(MailParticipant.class).withEmail("recipient@example.org"))
        .withSender(BEANS.get(MailParticipant.class).withEmail("sender@example.org"));

    MimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(mailMessage);
    message.saveChanges();
    String messageId = message.getMessageID();
    assertNotNull(messageId);

    MimeMessage replyMessage = (MimeMessage) message.reply(false);
    List<String> replyMessageIds = BEANS.get(MailHelper.class).extractInReplyMessageIds(replyMessage);
    assertEquals(1, replyMessageIds.size());
    assertEquals(messageId, replyMessageIds.get(0));
  }

  @Test
  public void testExtractInReplyMessageIdsWithCustomMessageId() throws MessagingException {
    MailMessage mailMessage = BEANS.get(MailMessage.class)
        .withBodyPlainText("lorem")
        .addToRecipient(BEANS.get(MailParticipant.class).withEmail("recipient@example.org"))
        .withSender(BEANS.get(MailParticipant.class).withEmail("sender@example.org"));

    CharsetSafeMimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(mailMessage);
    message.setCustomMessageId("<my-message-id@my-host.org>");
    message.saveChanges();
    String messageId = message.getMessageID();
    assertEquals("<my-message-id@my-host.org>", messageId);

    MimeMessage replyMessage = (MimeMessage) message.reply(false);
    List<String> replyMessageIds = BEANS.get(MailHelper.class).extractInReplyMessageIds(replyMessage);
    assertEquals(1, replyMessageIds.size());
    assertEquals(messageId, replyMessageIds.get(0));
  }

  @Test
  public void testGetPartCharset() throws MessagingException {
    MailHelper mailHelper = BEANS.get(MailHelper.class);

    assertNull(mailHelper.getPartCharset(null)); // no part

    MimeBodyPart part = new MimeBodyPart();
    assertNull(mailHelper.getPartCharset(part)); // no content type header

    part.setHeader(MailHelper.CONTENT_TYPE_ID, "text/plain"); // no charset parameter
    assertNull(mailHelper.getPartCharset(part));

    part.setHeader(MailHelper.CONTENT_TYPE_ID, "text/plain; charset=us-ascii");
    assertEquals(StandardCharsets.US_ASCII, mailHelper.getPartCharset(part));

    part.setHeader(MailHelper.CONTENT_TYPE_ID, "text/plain; charset=us-ascii; lorem=ipsum");
    assertEquals(StandardCharsets.US_ASCII, mailHelper.getPartCharset(part));

    part.setHeader(MailHelper.CONTENT_TYPE_ID, "text/plain; lorem=ipsum; charset=us-ascii");
    assertEquals(StandardCharsets.US_ASCII, mailHelper.getPartCharset(part));

    part.setHeader(MailHelper.CONTENT_TYPE_ID, "text/plain; charset=\"us-ascii\"");
    assertEquals(StandardCharsets.US_ASCII, mailHelper.getPartCharset(part));

    part.setHeader(MailHelper.CONTENT_TYPE_ID, "text/plain; charset=\"us-ascii\"; lorem=ipsum");
    assertEquals(StandardCharsets.US_ASCII, mailHelper.getPartCharset(part));

    part.setHeader(MailHelper.CONTENT_TYPE_ID, "text/plain; lorem=\"ipsum\"; charset=\"us-ascii\";");
    assertEquals(StandardCharsets.US_ASCII, mailHelper.getPartCharset(part));

    part.setHeader(MailHelper.CONTENT_TYPE_ID, "text/plain; lorem=\"ipsum\"; charset=\"UTF-8\";");
    assertEquals(StandardCharsets.UTF_8, mailHelper.getPartCharset(part));

    part.setHeader(MailHelper.CONTENT_TYPE_ID, "text/plain; charset=\"us-ascii"); // not valid according to RFC
    assertNull(mailHelper.getPartCharset(part));

    part.setHeader(MailHelper.CONTENT_TYPE_ID, "text/plain; charset=us-ascii\""); // not valid according to RFC
    assertNull(mailHelper.getPartCharset(part));

    part.setHeader(MailHelper.CONTENT_TYPE_ID, "text/plain; charset=my-own-charset\""); // not a valid charset
    assertNull(mailHelper.getPartCharset(part));
  }

  @Test
  public void testGetPlainText() {
    MailHelper mailHelper = BEANS.get(MailHelper.class);

    MimeMessage mimeMessage = mailHelper.createMimeMessage(BEANS.get(MailMessage.class).withBodyPlainText("plain text body\näpfel\nŻółw").withBodyHtml("<html><body>html body<br>äpfel<br>Żółw<body></html>"));
    assertEquals("plain text body\näpfel\nŻółw", mailHelper.getPlainText(mimeMessage));
  }

  @Test
  public void testGetHtmlBody() {
    MailHelper mailHelper = BEANS.get(MailHelper.class);

    MimeMessage mimeMessage = mailHelper.createMimeMessage(BEANS.get(MailMessage.class).withBodyPlainText("plain text body\näpfel\nŻółw").withBodyHtml("<html><body>html body<br>äpfel<br>Żółw<body></html>"));
    assertEquals("<html><body>html body<br>äpfel<br>Żółw<body></html>", mailHelper.getHtmlBody(mimeMessage));
  }

  @Test
  public void testReadContentAsString() {
    MailHelper mailHelper = BEANS.get(MailHelper.class);

    MimeMessage mimeMessage = mailHelper.createMimeMessage(BEANS.get(MailMessage.class).withBodyPlainText("plain text body\näpfel\nŻółw").withBodyHtml("<html><body>html body<br>äpfel<br>Żółw<body></html>"));
    List<Part> bodyParts = mailHelper.getBodyParts(mimeMessage);
    assertEquals("plain text body\näpfel\nŻółw", mailHelper.readContentAsString(mailHelper.getPlainTextPart(bodyParts)));
    assertEquals("<html><body>html body<br>äpfel<br>Żółw<body></html>", mailHelper.readContentAsString(mailHelper.getHtmlPart(bodyParts)));
  }

  @Test(expected = AssertionException.class)
  public void testGetAttachmentFilenameNoPart() {
    BEANS.get(MailHelper.class).getAttachmentFilename(null, s -> s);
  }

  @Test(expected = AssertionException.class)
  public void testGetAttachmentFilenameNoDefaultFilenameFunction() {
    MimeBodyPart part = new MimeBodyPart();
    BEANS.get(MailHelper.class).getAttachmentFilename(part, null);
  }

  @Test
  public void testGetAttachmentFilename() throws MessagingException, UnsupportedEncodingException {
    MailHelper mailHelper = BEANS.get(MailHelper.class);

    String defaultFilenameWithoutFileExtension = UUID.randomUUID().toString();
    Function<String, String> defaultFilenameFunction = (fileExtension) -> StringUtility.join(".", defaultFilenameWithoutFileExtension, fileExtension);

    MimeBodyPart part = new MimeBodyPart();
    assertEquals(defaultFilenameWithoutFileExtension + ".txt", mailHelper.getAttachmentFilename(part, defaultFilenameFunction)); // no filename or content type header, but MimeBodyPart#getContentType returns text/plain as fallback

    part.setFileName(MimeUtility.encodeText("=?UTF-8?Q?=c3=84pfel.png?="));
    assertEquals("Äpfel.png", mailHelper.getAttachmentFilename(part, defaultFilenameFunction));

    part = new MimeBodyPart();
    part.setHeader(MailHelper.CONTENT_TYPE_ID, "image/jpeg");
    assertEquals(defaultFilenameWithoutFileExtension + ".jpg", mailHelper.getAttachmentFilename(part, defaultFilenameFunction));
  }

  @Test
  public void testDecodeAttachmentFilename() {
    MailHelper mailHelper = BEANS.get(MailHelper.class);

    assertNull(mailHelper.decodeAttachmentFilename(null));
    assertEquals("", mailHelper.decodeAttachmentFilename(""));
    assertEquals("turtle.png", mailHelper.decodeAttachmentFilename("turtle.png"));
    assertEquals("Äpfel.png", mailHelper.decodeAttachmentFilename("=?UTF-8?Q?=c3=84pfel.png?="));
    assertEquals("Äpfel.png", mailHelper.decodeAttachmentFilename("=?iso-8859-2?Q?=C4pfel.png?="));
    assertEquals("żółw.png", mailHelper.decodeAttachmentFilename("=?UTF-8?B?xbzDs8WCdy5wbmc=?="));
    assertEquals("żółw.png", mailHelper.decodeAttachmentFilename("=?iso-8859-2?B?v/Ozdy5wbmc=?="));
    assertEquals("Żółw.png", mailHelper.decodeAttachmentFilename("=?utf-8?Q?Z=CC=87o=CC=81=C5=82w=2Epng?="));
  }

  @Test
  public void testGuessAttachmentFileExtension() {
    MailHelper mailHelper = BEANS.get(MailHelper.class);

    assertNull(mailHelper.guessAttachmentFileExtension(null));
    assertNull(mailHelper.guessAttachmentFileExtension(""));
    assertEquals("eml", mailHelper.guessAttachmentFileExtension(MailHelper.CONTENT_TYPE_MESSAGE_RFC822));
    assertEquals("jpg", mailHelper.guessAttachmentFileExtension("image/jpeg"));
    assertEquals("txt", mailHelper.guessAttachmentFileExtension("text/plain"));
    assertEquals("html", mailHelper.guessAttachmentFileExtension("text/html"));
    assertEquals("html", mailHelper.guessAttachmentFileExtension("text/html; charset=\"UTF-8\""));
    assertEquals("docx", mailHelper.guessAttachmentFileExtension("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
    assertEquals("pdf", mailHelper.guessAttachmentFileExtension("application/pdf"));
    assertNull(mailHelper.guessAttachmentFileExtension("lorem/ipsum")); // unknown
  }

  @Test
  public void testEmptyContentDisposition() {
    String eml = ""
        + "To: lorem@exampleorg\n"
        + "From: ipsum@example.org\n"
        + "MIME-Version: 1.0\n"
        + "Content-Type: text/plain; charset=utf-8\n"
        + "Content-Disposition: \n"
        + "Content-Transfer-Encoding: 7bit\n"
        + "\n"
        + "Lorem";

    MailHelper helper = BEANS.get(MailHelper.class);
    MimeMessage message = helper.createMessageFromBytes(eml.getBytes(StandardCharsets.UTF_8));
    assertEquals("Lorem", helper.getPlainText(message)); // failed with javax.mail.internet.ParseException: Expected disposition, got null
  }

  @Test
  public void testConvertMessageFromToBytes() throws Exception {
    MailHelper helper = BEANS.get(MailHelper.class);

    MailMessage mailMessage = BEANS.get(MailMessage.class)
        .withSubject("mock subject")
        .withBodyPlainText("lorem ipsum");
    MimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(mailMessage);

    byte[] bytes = helper.getMessageAsBytes(message);
    MimeMessage message2 = helper.createMessageFromBytes(bytes);

    assertEquals(message.getSubject(), message2.getSubject());
    assertEquals(message.getContent(), message2.getContent());
  }

  protected void verifyAddPrefixToSubject(String messageSubject, String subjectPrefix, String expectedSubject) throws MessagingException {
    MailMessage mailMessage = BEANS.get(MailMessage.class)
        .withSubject(messageSubject)
        .withBodyPlainText("lorem");

    MimeMessage message = BEANS.get(MailHelper.class).createMimeMessage(mailMessage);

    BEANS.get(MailHelper.class).addPrefixToSubject(message, subjectPrefix);
    assertEquals("Subject is wrong", expectedSubject, message.getSubject());
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
   */
  protected static void verifyMimeMessage(MimeMessage message, String plainText, String htmlText, String... attachmentFilenames) throws IOException, MessagingException {
    if (plainText != null) {
      assertEquals("wrong plain text", plainText, BEANS.get(MailHelper.class).getPlainText(message));
    }
    else {
      assertNull("wrong plain text", BEANS.get(MailHelper.class).getPlainText(message));
    }

    int bodyPartCount = 0;
    bodyPartCount += plainText == null ? 0 : 1;
    bodyPartCount += htmlText == null ? 0 : 1;

    List<Part> bodyParts = BEANS.get(MailHelper.class).getBodyParts(message);
    assertEquals("body parts size is wrong", bodyPartCount, bodyParts.size());

    Part plainTextPart = BEANS.get(MailHelper.class).getPlainTextPart(bodyParts);
    if (plainText != null) {
      assertNotNull("no plain text part found", plainTextPart);
      assertTrue("plain text part content is not string", plainTextPart.getContent() instanceof String);
      assertEquals("wrong plain text", plainText, plainTextPart.getContent());
    }
    else {
      assertNull("plain text part found", plainTextPart);
    }

    Part htmlPart = BEANS.get(MailHelper.class).getHtmlPart(bodyParts);
    if (htmlText != null) {
      assertNotNull("no html part found", htmlPart);
      assertTrue("html part content is not string", htmlPart.getContent() instanceof String);
      assertEquals("wrong html text", htmlText, htmlPart.getContent());
    }
    else {
      assertNull("html part found", htmlPart);
    }

    List<Part> attachmentParts = BEANS.get(MailHelper.class).getAttachmentParts(message);
    assertEquals("attachments parts size is wrong", attachmentFilenames.length, attachmentParts.size());
    Set<String> attachmentFilenamesSet = new HashSet<>();
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
    List<MailParticipant> participants = new ArrayList<>();
    for (String email : emails) {
      participants.add(new MailParticipant().withEmail(email));
    }
    return participants;
  }
}
