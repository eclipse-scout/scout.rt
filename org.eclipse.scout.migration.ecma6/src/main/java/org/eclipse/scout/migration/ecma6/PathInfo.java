package org.eclipse.scout.migration.ecma6;

import java.nio.file.Path;

public class PathInfo {

  private final Path m_path;
  private final Path m_modulePath;
  private final Path m_moduleRelativePath;

  public PathInfo(Path path, Path modulePath){
    m_path = path;
    m_modulePath = modulePath;
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
}
