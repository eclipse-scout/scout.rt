package org.eclipse.scout.migration.ecma6.context;

import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.IContextProperty;
import org.eclipse.scout.rt.platform.BEANS;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Context {

  private final Path m_sourceRootDirectory;
  private final Path m_targetRootDirectory;
  private final String m_namespace;


  private final Map<Path, WorkingCopy> m_workingCopies = new HashMap<>();


  public Context(Path sourceRootDirectory, Path targetRootDirectory, String namespace){
    m_sourceRootDirectory = sourceRootDirectory;
    m_targetRootDirectory = targetRootDirectory;
    m_namespace = namespace;
  }

  protected void setup(){
    BEANS.all(IContextProperty.class).forEach(p -> p.setup(this));
  }

  public Path getSourceRootDirectory() {
    return m_sourceRootDirectory;
  }

  public Path getTargetRootDirectory() {
    return m_targetRootDirectory;
  }

  public String getNamespace() {
    return m_namespace;
  }

  public WorkingCopy getWorkingCopy(Path file){
    return m_workingCopies.get(file);
  }

  public WorkingCopy ensureWorkingCopy(Path file) {
    return m_workingCopies.computeIfAbsent(file, p -> new WorkingCopy(p));
  }

  public Collection<WorkingCopy> getWorkingCopies(){
    return m_workingCopies.values();
  }

  public <VALUE> VALUE getProperty(Class<? extends IContextProperty<VALUE>> propertyClass){
    return BEANS.get(propertyClass).getValue();
  }
}
