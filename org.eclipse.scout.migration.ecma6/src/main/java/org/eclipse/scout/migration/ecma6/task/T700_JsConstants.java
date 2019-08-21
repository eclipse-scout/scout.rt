package org.eclipse.scout.migration.ecma6.task;

import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsClass;
import org.eclipse.scout.migration.ecma6.model.old.JsConstant;
import org.eclipse.scout.migration.ecma6.model.old.JsEnum;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.rt.platform.Order;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Order(700)
public class T700_JsConstants extends AbstractTask {
  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"), PathFilters.isClass());

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    String source = workingCopy.getSource();
    JsFile jsFile = context.ensureJsFile(workingCopy);

    for (JsClass clazz : jsFile.getJsClasses()) {
      source = updateConstants(source, clazz, workingCopy.getLineSeparator());
    }

    workingCopy.setSource(source);
  }

  protected String updateConstants(String source, JsClass clazz, String lineDelimiter) {
    List<JsConstant> constants = clazz.getConstants();
    if (constants.size() == 0) {
      return source;
    }
    for (JsConstant constant : constants) {
      source = updateConstant(source, constant, lineDelimiter);
    }
    return source;
  }

  protected String updateConstant(String source, JsConstant constant, String lineDelimiter) {
    StringBuilder patternBuilder = new StringBuilder();
    patternBuilder
        .append("([\\r\\n]{1})(")
        .append(constant.getJsClass().getNamespace())
        .append("\\.")
        .append(constant.getJsClass().getName());

    patternBuilder.append("\\.").append(constant.getName())
        .append(")");
    patternBuilder.append("((\\s*\\=\\s*[^\\;]*)?\\;)");
    Pattern pattern = Pattern.compile(patternBuilder.toString());
    Matcher matcher = pattern.matcher(source);
    if (matcher.find()) {
      System.out.println("FOUND: " + matcher.group());
      StringBuilder replacement = new StringBuilder();
      replacement.append(matcher.group(1));

      if (constant.hasParseErrors()) {
        replacement.append(constant.toTodoText(lineDelimiter)).append(lineDelimiter);
        replacement.append(matcher.group());
      }
      else {
        replacement.append("export");
        if (hasOtherAssignments(source, constant, matcher.start(2), matcher.end(2), matcher.group(3))) {
          replacement.append(" let");
        }
        else {
          replacement.append(" const");
        }
        replacement.append(" ").append(constant.getName())
            .append(matcher.group(3).replace("\\", "\\\\").replace("$", "\\$"));

      }
      source = matcher.replaceFirst(replacement.toString());
    }
    return source;
  }

  private boolean hasOtherAssignments(String source, JsConstant constant, int startFqn, int endFqn, String currentAssignment) {
    if (currentAssignment == null) {
      return true;
    }
    StringBuilder patternBuilder = new StringBuilder();
    patternBuilder
        .append("(")
        .append(constant.getJsClass().getNamespace())
        .append("\\.")
        .append(constant.getJsClass().getName())
        .append("\\.").append(constant.getName())
        .append(")")
        .append("\\s*\\=");

    Pattern pattern = Pattern.compile(patternBuilder.toString());
    Matcher matcher = pattern.matcher(source);
    while (matcher.find()) {
      if (matcher.start(1) == startFqn && matcher.end(1) == endFqn) {
        continue;
      }
      return true;
    }
    return false;
  }

}
