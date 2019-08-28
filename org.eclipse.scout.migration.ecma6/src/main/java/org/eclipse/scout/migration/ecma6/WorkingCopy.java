package org.eclipse.scout.migration.ecma6;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.scout.rt.platform.util.ObjectUtility;

public class WorkingCopy {

  private final Path m_path;
  private final String m_lineSeparator;
  private Path m_relativeTargetPath;
  private String m_initialSource;
  private String m_source;
  private boolean m_deleted;

  public WorkingCopy(Path path, String lineSeparator) {
    m_path = path;
    m_lineSeparator = lineSeparator;
  }

  public Path getPath() {
    return m_path;
  }

  public String getLineSeparator() {
    return m_lineSeparator;
  }

  public void setRelativeTargetPath(Path relativeTargetPath) {
    m_relativeTargetPath = relativeTargetPath;
  }

  public Path getRelativeTargetPath() {
    return m_relativeTargetPath;
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

  public boolean isDeleted() {
    return m_deleted;
  }

  public void setDeleted(boolean deleted) {
    m_deleted = deleted;
  }

  public void persist(Path destination) throws IOException {
    if (isDeleted()) {
      if (Files.exists(destination) && Files.isRegularFile(destination)) {
        // in case the source path of the migration is the same as the target: the file must really be deleted
        // otherwise it must just no be copied to the target
        Files.delete(destination);
      }
      return;
    }

    Files.createDirectories(destination.getParent());
    if (isDirty()) {
      Files.write(destination, getSource().getBytes());
    }
    else {
      try {
        Files.copy(getPath(), destination, REPLACE_EXISTING);
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(m_path)
        .append(" [dirty:").append(isDirty())
        .append(", deleted:").append(isDeleted())
        .append("]");
    return builder.toString();
  }
}
