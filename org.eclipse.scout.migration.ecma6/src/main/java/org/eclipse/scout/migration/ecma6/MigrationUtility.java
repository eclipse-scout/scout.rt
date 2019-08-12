package org.eclipse.scout.migration.ecma6;

import org.eclipse.scout.migration.ecma6.model.old.ISourceRange;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.old.JsImport;

import java.util.regex.Pattern;

public final class MigrationUtility {
  private MigrationUtility() {
  }

  public static void prependTodo(WorkingCopy workingCopy, String todoText) {
    String source = workingCopy.getSource();
    source = prependTodo(source, todoText, workingCopy.getLineSeparator());
    workingCopy.setSource(source);
  }

  public static String prependTodo(String source, String todoText, String lineSeparator) {
    source = "// TODO MIG: " + todoText + lineSeparator + source;
    return source;
  }
}
