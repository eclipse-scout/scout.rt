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
