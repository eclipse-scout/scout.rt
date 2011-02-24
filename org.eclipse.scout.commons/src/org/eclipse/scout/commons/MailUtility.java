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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
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

  public static MimeMessage createMimeMessageFromWordArchieve(File archiveFile, File[] attachments) throws ProcessingException {
    return instance.createMimeMessageFromWordArchieveInternal(archiveFile, attachments);
  }

  @SuppressWarnings("restriction")
  private MimeMessage createMimeMessageFromWordArchieveInternal(File archiveFile, File[] attachments) throws ProcessingException {
    try {
      File tempDir = IOUtility.createTempDirectory("");
      FileUtility.extractArchive(archiveFile, tempDir);

      String simpleName = archiveFile.getName();
      if (archiveFile.getName().lastIndexOf('.') != -1) {
        simpleName = archiveFile.getName().substring(0, archiveFile.getName().lastIndexOf('.'));
      }

      File plainTextFile = new File(tempDir, simpleName + ".txt");
      String plainTextMessage = null;
      if (plainTextFile.exists()) {
        Reader reader = new FileReader(plainTextFile);
        plainTextMessage = IOUtility.getContent(reader);
        reader.close();
      }

      File filesFolder = new File(tempDir, simpleName + "_files");
      List<DataSource> dataSourceList = new ArrayList<DataSource>();
      if (filesFolder.exists()) {
        // add all auxilary files as attachment
        for (File file : filesFolder.listFiles()) {
          // exclude Microsoft Word specific directory file. This is only used to edit HTML in Word.
          if (!file.getName().equals("filelist.xml")) {
            DataSource dataSource = MailUtility.createDataSource(file);
            dataSourceList.add(dataSource);
          }
        }
      }

      for (File attachment : attachments) {
        dataSourceList.add(MailUtility.createDataSource(attachment));
      }

      File htmlFile = new File(tempDir, simpleName + ".html");
      if (!htmlFile.exists()) {
        htmlFile = new File(tempDir, simpleName + ".htm");
      }
      String htmlMessage = null;
      if (htmlFile.exists()) {
        Reader reader = new FileReader(htmlFile);
        htmlMessage = IOUtility.getContent(reader);
        reader.close();
        // replace directory entry
        // replace all paths to the 'files directory' with the root directory
        htmlMessage = htmlMessage.replaceAll(simpleName + "_files/", "");

        // remove 'files directory' registration
        Pattern pattern = Pattern.compile("<link rel=File-List[^>].+?>", Pattern.DOTALL | Pattern.MULTILINE);
        htmlMessage = pattern.matcher(htmlMessage).replaceAll("");
      }

      MimeMessage mimeMessage = MailUtility.createMimeMessage(plainTextMessage, htmlMessage, dataSourceList.toArray(new DataSource[dataSourceList.size()]));
      mimeMessage.setFileName(simpleName + ".eml");
      mimeMessage.setHeader("X-Unsent", "1"); // only supported in Outlook 2010
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
    String type = EXT_TO_MIME_TYPE_MAP.get(ext);
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
    setupExtendedMimeMappings();
    fixMailcapCommandMap();
  }

  /**
   * Static extension to mimetype mapper
   */
  private static HashMap<String, String> EXT_TO_MIME_TYPE_MAP;

  /**
   * Static extension to mimetype mapper
   */
  private static void setupExtendedMimeMappings() {
    EXT_TO_MIME_TYPE_MAP = new HashMap<String, String>();
    EXT_TO_MIME_TYPE_MAP.put("ai", "application/postscript");
    EXT_TO_MIME_TYPE_MAP.put("aif", "audio/x-aiff");
    EXT_TO_MIME_TYPE_MAP.put("aifc", "audio/x-aiff");
    EXT_TO_MIME_TYPE_MAP.put("aiff", "audio/x-aiff");
    EXT_TO_MIME_TYPE_MAP.put("asc", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("au", "audio/basic");
    EXT_TO_MIME_TYPE_MAP.put("avi", "video/x-msvideo");
    EXT_TO_MIME_TYPE_MAP.put("bcpio", "application/x-bcpio");
    EXT_TO_MIME_TYPE_MAP.put("bin", "application/octet-stream");
    EXT_TO_MIME_TYPE_MAP.put("c", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("cc", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("ccad", "application/clariscad");
    EXT_TO_MIME_TYPE_MAP.put("cdf", "application/x-netcdf");
    EXT_TO_MIME_TYPE_MAP.put("class", "application/octet-stream");
    EXT_TO_MIME_TYPE_MAP.put("cpio", "application/x-cpio");
    EXT_TO_MIME_TYPE_MAP.put("cpt", "application/mac-compactpro");
    EXT_TO_MIME_TYPE_MAP.put("csh", "application/x-csh");
    EXT_TO_MIME_TYPE_MAP.put("css", "text/css");
    EXT_TO_MIME_TYPE_MAP.put("dcr", "application/x-director");
    EXT_TO_MIME_TYPE_MAP.put("dir", "application/x-director");
    EXT_TO_MIME_TYPE_MAP.put("dms", "application/octet-stream");
    EXT_TO_MIME_TYPE_MAP.put("doc", "application/msword");
    EXT_TO_MIME_TYPE_MAP.put("drw", "application/drafting");
    EXT_TO_MIME_TYPE_MAP.put("dvi", "application/x-dvi");
    EXT_TO_MIME_TYPE_MAP.put("dwg", "application/acad");
    EXT_TO_MIME_TYPE_MAP.put("dxf", "application/dxf");
    EXT_TO_MIME_TYPE_MAP.put("dxr", "application/x-director");
    EXT_TO_MIME_TYPE_MAP.put("eps", "application/postscript");
    EXT_TO_MIME_TYPE_MAP.put("etx", "text/x-setext");
    EXT_TO_MIME_TYPE_MAP.put("exe", "application/octet-stream");
    EXT_TO_MIME_TYPE_MAP.put("ez", "application/andrew-inset");
    EXT_TO_MIME_TYPE_MAP.put("f", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("f90", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("fli", "video/x-fli");
    EXT_TO_MIME_TYPE_MAP.put("gif", "image/gif");
    EXT_TO_MIME_TYPE_MAP.put("gtar", "application/x-gtar");
    EXT_TO_MIME_TYPE_MAP.put("gz", "application/x-gzip");
    EXT_TO_MIME_TYPE_MAP.put("h", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("hdf", "application/x-hdf");
    EXT_TO_MIME_TYPE_MAP.put("hh", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("hqx", "application/mac-binhex40");
    EXT_TO_MIME_TYPE_MAP.put("htm", "text/html");
    EXT_TO_MIME_TYPE_MAP.put("html", "text/html");
    EXT_TO_MIME_TYPE_MAP.put("ice", "x-conference/x-cooltalk");
    EXT_TO_MIME_TYPE_MAP.put("ief", "image/ief");
    EXT_TO_MIME_TYPE_MAP.put("iges", "model/iges");
    EXT_TO_MIME_TYPE_MAP.put("igs", "model/iges");
    EXT_TO_MIME_TYPE_MAP.put("ips", "application/x-ipscript");
    EXT_TO_MIME_TYPE_MAP.put("ipx", "application/x-ipix");
    EXT_TO_MIME_TYPE_MAP.put("jpe", "image/jpeg");
    EXT_TO_MIME_TYPE_MAP.put("jpeg", "image/jpeg");
    EXT_TO_MIME_TYPE_MAP.put("jpg", "image/jpeg");
    EXT_TO_MIME_TYPE_MAP.put("js", "application/x-javascript");
    EXT_TO_MIME_TYPE_MAP.put("kar", "audio/midi");
    EXT_TO_MIME_TYPE_MAP.put("latex", "application/x-latex");
    EXT_TO_MIME_TYPE_MAP.put("lha", "application/octet-stream");
    EXT_TO_MIME_TYPE_MAP.put("lsp", "application/x-lisp");
    EXT_TO_MIME_TYPE_MAP.put("lzh", "application/octet-stream");
    EXT_TO_MIME_TYPE_MAP.put("m", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("man", "application/x-troff-man");
    EXT_TO_MIME_TYPE_MAP.put("me", "application/x-troff-me");
    EXT_TO_MIME_TYPE_MAP.put("mesh", "model/mesh");
    EXT_TO_MIME_TYPE_MAP.put("mid", "audio/midi");
    EXT_TO_MIME_TYPE_MAP.put("midi", "audio/midi");
    EXT_TO_MIME_TYPE_MAP.put("mif", "application/vnd.mif");
    EXT_TO_MIME_TYPE_MAP.put("mime", "www/mime");
    EXT_TO_MIME_TYPE_MAP.put("mov", "video/quicktime");
    EXT_TO_MIME_TYPE_MAP.put("movie", "video/x-sgi-movie");
    EXT_TO_MIME_TYPE_MAP.put("mp2", "audio/mpeg");
    EXT_TO_MIME_TYPE_MAP.put("mp3", "audio/mpeg");
    EXT_TO_MIME_TYPE_MAP.put("mpe", "video/mpeg");
    EXT_TO_MIME_TYPE_MAP.put("mpeg", "video/mpeg");
    EXT_TO_MIME_TYPE_MAP.put("mpg", "video/mpeg");
    EXT_TO_MIME_TYPE_MAP.put("mpga", "audio/mpeg");
    EXT_TO_MIME_TYPE_MAP.put("ms", "application/x-troff-ms");
    EXT_TO_MIME_TYPE_MAP.put("msh", "model/mesh");
    EXT_TO_MIME_TYPE_MAP.put("nc", "application/x-netcdf");
    EXT_TO_MIME_TYPE_MAP.put("oda", "application/oda");
    EXT_TO_MIME_TYPE_MAP.put("pbm", "image/x-portable-bitmap");
    EXT_TO_MIME_TYPE_MAP.put("pdb", "chemical/x-pdb");
    EXT_TO_MIME_TYPE_MAP.put("pdf", "application/pdf");
    EXT_TO_MIME_TYPE_MAP.put("pgm", "image/x-portable-graymap");
    EXT_TO_MIME_TYPE_MAP.put("pgn", "application/x-chess-pgn");
    EXT_TO_MIME_TYPE_MAP.put("png", "image/png");
    EXT_TO_MIME_TYPE_MAP.put("pnm", "image/x-portable-anymap");
    EXT_TO_MIME_TYPE_MAP.put("pot", "application/mspowerpoint");
    EXT_TO_MIME_TYPE_MAP.put("ppm", "image/x-portable-pixmap");
    EXT_TO_MIME_TYPE_MAP.put("pps", "application/mspowerpoint");
    EXT_TO_MIME_TYPE_MAP.put("ppt", "application/mspowerpoint");
    EXT_TO_MIME_TYPE_MAP.put("ppz", "application/mspowerpoint");
    EXT_TO_MIME_TYPE_MAP.put("pre", "application/x-freelance");
    EXT_TO_MIME_TYPE_MAP.put("prt", "application/pro_eng");
    EXT_TO_MIME_TYPE_MAP.put("ps", "application/postscript");
    EXT_TO_MIME_TYPE_MAP.put("qt", "video/quicktime");
    EXT_TO_MIME_TYPE_MAP.put("ra", "audio/x-realaudio");
    EXT_TO_MIME_TYPE_MAP.put("ram", "audio/x-pn-realaudio");
    EXT_TO_MIME_TYPE_MAP.put("ras", "image/cmu-raster");
    EXT_TO_MIME_TYPE_MAP.put("rgb", "image/x-rgb");
    EXT_TO_MIME_TYPE_MAP.put("rm", "audio/x-pn-realaudio");
    EXT_TO_MIME_TYPE_MAP.put("roff", "application/x-troff");
    EXT_TO_MIME_TYPE_MAP.put("rpm", "audio/x-pn-realaudio-plugin");
    EXT_TO_MIME_TYPE_MAP.put("rtf", "text/rtf");
    EXT_TO_MIME_TYPE_MAP.put("rtx", "text/richtext");
    EXT_TO_MIME_TYPE_MAP.put("scm", "application/x-lotusscreencam");
    EXT_TO_MIME_TYPE_MAP.put("set", "application/set");
    EXT_TO_MIME_TYPE_MAP.put("sgm", "text/sgml");
    EXT_TO_MIME_TYPE_MAP.put("sgml", "text/sgml");
    EXT_TO_MIME_TYPE_MAP.put("sh", "application/x-sh");
    EXT_TO_MIME_TYPE_MAP.put("shar", "application/x-shar");
    EXT_TO_MIME_TYPE_MAP.put("silo", "model/mesh");
    EXT_TO_MIME_TYPE_MAP.put("sit", "application/x-stuffit");
    EXT_TO_MIME_TYPE_MAP.put("skd", "application/x-koan");
    EXT_TO_MIME_TYPE_MAP.put("skm", "application/x-koan");
    EXT_TO_MIME_TYPE_MAP.put("skp", "application/x-koan");
    EXT_TO_MIME_TYPE_MAP.put("skt", "application/x-koan");
    EXT_TO_MIME_TYPE_MAP.put("smi", "application/smil");
    EXT_TO_MIME_TYPE_MAP.put("smil", "application/smil");
    EXT_TO_MIME_TYPE_MAP.put("snd", "audio/basic");
    EXT_TO_MIME_TYPE_MAP.put("sol", "application/solids");
    EXT_TO_MIME_TYPE_MAP.put("spl", "application/x-futuresplash");
    EXT_TO_MIME_TYPE_MAP.put("src", "application/x-wais-source");
    EXT_TO_MIME_TYPE_MAP.put("step", "application/STEP");
    EXT_TO_MIME_TYPE_MAP.put("stl", "application/SLA");
    EXT_TO_MIME_TYPE_MAP.put("stp", "application/STEP");
    EXT_TO_MIME_TYPE_MAP.put("sv4cpio", "application/x-sv4cpio");
    EXT_TO_MIME_TYPE_MAP.put("sv4crc", "application/x-sv4crc");
    EXT_TO_MIME_TYPE_MAP.put("swf", "application/x-shockwave-flash");
    EXT_TO_MIME_TYPE_MAP.put("t", "application/x-troff");
    EXT_TO_MIME_TYPE_MAP.put("tar", "application/x-tar");
    EXT_TO_MIME_TYPE_MAP.put("tcl", "application/x-tcl");
    EXT_TO_MIME_TYPE_MAP.put("tex", "application/x-tex");
    EXT_TO_MIME_TYPE_MAP.put("texi", "application/x-texinfo");
    EXT_TO_MIME_TYPE_MAP.put("texinfo", "application/x-texinfo");
    EXT_TO_MIME_TYPE_MAP.put("tif", "image/tiff");
    EXT_TO_MIME_TYPE_MAP.put("tiff", "image/tiff");
    EXT_TO_MIME_TYPE_MAP.put("tr", "application/x-troff");
    EXT_TO_MIME_TYPE_MAP.put("tsi", "audio/TSP-audio");
    EXT_TO_MIME_TYPE_MAP.put("tsp", "application/dsptype");
    EXT_TO_MIME_TYPE_MAP.put("tsv", "text/tab-separated-values");
    EXT_TO_MIME_TYPE_MAP.put("txt", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("unv", "application/i-deas");
    EXT_TO_MIME_TYPE_MAP.put("ustar", "application/x-ustar");
    EXT_TO_MIME_TYPE_MAP.put("vcd", "application/x-cdlink");
    EXT_TO_MIME_TYPE_MAP.put("vda", "application/vda");
    EXT_TO_MIME_TYPE_MAP.put("viv", "video/vnd.vivo");
    EXT_TO_MIME_TYPE_MAP.put("vivo", "video/vnd.vivo");
    EXT_TO_MIME_TYPE_MAP.put("vrml", "model/vrml");
    EXT_TO_MIME_TYPE_MAP.put("wav", "audio/x-wav");
    EXT_TO_MIME_TYPE_MAP.put("wrl", "model/vrml");
    EXT_TO_MIME_TYPE_MAP.put("xbm", "image/x-xbitmap");
    EXT_TO_MIME_TYPE_MAP.put("xlc", "application/vnd.ms-excel");
    EXT_TO_MIME_TYPE_MAP.put("xll", "application/vnd.ms-excel");
    EXT_TO_MIME_TYPE_MAP.put("xlm", "application/vnd.ms-excel");
    EXT_TO_MIME_TYPE_MAP.put("xls", "application/vnd.ms-excel");
    EXT_TO_MIME_TYPE_MAP.put("xlw", "application/vnd.ms-excel");
    EXT_TO_MIME_TYPE_MAP.put("xml", "text/xml");
    EXT_TO_MIME_TYPE_MAP.put("xpm", "image/x-xpixmap");
    EXT_TO_MIME_TYPE_MAP.put("xwd", "image/x-xwindowdump");
    EXT_TO_MIME_TYPE_MAP.put("xyz", "chemical/x-pdb");
    EXT_TO_MIME_TYPE_MAP.put("zip", "application/zip");
    EXT_TO_MIME_TYPE_MAP.put("msg", "application/vnd.ms-outlook");
    EXT_TO_MIME_TYPE_MAP.put("eml", "message/rfc822");
    EXT_TO_MIME_TYPE_MAP.put("ini", "text/plain");
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
