package org.eclipse.scout.migration.ecma6.task.json;

import java.nio.file.Path;

import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.ApplicationScoped;

@ApplicationScoped
public interface IConstPlaceholderMapper {
  String migrate(String key, String value, Path file, Context context);
}
