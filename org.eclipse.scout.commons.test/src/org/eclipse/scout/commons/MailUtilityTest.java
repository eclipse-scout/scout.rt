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
package org.eclipse.scout.commons;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

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
    MimeMessage message = MailUtility.createMimeMessage("Body", null, null);
    assertNotNull(message);
    message = MailUtility.createMimeMessage(null, null, "Subject", "Body", null);
    assertNotNull(message);
  }

  @Test
  public void testWordSpecificPattern() throws Exception {
    assertFalse(isMatching("foobar.xml"));
    assertFalse(isMatching("item.xml"));
    assertFalse(isMatching("item0.xml"));
    assertFalse(isMatching("item00.xml"));
    assertTrue(isMatching("item000.xml"));
    assertTrue(isMatching("item001.xml"));
    assertTrue(isMatching("item0000.xml"));
    assertFalse(isMatching("item00000.xml"));

    assertFalse(isMatching("props.xml"));
    assertFalse(isMatching("props0.xml"));
    assertFalse(isMatching("props01.xml"));
    assertTrue(isMatching("props002.xml"));
    assertTrue(isMatching("props012.xml"));
    assertTrue(isMatching("props0123.xml"));
    assertFalse(isMatching("props01234.xml"));
  }

  private boolean isMatching(String fileName) {
    return MailUtility.wordPatternItem.matcher(fileName).matches() || MailUtility.wordPatternProps.matcher(fileName).matches();
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

    MimeMessage message = MailUtility.createMimeMessage("test", null, new DataSource[]{ds});
    message.writeTo(new ByteArrayOutputStream());
  }

  @Test
  public void testGetParts() throws ProcessingException, IOException, MessagingException {
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    DataSource[] attachments = new DataSource[]{
        MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sample1.dat", null),
        MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sample2.dat", null),
        MailUtility.createDataSource(new ByteArrayInputStream(sampleData), "sample3.dat", null)
    };

    MimeMessage message = MailUtility.createMimeMessage("plain text", "<html><body><p>plain text</p></html>", attachments);

    Assert.assertEquals("wrong plain text", "plain text", MailUtility.getPlainText(message));

    Part[] bodyParts = MailUtility.getBodyParts(message);
    Assert.assertEquals("body parts size is wrong", 2, bodyParts.length);

    Part[] attachmentParts = MailUtility.getAttachmentParts(message);
    Assert.assertEquals("attachments parts size is wrong", 3, attachmentParts.length);

    Part plainTextPart = MailUtility.getPlainTextPart(bodyParts);
    Assert.assertNotNull("no plain text part found", plainTextPart);
    Assert.assertTrue("plain text part content is not string", plainTextPart.getContent() instanceof String);
    Assert.assertEquals("wrong plain text", "plain text", (String) plainTextPart.getContent());

    Part htmlPart = MailUtility.getHtmlPart(bodyParts);
    Assert.assertNotNull("no html part found", htmlPart);
    Assert.assertTrue("html part content is not string", htmlPart.getContent() instanceof String);
    Assert.assertEquals("wrong html text", "<html><body><p>plain text</p></html>", (String) htmlPart.getContent());

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
    MimeMessage message = MailUtility.createMimeMessage("plain text", "<html><body><p>plain text</p></html>", null);
    Assert.assertEquals("attachments parts size is wrong", 0, MailUtility.getAttachmentParts(message).length);

    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    List<File> attachments = new ArrayList<File>();

    attachments.add(IOUtility.createTempFile("sample1.dat", sampleData));
    attachments.add(IOUtility.createTempFile("sample2.dat", sampleData));
    attachments.add(IOUtility.createTempFile("sample3.dat", sampleData));
    MailUtility.addAttachmentsToMimeMessage(message, attachments);
    Assert.assertEquals("attachments parts size is wrong", 3, MailUtility.getAttachmentParts(message).length);
  }
}
