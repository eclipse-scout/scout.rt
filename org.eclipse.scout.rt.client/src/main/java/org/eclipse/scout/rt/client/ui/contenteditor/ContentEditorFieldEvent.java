package org.eclipse.scout.rt.client.ui.contenteditor;

import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;

public class ContentEditorFieldEvent extends EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_CONTENT_ELEMENT_UPDATE = 10;

  private final int m_type;
  private ContentElement m_contentElement;

  public ContentEditorFieldEvent(IContentEditorField source, int type) {
    super(source);
    m_type = type;
  }

  @Override
  public int getType() {
    return m_type;
  }

  public ContentElement getContentElement() {
    return m_contentElement;
  }

  public void setContentElement(ContentElement contentElement) {
    m_contentElement = contentElement;
  }
}
