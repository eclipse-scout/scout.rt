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
import org.eclipse.scout.migration.ecma6.pathfilter.IMigrationPathFilter;
import org.eclipse.scout.migration.ecma6.task.ITask;
import org.eclipse.scout.migration.ecma6.task.post.IPostMigrationTask;
import org.eclipse.scout.migration.ecma6.task.pre.IPreMigrationTask;
import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Migration {
  private static final Logger LOG = LoggerFactory.getLogger(Migration.class);


  private List<ITask> m_tasks ;
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
    Files.createDirectories(BEANS.get(Configuration.class).getTargetModuleDirectory());
    m_context = new Context();
    m_context.setup();

    m_tasks = BEANS.all(ITask.class);
    m_tasks.forEach(task -> task.setup(m_context));
  }

  public void run() throws IOException {
    List<IPreMigrationTask> preMigrationTasks = BEANS.all(IPreMigrationTask.class);
    LOG.info("Execute pre migration tasks (#: "+preMigrationTasks.size()+")");
    preMigrationTasks.forEach(task -> task.execute(m_context));

    LOG.debug("Execute migration tasks (#: "+m_tasks.size()+")");
    visitFiles();

    List<IPostMigrationTask> postMigrationTasks = BEANS.all(IPostMigrationTask.class);
    LOG.info("Execute post migration tasks (#: "+postMigrationTasks.size()+")");
    postMigrationTasks.forEach(task -> task.execute(m_context));

  }

  private void visitFiles() throws IOException {

    IMigrationPathFilter pathFilter = BEANS.opt(IMigrationPathFilter.class);
    Files.walkFileTree(Configuration.get().getSourceModuleDirectory(), new SimpleFileVisitor<Path>() {

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        PathInfo info = new PathInfo(file, m_context.getModuleDirectory());
        if(pathFilter == null || pathFilter.test(info)){
          processFile(info, m_context);
        }
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
        if(dir.endsWith(Paths.get("src/main/js/jquery"))){
          return FileVisitResult.SKIP_SUBTREE;
        }
        if ("node_modules".equals(dirName)) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        if(Files.exists(dir.resolve(".classpath"))){
          m_context.setModuleDirectory(dir);
        }
        return FileVisitResult.CONTINUE;
      }
    });

    writeWorkingCopies();
  }

  private void processFile(PathInfo info, Context context) {

    m_tasks.stream().filter(task -> task.accept(info,  context))
        .forEach(task -> task.process(info, context));

  }

  private void writeWorkingCopies() {
    if (Configuration.get().cleanTargetBeforeWriteFiles()) {
      try {
        FileUtility.deleteDirectory(Configuration.get().getTargetModuleDirectory());
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
    m_context.getWorkingCopies().forEach(workingCopy -> writeWorkingCopy(workingCopy));
  }

  private void writeWorkingCopy(WorkingCopy workingCopy) {
    final Path sourceModule = Configuration.get().getSourceModuleDirectory();
    final Path targetModule = Configuration.get().getTargetModuleDirectory();
    try {

      final Path destination;
      if (workingCopy.getRelativeTargetPath() != null) {
        destination = targetModule.resolve(workingCopy.getRelativeTargetPath());
      }
      else {
        Path relativePath = sourceModule.relativize(workingCopy.getPath());
        destination = targetModule.resolve(relativePath);
      }
      workingCopy.persist(destination);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}
