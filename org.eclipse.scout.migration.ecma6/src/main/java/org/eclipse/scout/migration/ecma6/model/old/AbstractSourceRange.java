package org.eclipse.scout.migration.ecma6.model.old;

public abstract class AbstractSourceRange implements ISourceRange{
  private int m_startOffset;
  private int m_endOffset;

  public void setStartOffset(int startOffset) {
    m_startOffset = startOffset;
  }

  public int getStartOffset() {
    return m_startOffset;
  }

  public void setEndOffset(int endOffset) {
    m_endOffset = endOffset;
  }

  public int getEndOffset() {
    return m_endOffset;
  }
}
