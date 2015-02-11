/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link WordMailUtility}
 */
@SuppressWarnings("restriction")
public class WordMailUtilityTest {

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

  @SuppressWarnings("deprecation")
  private boolean isMatching(String fileName) {
    return WordMailUtility.wordPatternItem.matcher(fileName).matches() || WordMailUtility.wordPatternProps.matcher(fileName).matches();
  }

  @SuppressWarnings("deprecation")
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
    MimeMessage message = WordMailUtility.createMimeMessageFromWordArchive(archiveFile, attachments.toArray(new File[attachments.size()]));
    verifyPlainTextAndHtml(message, plainText, htmlText);
    verifyAttachments(message, "sample1.dat", "sample2.dat", MimeUtility.encodeText("sample3_öüä.dat", "UTF-8", null));
  }

  private void verifyPlainTextAndHtml(MimeMessage message, String plainText, String htmlText) throws ProcessingException, IOException, MessagingException {
    Assert.assertEquals("wrong plain text", plainText, org.eclipse.scout.commons.mail.MailUtility.getPlainText(message));

    List<Part> bodyParts = org.eclipse.scout.commons.mail.MailUtility.getBodyParts(message);
    Assert.assertEquals("body parts size is wrong", 2, bodyParts.size());

    List<Part> attachmentParts = org.eclipse.scout.commons.mail.MailUtility.getAttachmentParts(message);
    Assert.assertEquals("attachments parts size is wrong", 3, attachmentParts.size());

    Part plainTextPart = org.eclipse.scout.commons.mail.MailUtility.getPlainTextPart(bodyParts);
    Assert.assertNotNull("no plain text part found", plainTextPart);
    Assert.assertTrue("plain text part content is not string", plainTextPart.getContent() instanceof String);
    Assert.assertEquals("wrong plain text", plainText, (String) plainTextPart.getContent());

    Part htmlPart = org.eclipse.scout.commons.mail.MailUtility.getHtmlPart(bodyParts);
    Assert.assertNotNull("no html part found", htmlPart);
    Assert.assertTrue("html part content is not string", htmlPart.getContent() instanceof String);
    Assert.assertEquals("wrong html text", htmlText, (String) htmlPart.getContent());
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
    List<Part> attachmentParts = org.eclipse.scout.commons.mail.MailUtility.getAttachmentParts(message);
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
