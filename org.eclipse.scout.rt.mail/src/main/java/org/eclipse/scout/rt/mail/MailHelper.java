/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.mail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;
import javax.mail.util.ByteArrayDataSource;

import org.eclipse.scout.rt.charsetdetect.CharsetDetector;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.MimeType;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class handles MimeMessages and MailMessages. A MimeMessage is a physical representation of an email. It
 * represents most likely a 'rfc822' message. A MailMessage represents the logical representation of an email.
 *
 * <pre>
 * Example:
 * Representation of email address 'thomas@müller.de':
 *    MailMessage: 'thomas@müller.de'
 *    MimeMessage: 'xn--thomas@mller-klb.de'
 * </pre>
 *
 * @since 7.0
 */
@ApplicationScoped
public class MailHelper {

  private static final Logger LOG = LoggerFactory.getLogger(MailHelper.class);

  public static final String CONTENT_TYPE_ID = "Content-Type";
  public static final String CONTENT_ID_ID = "Content-ID";

  public static final String CONTENT_TRANSFER_ENCODING_ID = "Content-Transfer-Encoding";
  public static final String QUOTED_PRINTABLE = "quoted-printable";

  public static final String CONTENT_TYPE_TEXT_HTML = "text/html; charset=\"UTF-8\"";
  public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain; charset=\"UTF-8\"";
  public static final String CONTENT_TYPE_MESSAGE_RFC822 = "message/rfc822";
  public static final String CONTENT_TYPE_IMAGE_PREFIX = "image/";
  public static final String CONTENT_TYPE_MULTIPART_PREFIX = "multipart/";

  public static final String HEADER_IN_REPLY_TO = "In-Reply-To";

  /**
   * Returns a list of body parts.
   *
   * @param message
   *          Message to look for body parts.
   */
  public List<Part> getBodyParts(Part message) {
    List<Part> bodyCollector = new ArrayList<>();
    collectMailParts(message, bodyCollector, null, null);
    return bodyCollector;
  }

