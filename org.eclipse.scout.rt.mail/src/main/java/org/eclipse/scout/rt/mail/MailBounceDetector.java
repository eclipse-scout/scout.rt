/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.config.AbstractStringListConfigProperty;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of email bounce detector using DSN check and additional some heuristics on sender, subject and
 * content.
 */
@Order(5100)
public class MailBounceDetector implements IMailBounceDetector {

  private static final Logger LOG = LoggerFactory.getLogger(MailBounceDetector.class);

  protected static final String CONTENT_TYPE_MULTIPART_REPORT = "multipart/report";
  protected static final String CONTENT_TYPE_DELIVERY_STATUS = "message/delivery-status";

  protected static final Pattern DSN_ACTION_FAILED_PATTERN = Pattern.compile(".*^\\s*Action\\s*:\\s*failed.*", Pattern.MULTILINE | Pattern.DOTALL);

  /**
   * Tries to detect if email is a bounce.
   */
  @Override
  public boolean test(MimeMessage mimeMessage) {
    Assertions.assertNotNull(mimeMessage, "Mime message must be set");

    if (checkIsDsnBounce(mimeMessage)) {
      return true;
    }
    if (checkIsNonStandardBounce(mimeMessage)) {
      return true;
    }

    return false;
  }

  /**
   * Checks if the message is a Delivery Status Notification (with status = failed) according to RFC 3464 (and 6522).
   * Known senders of such messages are (depending on their configuration):
   * <ul>
   * <li>Postfix 2.3 or newer (http://www.postfix.org/DSN_README.html)
   * <li>Microsoft Exchange 2000/2003 or newer (http://support.microsoft.com/kb/262986/en-us)
   * </ul>
   *
   * @return true, if the message is a status notification
   * @see <a href="http://tools.ietf.org/html/rfc3464">RFC 3464</a>
   * @see <a href="http://tools.ietf.org/html/rfc6522">RFC 6522</a>
   */
  protected boolean checkIsDsnBounce(MimeMessage mimeMessage) {
    try {
      String contentType = mimeMessage.getContentType();
      if (!StringUtility.containsStringIgnoreCase(contentType, CONTENT_TYPE_MULTIPART_REPORT)) {
        return false;
      }

      Object content = mimeMessage.getContent();
      if (content instanceof MimeMultipart) {
        MimeMultipart mp = (MimeMultipart) content;
        for (int i = 0; i < mp.getCount(); i++) {
          BodyPart part = mp.getBodyPart(i);
          if (StringUtility.equalsIgnoreCase(CONTENT_TYPE_DELIVERY_STATUS, part.getContentType())) {
            String s;
            try (InputStream in = part.getInputStream()) {
              s = IOUtility.readString(in, null);
            }
            if (s != null && DSN_ACTION_FAILED_PATTERN.matcher(s).matches()) {
              return true;
            }
          }
        }
      }
    }
    catch (IOException | MessagingException | RuntimeException e) {
      LOG.warn("Could not read mime multipart message", e);
    }

    return false;
  }

  /**
   * Heuristic that tries to detect non-standardized bounce messages.
   */
  protected boolean checkIsNonStandardBounce(MimeMessage mimeMessage) {
    // Some of these properties also appear in normal or other kind of messages. We will check them all and decide later.
    boolean senderMatches;
    boolean hasNoReturnPathHeader;
    boolean isAutoReply;
    boolean subjectMatches;
    boolean contentMatches;

    try {
      senderMatches = matchesHeuristicSenderAddress(mimeMessage);
      subjectMatches = matchesHeuristicSubject(mimeMessage);
      contentMatches = matchesHeuristicContent(mimeMessage);
      isAutoReply = isAutoSubmitted(mimeMessage);
      hasNoReturnPathHeader = hasNoReturnPathHeader(mimeMessage);
    }
    catch (MessagingException | IOException e) {
      LOG.warn("Could not determine heuristically if message is bounce because content / header inaccessible for message id {}", BEANS.get(MailHelper.class).getMessageIdSafely(mimeMessage), e);
      return false;
    }

    return isNonStandardBounce(senderMatches, hasNoReturnPathHeader, isAutoReply, subjectMatches, contentMatches);
  }

  /**
   * Decision function for bounce recognition heuristic.
   *
   * @param senderMatches
   *          Whether the sender address indicates a bounce
   * @param hasNoReturnPathHeader
   *          Whether bounce is indicated by missing return path
   * @param isAutoReply
   *          Whether the message is an auto-reply
   * @param subjectMatches
   *          Whether the subject indicates a bounce
   * @param contentMatches
   *          Whether the content indicates a bounce
   * @return <tt>true</tt> if the message should be considered as bounce
   */
  protected boolean isNonStandardBounce(boolean senderMatches, boolean hasNoReturnPathHeader, boolean isAutoReply, boolean subjectMatches, boolean contentMatches) {
    int result = (senderMatches ? 1 : 0) + (hasNoReturnPathHeader ? 1 : 0) + (isAutoReply ? 1 : 0) + (subjectMatches ? 1 : 0) + (contentMatches ? 1 : 0);

    // At least 3 out of 5 are required to classify it as bounce.
    // Out-of-office replies typically reach 2 (Auto Reply, No Return path).
    return result >= 3;
  }

