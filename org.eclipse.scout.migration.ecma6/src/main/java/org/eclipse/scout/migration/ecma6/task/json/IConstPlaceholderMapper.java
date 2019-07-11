package org.eclipse.scout.migration.ecma6.task.json;

import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.ApplicationScoped;

import java.nio.file.Path;
import java.util.Set;

@ApplicationScoped
public interface IConstPlaceholderMapper {
  String migrate(String key, String value, Path file, Context context, Set<String> importsToAdd);
}
