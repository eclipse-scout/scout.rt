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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.activation.DataSource;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.mail.MailAttachment;

/**
 * @deprecated Will be removed in N release. Use org.eclipse.scout.commons.mail.MailUtility instead. See method comments
 *             for detailed migration notes.
 */
@Deprecated
@SuppressWarnings("restriction")
public final class MailUtility {

  public static final IScoutLogger LOG = ScoutLogManager.getLogger(MailUtility.class);

  public static final String CONTENT_TYPE_ID = org.eclipse.scout.commons.mail.MailUtility.CONTENT_TYPE_ID;

  public static final String CONTENT_TRANSFER_ENCODING_ID = org.eclipse.scout.commons.mail.MailUtility.CONTENT_TRANSFER_ENCODING_ID;
  public static final String QUOTED_PRINTABLE = org.eclipse.scout.commons.mail.MailUtility.QUOTED_PRINTABLE;

  public static final String CONTENT_TYPE_TEXT_HTML = org.eclipse.scout.commons.mail.MailUtility.CONTENT_TYPE_TEXT_HTML;
  public static final String CONTENT_TYPE_TEXT_PLAIN = org.eclipse.scout.commons.mail.MailUtility.CONTENT_TYPE_TEXT_PLAIN;
  public static final String CONTENT_TYPE_MESSAGE_RFC822 = org.eclipse.scout.commons.mail.MailUtility.CONTENT_TYPE_MESSAGE_RFC822;
  public static final String CONTENT_TYPE_MULTIPART = org.eclipse.scout.commons.mail.MailUtility.CONTENT_TYPE_MULTIPART;

  @SuppressWarnings("deprecation")
  public static final Pattern wordPatternItem = WordMailUtility.wordPatternItem;
  @SuppressWarnings("deprecation")
  public static final Pattern wordPatternProps = WordMailUtility.wordPatternProps;

  private MailUtility() {
  }

  /**
   * Container for the mail body in plain text and html.
   * For the correct html representation it contains also a list of files referenced in the html.
   *
   * @deprecated Will be removed in N release. Replacement: none.
   */
  @Deprecated
  public static class MailMessage {

    private String m_plainText;
    private String m_htmlText;
    private List<File> m_htmlAttachmentList;

    public MailMessage(String plainText, String htmlText, List<File> htmlAttachmentList) {
      m_plainText = plainText;
      m_htmlText = htmlText;
      m_htmlAttachmentList = htmlAttachmentList;
    }

    public String getPlainText() {
      return m_plainText;
    }

    public String getHtmlText() {
      return m_htmlText;
    }

    public List<File> getHtmlAttachmentList() {
      return m_htmlAttachmentList;
    }
  }

  /**
   * @deprecated Will be removed in N release. Use {@link org.eclipse.scout.commons.mail.MailUtility#getBodyParts(Part)}
   *             instead.
   */
  @Deprecated
  public static Part[] getBodyParts(Part message) throws ProcessingException {
    List<Part> bodyCollector = new ArrayList<Part>();
    collectMailParts(message, bodyCollector, null, null);
    return bodyCollector.toArray(new Part[bodyCollector.size()]);
  }

  /**
   * @deprecated Will be removed in N release. Use
   *             {@link org.eclipse.scout.commons.mail.MailUtility#getAttachmentParts(Part)} instead.
   */
  @Deprecated
  public static Part[] getAttachmentParts(Part message) throws ProcessingException {
    List<Part> attachmentCollector = new ArrayList<Part>();
    collectMailParts(message, null, attachmentCollector, null);
    return attachmentCollector.toArray(new Part[attachmentCollector.size()]);
  }

