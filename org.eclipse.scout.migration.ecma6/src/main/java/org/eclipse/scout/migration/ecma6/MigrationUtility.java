package org.eclipse.scout.migration.ecma6;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.util.StringUtility;

public final class MigrationUtility {

  private static final Pattern REGEX_COMMENT_REMOVE_1 = Pattern.compile("//.*?\r\n");
  private static final Pattern REGEX_COMMENT_REMOVE_2 = Pattern.compile("//.*?\n");
  private static final Pattern REGEX_COMMENT_REMOVE_3 = Pattern.compile("(?s)/\\*.*?\\*/");

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

  public static String parseMemberName(String fullyQualifiedName) {
    if (StringUtility.isNullOrEmpty(fullyQualifiedName)) {
      return null;
    }
    String[] segments = fullyQualifiedName.split("\\.");
    return segments[segments.length - 1];
  }

  public static String parseNamespace(String fullyQualifiedName) {
    if (StringUtility.isNullOrEmpty(fullyQualifiedName)) {
      return null;
    }
    String[] segments = fullyQualifiedName.split("\\.");
    return segments[0];
  }

  public static String removeFirstSegments(Path p, int numSegments) {
    int existingSegmentsCount = p.getNameCount();
    return p.subpath(Math.min(existingSegmentsCount - 1, numSegments), existingSegmentsCount).toString().replace('\\', '/');
  }

  public static String removeFirstSegments(String path, int numSegments) {
    return removeFirstSegments(Paths.get(path), numSegments);
  }

  public static String removeComments(CharSequence methodBody) {
    if (methodBody == null) {
      return null;
    }
    if (!StringUtility.hasText(methodBody)) {
      return methodBody.toString();
    }
    String retVal = REGEX_COMMENT_REMOVE_1.matcher(methodBody).replaceAll("");
    retVal = REGEX_COMMENT_REMOVE_2.matcher(retVal).replaceAll("");
    retVal = REGEX_COMMENT_REMOVE_3.matcher(retVal).replaceAll("");
    return retVal;
  }
}
