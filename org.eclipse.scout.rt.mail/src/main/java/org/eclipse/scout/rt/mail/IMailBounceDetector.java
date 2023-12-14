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

import jakarta.mail.internet.MimeMessage;

import org.eclipse.scout.rt.platform.ApplicationScoped;

@ApplicationScoped
public interface IMailBounceDetector {

  /**
   * @return <code>true</code> if email seems to be a bounce, <code>false</code> otherwise.
   */
  boolean test(MimeMessage mimeMessage);
}
