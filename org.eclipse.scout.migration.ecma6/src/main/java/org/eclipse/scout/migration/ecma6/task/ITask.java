package org.eclipse.scout.migration.ecma6.task;

import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.ApplicationScoped;

import java.nio.file.Path;

@ApplicationScoped
public interface ITask {

  void setup(Context context);

  boolean accept(Path file, Path moduleRelativeFile, Context context);

  void process(Path file, Context context);

}
