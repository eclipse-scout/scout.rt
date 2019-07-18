package org.eclipse.scout.migration.ecma6.task;

import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.ApplicationScoped;

@ApplicationScoped
public interface ITask {

  void setup(Context context);

  boolean accept(PathInfo pathInfo, Context context);

  void process(PathInfo pathInfo, Context context);

}
