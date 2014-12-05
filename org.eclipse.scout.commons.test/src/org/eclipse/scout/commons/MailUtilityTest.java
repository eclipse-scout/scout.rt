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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
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

    final String plainText = "plain text";
    final String htmlText = "<html><body><p>plain text</p></body></html>";
    MimeMessage message = MailUtility.createMimeMessage(plainText, htmlText, attachments);

    verifyPlainTextAndHtml(message, plainText, htmlText);

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

  private void verifyPlainTextAndHtml(MimeMessage message, String plainText, String htmlText) throws ProcessingException, IOException, MessagingException {
    Assert.assertEquals("wrong plain text", plainText, MailUtility.getPlainText(message));

    Part[] bodyParts = MailUtility.getBodyParts(message);
    Assert.assertEquals("body parts size is wrong", 2, bodyParts.length);

    Part[] attachmentParts = MailUtility.getAttachmentParts(message);
    Assert.assertEquals("attachments parts size is wrong", 3, attachmentParts.length);

    Part plainTextPart = MailUtility.getPlainTextPart(bodyParts);
    Assert.assertNotNull("no plain text part found", plainTextPart);
    Assert.assertTrue("plain text part content is not string", plainTextPart.getContent() instanceof String);
    Assert.assertEquals("wrong plain text", plainText, (String) plainTextPart.getContent());

    Part htmlPart = MailUtility.getHtmlPart(bodyParts);
    Assert.assertNotNull("no html part found", htmlPart);
    Assert.assertTrue("html part content is not string", htmlPart.getContent() instanceof String);
    Assert.assertEquals("wrong html text", htmlText, (String) htmlPart.getContent());
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
    MimeMessage message = MailUtility.createMimeMessage("plain text", "<html><body><p>plain text</p></html>", null);
    verifyAttachments(message /* no attachments*/);

    // add 3 attachments to mime message
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    List<File> attachments = new ArrayList<File>();
    attachments.add(IOUtility.createTempFile("sample1.dat", sampleData));
    attachments.add(IOUtility.createTempFile("sample2.dat", sampleData));
    attachments.add(IOUtility.createTempFile("sample3_öüä.dat", sampleData));
    MailUtility.addAttachmentsToMimeMessage(message, attachments);

    // verify added attachments in java instance
    verifyAttachments(message, "sample1.dat", "sample2.dat", MimeUtility.encodeText("sample3_öüä.dat"));

    // store and recreate mime message (byte[])
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    message.writeTo(bos);
    message = new MimeMessage(null, new ByteArrayInputStream(bos.toByteArray()));

    // verify new instance
    verifyAttachments(message, "sample1.dat", "sample2.dat", MimeUtility.encodeText("sample3_öüä.dat"));
  }

  @Test
  public void testCreateMimeMessageFromWordArchiveDirectory() throws ProcessingException, IOException, MessagingException {
    // create temp files for 3 attachments
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    List<File> attachments = new ArrayList<File>();
    attachments.add(IOUtility.createTempFile("sample1.dat", sampleData));
    attachments.add(IOUtility.createTempFile("sample2.dat", sampleData));
    attachments.add(IOUtility.createTempFile("sample3_öüä.dat", sampleData));

    // create files for word archive with plain text and html files (no additional attachments in archive)
    final String plainText = "plain text";
    final String htmlText = "<html><body><p>plain text</p></body></html>";
    final String archiveSimpleName = "myarchive";
    File archiveDir = IOUtility.createTempDirectory("word_archive");
    File txtFile = new File(archiveDir, archiveSimpleName + ".txt");
    File htmlFile = new File(archiveDir, archiveSimpleName + ".html");
    IOUtility.writeContent(new FileOutputStream(txtFile), plainText.getBytes());
    IOUtility.writeContent(new FileOutputStream(htmlFile), htmlText.getBytes());

    // create word archive file (zip)
    File archiveFile = IOUtility.createTempFile(archiveSimpleName + ".zip", null);
    FileUtility.compressArchive(archiveDir, archiveFile);

    // remove temp files
    txtFile.delete();
    htmlFile.delete();
    archiveDir.delete();

    // create mime message from word archive and verify message
    MimeMessage message = MailUtility.createMimeMessageFromWordArchive(archiveFile, attachments.toArray(new File[attachments.size()]));
    verifyPlainTextAndHtml(message, plainText, htmlText);
    verifyAttachments(message, "sample1.dat", "sample2.dat", MimeUtility.encodeText("sample3_öüä.dat"));
  }

  /**
   * Verifies that the mime message has the correct number of attachments with the correct filenames.
   *
   * @param message
   *          Message to check attachments
   * @param attachmentFilenames
   *          Filenames of attachments (use {@link MimeUtility#encodeText(String)} in case the filename as non-supported
   *          chars)
   * @throws ProcessingException
   * @throws MessagingException
   */
  protected void verifyAttachments(MimeMessage message, String... attachmentFilenames) throws ProcessingException, MessagingException {
    Part[] attachmentParts = MailUtility.getAttachmentParts(message);
    Assert.assertEquals("attachments parts size is wrong", attachmentFilenames.length, attachmentParts.length);
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
