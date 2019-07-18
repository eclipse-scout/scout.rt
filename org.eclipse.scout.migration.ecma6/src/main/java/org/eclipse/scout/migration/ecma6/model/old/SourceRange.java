package org.eclipse.scout.migration.ecma6.model.old;

public class SourceRange extends AbstractSourceRange{

  public SourceRange withStartOffset(int startOffset) {
    super.setStartOffset(startOffset);
    return this;
  }

  public SourceRange withEndOffset(int endOffset) {
    super.setEndOffset(endOffset);
    return this;
  }
}
