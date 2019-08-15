package org.eclipse.scout.migration.ecma6.context;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.migration.ecma6.FileUtility;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.model.old.JsClass;
import org.eclipse.scout.migration.ecma6.model.old.JsConstant;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.old.JsFileParser;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.Assertions;

public class Context {

  private final Path m_sourceRootDirectory;
  private final Path m_targetRootDirectory;
  private final String m_namespace;
  private Path m_currentModuleDirectory;

  private final Map<Path, WorkingCopy> m_workingCopies = new HashMap<>();
  private final Map<WorkingCopy, JsFile> m_jsFiles = new HashMap<>();
  private final Map<String /*fqn*/, JsClass> m_jsClasses = new HashMap<>();

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
    return m_workingCopies.computeIfAbsent(file, p -> new WorkingCopy(p, FileUtility.lineSeparator(p)));
  }

  public Collection<WorkingCopy> getWorkingCopies(){
    return m_workingCopies.values();
  }

  public <VALUE> VALUE getProperty(Class<? extends IContextProperty<VALUE>> propertyClass){
    return BEANS.get(propertyClass).getValue();
  }

  public JsClass getJsClass(String fullyQuallifiedName){
    return m_jsClasses.get(fullyQuallifiedName);
  }

  public Path getCurrentModuleDirectory() {
    return m_currentModuleDirectory;
  }

  public void setCurrentModuleDirectory(Path currentModuleDirectory) {
    m_currentModuleDirectory = currentModuleDirectory;
  }

  public Path relativeToModule(Path path){
    Assertions.assertNotNull(getCurrentModuleDirectory());
    return path.relativize(getCurrentModuleDirectory());
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
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if(dir.endsWith(Paths.get("src/main/js/jquery"))){
          return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if(FileUtility.hasExtension(file, "js")){
          JsFile jsClasses = ensureJsFile(ensureWorkingCopy(file));
          jsClasses.getJsClasses().forEach(jsClazz -> m_jsClasses.put(jsClazz.getFullyQuallifiedName(), jsClazz));
        }
        return FileVisitResult.CONTINUE;
      }
    });
    m_jsClasses.values().stream().map(f -> f.getFullyQuallifiedName()).forEach(s -> System.out.println(s));
  }

}
