package org.eclipse.scout.migration.ecma6.model.old;

public class JsCommentBlock extends AbstractJsElement {
  private String m_source;

  public void setSource(String source) {
    m_source = source;
  }

  public String getSource() {
    return m_source;
  }
}
