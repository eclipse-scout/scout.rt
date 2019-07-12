package org.eclipse.scout.migration.ecma6.context;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.migration.ecma6.FileUtility;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.old.JsFileParser;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;

public class Context {

  private final Path m_sourceRootDirectory;
  private final Path m_targetRootDirectory;
  private final String m_namespace;

  private final Map<Path, WorkingCopy> m_workingCopies = new HashMap<>();
  private final Map<WorkingCopy, JsFile> m_jsFiles = new HashMap<>();
  private Map<String /*fgn*/, Path /*file*/> m_fqnToFiles = new HashMap<>();

  public Context(Path sourceRootDirectory, Path targetRootDirectory, String namespace){
    m_sourceRootDirectory = sourceRootDirectory;
    m_targetRootDirectory = targetRootDirectory;
    m_namespace = namespace;

  }

  public void setup(){
    try {
      parseJsFiles();
    }
    catch (IOException e) {
      throw new ProcessingException("Could not parse JS Files.",e);
    }
    // setup context properties
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

  public Path getFile(String fullyQuallifiedName){
    return m_fqnToFiles.get(fullyQuallifiedName);
  }

  public JsFile ensureJsFile(WorkingCopy workingCopy){
    JsFile file = m_jsFiles.get(workingCopy);
    if(file == null){
      try{
        file = new JsFileParser(workingCopy).parse();
        m_jsFiles.put(workingCopy, file);
      }catch (IOException e){
        throw new VetoException("Could not parse working copy '"+workingCopy+"'.",e);
      }
    }
    return file;
  }



  protected void parseJsFiles() throws IOException {
    final Path src = getSourceRootDirectory().resolve("src/main/js");
    Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if(FileUtility.hasExtension(file, "js")){
          JsFile jsClasses = ensureJsFile(ensureWorkingCopy(file));
          jsClasses.getJsClasses().forEach(jsClazz -> m_fqnToFiles.put(jsClazz.getFullyQuallifiedName(), file));
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

}
