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

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

/**
 * <h3>{@link MailUtility}</h3> <br>
 * all methods and constants moved to {@link MailHelper}, this utility will be removed in scout 8.0 <br>
 *
 * @deprecated use {@link MailHelper} instead
 */
@Deprecated
public final class MailUtility { // TODO [7.1] hmu: remove this utility

  public static final String CONTENT_TYPE_ID = MailHelper.CONTENT_TYPE_ID;
  public static final String CONTENT_ID_ID = MailHelper.CONTENT_ID_ID;

  public static final String CONTENT_TRANSFER_ENCODING_ID = MailHelper.CONTENT_TRANSFER_ENCODING_ID;
  public static final String QUOTED_PRINTABLE = MailHelper.QUOTED_PRINTABLE;

  public static final String CONTENT_TYPE_TEXT_HTML = MailHelper.CONTENT_TYPE_TEXT_HTML;
  public static final String CONTENT_TYPE_TEXT_PLAIN = MailHelper.CONTENT_TYPE_TEXT_PLAIN;
  public static final String CONTENT_TYPE_MESSAGE_RFC822 = MailHelper.CONTENT_TYPE_MESSAGE_RFC822;
  public static final String CONTENT_TYPE_IMAGE_PREFIX = MailHelper.CONTENT_TYPE_IMAGE_PREFIX;
  public static final String CONTENT_TYPE_MULTIPART_PREFIX = MailHelper.CONTENT_TYPE_MULTIPART_PREFIX;

  private MailUtility() {
  }

  /**
   * @deprecated use {@link MailHelper#getBodyParts(Part)} instead
   */
  @Deprecated
  public static List<Part> getBodyParts(Part message) {
    return BEANS.get(MailHelper.class).getBodyParts(message);
  }

  /**
   * @deprecated use {@link MailHelper#getAttachmentParts(Part)} instead
   */
  @Deprecated
  public static List<Part> getAttachmentParts(Part message) {
    return BEANS.get(MailHelper.class).getAttachmentParts(message);
  }

  /**
   * @deprecated use {@link MailHelper#collectMailParts(Part, List, List, List)} instead
   */
  @Deprecated
  public static void collectMailParts(Part part, List<Part> bodyCollector, List<Part> attachmentCollector, List<Part> inlineAttachmentCollector) {
    BEANS.get(MailHelper.class).collectMailParts(part, bodyCollector, attachmentCollector, inlineAttachmentCollector);
  }

  /**
   * @deprecated use {@link MailHelper#getPlainText(Part)} instead
   */
  @Deprecated
  public static String getPlainText(Part part) {
    return BEANS.get(MailHelper.class).getPlainText(part);
  }

  /**
   * @deprecated use {@link MailHelper#getHtmlPart(List)} instead
   */
  @Deprecated
  public static Part getHtmlPart(List<? extends Part> bodyParts) {
    return BEANS.get(MailHelper.class).getHtmlPart(bodyParts);
  }

  /**
   * @deprecated use {@link MailHelper#getPlainTextPart(List)} instead
   */
  @Deprecated
  public static Part getPlainTextPart(List<Part> bodyParts) {
    return BEANS.get(MailHelper.class).getPlainTextPart(bodyParts);
  }

  /**
   * @deprecated use {@link MailHelper#createDataSource(File)} instead
   */
  @Deprecated
  public static DataSource createDataSource(File file) {
    return BEANS.get(MailHelper.class).createDataSource(file);
  }

  /**
   * @deprecated use {@link MailHelper#createDataSource(InputStream, String, String)} instead
   */
  @Deprecated
  public static DataSource createDataSource(InputStream inStream, String fileName, String fileExtension) {
    return BEANS.get(MailHelper.class).createDataSource(inStream, fileName, fileExtension);
  }

  /**
   * @deprecated use {@link MailHelper#createMimeMessage(MailMessage)} instead
   */
  @Deprecated
  public static MimeMessage createMimeMessage(MailMessage mailMessage) {
    return BEANS.get(MailHelper.class).createMimeMessage(mailMessage);
  }

  /**
   * @deprecated use {@link MailHelper#createMessageFromBytes(byte[])} instead
   */
  @Deprecated
  public static MimeMessage createMessageFromBytes(byte[] bytes) {
    return BEANS.get(MailHelper.class).createMessageFromBytes(bytes);
  }

  /**
   * @deprecated use {@link MailHelper#createMessageFromBytes(byte[], Session)} instead
   */
  @Deprecated
  public static MimeMessage createMessageFromBytes(byte[] bytes, Session session) {
    return BEANS.get(MailHelper.class).createMessageFromBytes(bytes, session);
  }

  /**
   * @deprecated use {@link MailHelper#addAttachmentsToMimeMessage(MimeMessage, List)} instead
   */
  @Deprecated
  public static void addAttachmentsToMimeMessage(MimeMessage msg, List<File> attachments) {
    BEANS.get(MailHelper.class).addAttachmentsToMimeMessage(msg, attachments);
  }

  /**
   * @deprecated use {@link MailHelper#addResourcesAsAttachments(MimeMessage, List)} instead
   */
  @Deprecated
  public static void addResourcesAsAttachments(MimeMessage msg, List<BinaryResource> attachments) {
    BEANS.get(MailHelper.class).addResourcesAsAttachments(msg, attachments);
  }

  /**
   * @deprecated use {@link MailHelper#getContentTypeForExtension(String)} instead
   */
  @Deprecated
  public static String getContentTypeForExtension(String ext) {
    return BEANS.get(MailHelper.class).getContentTypeForExtension(ext);
  }

  /**
   * @deprecated use {@link MailHelper#createInternetAddress(MailParticipant)} instead
   */
  @Deprecated
  public static InternetAddress createInternetAddress(MailParticipant participant) {
    return BEANS.get(MailHelper.class).createInternetAddress(participant);
  }

  /**
   * @deprecated use {@link MailHelper#createInternetAddress(String))} instead
   */
  @Deprecated
  public static InternetAddress createInternetAddress(String email) {
    return BEANS.get(MailHelper.class).createInternetAddress(email);
  }

  /**
   * @deprecated use {@link MailHelper#createInternetAddress(String, String)} instead
   */
  @Deprecated
  public static InternetAddress createInternetAddress(String email, String name) {
    return BEANS.get(MailHelper.class).createInternetAddress(email, name);
  }

  /**
   * @deprecated use {@link MailHelper#parseInternetAddressList(String)} instead
   */
  @Deprecated
  public static InternetAddress[] parseInternetAddressList(String addresslist) {
    return BEANS.get(MailHelper.class).parseInternetAddressList(addresslist);
  }

  /**
   * @deprecated use {@link MailHelper#getCharacterEncodingOfPart(Part)} instead
   */
  @Deprecated
  public static String getCharacterEncodingOfPart(Part part) throws MessagingException {
    return BEANS.get(MailHelper.class).getCharacterEncodingOfPart(part);
  }
}
