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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.IDN;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.util.ByteArrayDataSource;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MailUtility {

  private static final Logger LOG = LoggerFactory.getLogger(MailUtility.class);

  public static final String CONTENT_TYPE_ID = "Content-Type";
  public static final String CONTENT_ID_ID = "Content-ID";

  public static final String CONTENT_TRANSFER_ENCODING_ID = "Content-Transfer-Encoding";
  public static final String QUOTED_PRINTABLE = "quoted-printable";

  public static final String CONTENT_TYPE_TEXT_HTML = "text/html; charset=\"UTF-8\"";
  public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain; charset=\"UTF-8\"";
  public static final String CONTENT_TYPE_MESSAGE_RFC822 = "message/rfc822";
  public static final String CONTENT_TYPE_IMAGE_PREFIX = "image/";
  public static final String CONTENT_TYPE_MULTIPART_PREFIX = "multipart/";

  private MailUtility() {
  }

  /**
   * Returns a list of body parts.
   *
   * @param message
   *          Message to look for body parts.
   * @return
   */
  public static List<Part> getBodyParts(Part message) {
    List<Part> bodyCollector = new ArrayList<Part>();
    collectMailParts(message, bodyCollector, null, null);
    return bodyCollector;
  }

  /**
   * Returns a list of attachments parts.
   *
   * @param message
   *          Message to look for attachment parts.
   * @return
   */
  public static List<Part> getAttachmentParts(Part message) {
    List<Part> attachmentCollector = new ArrayList<Part>();
    collectMailParts(message, null, attachmentCollector, null);
    return attachmentCollector;
  }

  /**
   * Collects the body, attachment and inline attachment parts from the provided part.
   * <p>
   * A single collector can be null in order to collect only the relevant parts.
   *
   * @param part
   *          Part
   * @param bodyCollector
   *          Body collector (optional)
   * @param attachmentCollector
   *          Attachment collector (optional)
   * @param inlineAttachmentCollector
   *          Inline attachment collector (optional)
   */
  public static void collectMailParts(Part part, List<Part> bodyCollector, List<Part> attachmentCollector, List<Part> inlineAttachmentCollector) {
    if (part == null) {
      return;
    }
    try {
      String disp = part.getDisposition();
      if (disp != null && disp.equalsIgnoreCase(Part.ATTACHMENT)) {
        if (attachmentCollector != null) {
          attachmentCollector.add(part);
        }
      }
      else {
        Object content = null;
        try { // NOSONAR
          // getContent might throw a MessagingException for legitimate parts (e.g. some images end up in a javax.imageio.IIOException for example).
          content = part.getContent();
        }
        catch (MessagingException | IOException e) {
          LOG.info("Unable to mime part content", e);
        }

        if (content instanceof Multipart) {
          Multipart multiPart = (Multipart) part.getContent();
          for (int i = 0; i < multiPart.getCount(); i++) {
            collectMailParts(multiPart.getBodyPart(i), bodyCollector, attachmentCollector, inlineAttachmentCollector);
          }
        }
        else {
          if (part.isMimeType(CONTENT_TYPE_TEXT_PLAIN)) {
            if (bodyCollector != null) {
              bodyCollector.add(part);
            }
          }
          else if (part.isMimeType(CONTENT_TYPE_TEXT_HTML)) {
            if (bodyCollector != null) {
              bodyCollector.add(part);
            }
          }
          else if (part.isMimeType(CONTENT_TYPE_MESSAGE_RFC822) && part.getContent() instanceof MimeMessage) {
            // its a MIME message in rfc822 format as attachment therefore we have to set the filename for the attachment correctly.
            if (attachmentCollector != null) {
              MimeMessage msg = (MimeMessage) part.getContent();
              String filteredSubjectText = StringUtility.filterText(msg.getSubject(), "a-zA-Z0-9_-", "");
              String fileName = (StringUtility.hasText(filteredSubjectText) ? filteredSubjectText : "originalMessage") + ".eml";
              RFCWrapperPart wrapperPart = new RFCWrapperPart(part, fileName);
              attachmentCollector.add(wrapperPart);
            }
          }
          else if (disp != null && disp.equals(Part.INLINE)) {
            if (inlineAttachmentCollector != null) {
              inlineAttachmentCollector.add(part);
            }
          }
          else {
            String[] headerContentId = part.getHeader(CONTENT_ID_ID);
            if (headerContentId != null && headerContentId.length > 0 && StringUtility.hasText(headerContentId[0]) && part.getContentType() != null && part.getContentType().startsWith(CONTENT_TYPE_IMAGE_PREFIX)) {
              if (inlineAttachmentCollector != null) {
                inlineAttachmentCollector.add(part);
              }
            }
            else if (part.getFileName() != null /* assumption: file name = attachment (last resort) */) {
              if (attachmentCollector != null) {
                attachmentCollector.add(part);
              }
            }
            else {
              LOG.debug("Unknown mail message part, headers: [{}]", part.getAllHeaders());
            }
          }
        }
      }
    }
    catch (MessagingException | IOException e) {
      throw new ProcessingException("Unexpected: ", e);
    }
  }

  /**
   * @param part
   * @return the plainText part encoded with the encoding given in the MIME header or UTF-8 encoded or null if the
   *         plainText Part is not given
   */
  public static String getPlainText(Part part) {
    String text = null;
    try {
      List<Part> bodyParts = getBodyParts(part);
      Part plainTextPart = getPlainTextPart(bodyParts);

      if (plainTextPart instanceof MimePart) {
        MimePart mimePart = (MimePart) plainTextPart;
        try (InputStream in = mimePart.getInputStream()) {
          if (in != null) {
            byte[] content = IOUtility.readBytes(in);
            try { // NOSONAR
              text = new String(content, getCharacterEncodingOfMimePart(mimePart));
            }
            catch (UnsupportedEncodingException e) {
              LOG.warn("unsupporeted encoding", e);
              text = new String(content);
            }
          }
        }
      }
    }
    catch (MessagingException | IOException e) {
      throw new ProcessingException("Unexpected: ", e);
    }
    return text;
  }

  public static Part getHtmlPart(List<Part> bodyParts) {
    for (Part p : bodyParts) {
      try {
        if (p != null && p.isMimeType(CONTENT_TYPE_TEXT_HTML)) {
          return p;
        }
      }
      catch (Exception e) {
        throw new ProcessingException("Unexpected: ", e);
      }
    }
    return null;
  }

  public static Part getPlainTextPart(List<Part> bodyParts) {
    for (Part p : bodyParts) {
      try {
        if (p != null && p.isMimeType(CONTENT_TYPE_TEXT_PLAIN)) {
          return p;
        }
      }
      catch (Exception e) {
        throw new ProcessingException("Unexpected: ", e);
      }
    }
    return null;
  }

  public static DataSource createDataSource(File file) {
    try (InputStream in = new FileInputStream(file)) {
      int indexDot = file.getName().lastIndexOf('.');
      if (indexDot > 0) {
        String fileName = file.getName();
        String ext = fileName.substring(indexDot + 1);
        return createDataSource(in, fileName, ext);
      }
      else {
        return null;
      }
    }
    catch (Exception e) {
      throw new ProcessingException("Unexpected: ", e);
    }
  }

  /**
   * @param inStream
   * @param fileName
   *          e.g. "file.txt"
   * @param fileExtension
   *          e.g. "txt", "jpg"
   * @return
   */
  public static DataSource createDataSource(InputStream inStream, String fileName, String fileExtension) {
    try {
      String mimeType = getContentTypeForExtension(fileExtension);
      if (mimeType == null) {
        mimeType = "application/octet-stream";
      }
      ByteArrayDataSource item = new ByteArrayDataSource(inStream, mimeType);
      item.setName(fileName);
      return item;
    }
    catch (Exception e) {
      throw new ProcessingException("Unexpected: ", e);
    }
  }

  /**
   * Creates a mime message according to the mail message provided.
   *
   * @param mailMessage
   *          Definition of mime message properties.
   * @return Mime message
   */
  public static MimeMessage createMimeMessage(MailMessage mailMessage) {
    if (mailMessage == null) {
      throw new IllegalArgumentException("Mail message is missing");
    }

    try {
      CharsetSafeMimeMessage m = new CharsetSafeMimeMessage();
      MimeMultipart multiPart = new MimeMultipart();
      BodyPart bodyPart = createBodyPart(mailMessage.getBodyPlainText(), mailMessage.getBodyHtml());
      if (bodyPart == null) {
        return null;
      }
      multiPart.addBodyPart(bodyPart);
      // attachments
      for (MailAttachment attachment : mailMessage.getAttachments()) {
        MimeBodyPart part = new MimeBodyPart();
        DataHandler handler = new DataHandler(attachment.getDataSource());
        part.setDataHandler(handler);
        part.setFileName(attachment.getDataSource().getName());
        if (StringUtility.hasText(attachment.getContentId())) {
          part.setContentID("<" + attachment.getContentId() + ">");
        }
        multiPart.addBodyPart(part);
      }
      m.setContent(multiPart);

      if (mailMessage.getSender() != null && StringUtility.hasText(mailMessage.getSender().getEmail())) {
        InternetAddress addrSender = createInternetAddress(mailMessage.getSender());
        m.setFrom(addrSender);
        m.setSender(addrSender);
      }
      if (!CollectionUtility.isEmpty(mailMessage.getReplyTos())) {
        m.setReplyTo(createInternetAddresses(mailMessage.getReplyTos()));
      }
      if (StringUtility.hasText(mailMessage.getSubject())) {
        m.setSubject(mailMessage.getSubject(), StandardCharsets.UTF_8.name());
      }
      if (!CollectionUtility.isEmpty(mailMessage.getToRecipients())) {
        m.setRecipients(Message.RecipientType.TO, createInternetAddresses(mailMessage.getToRecipients()));
      }
      if (!CollectionUtility.isEmpty(mailMessage.getCcRecipients())) {
        m.setRecipients(Message.RecipientType.CC, createInternetAddresses(mailMessage.getCcRecipients()));
      }
      if (!CollectionUtility.isEmpty(mailMessage.getBccRecipients())) {
        m.setRecipients(Message.RecipientType.BCC, createInternetAddresses(mailMessage.getBccRecipients()));
      }
      return m;
    }
    catch (Exception e) {
      throw new ProcessingException("Failed to create MimeMessage.", e);
    }
  }

  private static BodyPart createBodyPart(String bodyTextPlain, String bodyTextHtml) throws MessagingException {
    if (!StringUtility.isNullOrEmpty(bodyTextPlain) && !StringUtility.isNullOrEmpty(bodyTextHtml)) {
      // multipart
      MimeBodyPart plainPart = createSingleBodyPart(bodyTextPlain, CONTENT_TYPE_TEXT_PLAIN);
      MimeBodyPart htmlPart = createSingleBodyPart(bodyTextHtml, CONTENT_TYPE_TEXT_HTML);

      Multipart multiPart = new MimeMultipart("alternative");
      multiPart.addBodyPart(plainPart);
      multiPart.addBodyPart(htmlPart);
      MimeBodyPart multiBodyPart = new MimeBodyPart();
      multiBodyPart.setContent(multiPart);
      return multiBodyPart;
    }
    else if (!StringUtility.isNullOrEmpty(bodyTextPlain)) {
      return createSingleBodyPart(bodyTextPlain, CONTENT_TYPE_TEXT_PLAIN);
    }
    else if (!StringUtility.isNullOrEmpty(bodyTextHtml)) {
      return createSingleBodyPart(bodyTextHtml, CONTENT_TYPE_TEXT_HTML);
    }
    return null;
  }

  /**
   * Creates a single mime body part.
   *
   * @param bodyText
   *          Body text
   * @param contentType
   *          Content type
   * @return Crated mime body part
   * @throws MessagingException
   */
  private static MimeBodyPart createSingleBodyPart(String bodyText, String contentType) throws MessagingException {
    MimeBodyPart part = new MimeBodyPart();
    part.setText(bodyText, StandardCharsets.UTF_8.name());
    part.addHeader(CONTENT_TYPE_ID, contentType);
    return part;
  }

  public static MimeMessage createMessageFromBytes(byte[] bytes) {
    return createMessageFromBytes(bytes, null);
  }

  public static MimeMessage createMessageFromBytes(byte[] bytes, Session session) {
    try {
      ByteArrayInputStream st = new ByteArrayInputStream(bytes);
      return new MimeMessage(session, st);
    }
    catch (Exception e) {
      throw new ProcessingException("Unexpected: ", e);
    }
  }

  /**
   * Adds the provided attachments to the existing mime message.
   * <p>
   * When working with {@link BinaryResource}, use {@link #addResourcesAsAttachments(MimeMessage, List)} instead.
   *
   * @param msg
   *          Mime message to attach files to
   * @param attachments
   *          List of attachments (files).
   * @since 4.1
   */
  public static void addAttachmentsToMimeMessage(MimeMessage msg, List<File> attachments) {
    if (CollectionUtility.isEmpty(attachments)) {
      return;
    }

    try {
      Multipart multiPart = prepareMessageForAttachments(msg);

      for (File attachment : attachments) {
        MimeBodyPart bodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(attachment);
        bodyPart.setDataHandler(new DataHandler(source));
        bodyPart.setFileName(attachment.getName());
        multiPart.addBodyPart(bodyPart);
      }
      msg.saveChanges();
    }
    catch (MessagingException e) {
      throw new ProcessingException("Failed to add attachment to existing mime message", e);
    }
    catch (IOException e) {
      throw new ProcessingException("Failed to add attachment to existing mime message", e);
    }
  }

  /**
   * Adds the provided attachments to the existing mime message.
   * <p>
   * When working with {@link File}, use {@link #addAttachmentsToMimeMessage(MimeMessage, List)} instead.
   *
   * @param msg
   *          Mime message to attach files to
   * @param attachments
   *          List of attachments (binary resources).
   * @since 6.0
   */
  public static void addResourcesAsAttachments(MimeMessage msg, List<BinaryResource> attachments) {
    if (CollectionUtility.isEmpty(attachments)) {
      return;
    }

    try {
      Multipart multiPart = prepareMessageForAttachments(msg);

      for (BinaryResource attachment : attachments) {
        MimeBodyPart bodyPart = new MimeBodyPart();
        DataSource source = new BinaryResourceDataSource(attachment);
        bodyPart.setDataHandler(new DataHandler(source));
        bodyPart.setFileName(attachment.getFilename());
        multiPart.addBodyPart(bodyPart);
      }
      msg.saveChanges();
    }
    catch (MessagingException e) {
      throw new ProcessingException("Failed to add attachment to existing mime message", e);
    }
    catch (IOException e) {
      throw new ProcessingException("Failed to add attachment to existing mime message", e);
    }
  }

  /**
   * Prepares the mime message so that attachments can be added to the returned {@link Multipart}.
   *
   * @param msg
   *          Mime message to prepare
   * @return Multipart to which attachments can be added
   * @throws IOException
   * @throws MessagingException
   */
  private static Multipart prepareMessageForAttachments(MimeMessage msg) throws IOException, MessagingException {
    Object messageContent = msg.getContent();

    Multipart multiPart = null;
    if (messageContent instanceof Multipart && StringUtility.containsStringIgnoreCase(((Multipart) messageContent).getContentType(), "multipart/mixed")) {
      // already contains attachments
      // use the existing multipart
      multiPart = (Multipart) messageContent;
    }
    else if (messageContent instanceof Multipart) {
      MimeBodyPart multiPartBody = new MimeBodyPart();
      multiPartBody.setContent((Multipart) messageContent);

      multiPart = new MimeMultipart(); //mixed
      msg.setContent(multiPart);

      multiPart.addBodyPart(multiPartBody);
    }
    else if (messageContent instanceof String) {
      MimeBodyPart multiPartBody = new MimeBodyPart();
      String message = (String) messageContent;

      String contentTypeHeader = StringUtility.join(" ", msg.getHeader(CONTENT_TYPE_ID));
      if (StringUtility.containsStringIgnoreCase(contentTypeHeader, "html")) {
        // html
        multiPartBody.setContent(message, MailUtility.CONTENT_TYPE_TEXT_HTML);
        multiPartBody.setHeader(CONTENT_TYPE_ID, MailUtility.CONTENT_TYPE_TEXT_HTML);
        multiPartBody.setHeader(CONTENT_TRANSFER_ENCODING_ID, QUOTED_PRINTABLE);
      }
      else {
        // plain text
        multiPartBody.setText(message);
      }

      multiPart = new MimeMultipart(); //mixed
      msg.setContent(multiPart);

      multiPart.addBodyPart(multiPartBody);
    }
    else {
      throw new ProcessingException("Unsupported mime message format. Unable to add attachments.");
    }
    return multiPart;
  }

  /**
   * @since 2.7
   */
  public static String getContentTypeForExtension(String ext) {
    if (ext == null) {
      return null;
    }
    if (ext.startsWith(".")) {
      ext = ext.substring(1);
    }
    ext = ext.toLowerCase();
    String type = FileUtility.getContentTypeForExtension(ext);
    if (type == null) {
      type = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType("tmp." + ext);
    }
    return type;
  }

  /**
   * Creates an Internet address for the given mail participant allowing usage of an internationalized domain name.
   *
   * @return {@link InternetAddress} instance or {@code null} if no participant is provided
   */
  public static InternetAddress createInternetAddress(MailParticipant participant) {
    if (participant == null) {
      return null;
    }
    return createInternetAddress(participant.getEmail(), participant.getName());
  }

  /**
   * Creates an Internet address for the given mail participant allowing usage of an internationalized domain name.
   *
   * @return {@link InternetAddress} instance or {@code null} if no or empty email is provided
   */
  public static InternetAddress createInternetAddress(String email) {
    return createInternetAddress(email, null);
  }

  /**
   * Creates an Internet address for the given mail participant allowing usage of an internationalized domain name.
   *
   * @return {@link InternetAddress} instance or {@code null} if no or empty email is provided
   */
  public static InternetAddress createInternetAddress(String email, String name) {
    if (StringUtility.isNullOrEmpty(email)) {
      return null;
    }

    try {
      InternetAddress internetAddress = new InternetAddress(IDN.toASCII(email));
      if (StringUtility.hasText(name)) {
        internetAddress.setPersonal(name, StandardCharsets.UTF_8.name());
      }
      return internetAddress;
    }
    catch (AddressException e) {
      throw new ProcessingException("Failed to create internet address for {}", email, e);
    }
    catch (UnsupportedEncodingException e) {
      throw new ProcessingException("Failed to set personal name for {}", name, e);
    }
  }

  /**
   * Parse a comma-separated list of email addresses allowing usage of internationalized domain names.
   *
   * @return array of parsed {@link InternetAddress} instances
   */
  public static InternetAddress[] parseInternetAddressList(String addresslist) {
    String[] addressListSplitted = StringUtility.split(addresslist, ",");
    InternetAddress[] result = new InternetAddress[addressListSplitted.length];
    for (int i = 0; i < addressListSplitted.length; i++) {
      result[i] = createInternetAddress(addressListSplitted[i]);
    }
    return result;
  }

  /**
   * Careful: this method returns null when the list of addresses is empty! This is a (stupid) default by
   * javax.mime.Message.
   * <p>
   * Array instead of list is returned in order to directly used to result with
   * {@link MimeMessage#setRecipients(javax.mail.Message.RecipientType, javax.mail.Address[])}.
   */
  private static InternetAddress[] createInternetAddresses(List<MailParticipant> participants) {
    if (CollectionUtility.isEmpty(participants)) {
      return null;
    }
    ArrayList<InternetAddress> addrList = new ArrayList<InternetAddress>();
    for (MailParticipant participant : participants) {
      addrList.add(createInternetAddress(participant));
    }
    return addrList.toArray(new InternetAddress[addrList.size()]);
  }

  private static String getCharacterEncodingOfMimePart(MimePart part) throws MessagingException {
    Pattern pattern = Pattern.compile("charset=\".*\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(part.getContentType());
    String characterEncoding = StandardCharsets.UTF_8.name(); // default, a good guess in Europe
    if (matcher.find()) {
      if (matcher.group(0).split("\"").length >= 2) {
        characterEncoding = matcher.group(0).split("\"")[1];
      }
    }
    else {
      final String chartsetEquals = "charset=";
      if (part.getContentType().contains(chartsetEquals)) {
        String[] contentTypeParts = part.getContentType().split(chartsetEquals);
        if (contentTypeParts.length == 2) {
          characterEncoding = contentTypeParts[1];
        }
      }
    }
    return characterEncoding;
  }
}
