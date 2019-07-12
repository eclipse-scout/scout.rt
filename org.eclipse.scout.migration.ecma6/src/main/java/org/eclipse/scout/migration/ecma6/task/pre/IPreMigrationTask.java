package org.eclipse.scout.migration.ecma6.task.pre;

import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.ApplicationScoped;

@ApplicationScoped
public interface IPreMigrationTask {

  void execute(Context context);

}
