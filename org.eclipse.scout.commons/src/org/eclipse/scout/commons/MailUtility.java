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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.util.ByteArrayDataSource;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public final class MailUtility {

  public static final IScoutLogger LOG = ScoutLogManager.getLogger(MailUtility.class);

  private static final String CONTENT_TYPE_ID = "Content-Type";
  public static final String CONTENT_TYPE_TEXT_HTML = "text/html; charset=\"UTF-8\"";
  public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain; charset=\"UTF-8\"";
  public static final String CONTENT_TYPE_MESSAGE_RFC822 = "message/rfc822";
  public static final String CONTENT_TYPE_MULTIPART = "alternative";

  private static MailUtility instance = new MailUtility();

  private MailUtility() {
  }

  public static Part[] getBodyParts(Part message) throws ProcessingException {
    return instance.getBodyPartsImpl(message);
  }

  private Part[] getBodyPartsImpl(Part message) throws ProcessingException {
    List<Part> bodyCollector = new ArrayList<Part>();
    List<Part> attachementCollector = new ArrayList<Part>();
    collectMailPartsReqImpl(message, bodyCollector, attachementCollector);
    return bodyCollector.toArray(new Part[bodyCollector.size()]);
  }

  public static Part[] getAttachmentParts(Part message) throws ProcessingException {
    return instance.getAttachmentPartsImpl(message);
  }

  private Part[] getAttachmentPartsImpl(Part message) throws ProcessingException {
    List<Part> bodyCollector = new ArrayList<Part>();
    List<Part> attachementCollector = new ArrayList<Part>();
    collectMailPartsReqImpl(message, bodyCollector, attachementCollector);
    return attachementCollector.toArray(new Part[attachementCollector.size()]);
  }

  public static void collectMailParts(Part message, List<Part> bodyCollector, List<Part> attachementCollector) throws ProcessingException {
    instance.collectMailPartsReqImpl(message, bodyCollector, attachementCollector);
  }

  private void collectMailPartsReqImpl(Part part, List<Part> bodyCollector, List<Part> attachementCollector) throws ProcessingException {
    if (part == null) {
      return;
    }
    try {
      String disp = part.getDisposition();
      if (disp != null && disp.equalsIgnoreCase(Part.ATTACHMENT)) {
        attachementCollector.add(part);
      }
      else if (part.getContent() instanceof Multipart) {
        Multipart multiPart = (Multipart) part.getContent();
        for (int i = 0; i < multiPart.getCount(); i++) {
          collectMailPartsReqImpl(multiPart.getBodyPart(i), bodyCollector, attachementCollector);
        }
      }
      else {
        if (part.isMimeType(CONTENT_TYPE_TEXT_PLAIN)) {
          bodyCollector.add(part);
        }
        else if (part.isMimeType(CONTENT_TYPE_TEXT_HTML)) {
          bodyCollector.add(part);
        }
        else if (part.isMimeType(CONTENT_TYPE_MESSAGE_RFC822)) {
          // its a MIME message in rfc822 format as attachment therefore we have to set the filename for the attachment correctly.
          MimeMessage msg = (MimeMessage) part.getContent();
          part.setFileName(msg.getSubject() + ".eml");
          attachementCollector.add(part);
        }
      }
    }
    catch (ProcessingException e) {
      throw e;
    }
    catch (Throwable t) {
      throw new ProcessingException("Unexpected: ", t);
    }
  }

  /**
   * @param part
   * @return the plainText part encoded with the encoding given in the MIME header or UTF-8 encoded or null if the
   *         plainText Part is not given
   * @throws ProcessingException
   */
  public static String getPlainText(Part part) throws ProcessingException {
    return instance.getPlainTextImpl(part);
  }

  private String getPlainTextImpl(Part part) throws ProcessingException {
    String text = null;
    try {
      Part[] bodyParts = getBodyPartsImpl(part);
      Part plainTextPart = getPlainTextPart(bodyParts);

      if (plainTextPart instanceof MimePart) {
        MimePart mimePart = (MimePart) plainTextPart;
        byte[] content = IOUtility.getContent(mimePart.getInputStream());
        if (content != null) {
          try {
            text = new String(content, getCharacterEncodingOfMimePart(mimePart));
          }
          catch (UnsupportedEncodingException e) {
            text = new String(content);
          }

        }
      }
    }
    catch (ProcessingException e) {
      throw e;
    }
    catch (Throwable t) {
      throw new ProcessingException("Unexpected: ", t);
    }
    return text;
  }

  public static Part getHtmlPart(Part[] bodyParts) throws ProcessingException {
    for (Part p : bodyParts) {
      try {
        if (p != null && p.isMimeType(CONTENT_TYPE_TEXT_HTML)) {
          return p;
        }
      }
      catch (Throwable t) {
        throw new ProcessingException("Unexpected: ", t);
      }
    }
    return null;
  }

  public static Part getPlainTextPart(Part[] bodyParts) throws ProcessingException {
    for (Part p : bodyParts) {
      try {
        if (p != null && p.isMimeType(CONTENT_TYPE_TEXT_PLAIN)) {
          return p;
        }
      }
      catch (Throwable t) {
        throw new ProcessingException("Unexpected: ", t);
      }
    }
    return null;
  }

  public static DataSource createDataSource(File file) throws ProcessingException {
    try {
      int indexDot = file.getName().lastIndexOf(".");
      if (indexDot > 0) {
        String fileName = file.getName();
        String ext = fileName.substring(indexDot + 1);
        return instance.createDataSourceImpl(new FileInputStream(file), fileName, ext);
      }
      else {
        return null;
      }
    }
    catch (Throwable t) {
      throw new ProcessingException("Unexpected: ", t);
    }
  }

  public static DataSource createDataSource(InputStream inStream, String fileName, String fileExtension) throws ProcessingException {
    return instance.createDataSourceImpl(inStream, fileName, fileExtension);
  }

  /**
   * @param inStream
   * @param fileName
   *          e.g. "file.txt"
   * @param fileExtension
   *          e.g. "txt", "jpg"
   * @return
   * @throws ProcessingException
   */
  private DataSource createDataSourceImpl(InputStream inStream, String fileName, String fileExtension) throws ProcessingException {
    try {
      ByteArrayDataSource item = new ByteArrayDataSource(inStream, getContentTypeForExtension(fileExtension));
      item.setName(fileName);
      return item;
    }
    catch (Throwable t) {
      throw new ProcessingException("Unexpected: ", t);
    }
  }

  public static String extractPlainTextFromWordArchive(File archiveFile) {
    return instance.extractPlainTextFromWordArchiveInternal(archiveFile);
  }

  private String extractPlainTextFromWordArchiveInternal(File archiveFile) {
    String plainText = null;
    try {
      File tempDir = IOUtility.createTempDirectory("");
      FileUtility.extractArchive(archiveFile, tempDir);

      String simpleName = archiveFile.getName();
      if (archiveFile.getName().lastIndexOf('.') != -1) {
        simpleName = archiveFile.getName().substring(0, archiveFile.getName().lastIndexOf('.'));
      }

      File plainTextFile = new File(tempDir, simpleName + ".txt");
      if (plainTextFile.exists() && plainTextFile.canRead()) {
        Reader reader = new FileReader(plainTextFile);
        plainText = IOUtility.getContent(reader);
        reader.close();
      }
    }
    catch (Exception e) {
      LOG.error("Error occured while trying to extract plain text file", e);
    }
    return plainText;
  }

  /**
   * Create {@link MimeMessage} from plain text fields.
   * 
   * @rn aho, 19.01.2009
   */
  public static MimeMessage createMimeMessage(String[] toRecipients, String sender, String subject, String bodyTextPlain, DataSource[] attachements) throws ProcessingException {
    return instance.createMimeMessageInternal(toRecipients, null, null, sender, subject, bodyTextPlain, attachements);
  }

  /**
   * Create {@link MimeMessage} from plain text fields.
   * 
   * @rn aho, 19.01.2009
   */
  public static MimeMessage createMimeMessage(String[] toRecipients, String[] ccRecipients, String[] bccRecipients, String sender, String subject, String bodyTextPlain, DataSource[] attachements) throws ProcessingException {
    return instance.createMimeMessageInternal(toRecipients, ccRecipients, bccRecipients, sender, subject, bodyTextPlain, attachements);
  }

  private MimeMessage createMimeMessageInternal(String[] toRecipients, String[] ccRecipients, String[] bccRecipients, String sender, String subject, String bodyTextPlain, DataSource[] attachements) throws ProcessingException {
    try {
      MimeMessage msg = MailUtility.createMimeMessage(bodyTextPlain, null, attachements);
      if (sender != null) {
        InternetAddress addrSender = new InternetAddress(sender);
        msg.setFrom(addrSender);
        msg.setSender(addrSender);
      }
      msg.setSentDate(new java.util.Date());
      //
      msg.setSubject(subject, "UTF-8");
      //
      msg.setRecipients(Message.RecipientType.TO, parseAddresses(toRecipients));
      msg.setRecipients(Message.RecipientType.CC, parseAddresses(ccRecipients));
      msg.setRecipients(Message.RecipientType.BCC, parseAddresses(bccRecipients));
      return msg;
    }
    catch (ProcessingException pe) {
      throw pe;
    }
    catch (Exception e) {
      throw new ProcessingException("Failed to create MimeMessage.", e);
    }
  }

  public static MimeMessage createMimeMessage(String messagePlain, String messageHtml, DataSource[] attachements) throws ProcessingException {
    return instance.createMimeMessageInternal(messagePlain, messageHtml, attachements);
  }

  public static MimeMessage createMimeMessageFromWordArchive(File archiveFile, File[] attachments, boolean markAsUnsent) throws ProcessingException {
    return instance.createMimeMessageFromWordArchiveInternal(archiveFile, attachments, markAsUnsent);
  }

  public static MimeMessage createMimeMessageFromWordArchive(File archiveFile, File[] attachments) throws ProcessingException {
    return instance.createMimeMessageFromWordArchiveInternal(archiveFile, attachments, false);
  }

  private MimeMessage createMimeMessageFromWordArchiveInternal(File archiveFile, File[] attachments, boolean markAsUnsent) throws ProcessingException {
    try {
      File tempDir = IOUtility.createTempDirectory("");
      FileUtility.extractArchive(archiveFile, tempDir);

      String simpleName = archiveFile.getName();
      if (archiveFile.getName().lastIndexOf('.') != -1) {
        simpleName = archiveFile.getName().substring(0, archiveFile.getName().lastIndexOf('.'));
      }

      File plainTextFile = new File(tempDir, simpleName + ".txt");
      String plainTextMessage = null;
      boolean hasPlainText = false;
      if (plainTextFile.exists()) {
        Reader reader = new FileReader(plainTextFile);
        plainTextMessage = IOUtility.getContent(reader);
        reader.close();
        hasPlainText = StringUtility.hasText(plainTextMessage);
      }

      String folderName = null;
      List<DataSource> htmlDataSourceList = new ArrayList<DataSource>();
      for (File filesFolder : tempDir.listFiles()) {
        // in this archive file, exactly one directory should exist
        // word names this directory differently depending on the language
        if (filesFolder.isDirectory() && filesFolder.getName().startsWith(simpleName)) {
          // we accept the first directory that meets the constraint above
          // add all auxiliary files as attachment
          folderName = filesFolder.getName();
          for (File file : filesFolder.listFiles()) {
            // exclude Microsoft Word specific directory file. This is only used to edit HTML in Word.
            if (!file.getName().equalsIgnoreCase("filelist.xml") &&
                !file.getName().equalsIgnoreCase("colorschememapping.xml") &&
                !file.getName().equalsIgnoreCase("themedata.thmx") &&
                !file.getName().equalsIgnoreCase("editdata.mso")) {
              FileDataSource fds = new FileDataSource(file);
              htmlDataSourceList.add(fds);
            }
          }
          break;
        }
      }

      File htmlFile = new File(tempDir, simpleName + ".html");
      String htmlMessage = null;
      boolean hasHtml = false;
      if (htmlFile.exists()) {
        Reader reader = new FileReader(htmlFile);
        htmlMessage = IOUtility.getContent(reader);
        reader.close();
        // replace directory entry
        // replace all paths to the 'files directory' with the root directory
        htmlMessage = htmlMessage.replaceAll("\"" + folderName + "/", "\"cid:");

        // remove special/unused files
        htmlMessage = htmlMessage.replaceAll("<link rel=File-List href=\"cid:filelist.xml\">", "");
        htmlMessage = htmlMessage.replaceAll("<link rel=colorSchemeMapping href=\"cid:colorschememapping.xml\">", "");
        htmlMessage = htmlMessage.replaceAll("<link rel=themeData href=\"cid:themedata.thmx\">", "");
        htmlMessage = htmlMessage.replaceAll("<link rel=Edit-Time-Data href=\"cid:editdata.mso\">", "");
        hasHtml = StringUtility.hasText(htmlMessage);
      }

      if (!hasPlainText && !hasHtml) {
        throw new ProcessingException("message has no body");
      }

      MimeMessage mimeMessage = new CharsetSafeMimeMessage();
      Multipart multiPart = null;
      if (hasPlainText && hasHtml) {
        multiPart = new MimeMultipart("alternative");
      }
      else {
        multiPart = new MimeMultipart();
      }
      mimeMessage.setContent(multiPart);

      // [<< text body
      if (hasPlainText) {
        MimeBodyPart bodyPartText = new MimeBodyPart();
        bodyPartText.setText(plainTextMessage, "UTF-8");
        bodyPartText.setHeader(CONTENT_TYPE_ID, CONTENT_TYPE_TEXT_PLAIN);
        bodyPartText.setHeader("Content-Transfer-Encoding", "quoted-printable");
        multiPart.addBodyPart(bodyPartText);
      }
      // ]>> text body

      // [<< html body
      if (hasHtml) {
        MimeBodyPart bodyPartHtml = new MimeBodyPart();
        multiPart.addBodyPart(bodyPartHtml);
        Multipart multiPartHtml = new MimeMultipart("related");
        bodyPartHtml.setContent(multiPartHtml);
        MimeBodyPart part = new MimeBodyPart();
        part.setContent(htmlMessage, CONTENT_TYPE_TEXT_HTML);
        part.setHeader("Content-Transfer-Encoding", "quoted-printable");
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
      // ]>> html body

      // [<< attachements
      if (attachments != null) {
        for (File attachment : attachments) {
          MimeBodyPart part = new MimeBodyPart();
          FileDataSource fds = new FileDataSource(attachment);
          DataHandler handler = new DataHandler(fds);
          part.setDataHandler(handler);
          part.setFileName(attachment.getName());
          multiPart.addBodyPart(part);
        }
      }
      // [>> attachements

      if (markAsUnsent) {
        mimeMessage.setHeader("X-Unsent", "1"); // only supported in Outlook 2010
      }
      return mimeMessage;
    }
    catch (IOException e) {
      throw new ProcessingException("Error occured while accessing files", e);
    }
    catch (MessagingException e) {
      throw new ProcessingException("Error occured while creating MIME-message", e);
    }
  }

  private MimeMessage createMimeMessageInternal(String bodyTextPlain, String bodyTextHtml, DataSource[] attachements) throws ProcessingException {
    try {
      CharsetSafeMimeMessage m = new CharsetSafeMimeMessage();
      MimeMultipart multiPart = new MimeMultipart();
      BodyPart bodyPart = createBodyPart(bodyTextPlain, bodyTextHtml);
      if (bodyPart == null) {
        return null;
      }
      multiPart.addBodyPart(bodyPart);
      // attachements
      if (attachements != null) {
        for (DataSource source : attachements) {
          MimeBodyPart part = new MimeBodyPart();
          DataHandler handler = new DataHandler(source);
          part.setDataHandler(handler);
          part.setFileName(source.getName());
          multiPart.addBodyPart(part);
        }
      }
      m.setContent(multiPart);
      return m;
    }
    catch (Throwable t) {
      throw new ProcessingException("Failed to create MimeMessage.", t);
    }
  }

  private BodyPart createBodyPart(String bodyTextPlain, String bodyTextHtml) throws MessagingException {
    if (!StringUtility.isNullOrEmpty(bodyTextPlain) && !StringUtility.isNullOrEmpty(bodyTextHtml)) {
      // multipart
      MimeBodyPart plainPart = new MimeBodyPart();
      plainPart.setText(bodyTextPlain, "UTF-8");
      plainPart.addHeader(CONTENT_TYPE_ID, CONTENT_TYPE_TEXT_PLAIN);
      MimeBodyPart htmlPart = new MimeBodyPart();
      htmlPart.setText(bodyTextHtml, "UTF-8");
      htmlPart.addHeader(CONTENT_TYPE_ID, CONTENT_TYPE_TEXT_HTML);

      Multipart multiPart = new MimeMultipart("alternative");
      multiPart.addBodyPart(plainPart);
      multiPart.addBodyPart(htmlPart);
      MimeBodyPart multiBodyPart = new MimeBodyPart();
      multiBodyPart.setContent(multiPart);
      return multiBodyPart;
    }
    else if (!StringUtility.isNullOrEmpty(bodyTextPlain)) {
      MimeBodyPart part = new MimeBodyPart();
      part.setText(bodyTextPlain, "UTF-8");
      part.addHeader(CONTENT_TYPE_ID, CONTENT_TYPE_TEXT_PLAIN);
      return part;
    }
    else if (!StringUtility.isNullOrEmpty(bodyTextHtml)) {
      MimeBodyPart part = new MimeBodyPart();
      part.setText(bodyTextHtml, "UTF-8");
      part.addHeader(CONTENT_TYPE_ID, CONTENT_TYPE_TEXT_HTML);
      return part;
    }
    return null;
  }

  public static MimeMessage createMessageFromBytes(byte[] bytes) throws ProcessingException {
    return instance.createMessageFromBytesImpl(bytes);
  }

  private MimeMessage createMessageFromBytesImpl(byte[] bytes) throws ProcessingException {
    try {
      ByteArrayInputStream st = new ByteArrayInputStream(bytes);
      return new MimeMessage(null, st);
    }
    catch (Throwable t) {
      throw new ProcessingException("Unexpected: ", t);
    }
  }

  /**
   * @since 2.7
   */
  public static String getContentTypeForExtension(String ext) {
    if (ext == null) return null;
    if (ext.startsWith(".")) ext = ext.substring(1);
    ext = ext.toLowerCase();
    String type = FileUtility.getContentTypeForExtension(ext);
    if (type == null) {
      type = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType("tmp." + ext);
    }
    return type;
  }

  /**
   * Careful: this method returns null when the list of addresses is empty! This is a (stupid) default by
   * javax.mime.Message
   */
  private InternetAddress[] parseAddresses(String[] addresses) throws AddressException {
    if (addresses == null) {
      return null;
    }
    ArrayList<InternetAddress> addrList = new ArrayList<InternetAddress>();
    for (int i = 0; i < Array.getLength(addresses); i++) {
      addrList.add(new InternetAddress(addresses[i]));
    }
    if (addrList.size() == 0) {
      return null;
    }
    else {
      return addrList.toArray(new InternetAddress[addrList.size()]);
    }
  }

  private String getCharacterEncodingOfMimePart(MimePart part) throws MessagingException {
    Pattern pattern = Pattern.compile("charset=\".*\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(part.getContentType());
    String characterEncoding = "UTF-8"; // default, a good guess in Europe
    if (matcher.find()) {
      if (matcher.group(0).split("\"").length >= 2) {
        characterEncoding = matcher.group(0).split("\"")[1];
      }
    }
    else {
      if (part.getContentType().contains("charset=")) {
        if (part.getContentType().split("charset=").length == 2) {
          characterEncoding = part.getContentType().split("charset=")[1];
        }
      }
    }
    return characterEncoding;
  }

  static {
    fixMailcapCommandMap();
  }

  /**
   * jax-ws in jre 1.6.0 and priopr to 1.2.7 breaks support for "Umlaute" ä, ö, ü due to a bug in
   * StringDataContentHandler.writeTo
   * <p>
   * This patch uses reflection to eliminate this buggy mapping from the command map and adds the default text_plain
   * mapping (if available, e.g. sun jre)
   */
  @SuppressWarnings("unchecked")
  private static void fixMailcapCommandMap() {
    try {
      //set the com.sun.mail.handlers.text_plain to level 0 (programmatic) to prevent others from overriding in level 0
      Class textPlainClass;
      try {
        textPlainClass = Class.forName("com.sun.mail.handlers.text_plain");
      }
      catch (Throwable t) {
        //class not found, cancel
        return;
      }
      CommandMap cmap = MailcapCommandMap.getDefaultCommandMap();
      if (!(cmap instanceof MailcapCommandMap)) {
        return;
      }
      ((MailcapCommandMap) cmap).addMailcap("text/plain;;x-java-content-handler=" + textPlainClass.getName());
      //use reflection to clear out all other mappings of text/plain in level 0
      Field f = MailcapCommandMap.class.getDeclaredField("DB");
      f.setAccessible(true);
      Object[] dbArray = (Object[]) f.get(cmap);
      f = Class.forName("com.sun.activation.registries.MailcapFile").getDeclaredField("type_hash");
      f.setAccessible(true);
      Map<Object, Object> db0 = (Map<Object, Object>) f.get(dbArray[0]);
      Map<Object, Object> typeMap = (Map<Object, Object>) db0.get("text/plain");
      List<String> handlerList = (List<String>) typeMap.get("content-handler");
      //put text_plain in front
      handlerList.remove("com.sun.mail.handlers.text_plain");
      handlerList.add(0, "com.sun.mail.handlers.text_plain");
    }
    catch (Throwable t) {
      ScoutLogManager.getLogger(MailUtility.class).warn("Failed fixing MailcapComandMap string handling: " + t);
    }
  }
}
