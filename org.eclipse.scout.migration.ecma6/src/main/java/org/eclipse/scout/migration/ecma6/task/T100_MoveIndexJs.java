package org.eclipse.scout.migration.ecma6.task;

import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.rt.platform.Order;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

@Order(100)
public class T100_MoveIndexJs extends AbstractTask {

  private Predicate<Path> m_indexJsFilter;

  @Override
  public void setup(Context context) {
    m_indexJsFilter = PathFilters.oneOf(context.getSourceRootDirectory().resolve(Paths.get("src/main/resources/WebContent/res/index.js")));
  }

  @Override
  public boolean accept(Path file, Path moduleRelativeFile, Context context) {
    return m_indexJsFilter.test(file);
  }

  @Override
  public void process(Path file, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(file);
    // move it to root
    workingCopy.setRelativeTargetPath(file.getFileName());
  }
}
