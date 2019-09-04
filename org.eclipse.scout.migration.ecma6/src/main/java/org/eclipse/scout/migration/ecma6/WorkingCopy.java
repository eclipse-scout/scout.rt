package org.eclipse.scout.migration.ecma6;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

public class WorkingCopy {

  private final Path m_path;
  private String m_lineSeparator;
  private Path m_relativeTargetPath;
  private String m_initialSource;
  private String m_source;
  private boolean m_deleted;


  public WorkingCopy(Path path) {
    m_path = path;
  }

  public Path getPath() {
    return m_path;
  }

  public String getLineSeparator() {
    if(m_lineSeparator == null){
      if(getPath() != null && Files.exists(getPath())){
        m_lineSeparator =     FileUtility.lineSeparator(getPath());
      }else {
        m_lineSeparator = "\n";
      }
    }
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

  public void storeSource()  {
    if(isDirty()){
      try {
        Files.write(getPath(), getSource().getBytes());
      }
      catch (IOException e) {
        throw new ProcessingException("could not write working copy '"+toString()+"'.",e);
      }
    }
    // undirty
    m_source = null;
    m_initialSource = null;
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
        throw new ProcessingException("Unable to copy file '{}'.", getPath(), e);
      }
    }
  }

  @Override
  public String toString() {
    //noinspection StringBufferReplaceableByString
    StringBuilder builder = new StringBuilder();
    builder.append(m_path)
        .append(" [dirty:").append(isDirty())
        .append(", deleted:").append(isDeleted())
        .append("]");
    return builder.toString();
  }
}
