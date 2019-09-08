package org.eclipse.scout.migration.ecma6.task.json;

import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

import java.nio.file.Path;
import java.util.Set;

public class MenuBarPositionConstMapper implements IConstPlaceholderMapper {
  @Override
  public String migrate(String key, String value, Path file, Context context) {
    if (!"menuBarPosition".equals(key)) {
      return null;
    }
    if (!ObjectUtility.isOneOf(value, "AUTO", "TOP", "BOTTOM", "TITLE")) {
      // cannot resolve to the enum
      return null;
    }
    return "scout.GroupBox.MenuBarPosition." + value;
  }
}
