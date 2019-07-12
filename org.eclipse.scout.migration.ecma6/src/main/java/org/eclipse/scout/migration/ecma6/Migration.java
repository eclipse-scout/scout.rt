package org.eclipse.scout.migration.ecma6;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.context.IContextProperty;
import org.eclipse.scout.migration.ecma6.task.ITask;
import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Migration {

  private static final Logger LOG = LoggerFactory.getLogger(Migration.class);

//  private static Path SOURCE_ROOT_DIRECTORY = Paths.get("C:\\dev\\ideWorkspaces\\scout-10_0-crm-16_2\\bsiagbsicrm\\com.bsiag.bsicrm.ui.html");
//  private static Path SOURCE_ROOT_DIRECTORY = Paths.get("C:\\dev\\ideWorkspaces\\scout-10_0-crm-16_2\\bsistudio\\com.bsiag.bsistudio.lab.ui.html");
  private static Path SOURCE_ROOT_DIRECTORY = Paths.get("C:\\dev\\ideWorkspaces\\scout-10_0-crm-16_2\\org.eclipse.scout.rt\\org.eclipse.scout.rt.ui.html");
  private static Path TARGET_ROOT_DIR = Paths.get("C:/tmp/max24h/migEcma6/org.eclipse.scout.rt.ui.html");
  private static String NAMESPACE = "scout";
  private static boolean CLEAN_TARGET = true;

  private List<ITask> m_tasks;
  private Context m_context;

  public static void main(String[] args) {
    try {
      Migration migration = new Migration();
      migration.init();
      migration.run();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Migration() {
  }

  public void init() throws IOException {
    if (!Files.exists(SOURCE_ROOT_DIRECTORY)) {
      System.err.println("Source DIR '" + SOURCE_ROOT_DIRECTORY + "' does not exist. Exit migration.");
      return;
    }
    Files.createDirectories(TARGET_ROOT_DIR);
    m_context = new Context(SOURCE_ROOT_DIRECTORY, TARGET_ROOT_DIR, NAMESPACE);
    // setup context properties
    BEANS.all(IContextProperty.class).forEach(contextProperty -> contextProperty.setup(m_context));

    m_tasks = BEANS.all(ITask.class);
    m_tasks.forEach(task -> task.setup(m_context));
  }

  public void run() throws IOException {
    LOG.debug("Start migration");

    visitFiles();

  }

  private void visitFiles() throws IOException {
    Files.walkFileTree(m_context.getSourceRootDirectory(), new SimpleFileVisitor<Path>() {

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        processFile(file, m_context);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
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
    writeWorkingCopies();
  }

  private void processFile(Path file, Context context) {
    // create working copy
    context.ensureWorkingCopy(file);
    m_tasks.stream().filter(task -> task.accept(file, context))
        .forEach(task -> task.process(file, context));

  }

  private void writeWorkingCopies() {
    if (CLEAN_TARGET) {
      try {
        FileUtility.deleteDirectory(m_context.getTargetRootDirectory());
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
    m_context.getWorkingCopies().forEach(workingCopy -> writeWorkingCopy(workingCopy));
  }

  private void writeWorkingCopy(WorkingCopy workingCopy) {
    try {

      final Path destination;
      if (workingCopy.getRelativeTargetPath() != null) {
        destination = TARGET_ROOT_DIR.resolve(workingCopy.getRelativeTargetPath());
      }
      else {
        Path relativePath = SOURCE_ROOT_DIRECTORY.relativize(workingCopy.getPath());
        destination = TARGET_ROOT_DIR.resolve(relativePath);
      }
      workingCopy.persist(destination);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}
