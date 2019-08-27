package org.eclipse.scout.migration.ecma6.context;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.migration.ecma6.FileUtility;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.model.api.ApiParser;
import org.eclipse.scout.migration.ecma6.model.api.ApiWriter;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement;
import org.eclipse.scout.migration.ecma6.model.api.Libraries;
import org.eclipse.scout.migration.ecma6.model.api.LibraryApis;
import org.eclipse.scout.migration.ecma6.model.old.JsClass;
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
  private Path m_moduleDirectory;

  private final Map<Path, WorkingCopy> m_workingCopies = new HashMap<>();
  private final Map<WorkingCopy, JsFile> m_jsFiles = new HashMap<>();
  private final Map<String /*fqn*/, JsClass> m_jsClasses = new HashMap<>();
  private Path m_libraryStoreFile;
  private String m_libraryName;
  private Path m_libraryApiDirectory;
  private Libraries m_libraries;
  private INamedElement m_api;


  public Context(Path sourceRootDirectory, Path targetRootDirectory, String namespace) {
    m_sourceRootDirectory = sourceRootDirectory;
    m_targetRootDirectory = targetRootDirectory;
    m_namespace = namespace;
  }

  public void setup() {
    try {
      readLibraryApis();
    }
    catch (IOException e) {
      throw new ProcessingException("Could not parse Library APIs in '" + getLibraryApiDirectory() + "'.", e);
    }
    try {
      parseJsFiles();
      setupCurrentApi();
    }
    catch (IOException e) {
      throw new ProcessingException("Could not parse JS Files.", e);
    }
    // setup context properties
    BEANS.all(IContextProperty.class).forEach(p -> p.setup(this));
  }

  private void setupCurrentApi() {
    ApiWriter writer = new ApiWriter();
    m_api = writer.createLibraryFromCurrentModule(getNamespace(), this);
  }

  protected void readLibraryApis() throws IOException {
    if (getLibraryApiDirectory() != null && Files.isDirectory(getLibraryApiDirectory())) {
      ApiParser parser = new ApiParser(getLibraryApiDirectory());
      m_libraries = parser.parse();
    }else{
      m_libraries = new Libraries();
    }
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

  public WorkingCopy getWorkingCopy(Path file) {
    return m_workingCopies.get(file);
  }

  public WorkingCopy ensureWorkingCopy(Path file) {
    return m_workingCopies.computeIfAbsent(file, p -> new WorkingCopy(p, FileUtility.lineSeparator(p)));
  }

  public Collection<WorkingCopy> getWorkingCopies() {
    return m_workingCopies.values();
  }

  public <VALUE> VALUE getProperty(Class<? extends IContextProperty<VALUE>> propertyClass) {
    return BEANS.get(propertyClass).getValue();
  }

  public JsClass getJsClass(String fullyQuallifiedName) {
    return m_jsClasses.get(fullyQuallifiedName);
  }

  public Path getModuleDirectory() {
    return m_moduleDirectory;
  }

  public void setModuleDirectory(Path moduleDirectory) {
    m_moduleDirectory = moduleDirectory;
  }

  public void setLibraryName(String libraryName) {
    m_libraryName = libraryName;
  }

  public String getLibraryName() {
    return m_libraryName;
  }

  public Path relativeToModule(Path path) {
    Assertions.assertNotNull(getModuleDirectory());
    return path.relativize(getModuleDirectory());
  }

  public void setLibraryStoreFile(Path libraryStoreFile) {
    m_libraryStoreFile = libraryStoreFile;
  }

  public Path getLibraryStoreFile() {
    return m_libraryStoreFile;
  }

  public JsFile ensureJsFile(WorkingCopy workingCopy) {
    JsFile file = m_jsFiles.get(workingCopy);
    if (file == null) {
      try {
        file = new JsFileParser(workingCopy).parse();
        m_jsFiles.put(workingCopy, file);
      }
      catch (IOException e) {
        throw new VetoException("Could not parse working copy '" + workingCopy + "'.", e);
      }
    }
    return file;
  }

  public INamedElement getApi() {
    return m_api;
  }

  public void setLibraryApiDirectory(Path libraryApiDirectory) {
    m_libraryApiDirectory = libraryApiDirectory;
  }

  public Path getLibraryApiDirectory() {
    return m_libraryApiDirectory;
  }

  protected void parseJsFiles() throws IOException {

    final Path src = getSourceRootDirectory().resolve("src/main/js");
    Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.endsWith(Paths.get("src/main/js/jquery"))) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (FileUtility.hasExtension(file, "js")) {
          JsFile jsClasses = ensureJsFile(ensureWorkingCopy(file));
          jsClasses.getJsClasses().forEach(jsClazz -> m_jsClasses.put(jsClazz.getFullyQuallifiedName(), jsClazz));
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

  public Collection<JsClass> getAllJsClasses() {
    return Collections.unmodifiableCollection(m_jsClasses.values());
  }
}
