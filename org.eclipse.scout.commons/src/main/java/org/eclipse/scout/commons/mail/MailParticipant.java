package org.eclipse.scout.commons.mail;
/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

import java.io.Serializable;

import org.eclipse.scout.commons.StringUtility;

/**
 * Mutable representation of an Internet email address including a display name.
 *
 * @since 5.2
 */
public class MailParticipant implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Email address
   */
  private String m_email;

  /**
   * Display name
   */
  private String m_name;

  /**
   * Default Constructor.
   */
  public MailParticipant() {
  }

  /**
   * Creates a participant with an email address and no additional display name.
   */
  public MailParticipant(String email) {
    this(email, null);
  }

  /**
   * Creates a participant with an email address and an additional display name.
   *
   * @param email
   *          Email address
   * @param name
   *          Display name
   */
  public MailParticipant(String email, String name) {
    this.m_email = email;
    this.m_name = name;
  }

  /**
   * Convenience method to set email and display name.
   */
  public MailParticipant with(String email, String name) {
    withEmail(email);
    withName(name);
    return this;
  }

  /**
   * @return Email address
   */
  public String getEmail() {
    return m_email;
  }

  /**
   * Set email
   *
   * @param email
   *          Email to set
   * @return Mail participant
   */
  public MailParticipant withEmail(String email) {
    m_email = email;
    return this;
  }

  /**
   * @return Display name
   */
  public String getName() {
    return m_name;
  }

  /**
   * Set display name
   *
   * @param name
   *          Display name to set
   * @return Mail participant
   */
  public MailParticipant withName(String name) {
    m_name = name;
    return this;
  }

  @Override
  public String toString() {
    if (StringUtility.isNullOrEmpty(m_name)) {
      return m_email;
    }
    return StringUtility.join(" ", m_name, StringUtility.box("<", m_email, ">"));
  }

}
