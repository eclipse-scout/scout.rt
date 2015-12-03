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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Class representing a mail message that is used to create a mime message with
 * {@link MailUtility#createMimeMessage(MailMessage)}.
 */
public class MailMessage {

  private final List<MailParticipant> m_toRecipients = new ArrayList<MailParticipant>();
  private final List<MailParticipant> m_ccRecipients = new ArrayList<MailParticipant>();
  private final List<MailParticipant> m_bccRecipients = new ArrayList<MailParticipant>();
  private MailParticipant m_sender;
  private final List<MailParticipant> m_replyTos = new ArrayList<MailParticipant>();
  private String m_subject;
  private String m_bodyPlainText;
  private String m_bodyHtml;
  private final List<MailAttachment> m_attachments = new ArrayList<MailAttachment>();

  /**
   * Creates an empty mail message.
   */
  public MailMessage() {
  }

  /**
   * @return an unmodifiable list of TO recipients.
   */
  public List<MailParticipant> getToRecipients() {
    return Collections.unmodifiableList(m_toRecipients);
  }

  /**
   * Adds the recipient to the TO list.
   *
   * @param toRecipient
   *          TO recipient to add
   */
  public MailMessage addToRecipient(MailParticipant toRecipient) {
    m_toRecipients.add(toRecipient);
    return this;
  }

  /**
   * Adds the recipients to the TO list.
   *
   * @param toRecipients
   *          TO recipients to add
   */
  public MailMessage addToRecipients(Collection<MailParticipant> toRecipients) {
    if (toRecipients != null) {
      m_toRecipients.addAll(toRecipients);
    }
    return this;
  }

  /**
   * Clears the list of the TO recipients.
   */
  public MailMessage clearToRecipients() {
    m_toRecipients.clear();
    return this;
  }

  /**
   * @return an unmodifiable list of CC recipients.
   */
  public List<MailParticipant> getCcRecipients() {
    return Collections.unmodifiableList(m_ccRecipients);
  }

  /**
   * Adds the recipient to the CC list.
   *
   * @param ccRecipient
   *          CC recipient to add
   */
  public MailMessage addCcRecipient(MailParticipant ccRecipient) {
    m_ccRecipients.add(ccRecipient);
    return this;
  }

  /**
   * Adds the recipients to the CC list.
   *
   * @param ccRecipients
   *          CC recipients to add
   */
  public MailMessage addCcRecipients(Collection<MailParticipant> ccRecipients) {
    if (ccRecipients != null) {
      m_ccRecipients.addAll(ccRecipients);
    }
    return this;
  }

  /**
   * Clears the list of the CC recipients.
   */
  public MailMessage clearCcRecipients() {
    m_ccRecipients.clear();
    return this;
  }

  /**
   * @return an unmodifiable list of BCC recipients.
   */
  public List<MailParticipant> getBccRecipients() {
    return Collections.unmodifiableList(m_bccRecipients);
  }

  /**
   * Adds the recipient to the BCC list.
   *
   * @param bccRecipient
   *          BCC recipient to add
   */
  public MailMessage addBccRecipient(MailParticipant bccRecipient) {
    m_bccRecipients.add(bccRecipient);
    return this;
  }

  /**
   * Adds the recipients to the BCC list.
   *
   * @param bccRecipients
   *          BCC recipients to add
   */
  public MailMessage addBccRecipients(Collection<MailParticipant> bccRecipients) {
    if (bccRecipients != null) {
      m_bccRecipients.addAll(bccRecipients);
    }
    return this;
  }

  /**
   * Clears the list of the BCC recipients.
   */
  public MailMessage clearBccRecipients() {
    m_bccRecipients.clear();
    return this;
  }

  /**
   * @return Sender
   */
  public MailParticipant getSender() {
    return m_sender;
  }

  /**
   * Set sender.
   *
   * @param sender
   *          Sender
   */
  public MailMessage withSender(MailParticipant sender) {
    m_sender = sender;
    return this;
  }

  /**
   * @return an unmodifiable list of reply TO's.
   */
  public List<MailParticipant> getReplyTos() {
    return Collections.unmodifiableList(m_replyTos);
  }

  /**
   * Adds the recipient to the reply TO list.
   *
   * @param replyTo
   *          TO recipient to add
   */
  public MailMessage addReplyTo(MailParticipant replyTo) {
    m_replyTos.add(replyTo);
    return this;
  }

  /**
   * Adds the recipients to the reply TO list.
   *
   * @param replyTos
   *          reply TO recipients to add
   */
  public MailMessage addReplyTos(Collection<MailParticipant> replyTos) {
    if (replyTos != null) {
      m_replyTos.addAll(replyTos);
    }
    return this;
  }

  /**
   * Clears the list of the reply TO's.
   */
  public MailMessage clearReplyTos() {
    m_replyTos.clear();
    return this;
  }

  public String getSubject() {
    return m_subject;
  }

  public MailMessage withSubject(String subject) {
    m_subject = subject;
    return this;
  }

  public String getBodyPlainText() {
    return m_bodyPlainText;
  }

  public MailMessage withBodyPlainText(String bodyPlainText) {
    m_bodyPlainText = bodyPlainText;
    return this;
  }

  public String getBodyHtml() {
    return m_bodyHtml;
  }

  public MailMessage withBodyHtml(String bodyHtml) {
    m_bodyHtml = bodyHtml;
    return this;
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
  public MailMessage withAttachment(MailAttachment attachment) {
    m_attachments.add(attachment);
    return this;
  }

  /**
   * Adds the attachments.
   *
   * @param attachments
   */
  public MailMessage withAttachments(Collection<MailAttachment> attachments) {
    m_attachments.addAll(attachments);
    return this;
  }

  /**
   * Clears the attachment list.
   */
  public MailMessage clearAttachments() {
    m_attachments.clear();
    return this;
  }
}
