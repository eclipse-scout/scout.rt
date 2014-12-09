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
package org.eclipse.scout.commons.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Class representing a mail message that is used to create a mime message with
 * {@link MailUtility#createMimeMessage(MailMessage)}.
 */
public class MailMessage {

  private final List<String> m_toRecipients = new ArrayList<String>();
  private final List<String> m_ccRecipients = new ArrayList<String>();
  private final List<String> m_bccRecipients = new ArrayList<String>();
  private String m_sender;
  private String m_subject;
  private String m_bodyPlainText;
  private String m_bodyHtml;
  private final List<MailAttachment> m_attachments = new ArrayList<MailAttachment>();

  /**
   * Creates an empty mail message.
   */
  public MailMessage() {
    this(null, null);
  }

  /**
   * Creates a mail message with body (plain text & html).
   *
   * @param bodyPlainText
   *          Plain text
   * @param bodyHtml
   *          HTML
   */
  public MailMessage(String bodyPlainText, String bodyHtml) {
    this(bodyPlainText, bodyHtml, null, null, null);
  }

  /**
   * Creates a mail message with body (plain text & html), subject, sender and TO recipients.
   *
   * @param bodyPlainText
   *          Plain text
   * @param bodyHtml
   *          HTML
   * @param subject
   *          Subject
   * @param sender
   *          Sender
   * @param toRecipients
   *          List of TO recipients
   */
  public MailMessage(String bodyPlainText, String bodyHtml, String subject, String sender, List<String> toRecipients) {
    m_bodyPlainText = bodyPlainText;
    m_bodyHtml = bodyHtml;
    m_subject = subject;
    m_sender = sender;
    addToRecipients(toRecipients);
  }

  /**
   * @return an unmodifiable list of TO recipients.
   */
  public List<String> getToRecipients() {
    return Collections.unmodifiableList(m_toRecipients);
  }

  /**
   * Adds the recipient to the TO list.
   *
   * @param toRecipient
   *          TO recipient to add
   */
  public void addToRecipient(String toRecipient) {
    m_toRecipients.add(toRecipient);
  }

  /**
   * Adds the recipients to the TO list.
   *
   * @param toRecipients
   *          TO recipients to add
   */
  public void addToRecipients(Collection<String> toRecipients) {
    if (toRecipients != null) {
      m_toRecipients.addAll(toRecipients);
    }
  }

  /**
   * Clears the list of the TO recipients.
   */
  public void clearToRecipients() {
    m_toRecipients.clear();
  }

  /**
   * @return an unmodifiable list of CC recipients.
   */
  public List<String> getCcRecipients() {
    return Collections.unmodifiableList(m_ccRecipients);
  }

  /**
   * Adds the recipient to the CC list.
   *
   * @param ccRecipient
   *          CC recipient to add
   */
  public void addCcRecipient(String ccRecipient) {
    m_ccRecipients.add(ccRecipient);
  }

  /**
   * Adds the recipients to the CC list.
   *
   * @param ccRecipients
   *          CC recipients to add
   */
  public void addCcRecipients(Collection<String> ccRecipients) {
    if (ccRecipients != null) {
      m_ccRecipients.addAll(ccRecipients);
    }
  }

  /**
   * Clears the list of the CC recipients.
   */
  public void clearCcRecipients() {
    m_ccRecipients.clear();
  }

  /**
   * @return an unmodifiable list of BCC recipients.
   */
  public List<String> getBccRecipients() {
    return Collections.unmodifiableList(m_bccRecipients);
  }

  /**
   * Adds the recipient to the BCC list.
   *
   * @param bccRecipient
   *          BCC recipient to add
   */
  public void addBccRecipient(String bccRecipient) {
    m_bccRecipients.add(bccRecipient);
  }

  /**
   * Adds the recipients to the BCC list.
   *
   * @param bccRecipients
   *          BCC recipients to add
   */
  public void addBccRecipients(Collection<String> bccRecipients) {
    if (bccRecipients != null) {
      m_bccRecipients.addAll(bccRecipients);
    }
  }

  /**
   * Clears the list of the BCC recipients.
   */
  public void clearBccRecipients() {
    m_bccRecipients.clear();
  }

  public String getSender() {
    return m_sender;
  }

  public void setSender(String sender) {
    m_sender = sender;
  }

  public String getSubject() {
    return m_subject;
  }

  public void setSubject(String subject) {
    m_subject = subject;
  }

  public String getBodyPlainText() {
    return m_bodyPlainText;
  }

  public void setBodyPlainText(String bodyPlainText) {
    m_bodyPlainText = bodyPlainText;
  }

  public String getBodyHtml() {
    return m_bodyHtml;
  }

  public void setBodyHtml(String bodyHtml) {
    m_bodyHtml = bodyHtml;
  }

  /**
   * @return an unmodifiable list of attachments.
   */
  public List<MailAttachment> getAttachments() {
    return Collections.unmodifiableList(m_attachments);
  }

  /**
   * Adds the attachment.
   *
   * @param attachment
   */
  public void addAttachment(MailAttachment attachment) {
    m_attachments.add(attachment);
  }

  /**
   * Adds the attachments.
   *
   * @param attachments
   */
  public void addAttachments(Collection<MailAttachment> attachments) {
    m_attachments.addAll(attachments);
  }

  /**
   * Clears the attachment list.
   */
  public void clearAttachments() {
    m_attachments.clear();
  }
}
