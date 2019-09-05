package org.eclipse.scout.migration.ecma6.model.old;

public abstract class AbstractSourceElement implements ISourceElement {
  private String m_source;


  public String getSource() {
    return m_source;
  }

  public void setSource(String source) {
    m_source = source;
  }
}
