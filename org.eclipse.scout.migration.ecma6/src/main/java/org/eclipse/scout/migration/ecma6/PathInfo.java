package org.eclipse.scout.migration.ecma6;

import java.nio.file.Path;

import org.eclipse.scout.migration.ecma6.configuration.Configuration;

public class PathInfo {

  private final Path m_path;
  private final Path m_modulePath;
  private final Path m_moduleRelativePath;

  public PathInfo(Path path) {
    m_path = path;
    m_modulePath = Configuration.get().getSourceModuleDirectory();
    if(m_modulePath != null) {
      m_moduleRelativePath = m_modulePath.relativize(m_path);
    }else{
      m_moduleRelativePath = null;
    }
  }

  public Path getPath() {
    return m_path;
  }

  public Path getModulePath() {
    return m_modulePath;
  }

  public Path getModuleRelativePath() {
    return m_moduleRelativePath;
  }

  @Override
  public String toString() {
    //noinspection StringBufferReplaceableByString
    StringBuilder builder = new StringBuilder();
    builder.append(getPath()).append(" - ").append(getModuleRelativePath());
    return builder.toString();
  }
}
