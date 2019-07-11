package org.eclipse.scout.migration.ecma6.task.json;

import java.nio.file.Path;
import java.util.Set;

import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

public class ActionStyleConstMapper implements IConstPlaceholderMapper {
  @Override
  public String migrate(String key, String value, Path file, Context context, Set<String> importsToAdd) {
    if (!"actionStyle".equals(key)) {
      return null;
    }
    if (!ObjectUtility.isOneOf(value, "DEFAULT", "BUTTON")) {
      // cannot resolve to the enum
      return null;
    }
    importsToAdd.add("Action");
    return "Action.ActionStyle." + value;
  }
}
