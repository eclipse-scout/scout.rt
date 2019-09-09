package org.eclipse.scout.migration.ecma6.model.old;

public abstract class AbstractSourceElement implements ISourceElement {
  private boolean m_memoryOnly;
  private String m_source;

  @Override
  public String getSource() {
    return m_source;
  }

  public void setSource(String source) {
    m_source = source;
  }

  @Override
  public boolean isMemoryOnly() {
    return m_memoryOnly;
  }

  public void setMemoryOnly(boolean memoryOnly) {
    m_memoryOnly = memoryOnly;
  }
}
