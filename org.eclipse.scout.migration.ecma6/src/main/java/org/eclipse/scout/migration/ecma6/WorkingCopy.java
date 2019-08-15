package org.eclipse.scout.migration.ecma6;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.scout.rt.platform.util.ObjectUtility;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class WorkingCopy {

  private final Path m_path;
  private final String m_lineSeparator;
  private Path m_relativeTargetPath;
  private String m_initialSource;
  private String m_source;


  public WorkingCopy(Path path, String lineSeparator){
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

  public boolean isDirty(){
    return m_initialSource != null;
  }

  public String getInitialSource() {
    if(m_initialSource == null){
      return getSource();
    }
    return m_initialSource;
  }

  public String getSource() {
    if(m_source == null){
      readSource();
    }
    return m_source;
  }

  public void setSource(String source){
    if(!isDirty()&& ObjectUtility.equals(source, m_source) ){
      return;
    }
    if(m_initialSource == null){
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

  public void persist(Path destination) throws IOException {
    Files.createDirectories(destination.getParent());
    if(isDirty()){
      Files.write(destination, getSource().getBytes());
    }else{
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
      .append(" [dirty:").append(isDirty()).append("]");
    return builder.toString();
  }
}
