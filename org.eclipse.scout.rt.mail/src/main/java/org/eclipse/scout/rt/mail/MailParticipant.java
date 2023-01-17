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

import java.io.Serializable;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Mutable representation of an Internet email address including a display name.
 *
 * @since 5.2
 */
@Bean
public class MailParticipant implements Serializable {

  private static final long serialVersionUID = 1L;

  private String m_email;
  private String m_name;

  public String getEmail() {
    return m_email;
  }

  public MailParticipant withEmail(String email) {
    m_email = email;
    return this;
  }

  public String getName() {
    return m_name;
  }

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
