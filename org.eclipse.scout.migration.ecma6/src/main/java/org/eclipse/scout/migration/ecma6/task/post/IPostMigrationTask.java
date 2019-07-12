package org.eclipse.scout.migration.ecma6.task.post;

import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.ApplicationScoped;

@ApplicationScoped
public interface IPostMigrationTask {

  void execute(Context context);

}