  /**
   * Returns a list of attachments parts.
   *
   * @param message
   *          Message to look for attachment parts.
   */
  public List<Part> getAttachmentParts(Part message) {
    List<Part> attachmentCollector = new ArrayList<>();
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
  public void collectMailParts(Part part, List<Part> bodyCollector, List<Part> attachmentCollector, List<Part> inlineAttachmentCollector) {
    if (part == null) {
      return;
    }
    try {
      String disposition = getDispositionSafely(part);
      if (disposition != null && disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
        if (attachmentCollector != null) {
          attachmentCollector.add(part);
        }
      }
      else {
        Object content = null;
        try { // NOSONAR
          // getContent might throw a MessagingException for legitimate parts (e.g. some images end up in a javax.imageio.IIOException for example).
          content = getPartContent(part);
        }
        catch (MessagingException | IOException e) {
          Exception exceptionForLog = LOG.isDebugEnabled() ? e : null;
          LOG.info("Unable to get mime part content due to {}: {}", e.getClass().getSimpleName(), e.getMessage(), exceptionForLog);
        }

        if (content instanceof Multipart) {
          Multipart multiPart = (Multipart) content;
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
          else if (part.isMimeType(CONTENT_TYPE_MESSAGE_RFC822) && content instanceof MimeMessage) {
            // it's a MIME message in rfc822 format as attachment therefore we have to set the filename for the attachment correctly.
            if (attachmentCollector != null) {
              String fileName = getFilenameFromRfc822Attachment(part);
              if (StringUtility.isNullOrEmpty(fileName)) {
                fileName = "originalMessage.eml";
              }
              RFCWrapperPart wrapperPart = new RFCWrapperPart(part, fileName);
              attachmentCollector.add(wrapperPart);
            }
          }
          else if (disposition != null && disposition.equals(Part.INLINE)) {
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

  protected String getDispositionSafely(Part part) {
    try {
      return part.getDisposition();
    }
    catch (MessagingException e) {
      // Rare cases where content disposition header is set but empty, assuming no disposition at all.
      LOG.info("Unable to get disposition", LOG.isDebugEnabled() ? e : null);
      return null;
    }
  }

  protected Object getPartContent(Part part) throws MessagingException, IOException {
    if (part == null) {
      return null;
    }
    if (part instanceof MimePart) {
      autoFixEncoding((MimePart) part);
    }
    autoFixCharset(part);
    return part.getContent();
  }

  /**
   * Fixes a special behavior when access of {@link Part#getContent()} results in an {@link NullPointerException} due to
   * an invalid charset.
   * <p>
   * If charset is valid or {@link Part#getContent()} doesn't result in a {@link NullPointerException}, no fix is
   * applied.
   * <p>
   * As java.io.InputStreamReader.InputStreamReader(InputStream, String) might throw up with a
   * {@link NullPointerException} in case of an unknown character set, if such an exception occurs, the character set is
   * replaced with {@link StandardCharsets#UTF_8} even though this may lead to display errors.
   * </p>
   */
  protected void autoFixCharset(Part part) {
    String charset = null;
    try {
      charset = getPartCharsetInternal(part);
      if (charset == null || Charset.isSupported(charset)) {
        return;
      }
      part.getContent();
    }
    catch (NullPointerException | UnsupportedEncodingException e) { // NOSONAR
      String msgId = null;
      if (part instanceof MimeMessage) {
        msgId = getMessageIdSafely((MimeMessage) part);
      }
      Exception exceptionForLog = LOG.isDebugEnabled() ? e : null;
      LOG.info("Mail part '{}' uses an unsupported character set '{}'. Use UTF-8 as fallback.", msgId, charset, exceptionForLog);
      // Update charset of content type so that when accessing part.getContent() again no NPE is thrown
      // (UnsupportedEncodingException might still be thrown when part.getContent() didn't result in an NPE).
      if (charset == null) {
        return; // cannot fix
      }
      try {
        String contentType = part.getContentType(); // cannot be null because otherwise charset would have been null already
        part.setHeader(CONTENT_TYPE_ID, contentType.replace(charset, StandardCharsets.UTF_8.name()));
      }
      catch (Exception ex) {
        LOG.info("Unable to switch to default character set for message with ID '{}'.", msgId);
      }
    }
    catch (Exception e) {
      LOG.trace("autoFixCharset: Exception has occurred.", e); // explicitly trace
    }
  }

  /**
   * Some parts use an invalid encoding (not to be confused with the charset!) which is not supported. If this is
   * detected, remove it and try with the default encoding.<br>
   * Supported encodings are listed in {@link MimeUtility#decode(InputStream, String)} If this fix is not applied, a
   * MessagingException would be thrown on {@link Part#getContent()}
   */
  protected void autoFixEncoding(MimePart part) {
    if (part == null) {
      return;
    }
    String encoding = null;
    try {
      encoding = part.getEncoding();
      if (encoding == null) {
        return; // no encoding present. No need to test it
      }
      part.getContent(); // triggers the decode
    }
    catch (Exception e) {
      // use exception message as indicator because no list of supported encodings exists to be checked against
      String exMsg = e.getMessage();
      String identifier = "Unknown encoding";
      String msgId = null;
      if (part instanceof MimeMessage) {
        msgId = getMessageIdSafely((MimeMessage) part);
      }
      if (exMsg != null && exMsg.regionMatches(true, 0, identifier, 0, identifier.length())) {
        // Supplied encoding is not supported.
        // Remove it as otherwise MimeUtility#decode fails with MessagingException
        // If no header is supplied the data will be read as is (no special decoding)
        Exception exceptionForLog = LOG.isDebugEnabled() ? e : null;
        LOG.info("Mail part '{}' uses an unsupported Content-Transfer-Encoding '{}' . Use default encoding as fallback.", msgId, encoding, exceptionForLog);
        try {
          part.removeHeader("Content-Transfer-Encoding");
        }
        catch (Exception ex) {
          LOG.info("Unable to switch to default encoding for message with ID '{}'.", msgId);
        }
      }
      else {
        LOG.trace("autoFixTransferEncoding: Exception has occurred for message with ID '{}'.", msgId, e); // explicitly trace
      }
    }
  }

  /**
   * @param message
   *          Message to look for html body part and read content from.
   * @return Content from html body part encoded with the encoding given in the MIME header or UTF-8 encoded or null if
   *         the html part is not given.
   */
  public String getHtmlBody(Part message) {
    List<Part> bodyParts = getBodyParts(message);
    Part htmlPart = getHtmlPart(bodyParts);
    return readContentAsString(htmlPart);
  }

  /**
   * @param message
   *          Message to look for plain text body part and read content from.
   * @return Content from plain text part encoded with the encoding given in the MIME header or UTF-8 encoded or null if
   *         the plainText Part is not given
   */
  public String getPlainText(Part message) {
    List<Part> bodyParts = getBodyParts(message);
    Part plainTextPart = getPlainTextPart(bodyParts);
    return readContentAsString(plainTextPart);
  }

  /**
   * Reads the content of the part as string, with the encoding given in the MIME header or UTF-8 encoded.
   */
  public String readContentAsString(Part part) {
    if (part == null) {
      return null;
    }

    try {
      getPartContent(part); // to apply auto-fixes
      String charset = getPartCharsetSafely(part).name();
      try (InputStream in = part.getInputStream()) {
        return in == null ? null : IOUtility.readString(in, charset);
      }
    }
    catch (IOException | MessagingException e) {
      throw new ProcessingException("Failed to read content as string", e);
    }
  }

  public Part getHtmlPart(List<? extends Part> bodyParts) {
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

  public Part getPlainTextPart(List<Part> bodyParts) {
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

  public DataSource createDataSource(File file) {
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
   * @param fileName
   *          e.g. "file.txt"
   * @param fileExtension
   *          e.g. "txt", "jpg"
   */
  public DataSource createDataSource(InputStream inStream, String fileName, String fileExtension) {
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
  // See methods testCreateMimeMessage*Structure within MailHelperCreateMimeMessageTest for the various content structures created by this method.
  public CharsetSafeMimeMessage createMimeMessage(MailMessage mailMessage) {
    if (mailMessage == null) {
      throw new IllegalArgumentException("Mail message is missing");
    }

    List<? extends MailAttachment> inlineAttachments = mailMessage.getInlineAttachments();
    List<? extends MailAttachment> attachments = mailMessage.getAttachments();

    boolean hasPlainText = !StringUtility.isNullOrEmpty(mailMessage.getBodyPlainText());
    boolean hasHtml = !StringUtility.isNullOrEmpty(mailMessage.getBodyHtml());
    boolean hasInlineAttachments = !inlineAttachments.isEmpty();
    boolean hasAttachments = !attachments.isEmpty();

    if (!hasPlainText && !hasHtml) {
      // No content for email
      return null;
    }

    try {
      CharsetSafeMimeMessage m = new CharsetSafeMimeMessage();

      if (!hasAttachments && !hasInlineAttachments) {
        // Use message as part for plain/html directly
        setPlainHtmlBodyPart(m, mailMessage.getBodyPlainText(), mailMessage.getBodyHtml());
      }
      else {
        // Attachments or inline attachments available

        // Create own body part for plain/html part
        MimeBodyPart plainHtmlPart = new MimeBodyPart();
        setPlainHtmlBodyPart(plainHtmlPart, mailMessage.getBodyPlainText(), mailMessage.getBodyHtml());

        // If inline attachments are available, "repack" plainHtmlPart into a related part with the inline attachments
        MimeMultipart relatedPart = null;
        if (!inlineAttachments.isEmpty()) {
          relatedPart = new MimeMultipart("related");
          relatedPart.addBodyPart(plainHtmlPart);

          for (MailAttachment attachment : inlineAttachments) {
            relatedPart.addBodyPart(createAttachmentPart(attachment, true, m.getCharset()));
          }

          m.setContent(relatedPart);
        }

        if (attachments.isEmpty()) {
          // No inline attachments were available, thus related part is set
          Assertions.assertNotNull(relatedPart, "Invalid state because related part is null");
          m.setContent(relatedPart);
        }
        else {
          // create multipart/mixed part for attachments
          MimeMultipart multiPart = new MimeMultipart();

          if (relatedPart != null) {
            // Inline attachments were available and a related part was created
            MimeBodyPart relatedBodyPart = new MimeBodyPart();
            relatedBodyPart.setContent(relatedPart);
            multiPart.addBodyPart(relatedBodyPart);
          }
          else {
            // No online attachments, thus use plainHtmlPart directly
            multiPart.addBodyPart(plainHtmlPart);
          }

          for (MailAttachment attachment : attachments) {
            multiPart.addBodyPart(createAttachmentPart(attachment, false, m.getCharset()));
          }

          m.setContent(multiPart);
        }
      }

      if (mailMessage.getSender() != null && StringUtility.hasText(mailMessage.getSender().getEmail())) {
        InternetAddress addressSender = createInternetAddress(mailMessage.getSender());
        m.setFrom(addressSender);
        m.setSender(addressSender);
      }
      if (!CollectionUtility.isEmpty(mailMessage.getReplyTos())) {
        m.setReplyTo(createInternetAddresses(mailMessage.getReplyTos()));
      }
      if (StringUtility.hasText(mailMessage.getSubject())) {
        m.setSubject(mailMessage.getSubject(), StandardCharsets.UTF_8.name());
      }
      if (!CollectionUtility.isEmpty(mailMessage.getToRecipients())) {
        m.setRecipients(RecipientType.TO, createInternetAddresses(mailMessage.getToRecipients()));
      }
      if (!CollectionUtility.isEmpty(mailMessage.getCcRecipients())) {
        m.setRecipients(RecipientType.CC, createInternetAddresses(mailMessage.getCcRecipients()));
      }
      if (!CollectionUtility.isEmpty(mailMessage.getBccRecipients())) {
        m.setRecipients(RecipientType.BCC, createInternetAddresses(mailMessage.getBccRecipients()));
      }

      m.saveChanges();

      return m;
    }
    catch (Exception e) {
      throw new ProcessingException("Failed to create MimeMessage.", e);
    }
  }

  /**
   * @param inline
   *          <code>true</code> if it's an inline attachment, <code>false</code> otherwise.
   */
  protected MimeBodyPart createAttachmentPart(MailAttachment attachment, boolean inline, String charset) throws MessagingException {
    MimeBodyPart part = new MimeBodyPart();
    DataHandler handler = new DataHandler(attachment.getDataSource());
    part.setDataHandler(handler);
    if (StringUtility.hasText(attachment.getDataSource().getName())) {
      part.setFileName(encodeAttachmentFilename(attachment.getDataSource().getName(), charset));
    }
    else {
      // part.setFilename would implicitly set disposition to attachment, but filename is null and therefore can not be set (would result in a npe)
      // this case is used to create MimeMessages for unit tests
      part.setDisposition(MimeBodyPart.ATTACHMENT);
    }
    if (StringUtility.hasText(attachment.getContentId())) {
      part.setContentID("<" + attachment.getContentId() + ">");
    }
    if (inline) {
      part.setDisposition(MimeBodyPart.INLINE);
    }
    return part;
  }

  protected void setPlainHtmlBodyPart(MimePart part, String bodyTextPlain, String bodyTextHtml) throws MessagingException {
    if (!StringUtility.isNullOrEmpty(bodyTextPlain) && !StringUtility.isNullOrEmpty(bodyTextHtml)) {
      // multipart
      MimeBodyPart plainPart = new MimeBodyPart();
      MimeBodyPart htmlPart = new MimeBodyPart();

      setSingleBodyPart(plainPart, bodyTextPlain, CONTENT_TYPE_TEXT_PLAIN);
      setSingleBodyPart(htmlPart, bodyTextHtml, CONTENT_TYPE_TEXT_HTML);

      Multipart alternativePart = new MimeMultipart("alternative");
      alternativePart.addBodyPart(plainPart);
      alternativePart.addBodyPart(htmlPart);

      part.setContent(alternativePart);
    }
    else if (!StringUtility.isNullOrEmpty(bodyTextPlain)) {
      setSingleBodyPart(part, bodyTextPlain, CONTENT_TYPE_TEXT_PLAIN);
    }
    else if (!StringUtility.isNullOrEmpty(bodyTextHtml)) {
      setSingleBodyPart(part, bodyTextHtml, CONTENT_TYPE_TEXT_HTML);
    }
  }

  /**
   * Creates a single mime body part.
   *
   * @param bodyText
   *          Body text
   * @param contentType
   *          Content type
   */
  protected void setSingleBodyPart(MimePart part, String bodyText, String contentType) throws MessagingException {
    part.setText(bodyText, StandardCharsets.UTF_8.name());
    part.addHeader(CONTENT_TYPE_ID, contentType);
  }

  /**
   * @return {@link MimeMessage} created out of given {@code byte[]}
   */
  public MimeMessage createMessageFromBytes(byte[] bytes) {
    return createMessageFromBytes(bytes, null);
  }

  /**
   * @return {@link MimeMessage} created out of given {@code byte[]}
   */
  public MimeMessage createMessageFromBytes(byte[] bytes, Session session) {
    try {
      ByteArrayInputStream st = new ByteArrayInputStream(bytes);
      return new MimeMessage(session, st);
    }
    catch (Exception e) {
      throw new ProcessingException("Unexpected: ", e);
    }
  }

  /**
   * @return The given {@link MimeMessage} as byte[] formatted according to RFC 822.
   */
  public byte[] getMessageAsBytes(MimeMessage mimeMessage) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      mimeMessage.writeTo(out);
      return out.toByteArray();
    }
    catch (MessagingException | IOException e) {
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
  public void addAttachmentsToMimeMessage(MimeMessage msg, List<File> attachments) {
    if (CollectionUtility.isEmpty(attachments)) {
      return;
    }

    try {
      Multipart multiPart = prepareMessageForAttachments(msg);
      String charset = getPartCharsetSafely(msg).name();

      for (File attachment : attachments) {
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setDataHandler(new DataHandler(new FileDataSource(attachment)));
        bodyPart.setFileName(encodeAttachmentFilename(attachment.getName(), charset));
        multiPart.addBodyPart(bodyPart);
      }
      msg.saveChanges();
    }
    catch (MessagingException | IOException e) {
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
  public void addResourcesAsAttachments(MimeMessage msg, List<BinaryResource> attachments) {
    if (CollectionUtility.isEmpty(attachments)) {
      return;
    }

    try {
      Multipart multiPart = prepareMessageForAttachments(msg);
      String charset = getPartCharsetSafely(msg).name();

      for (BinaryResource attachment : attachments) {
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setDataHandler(new DataHandler(new BinaryResourceDataSource(attachment)));
        bodyPart.setFileName(encodeAttachmentFilename(attachment.getFilename(), charset));
        multiPart.addBodyPart(bodyPart);
      }
      msg.saveChanges();
    }
    catch (MessagingException | IOException e) {
      throw new ProcessingException("Failed to add attachment to existing mime message", e);
    }
  }

  /**
   * Prepares the mime message so that attachments can be added to the returned {@link Multipart}.
   *
   * @param msg
   *          Mime message to prepare
   * @return Multipart to which attachments can be added
   */
  protected Multipart prepareMessageForAttachments(MimeMessage msg) throws IOException, MessagingException {
    Object messageContent = getPartContent(msg);

    Multipart multiPart;
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
        multiPartBody.setContent(message, CONTENT_TYPE_TEXT_HTML);
        multiPartBody.setHeader(CONTENT_TYPE_ID, CONTENT_TYPE_TEXT_HTML);
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
  public String getContentTypeForExtension(String ext) {
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
  public InternetAddress createInternetAddress(MailParticipant participant) {
    if (participant == null) {
      return null;
    }
    return createInternetAddress(participant.getEmail(), participant.getName());
  }

  /**
   * Creates an Internet address for the given mail participant allowing usage of an internationalized domain name.
   *
   * @return {@link InternetAddress} instance or {@code null} if no email or an empty email is provided
   */
  public InternetAddress createInternetAddress(String email) {
    return createInternetAddress(email, null);
  }

  /**
   * Creates an Internet address for the given mail participant allowing usage of an internationalized domain name.
   *
   * @return {@link InternetAddress} instance or {@code null} if no email or an empty email is provided
   */
  public InternetAddress createInternetAddress(String email, String name) {
    if (StringUtility.isNullOrEmpty(email)) {
      return null;
    }

    try {
      InternetAddress internetAddress = new InternetAddress(BEANS.get(MailIDNConverter.class).toASCII(email));
      if (StringUtility.hasText(name)) {
        internetAddress.setPersonal(name, StandardCharsets.UTF_8.name());
      }
      return internetAddress;
    }
    catch (AddressException | IllegalArgumentException e) {
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
  public InternetAddress[] parseInternetAddressList(String addressList) {
    String[] addressListSplit = StringUtility.split(addressList, ",");
    InternetAddress[] result = new InternetAddress[addressListSplit.length];
    for (int i = 0; i < addressListSplit.length; i++) {
      result[i] = createInternetAddress(addressListSplit[i]);
    }
    return result;
  }

  /**
   * Careful: this method returns null when the list of addresses is empty! This is a (stupid) default by
   * javax.mime.Message.
   * <p>
   * Array instead of list is returned in order to directly used to result with
   * {@link MimeMessage#setRecipients(RecipientType, javax.mail.Address[])}.
   */
  protected InternetAddress[] createInternetAddresses(List<? extends MailParticipant> participants) {
    if (CollectionUtility.isEmpty(participants)) {
      return null;
    }
    ArrayList<InternetAddress> addressList = new ArrayList<>();
    for (MailParticipant participant : participants) {
      InternetAddress internetAddress = createInternetAddress(participant);
      if (internetAddress != null) {
        addressList.add(internetAddress);
      }
    }
    return addressList.toArray(new InternetAddress[0]);
  }

  /**
   * Parses the {@link Charset} for the given {@link Part}.<br>
   * If the {@link Part} declares a {@link Charset}, this charset is used if supported by the system.<br>
   * Otherwise, the best matching charset is detected based on the content of the {@link Part}. If it cannot be
   * detected, UTF-8 is used.
   *
   * @param part
   *          The part for which the {@link Charset} should be parsed. May be {@code null}.
   * @return The best {@link Charset} to read the content of the {@link Part} given.
   * @throws MessagingException
   *           if the content-type of the {@link Part} cannot be retrieved.
   */
  protected Charset getPartCharsetSafely(Part part) throws MessagingException {
    // 1. try to use charset declared in part (if present)
    Charset charset = getPartCharset(part);
    if (charset != null) {
      return charset;
    }

    // 2. charset not specified in part or not supported: try to detect the charset based on content
    charset = guessPartCharset(part);
    if (charset != null) {
      return charset;
    }

    // 3. default if also detection fails
    return StandardCharsets.UTF_8;
  }

  protected Charset guessPartCharset(Part part) {
    if (part == null) {
      return null;
    }

    try (InputStream in = part.getInputStream()) {
      return BEANS.get(CharsetDetector.class).guessCharset(in, false /* not needed as the stream is not reused */);
    }
    catch (Exception e) {
      Exception exceptionForLog = LOG.isDebugEnabled() ? e : null;
      LOG.info("Part charset could not be detected for '{}'.", part, exceptionForLog);
    }
    return null;
  }

  /**
   * Parses the charset declared in the given part. If none or and invalid charset is found, {@code null} is returned.
   */
  @SuppressWarnings("squid:S1166") // catch of IllegalCharsetNameException without a rethrow
  public Charset getPartCharset(Part part) throws MessagingException {
    String charset = getPartCharsetInternal(part);
    if (charset == null) {
      return null;
    }

    try {
      if (Charset.isSupported(charset)) {
        return Charset.forName(charset);
      }
    }
    catch (IllegalCharsetNameException e) {
      // ignore exception itself (use trace log)
      LOG.trace("Part has an invalid charset '{}'", charset, e);
    }
    return null;
  }

  /**
   * @return Charset as string
   */
  protected String getPartCharsetInternal(Part part) throws MessagingException {
    if (part == null) {
      return null;
    }

    String contentType = part.getContentType();
    if (contentType == null) {
      return null;
    }

    try {
      return new ContentType(contentType).getParameter("charset");
    }
    catch (ParseException e) {
      LOG.trace("Failed to parse content type '{}'", contentType, e);
      return null;
    }
  }

  /**
   * @return {@code true} if syntax of specified {@code emailAddress} is valid, else {@code false}.
   */
  @SuppressWarnings("squid:S1166")
  public boolean isEmailAddressValid(String emailAddress) {
    if (StringUtility.isNullOrEmpty(emailAddress)) {
      return false;
    }

    try {
      new InternetAddress(BEANS.get(MailIDNConverter.class).toASCII(emailAddress), true);
      return true;
    }
    catch (AddressException | IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * If {@link MimeMessage#getFrom()} contains no addresses, default from email is used instead.
   */
  public void ensureFromAddress(MimeMessage message, String defaultFromEmail) {
    try {
      Address[] fromAddresses = message.getFrom();
      if (fromAddresses != null && fromAddresses.length != 0) {
        // From address is already set
        return;
      }

      if (StringUtility.hasText(defaultFromEmail)) {
        message.setFrom(createInternetAddress(defaultFromEmail));
      }
    }
    catch (MessagingException e) {
      throw new ProcessingException("Couldn't apply 'default from' to message", e);
    }
  }

  /**
   * Prepends the subject prefix to the existing subject if the subject doesn't already start with the subject prefix.
   * <p>
   * If the subject prefix has no text, nothing is added. If there is no subject yet, the new subject is the subject
   * prefix.
   *
   * @param message
   *          Message to add the prefix to ({@link MimeMessage#getSubject()}.
   * @param subjectPrefix
   *          Subject prefix to add.
   */
  public void addPrefixToSubject(MimeMessage message, String subjectPrefix) {
    if (message == null) {
      return;
    }

    try {
      String messageSubject = message.getSubject();
      if (StringUtility.hasText(subjectPrefix) && !StringUtility.startsWith(messageSubject, subjectPrefix)) {
        message.setSubject(StringUtility.join("", subjectPrefix, messageSubject), StandardCharsets.UTF_8.name());
      }
    }
    catch (MessagingException e) {
      throw new ProcessingException("Couldn't add the prefix to the message's subject", e);
    }
  }

  /**
   * Extract message ids by reading the {@link #HEADER_IN_REPLY_TO} headers if available or otherwise try to read
   * message id lines from the third part of the message (contains details of the DSN, see
   * https://tools.ietf.org/html/rfc3461#section-6.2).
   */
  public List<String> extractInReplyMessageIds(MimeMessage mimeMessage) {
    if (mimeMessage == null) {
      return Collections.emptyList();
    }

    String[] replyToHeaders;
    try {
      replyToHeaders = mimeMessage.getHeader(HEADER_IN_REPLY_TO);
    }
    catch (MessagingException e1) {
      Exception exceptionForLog = LOG.isDebugEnabled() ? e1 : null;
      LOG.info("Could not parse headers for message with id: {}", getMessageIdSafely(mimeMessage), exceptionForLog);
      return Collections.emptyList();
    }

    if (replyToHeaders == null || replyToHeaders.length == 0) {
      try {
        Object object = getPartContent(mimeMessage);
        if (object instanceof Multipart) {
          Multipart content = (Multipart) object;
          if (content.getCount() >= 3) {
            // Try third part of the message, contains details of the DSN (https://tools.ietf.org/html/rfc3461#section-6.2).
            BodyPart part = content.getBodyPart(2);
            Charset charset = getPartCharsetSafely(part);
            try (InputStreamReader in = new InputStreamReader(part.getInputStream(), charset); BufferedReader reader = new BufferedReader(in)) {
              String s;
              while ((s = reader.readLine()) != null) {
                s = s.trim();
                if (s.toLowerCase().startsWith("Message-ID:".toLowerCase())) {
                  replyToHeaders = new String[]{s.substring("Message-ID:".length()).trim()};
                  break;
                }
              }
            }
          }
        }
      }
      catch (IOException | MessagingException e) {
        Exception exceptionForLog = LOG.isDebugEnabled() ? e : null;
        LOG.info("Unable to get third part of dsn-message for message with id: {}", getMessageIdSafely(mimeMessage), exceptionForLog);
        return Collections.emptyList();
      }
    }

    if (replyToHeaders == null || replyToHeaders.length == 0) {
      LOG.debug("Message IDs couldn't be extracted because it does not have an 'In-Reply-To' header or other DSN information to with original Message-ID. Id: {}", getMessageIdSafely(mimeMessage));
      return Collections.emptyList();
    }

    return Arrays.asList(replyToHeaders);
  }

  /**
   * Retrieves the value of the 'Message-Id' header field of the message, without throwing an exception.
   *
   * @param mimeMessage
   *          message
   * @return Message-Id or {@code null}
   */
  public String getMessageIdSafely(MimeMessage mimeMessage) {
    if (mimeMessage == null) {
      return null;
    }

    try {
      return mimeMessage.getMessageID();
    }
    catch (MessagingException e) {
      LOG.info("Could not retrieve message id", LOG.isDebugEnabled() ? e : null);
      return null;
    }
  }

  /**
   * Returns the decoded and normalized filename from {@link Part#getFileName()} if available. Otherwise, guesses the
   * file extension via content type ({@link Part#getContentType()}) and calls the default filename function with the
   * file extension (or {@code null} if none could be guessed).
   *
   * @param part
   *          Attachment part
   * @param defaultFilenameFunction
   *          Mandatory function called with guessed file extension (might be null) if no filename was found.
   * @return Filename of the given attachment part.
   */
  public String getAttachmentFilename(Part part, Function<String, String> defaultFilenameFunction) {
    Assertions.assertNotNull(part, "Part must not be null");
    Assertions.assertNotNull(defaultFilenameFunction, "Default filename function must not be null");
    try {
      String filename = decodeAttachmentFilename(part.getFileName());
      if (filename != null) {
        return filename;
      }

      String fileExtension = guessAttachmentFileExtension(part.getContentType());
      return defaultFilenameFunction.apply(fileExtension);
    }
    catch (MessagingException e) {
      LOG.info("Failed to get attachment filename", LOG.isDebugEnabled() ? e : null);
      return null;
    }
  }

  public String getFilenameFromRfc822Attachment(Part part) throws MessagingException, IOException {
    if (part == null) {
      return null;
    }
    Object content = getPartContent(part);
    if (!(content instanceof MimeMessage)) {
      return null;
    }
    String subject = ((MimeMessage) content).getSubject();
    if (StringUtility.hasText(subject)) {
      String name = FileUtility.toValidFilename(subject);
      if (StringUtility.hasText(name)) {
        return name + "." + guessAttachmentFileExtension(part.getContentType());
      }
    }
    return null;
  }

  /**
   * Decodes an attachment filename.
   * <p>
   * Used internal by {@link #getAttachmentFilename(Part, Function)}.
   *
   * @param filename
   *          Filename as provided by {@link Part#getFileName()}.
   */
  protected String decodeAttachmentFilename(String filename) {
    if (filename == null) {
      return null;
    }

    try {
      String decoded = MimeUtility.decodeText(filename);
      return Normalizer.normalize(decoded, Normalizer.Form.NFC);
    }
    catch (Exception e) {
      LOG.info("Failed to clean attachment filename", LOG.isDebugEnabled() ? e : null);
      return filename;
    }
  }

  /**
   * Encodes an attachment filename.
   */
  protected String encodeAttachmentFilename(String filename, String charset) {
    if (filename == null) {
      return null;
    }
    if (StringUtility.isNullOrEmpty(charset)) {
      charset = StandardCharsets.UTF_8.name();
    }
    try {
      return MimeUtility.encodeText(filename, MimeUtility.mimeCharset(charset), null);
    }
    catch (UnsupportedEncodingException e) {
      LOG.info("Failed to encode attachment filename", LOG.isDebugEnabled() ? e : null);
      return filename;
    }
  }

  /**
   * Guesses attachment file extension based on a content type.
   * <p>
   * Used internal by {@link #getAttachmentFilename(Part, Function)} if not filename is available.
   *
   * @param contentType
   *          Content type as provided by {@link Part#getContentType()}.
   * @return File extension for content type (e.g. txt, eml, ...) if one is found, {@code null} otherwise.
   */
  protected String guessAttachmentFileExtension(String contentType) {
    if (contentType == null) {
      return null;
    }

    ContentType ct;
    try {
      ct = new ContentType(contentType);
    }
    catch (ParseException e) {
      Exception exceptionForLog = LOG.isDebugEnabled() ? e : null;
      LOG.info("Failed to parse content type '{}'", contentType, exceptionForLog);
      return null;
    }

    String baseType = ct.getBaseType();
    MimeType mimeType = MimeType.convertToMimeType(baseType);
    return mimeType == null ? null : mimeType.getFileExtension();
  }
}
