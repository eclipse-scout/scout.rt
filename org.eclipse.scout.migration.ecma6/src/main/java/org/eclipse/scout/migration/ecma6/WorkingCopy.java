package org.eclipse.scout.migration.ecma6;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

public class WorkingCopy {

  private final Path m_path;
  private String m_lineSeparator;
  private String m_initialSource;
  private String m_source;

  public WorkingCopy(Path path) {
    m_path = path;
  }

  public Path getPath() {
    return m_path;
  }

  public String getLineDelimiter() {
    if (m_lineSeparator == null) {
      if (getPath() != null && Files.exists(getPath())) {
        m_lineSeparator = FileUtility.lineSeparator(getPath());
      }
      else {
        m_lineSeparator = "\n";
      }
    }
    return m_lineSeparator;
  }

  public boolean isDirty() {
    return m_initialSource != null;
  }

  public String getInitialSource() {
    if (m_initialSource == null) {
      return getSource();
    }
    return m_initialSource;
  }

  public String getSource() {
    if (m_source == null) {
      readSource();
    }
    return m_source;
  }

  public void setSource(String source) {
    if (!isDirty() && ObjectUtility.equals(source, m_source)) {
      return;
    }
    if (m_initialSource == null) {
      m_initialSource = m_source;
    }
    m_source = source;
  }

  private void readSource() {
    try {
      m_source = new String(Files.readAllBytes(getPath()));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void storeSource() {
    if (isDirty()) {
      try {
        Files.write(getPath(), getSource().getBytes());
      }
      catch (IOException e) {
        throw new ProcessingException("could not write working copy '" + toString() + "'.", e);
      }
    }
    // undirty
    m_source = null;
    m_initialSource = null;
  }

  @Override
  public String toString() {
    //noinspection StringBufferReplaceableByString
    StringBuilder builder = new StringBuilder();
    builder.append(m_path)
        .append(" [dirty:").append(isDirty())
        .append("]");
    return builder.toString();
  }
}
