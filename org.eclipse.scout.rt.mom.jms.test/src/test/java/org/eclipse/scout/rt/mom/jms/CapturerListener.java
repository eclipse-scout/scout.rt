/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.jms;

import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IMessageListener;

public class CapturerListener<DTO> implements IMessageListener<DTO> {

  private final Capturer<DTO> m_capturer;

  public CapturerListener(Capturer<DTO> capturer) {
    m_capturer = capturer;
  }

  @Override
  public void onMessage(IMessage<DTO> message) {
    m_capturer.set(message.getTransferObject());
  }
}
