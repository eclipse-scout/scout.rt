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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;

import org.eclipse.scout.commons.MailUtility.MailMessage;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Non-public class encapsulating mail functionality concerned with the Word ZIP archive.
 *
 * @deprecated Will be removed in N release. Replacement: none.
 */
@Deprecated
@SuppressWarnings({"restriction", "deprecation"})
final class WordMailUtility {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(WordMailUtility.class);

  public static final Pattern wordPatternItem = Pattern.compile("item\\d{3,4}\\.xml");
  public static final Pattern wordPatternProps = Pattern.compile("props\\d{3,4}\\.xml");

  private static final String UTF_8 = "UTF-8";

  /**
   * @deprecated Will be removed in N release. Replacement: none.
   */
  @Deprecated
  public static MailMessage extractMailMessageFromWordArchive(File archiveFile) {
    return extractMailMessageFromWordArchiveInternal(archiveFile);
  }

  private static MailMessage extractMailMessageFromWordArchiveInternal(File archiveFile) {
    MailMessage mailMessage = null;
    File tempDir = extractWordArchive(archiveFile);
    String simpleName = extractSimpleNameFromWordArchive(archiveFile);
    String messagePlainText = extractPlainTextFromWordArchiveInternal(tempDir, simpleName);
    String messageHtml = extractHtmlFromWordArchiveInternal(tempDir, simpleName);
    // replace directory entry
    // replace all paths to the 'files directory' with the root directory
    File attachmentFolder = null;
    if (tempDir.isDirectory()) {
      for (File file : tempDir.listFiles()) {
        if (file.isDirectory() && file.getName().startsWith(simpleName)) {
          attachmentFolder = file;
          break;
        }
      }
    }
    String folderName = null;
    if (attachmentFolder != null) {
      folderName = attachmentFolder.getName();
    }
    messageHtml = messageHtml.replaceAll(folderName + "/", "");
    messageHtml = removeWordTags(messageHtml);
    // now loop through the directory and search all the files needed for a correct representation of the html mail
    List<File> attachmentList = new ArrayList<File>();
    if (attachmentFolder != null) {
      for (File attFile : attachmentFolder.listFiles()) {
        // exclude Microsoft Word specific directory file. This is only used to edit HTML in Word.
        if (!attFile.isDirectory() && !isWordSpecificFile(attFile.getName())) {
          attachmentList.add(attFile);
        }
      }
    }
    mailMessage = new MailMessage(messagePlainText, messageHtml, attachmentList);
    return mailMessage;
  }

  private static String extractHtmlFromWordArchiveInternal(File dir, String simpleName) {
    String txt = null;
    try {
      txt = extractTextFromWordArchiveInternal(dir, simpleName, "html");
    }
    catch (Exception e) {
      LOG.error("Error occured while trying to extract plain text file", e);
    }
    return txt;
  }

  private static String extractSimpleNameFromWordArchive(File archiveFile) {
    String simpleName = archiveFile.getName();
    if (archiveFile.getName().lastIndexOf('.') != -1) {
      simpleName = archiveFile.getName().substring(0, archiveFile.getName().lastIndexOf('.'));
    }
    return simpleName;
  }

  private static File extractWordArchive(File archiveFile) {
    File tempDir = null;
    try {
      tempDir = IOUtility.createTempDirectory("");
      FileUtility.extractArchive(archiveFile, tempDir);
    }
    catch (Exception e) {
      LOG.error("Error occured while trying to extract word archive", e);
    }
    return tempDir;
  }

  /**
   * @deprecated Will be removed in N release. Replacement: none.
   */
  @Deprecated
  public static String extractPlainTextFromWordArchive(File archiveFile) {
    return extractPlainTextFromWordArchiveInternal(archiveFile);
  }

  private static String extractPlainTextFromWordArchiveInternal(File archiveFile) {
    File tempDir = extractWordArchive(archiveFile);
    String simpleName = extractSimpleNameFromWordArchive(archiveFile);
    return extractPlainTextFromWordArchiveInternal(tempDir, simpleName);
  }

  private static String extractPlainTextFromWordArchiveInternal(File dir, String simpleName) {
    String plainText = null;
    try {
      plainText = extractTextFromWordArchiveInternal(dir, simpleName, "txt");
    }
    catch (Exception e) {
      LOG.error("Error occured while trying to extract plain text file", e);
    }
    return plainText;
  }

  private static String extractTextFromWordArchiveInternal(File dir, String simpleName, String fileType) throws ProcessingException, IOException {
    String txt = null;
    File plainTextFile = new File(dir, simpleName + "." + fileType);
    if (plainTextFile.exists() && plainTextFile.canRead()) {
      txt = IOUtility.getContentInEncoding(plainTextFile.getPath(), UTF_8);
    }
    return txt;
  }

