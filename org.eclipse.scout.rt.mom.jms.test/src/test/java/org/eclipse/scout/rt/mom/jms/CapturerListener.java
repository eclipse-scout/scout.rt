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
