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

import org.eclipse.scout.rt.platform.Bean;

/**
 * Mutable representation of an Internet email address including a display name.
 *
 * @since 5.2
 * @deprecated Use {@link org.eclipse.scout.rt.mail.MailParticipant} instead.
 */
@Deprecated
@Bean
public class MailParticipant extends org.eclipse.scout.rt.mail.MailParticipant {

  private static final long serialVersionUID = 1L;

  @Override
  public MailParticipant withEmail(String email) {
    super.withEmail(email);
    return this;
  }

  @Override
  public MailParticipant withName(String name) {
    super.withName(name);
    return this;
  }
}
