package org.eclipse.scout.migration.ecma6.task;

import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.rt.platform.Order;

import java.nio.file.Path;
import java.nio.file.Paths;

@Order(100)
public class T100_MoveIndexJs extends AbstractTask {
  private Path m_relativeIndexJsOld = Paths.get("src/main/resources/WebContent/res/index.js");
  @Override
  public boolean accept(Path file, Context context) {
    return file.endsWith(m_relativeIndexJsOld);
  }

  @Override
  public void process(Path file, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(file);
    // move it to root
    workingCopy.setRelativeTargetPath(file.getFileName());
  }
}
