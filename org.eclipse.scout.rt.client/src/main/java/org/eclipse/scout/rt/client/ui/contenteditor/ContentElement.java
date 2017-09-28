package org.eclipse.scout.rt.client.ui.contenteditor;

public class ContentElement {
  private String m_content;
  private String m_slot;
  private String m_elementId;

  public ContentElement(String content, String slot, String elementId) {
    m_content = content;
    m_slot = slot;
    m_elementId = elementId;
  }

  public String getContent() {
    return m_content;
  }

  public void setContent(String content) {
    m_content = content;
  }

  public String getSlot() {
    return m_slot;
  }

  public void setSlot(String slot) {
    m_slot = slot;
  }

  public String getElementId() {
    return m_elementId;
  }

  public void setElementId(String elementId) {
    m_elementId = elementId;
  }
}