  /**
   * @deprecated Will be removed in N release. Use {@link #collectMailParts(Part, List, List, List) instead.
   */
  @Deprecated
  public static void collectMailParts(Part message, List<Part> bodyCollector, List<Part> attachmentCollector) throws ProcessingException {
    org.eclipse.scout.commons.mail.MailUtility.collectMailParts(message, bodyCollector, attachmentCollector, null);
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
   * @throws ProcessingException
   * @deprecated Will be removed in N release. Use
   *             {@link org.eclipse.scout.commons.mail.MailUtility#collectMailParts(Part, List, List, List)} instead.
   */
  @Deprecated
  public static void collectMailParts(Part part, List<Part> bodyCollector, List<Part> attachmentCollector, List<Part> inlineAttachmentCollector) throws ProcessingException {
    org.eclipse.scout.commons.mail.MailUtility.collectMailParts(part, bodyCollector, attachmentCollector, inlineAttachmentCollector);
  }

  /**
   * @param part
   * @return the plainText part encoded with the encoding given in the MIME header or UTF-8 encoded or null if the
   *         plainText Part is not given
   * @throws ProcessingException
   * @deprecated Will be removed in N release. Use {@link org.eclipse.scout.commons.mail.MailUtility#getPlainText(Part)}
   *             instead.
   */
  @Deprecated
  public static String getPlainText(Part part) throws ProcessingException {
    return org.eclipse.scout.commons.mail.MailUtility.getPlainText(part);
  }

  /**
   * @deprecated Will be removed in N release. Use {@link #getHtmlPart(List)} instead.
   */
  @Deprecated
  public static Part getHtmlPart(Part[] bodyParts) throws ProcessingException {
    return org.eclipse.scout.commons.mail.MailUtility.getHtmlPart(CollectionUtility.arrayList(bodyParts));
  }

  /**
   * @deprecated Will be removed in N release. Use {@link #getPlainTextPart(List)} instead.
   */
  @Deprecated
  public static Part getPlainTextPart(Part[] bodyParts) throws ProcessingException {
    return org.eclipse.scout.commons.mail.MailUtility.getPlainTextPart(CollectionUtility.arrayList(bodyParts));
  }

  /**
   * @deprecated Will be removed in N release. Use
   *             {@link org.eclipse.scout.commons.mail.MailUtility#createDataSource(File))} instead.
   */
  @Deprecated
  public static DataSource createDataSource(File file) throws ProcessingException {
    return org.eclipse.scout.commons.mail.MailUtility.createDataSource(file);
  }

  /**
   * @param inStream
   * @param fileName
   *          e.g. "file.txt"
   * @param fileExtension
   *          e.g. "txt", "jpg"
   * @return
   * @throws ProcessingException
   * @deprecated Will be removed in N release. Use
   *             {@link org.eclipse.scout.commons.mail.MailUtility#createDataSource(InputStream, String, String)}
   *             instead.
   */
  @Deprecated
  public static DataSource createDataSource(InputStream inStream, String fileName, String fileExtension) throws ProcessingException {
    return org.eclipse.scout.commons.mail.MailUtility.createDataSource(inStream, fileName, fileExtension);
  }

  /**
   * @deprecated Will be removed in N release. Replacement: none.
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  public static MailMessage extractMailMessageFromWordArchive(File archiveFile) {
    return WordMailUtility.extractMailMessageFromWordArchive(archiveFile);
  }

  /**
   * @deprecated Will be removed in N release. Replacement: none.
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  public static String extractPlainTextFromWordArchive(File archiveFile) {
    return WordMailUtility.extractPlainTextFromWordArchive(archiveFile);
  }

  /**
   * Create {@link MimeMessage} from plain text fields.
   *
   * @rn aho, 19.01.2009
   * @deprecated Will be removed in N release. Use
   *             {@link #createMimeMessage(org.eclipse.scout.commons.mail.MailMessage)} instead. SentDate
   *             is not set anymore, if required, use msg.setSentDate(new java.util.Date());
   */
  @Deprecated
  public static MimeMessage createMimeMessage(String[] toRecipients, String sender, String subject, String bodyTextPlain, DataSource[] attachments) throws ProcessingException {
    return createMimeMessage(toRecipients, null, null, sender, subject, bodyTextPlain, attachments);
  }

  /**
   * Create {@link MimeMessage} from plain text fields.
   *
   * @rn aho, 19.01.2009
   * @deprecated Will be removed in N release. Use
   *             {@link #createMimeMessage(org.eclipse.scout.commons.mail.MailMessage)} instead. SentDate
   *             is not set anymore, if required, use msg.setSentDate(new java.util.Date());
   */
  @Deprecated
  public static MimeMessage createMimeMessage(String[] toRecipients, String[] ccRecipients, String[] bccRecipients, String sender, String subject, String bodyTextPlain, DataSource[] attachments) throws ProcessingException {
    org.eclipse.scout.commons.mail.MailMessage mailMessage = new org.eclipse.scout.commons.mail.MailMessage();
    mailMessage.addToRecipients(CollectionUtility.arrayList(toRecipients));
    mailMessage.addCcRecipients(CollectionUtility.arrayList(ccRecipients));
    mailMessage.addBccRecipients(CollectionUtility.arrayList(bccRecipients));
    mailMessage.setSender(sender);
    mailMessage.setSubject(subject);
    mailMessage.setBodyPlainText(bodyTextPlain);
    if (attachments != null && attachments.length > 0) {
      for (DataSource source : attachments) {
        mailMessage.addAttachment(new MailAttachment(source, null));
      }
    }
    return org.eclipse.scout.commons.mail.MailUtility.createMimeMessage(mailMessage);
  }

  /**
   * @deprecated Will be removed in N release. Use
   *             {@link #createMimeMessage(org.eclipse.scout.commons.mail.MailMessage)} instead.
   */
  @Deprecated
  public static MimeMessage createMimeMessage(String messagePlain, String messageHtml, DataSource[] attachments) throws ProcessingException {
    org.eclipse.scout.commons.mail.MailMessage mailMessage = new org.eclipse.scout.commons.mail.MailMessage();
    mailMessage.setBodyPlainText(messagePlain);
    mailMessage.setBodyHtml(messageHtml);
    if (attachments != null && attachments.length > 0) {
      for (DataSource source : attachments) {
        mailMessage.addAttachment(new MailAttachment(source, null));
      }
    }
    return org.eclipse.scout.commons.mail.MailUtility.createMimeMessage(mailMessage);
  }

  /**
   * @deprecated Will be removed in N release. Replacement: none.
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  public static MimeMessage createMimeMessageFromWordArchiveDirectory(File archiveDir, String simpleName, File[] attachments, boolean markAsUnsent) throws ProcessingException {
    return WordMailUtility.createMimeMessageFromWordArchiveDirectory(archiveDir, simpleName, attachments, markAsUnsent);
  }

  /**
   * @deprecated Will be removed in N release. Replacement: none.
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  public static MimeMessage createMimeMessageFromWordArchive(File archiveFile, File[] attachments) throws ProcessingException {
    return WordMailUtility.createMimeMessageFromWordArchive(archiveFile, attachments);
  }

  /**
   * @deprecated Will be removed in N release. Replacement: none.
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  public static MimeMessage createMimeMessageFromWordArchive(File archiveFile, File[] attachments, boolean markAsUnsent) throws ProcessingException {
    return WordMailUtility.createMimeMessageFromWordArchive(archiveFile, attachments, markAsUnsent);
  }

  /**
   * @deprecated Will be removed in N release. Use
   *             {@link org.eclipse.scout.commons.mail.MailUtility#createMessageFromBytes(byte[])} instead.
   */
  @Deprecated
  public static MimeMessage createMessageFromBytes(byte[] bytes) throws ProcessingException {
    return org.eclipse.scout.commons.mail.MailUtility.createMessageFromBytes(bytes);
  }

  /**
   * @deprecated Will be removed in N release. Use
   *             {@link org.eclipse.scout.commons.mail.MailUtility#createMessageFromBytes(byte[], Session)} instead.
   */
  @Deprecated
  public static MimeMessage createMessageFromBytes(byte[] bytes, Session session) throws ProcessingException {
    return org.eclipse.scout.commons.mail.MailUtility.createMessageFromBytes(bytes, session);
  }

  /**
   * Adds the provided attachments to the existing mime message.
   *
   * @param msg
   *          Mime message to attach files to
   * @param attachments
   *          List of attachments (files).
   * @throws ProcessingException
   * @since 4.1
   * @deprecated Will be removed in N release. Use
   *             {@link org.eclipse.scout.commons.mail.MailUtility#addAttachmentsToMimeMessage(MimeMessage, List)}
   *             instead.
   */
  @Deprecated
  public static void addAttachmentsToMimeMessage(MimeMessage msg, List<File> attachments) throws ProcessingException {
    org.eclipse.scout.commons.mail.MailUtility.addAttachmentsToMimeMessage(msg, attachments);
  }

  /**
   * @since 2.7
   * @deprecated Will be removed in N release. Use
   *             {@link org.eclipse.scout.commons.mail.MailUtility#getContentTypeForExtension(String)} instead.
   */
  @Deprecated
  public static String getContentTypeForExtension(String ext) {
    return org.eclipse.scout.commons.mail.MailUtility.getContentTypeForExtension(ext);
  }

}
