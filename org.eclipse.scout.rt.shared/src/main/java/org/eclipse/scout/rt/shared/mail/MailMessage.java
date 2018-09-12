/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.mail;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

/**
 * Class representing a mail message that is used to create a mime message with
 * {@link MailUtility#createMimeMessage(MailMessage)}.
 *
 * @deprecated Use {@link org.eclipse.scout.rt.mail.MailMessage} instead.
 */
@Deprecated
@Bean
@SuppressWarnings("deprecation")
public class MailMessage extends org.eclipse.scout.rt.mail.MailMessage {

  /**
   * @return an unmodifiable list of TO recipients.
   */
  @Override
  public List<MailParticipant> getToRecipients() {
    return mapParticipant(super.getToRecipients());
  }

  /**
   * Adds the recipient to the TO list.
   *
   * @param toRecipient
   *          TO recipient to add
   */
  public MailMessage addToRecipient(MailParticipant toRecipient) {
    super.addToRecipient(toRecipient);
    return this;
  }

  /**
   * Adds the recipients to the TO list.
   *
   * @param toRecipients
   *          TO recipients to add
   */
  @Override
  public MailMessage addToRecipients(Collection<? extends org.eclipse.scout.rt.mail.MailParticipant> toRecipients) {
    super.addToRecipients(toRecipients);
    return this;
  }

  /**
   * Clears the list of the TO recipients.
   */
  @Override
  public MailMessage clearToRecipients() {
    super.clearToRecipients();
    return this;
  }

  /**
   * @return an unmodifiable list of CC recipients.
   */
  @Override
  public List<MailParticipant> getCcRecipients() {
    return mapParticipant(super.getCcRecipients());
  }

  /**
   * Adds the recipient to the CC list.
   *
   * @param ccRecipient
   *          CC recipient to add
   */
  public MailMessage addCcRecipient(MailParticipant ccRecipient) {
    super.addCcRecipient(ccRecipient);
    return this;
  }

  /**
   * Adds the recipients to the CC list.
   *
   * @param ccRecipients
   *          CC recipients to add
   */
  @Override
  public MailMessage addCcRecipients(Collection<? extends org.eclipse.scout.rt.mail.MailParticipant> ccRecipients) {
    super.addCcRecipients(ccRecipients);
    return this;
  }

  /**
   * Clears the list of the CC recipients.
   */
  @Override
  public MailMessage clearCcRecipients() {
    super.clearCcRecipients();
    return this;
  }

  /**
   * @return an unmodifiable list of BCC recipients.
   */
  @Override
  public List<MailParticipant> getBccRecipients() {
    return mapParticipant(super.getBccRecipients());
  }

  /**
   * Adds the recipient to the BCC list.
   *
   * @param bccRecipient
   *          BCC recipient to add
   */
  public MailMessage addBccRecipient(MailParticipant bccRecipient) {
    super.addBccRecipient(bccRecipient);
    return this;
  }

  /**
   * Adds the recipients to the BCC list.
   *
   * @param bccRecipients
   *          BCC recipients to add
   */
  @Override
  public MailMessage addBccRecipients(Collection<? extends org.eclipse.scout.rt.mail.MailParticipant> bccRecipients) {
    super.addBccRecipients(bccRecipients);
    return this;
  }

  /**
   * Clears the list of the BCC recipients.
   */
  @Override
  public MailMessage clearBccRecipients() {
    super.clearBccRecipients();
    return this;
  }

  /**
   * @return Sender
   */
  @Override
  public MailParticipant getSender() {
    return map(super.getSender());
  }

  /**
   * Set sender.
   *
   * @param sender
   *          Sender
   */
  public MailMessage withSender(MailParticipant sender) {
    super.withSender(sender);
    return this;
  }

  /**
   * @return an unmodifiable list of reply TO's.
   */
  @Override
  public List<MailParticipant> getReplyTos() {
    return mapParticipant(super.getReplyTos());
  }

  /**
   * Adds the recipient to the reply TO list.
   *
   * @param replyTo
   *          TO recipient to add
   */
  public MailMessage addReplyTo(MailParticipant replyTo) {
    super.addReplyTo(replyTo);
    return this;
  }

  /**
   * Adds the recipients to the reply TO list.
   *
   * @param replyTos
   *          reply TO recipients to add
   */
  @Override
  public MailMessage addReplyTos(Collection<? extends org.eclipse.scout.rt.mail.MailParticipant> replyTos) {
    super.addReplyTos(replyTos);
    return this;
  }

  /**
   * Clears the list of the reply TO's.
   */
  @Override
  public MailMessage clearReplyTos() {
    super.clearReplyTos();
    return this;
  }

  @Override
  public MailMessage withSubject(String subject) {
    super.withSubject(subject);
    return this;
  }

  @Override
  public MailMessage withBodyPlainText(String bodyPlainText) {
    super.withBodyPlainText(bodyPlainText);
    return this;
  }

  @Override
  public MailMessage withBodyHtml(String bodyHtml) {
    super.withBodyHtml(bodyHtml);
    return this;
  }

  /**
   * @return an unmodifiable list of attachments.
   */
  @Override
  public List<MailAttachment> getAttachments() {
    return mapAttachments(super.getAttachments());
  }

  /**
   * Adds the attachment.
   *
   * @param attachment
   */
  public MailMessage withAttachment(MailAttachment attachment) {
    super.withAttachment(attachment);
    return this;
  }

  /**
   * Adds the attachments.
   *
   * @param attachments
   */
  @Override
  public MailMessage withAttachments(Collection<? extends org.eclipse.scout.rt.mail.MailAttachment> attachments) {
    super.withAttachments(attachments);
    return this;
  }

  /**
   * Clears the attachment list.
   */
  @Override
  public MailMessage clearAttachments() {
    super.clearAttachments();
    return this;
  }

  protected List<MailParticipant> mapParticipant(List<? extends org.eclipse.scout.rt.mail.MailParticipant> mailParticipants) {
    if (mailParticipants == null) {
      return null;
    }

    return mailParticipants.stream().map(mailParticipant -> map(mailParticipant)).collect(Collectors.toList());
  }

  protected MailParticipant map(org.eclipse.scout.rt.mail.MailParticipant mailParticipant) {
    if (mailParticipant == null) {
      return null;
    }

    return BEANS.get(MailParticipant.class)
        .withName(mailParticipant.getName())
        .withEmail(mailParticipant.getEmail());
  }

  protected List<MailAttachment> mapAttachments(List<? extends org.eclipse.scout.rt.mail.MailAttachment> attachments) {
    if (attachments == null) {
      return null;
    }

    return attachments.stream().map(attachment -> mapAttachment(attachment)).collect(Collectors.toList());
  }

  protected MailAttachment mapAttachment(org.eclipse.scout.rt.mail.MailAttachment attachment) {
    if (attachment == null) {
      return null;
    }

    return new MailAttachment(attachment);
  }
}