  /**
   * @deprecated Will be removed in N release. Replacement: none.
   */
  @Deprecated
  public static MimeMessage createMimeMessageFromWordArchiveDirectory(File archiveDir, String simpleName, File[] attachments, boolean markAsUnsent) throws ProcessingException {
    return createMimeMessageFromWordArchiveInternal(archiveDir, simpleName, attachments, markAsUnsent);
  }

  /**
   * @deprecated Will be removed in N release. Replacement: none.
   */
  @Deprecated
  public static MimeMessage createMimeMessageFromWordArchive(File archiveFile, File[] attachments) throws ProcessingException {
    return createMimeMessageFromWordArchive(archiveFile, attachments, false);
  }

  /**
   * @deprecated Will be removed in N release. Replacement: none.
   */
  @Deprecated
  public static MimeMessage createMimeMessageFromWordArchive(File archiveFile, File[] attachments, boolean markAsUnsent) throws ProcessingException {
    try {
      File tempDir = IOUtility.createTempDirectory("");
      FileUtility.extractArchive(archiveFile, tempDir);

      String simpleName = archiveFile.getName();
      if (archiveFile.getName().lastIndexOf('.') != -1) {
        simpleName = archiveFile.getName().substring(0, archiveFile.getName().lastIndexOf('.'));
      }
      return createMimeMessageFromWordArchiveInternal(tempDir, simpleName, attachments, markAsUnsent);
    }
    catch (ProcessingException pe) {
      throw pe;
    }
    catch (IOException e) {
      throw new ProcessingException("Error occured while accessing files", e);
    }
  }

  private static MimeMessage createMimeMessageFromWordArchiveInternal(File archiveDir, String simpleName, File[] attachments, boolean markAsUnsent) throws ProcessingException {
    try {
      File plainTextFile = new File(archiveDir, simpleName + ".txt");
      String plainTextMessage = null;
      boolean hasPlainText = false;
      if (plainTextFile.exists()) {
        plainTextMessage = IOUtility.getContentInEncoding(plainTextFile.getPath(), UTF_8);
        hasPlainText = StringUtility.hasText(plainTextMessage);
      }

      String folderName = null;
      List<DataSource> htmlDataSourceList = new ArrayList<DataSource>();
      for (File filesFolder : archiveDir.listFiles()) {
        // in this archive file, exactly one directory should exist
        // word names this directory differently depending on the language
        if (filesFolder.isDirectory() && filesFolder.getName().startsWith(simpleName)) {
          // we accept the first directory that meets the constraint above
          // add all auxiliary files as attachment
          folderName = filesFolder.getName();
          for (File file : filesFolder.listFiles()) {
            // exclude Microsoft Word specific directory file. This is only used to edit HTML in Word.
            String filename = file.getName();
            if (!isWordSpecificFile(filename)) {
              FileDataSource fds = new FileDataSource(file);
              htmlDataSourceList.add(fds);
            }
          }
          break;
        }
      }

      File htmlFile = new File(archiveDir, simpleName + ".html");
      String htmlMessage = null;
      boolean hasHtml = false;
      if (htmlFile.exists()) {
        htmlMessage = IOUtility.getContentInEncoding(htmlFile.getPath(), UTF_8);
        // replace directory entry
        // replace all paths to the 'files directory' with the root directory
        htmlMessage = htmlMessage.replaceAll("\"" + folderName + "/", "\"cid:");

        htmlMessage = removeWordTags(htmlMessage);
        // remove any VML elements
        htmlMessage = htmlMessage.replaceAll("<!--\\[if gte vml 1(.*\\r?\\n)*?.*?endif\\]-->", "");
        // remove any VML elements part2
        htmlMessage = Pattern.compile("<!\\[if !vml\\]>(.*?)<!\\[endif\\]>", Pattern.DOTALL).matcher(htmlMessage).replaceAll("$1");
        // remove not referenced attachments
        for (Iterator<DataSource> it = htmlDataSourceList.iterator(); it.hasNext();) {
          DataSource ds = it.next();
          if (!htmlMessage.contains("cid:" + ds.getName())) {
            it.remove();
          }
        }
        hasHtml = StringUtility.hasText(htmlMessage);
      }

      if (!hasPlainText && !hasHtml) {
        throw new ProcessingException("message has no body");
      }

      MimeMessage mimeMessage = new CharsetSafeMimeMessage();
      MimePart bodyPart = null;
      if (attachments != null && attachments.length > 0) {
        MimeMultipart multiPart = new MimeMultipart(); //mixed
        mimeMessage.setContent(multiPart);
        //add a holder for the body text
        MimeBodyPart multiPartBody = new MimeBodyPart();
        multiPart.addBodyPart(multiPartBody);
        bodyPart = multiPartBody;
        //add the attachments
        for (File attachment : attachments) {
          MimeBodyPart part = new MimeBodyPart();
          FileDataSource fds = new FileDataSource(attachment);
          DataHandler handler = new DataHandler(fds);
          part.setDataHandler(handler);
          part.setFileName(MimeUtility.encodeText(attachment.getName(), "UTF-8", null));
          multiPart.addBodyPart(part);
        }
      }
      else {
        //no attachments -> no need for multipart/mixed element
        bodyPart = mimeMessage;
      }

      if (hasPlainText && hasHtml) {
        MimeMultipart alternativePart = new MimeMultipart("alternative");
        bodyPart.setContent(alternativePart);
        MimeBodyPart plainBodyPart = new MimeBodyPart();
        alternativePart.addBodyPart(plainBodyPart);
        writePlainBody(plainBodyPart, plainTextMessage);
        MimeBodyPart htmlBodyPart = new MimeBodyPart();
        alternativePart.addBodyPart(htmlBodyPart);
        writeHtmlBody(htmlBodyPart, htmlMessage, htmlDataSourceList);
      }
      else if (hasPlainText) { //plain text only
        writePlainBody(bodyPart, plainTextMessage);
      }
      else { //html only
        writeHtmlBody(bodyPart, htmlMessage, htmlDataSourceList);
      }

      if (markAsUnsent) {
        mimeMessage.setHeader("X-Unsent", "1"); // only supported in Outlook 2010
      }
      return mimeMessage;
    }
    catch (MessagingException e) {
      throw new ProcessingException("Error occured while creating MIME-message", e);
    }
    catch (UnsupportedEncodingException e) {
      throw new ProcessingException("Error occured while creating MIME-message", e);
    }
  }

