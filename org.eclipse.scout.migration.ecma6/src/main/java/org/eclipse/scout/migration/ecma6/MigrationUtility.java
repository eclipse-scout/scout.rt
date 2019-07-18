package org.eclipse.scout.migration.ecma6;

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
