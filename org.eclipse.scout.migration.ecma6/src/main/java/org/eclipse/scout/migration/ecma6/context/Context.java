package org.eclipse.scout.migration.ecma6.context;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.migration.ecma6.Configuration;
import org.eclipse.scout.migration.ecma6.FileUtility;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.model.api.ApiParser;
import org.eclipse.scout.migration.ecma6.model.api.ApiWriter;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement;
import org.eclipse.scout.migration.ecma6.model.api.Libraries;
import org.eclipse.scout.migration.ecma6.model.old.FrameworkExtensionMarker;
import org.eclipse.scout.migration.ecma6.model.old.JsClass;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.old.JsFileParser;
import org.eclipse.scout.migration.ecma6.model.old.JsTopLevelEnum;
import org.eclipse.scout.migration.ecma6.model.old.JsUtility;
import org.eclipse.scout.migration.ecma6.model.old.JsUtilityFunction;
import org.eclipse.scout.migration.ecma6.model.old.JsUtilityVariable;
import org.eclipse.scout.migration.ecma6.pathfilter.IMigrationExcludePathFilter;
import org.eclipse.scout.migration.ecma6.task.T40010_LessModule;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Context {
  private static final Logger LOG = LoggerFactory.getLogger(Context.class);

  private final Map<Path, WorkingCopy> m_workingCopies = new HashMap<>();
  private final Map<WorkingCopy, JsFile> m_jsFiles = new HashMap<>();
  private final Map<String /*fqn*/, JsClass> m_jsClasses = new HashMap<>();
  private final List<Path> m_lessFiles = new ArrayList<>();
  @FrameworkExtensionMarker
  private final Map<String /*fqn*/, JsUtility> m_jsUtilities = new HashMap<>();
  private final Map<String /*fqn*/, JsTopLevelEnum> m_jsTopLevelEnums = new HashMap<>();
  private Libraries m_libraries;
  private INamedElement m_api;

  public void setup() {
    try {
      readLibraryApis();
    }
    catch (IOException e) {
      throw new ProcessingException("Could not parse Library APIs in '" + Configuration.get().getLibraryApiDirectory() + "'.", e);
    }
    try {
      parseJsFiles();
      parseLessFiles();
      rebuildLocalApi();
    }
    catch (IOException e) {
      throw new ProcessingException("Could not parse Files.", e);
    }
    // setup context properties
    BEANS.all(IContextProperty.class).forEach(p -> p.setup(this));
  }

  protected void parseLessFiles() throws IOException {
    Files.walkFileTree(Configuration.get().getSourceModuleDirectory(), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (file.getFileName().toString().endsWith(T40010_LessModule.LESS_FILE_SUFFIX)) {
          m_lessFiles.add(file);
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        String dirName = dir.getFileName().toString();
        if ("target".equalsIgnoreCase(dirName)) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        if (".git".equals(dirName)) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        if ("node_modules".equals(dirName)) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

  public void rebuildLocalApi() {
    ApiWriter writer = new ApiWriter();
    m_api = writer.createLibraryFromCurrentModule(Configuration.get().getNamespace(), this);
  }

  protected void readLibraryApis() throws IOException {
    Path libraryApiDirectory = Configuration.get().getLibraryApiDirectory();
    if (libraryApiDirectory != null) {
      ApiParser parser = new ApiParser(libraryApiDirectory);
      m_libraries = parser.parse();
    }
    else {
      m_libraries = new Libraries();
    }
  }

  public WorkingCopy getWorkingCopy(Path file) {
    return m_workingCopies.get(file);
  }

  public WorkingCopy newFile(Path file) {
    WorkingCopy wc = createWorkingCopy(file);
    wc.setSource("");
    return wc;
  }

  public WorkingCopy ensureWorkingCopy(Path file) {
    return createWorkingCopy(file);
  }

  protected WorkingCopy createWorkingCopy(Path file) {
    return m_workingCopies.computeIfAbsent(file, WorkingCopy::new);
  }

  public Collection<WorkingCopy> getWorkingCopies() {
    return m_workingCopies.values();
  }

  public <VALUE> VALUE getProperty(Class<? extends IContextProperty<VALUE>> propertyClass) {
    return BEANS.get(propertyClass).getValue();
  }

  public JsClass getJsClass(String fullyQualifiedName) {
    return m_jsClasses.get(fullyQualifiedName);
  }

  public Path relativeToModule(Path path) {
    Assertions.assertNotNull(Configuration.get().getSourceModuleDirectory());
    return path.relativize(Configuration.get().getSourceModuleDirectory());
  }

  public JsFile getJsFile(WorkingCopy workingCopy) {
    return m_jsFiles.get(workingCopy);
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

  public Libraries getLibraries() {
    return m_libraries;
  }

  protected void parseJsFiles() throws IOException {
    final Path src = BEANS.get(Configuration.class).getSourceModuleDirectory().resolve("src/main/js");
    if (!Files.exists(src) || !Files.isDirectory(src)) {
      LOG.info("Could not find '" + src + "' to parse js files.");
      return;
    }
    Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (dir.endsWith(Paths.get("src/main/js/jquery"))) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (!FileUtility.hasExtension(file, "js")) {
          return FileVisitResult.CONTINUE;
        }
        PathInfo info = new PathInfo(file);
        if (BEANS.all(IMigrationExcludePathFilter.class).stream().anyMatch(filter -> filter.test(info))) {
          return FileVisitResult.CONTINUE;
        }
        JsFile jsFile = ensureJsFile(ensureWorkingCopy(file));
        jsFile.getJsClasses().forEach(jsClazz -> m_jsClasses.put(jsClazz.getFullyQualifiedName(), jsClazz));
        jsFile.getJsUtilities().forEach(jsUtil -> m_jsUtilities.put(jsUtil.getFullyQualifiedName(), jsUtil));
        jsFile.getJsTopLevelEnums().forEach(jsEnum -> m_jsTopLevelEnums.put(jsEnum.getFqn(), jsEnum));

        return FileVisitResult.CONTINUE;
      }
    });
  }

  public Collection<JsClass> getAllJsClasses() {
    return Collections.unmodifiableCollection(m_jsClasses.values());
  }

  public List<Path> getAllLessFiles() {
    return Collections.unmodifiableList(m_lessFiles);
  }

  @FrameworkExtensionMarker
  public Collection<JsUtility> getAllJsUtilities() {
    return Collections.unmodifiableCollection(m_jsUtilities.values());
  }

  @FrameworkExtensionMarker
  public JsUtility getJsUtility(String fullyQualifiedName) {
    return m_jsUtilities.get(fullyQualifiedName);
  }

  public JsUtilityFunction getJsUtilityFunction(String fullyQualifiedName) {
    int i = fullyQualifiedName.lastIndexOf('.');
    if (i < 0) return null;
    JsUtility u = m_jsUtilities.get(fullyQualifiedName.substring(0, i));
    if (u == null) return null;
    return u.getFunction(fullyQualifiedName.substring(i + 1));
  }

  public JsUtilityVariable getJsUtilityVariable(String fullyQualifiedName) {
    int i = fullyQualifiedName.lastIndexOf('.');
    if (i < 0) return null;
    JsUtility u = m_jsUtilities.get(fullyQualifiedName.substring(0, i));
    if (u == null) return null;
    return u.getVariable(fullyQualifiedName.substring(i + 1));
  }

  public Collection<JsTopLevelEnum> getAllTopLevelEnums() {
    return Collections.unmodifiableCollection(m_jsTopLevelEnums.values());
  }

  public JsTopLevelEnum getJsTopLevelEnum(String fullyQualifiedName) {
    return m_jsTopLevelEnums.get(fullyQualifiedName);
  }
}