  private static String removeWordTags(String htmlMessage) {
    // remove special/unused files
    htmlMessage = htmlMessage.replaceAll("<link rel=File-List href=\"cid:filelist.xml\">", "");
    htmlMessage = htmlMessage.replaceAll("<link rel=colorSchemeMapping href=\"cid:colorschememapping.xml\">", "");
    htmlMessage = htmlMessage.replaceAll("<link rel=themeData href=\"cid:themedata.thmx\">", "");
    htmlMessage = htmlMessage.replaceAll("<link rel=Edit-Time-Data href=\"cid:editdata.mso\">", "");

    // remove Microsoft Word tags
    htmlMessage = htmlMessage.replaceAll("<!--\\[if gte mso(.*\\r?\\n)*?.*?endif\\]-->", "");

    return htmlMessage;
  }

  private static boolean isWordSpecificFile(String filename) {
    return "filelist.xml".equalsIgnoreCase(filename)
        || "colorschememapping.xml".equalsIgnoreCase(filename)
        || "themedata.thmx".equalsIgnoreCase(filename)
        || "header.html".equalsIgnoreCase(filename)
        || "editdata.mso".equalsIgnoreCase(filename)
        || wordPatternItem.matcher(filename).matches()
        || wordPatternProps.matcher(filename).matches();
  }

  private static void writeHtmlBody(MimePart htmlBodyPart, String htmlMessage, List<DataSource> htmlDataSourceList) throws MessagingException {
    Multipart multiPartHtml = new MimeMultipart("related");
    htmlBodyPart.setContent(multiPartHtml);
    MimeBodyPart part = new MimeBodyPart();
    part.setContent(htmlMessage, MailUtility.CONTENT_TYPE_TEXT_HTML);
    part.setHeader(MailUtility.CONTENT_TYPE_ID, MailUtility.CONTENT_TYPE_TEXT_HTML);
    part.setHeader(MailUtility.CONTENT_TRANSFER_ENCODING_ID, MailUtility.QUOTED_PRINTABLE);
    multiPartHtml.addBodyPart(part);
    for (DataSource source : htmlDataSourceList) {
      part = new MimeBodyPart();
      DataHandler handler = new DataHandler(source);
      part.setDataHandler(handler);
      part.setFileName(source.getName());
      part.addHeader("Content-ID", "<" + source.getName() + ">");
      multiPartHtml.addBodyPart(part);
    }
  }

  private static void writePlainBody(MimePart plainBodyPart, String plainTextMessage) throws MessagingException {
    plainBodyPart.setText(plainTextMessage, UTF_8);
    plainBodyPart.setHeader(MailUtility.CONTENT_TYPE_ID, MailUtility.CONTENT_TYPE_TEXT_PLAIN);
    plainBodyPart.setHeader(MailUtility.CONTENT_TRANSFER_ENCODING_ID, MailUtility.QUOTED_PRINTABLE);
  }
}