  /**
   * Checks if the sender of this message is typical for a bounce message (heuristic).
   */
  protected boolean matchesHeuristicSenderAddress(MimeMessage mimeMessage) throws MessagingException {
    try {
      Address[] addresses = mimeMessage.getFrom();
      if (addresses == null) {
        return true;
      }

      List<String> senderPrefixes = BEANS.get(MailBounceDetectorHeuristicSenderPrefixesProperty.class).getValue();
      for (Address a : addresses) {
        String sender = a.toString().toLowerCase();

        if (a instanceof InternetAddress) {
          InternetAddress internetAddress = (InternetAddress) a;
          if (StringUtility.hasText(internetAddress.getAddress())) {
            sender = StringUtility.lowercase(internetAddress.getAddress());
          }
          // Theoretically we could also inspect the "personal" field, which would be the "display name" of the sender.
          // But since our pattern consists so far of email-address parts, we don't do this at this point.
        }

        if (sender == null) {
          continue;
        }

        for (String prefix : senderPrefixes) {
          if (sender.startsWith(prefix.toLowerCase())) {
            return true;
          }
        }
      }
      return false;
    }
    catch (AddressException e) {
      LOG.warn("Could not read sender of mime message", e);
      return false;
    }
  }

  /**
   * Checks if the subject of the message is typical for a bounce message (heuristic).
   */
  protected boolean matchesHeuristicSubject(MimeMessage mimeMessage) throws MessagingException {
    String subject = StringUtility.lowercase(mimeMessage.getSubject());
    if (subject == null) {
      return false;
    }

    for (String s : BEANS.get(MailBounceDetectorHeuristicSubjectsProperty.class).getValue()) {
      if (subject.contains(s.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the content of the message is typical for a bounce message (heuristic).
   */
  protected boolean matchesHeuristicContent(MimeMessage mimeMessage) throws IOException, MessagingException {
    Object content = mimeMessage.getContent();
    if (content instanceof String) {
      String stringContent = StringUtility.lowercase((String) content);
      for (String s : BEANS.get(MailBounceDetectorHeuristicContentsProperty.class).getValue()) {
        if (stringContent.contains(s.toLowerCase())) {
          return true;
        }
      }
    }

    return false;
  }

  protected boolean isAutoSubmitted(MimeMessage mimeMessage) throws MessagingException {
    // Check for auto-submitted
    String[] values = mimeMessage.getHeader("Auto-Submitted"); // See RFC 3834
    if (values == null) {
      return false;
    }

    for (String value : values) {
      if ("no".equalsIgnoreCase(value)) {
        // Means likely generated/sent manually
        break;
      }
      else if ("auto-replied".equalsIgnoreCase(value)) {
        return true;
      }
    }

    return false;
  }

  protected boolean hasNoReturnPathHeader(MimeMessage mimeMessage) throws MessagingException {
    // Check for return path header
    String[] values = mimeMessage.getHeader("Return-Path");
    // See RFC 3834, Section 7
    if (values == null) {
      return true;
    }

    for (String value : values) {
      if ("<>".equalsIgnoreCase(value)) {
        return true;
      }
    }

    return false;
  }

  public static class MailBounceDetectorHeuristicSenderPrefixesProperty extends AbstractStringListConfigProperty {

    @Override
    public String getKey() {
      return "scout.mail.bouncedetector.heuristic.senderPrefixes";
    }

    @Override
    public String description() {
      return "Non-standard email bounce detection: sender is checked against the provided list of heuristic sender prefixes (prefix match, case-insensitive)";
    }

    @Override
    public List<String> getDefaultValue() {
      return Arrays.asList(
          "postmaster",
          "mailer-daemon");
    }
  }

  public static class MailBounceDetectorHeuristicSubjectsProperty extends AbstractStringListConfigProperty {

    @Override
    public String getKey() {
      return "scout.mail.bouncedetector.heuristic.subjects";
    }

    @Override
    public String description() {
      return "Non-standard email bounce detection: subject is checked against the provided list of heuristic subjects (partial match, case-insensitive)";
    }

    @Override
    public List<String> getDefaultValue() {
      return Arrays.asList(
          "delivery notification",
          "mail delivery",
          "undeliverable",
          "delivery failure",
          "unzustellbar");
    }
  }

  public static class MailBounceDetectorHeuristicContentsProperty extends AbstractStringListConfigProperty {

    @Override
    public String getKey() {
      return "scout.mail.bouncedetector.heuristic.contents";
    }

    @Override
    public String description() {
      return "Non-standard email bounce detection: content is checked against the provided list of heuristic contents (partial match, case-insensitive)";
    }

    @Override
    public List<String> getDefaultValue() {
      return Arrays.asList(
          "could not be delivered",
          "delivery status notification",
          "MTA Response",
          "undeliverable");
    }
  }
}
