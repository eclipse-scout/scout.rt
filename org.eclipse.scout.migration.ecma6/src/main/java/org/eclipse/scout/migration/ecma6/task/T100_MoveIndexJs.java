package org.eclipse.scout.migration.ecma6.task;

import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.rt.platform.Order;

import java.nio.file.Paths;
import java.util.function.Predicate;

@Order(100)
public class T100_MoveIndexJs extends AbstractTask {

  private Predicate<PathInfo> m_indexJsFilter = PathFilters.oneOf(Paths.get("src/main/resources/WebContent/res/index.js"));


  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_indexJsFilter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    // move it to root
    workingCopy.setRelativeTargetPath(pathInfo.getPath().getFileName());
  }
}
