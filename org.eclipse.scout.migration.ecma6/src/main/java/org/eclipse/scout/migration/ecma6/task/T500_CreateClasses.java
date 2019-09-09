package org.eclipse.scout.migration.ecma6.task;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.ISourceElement;
import org.eclipse.scout.migration.ecma6.model.old.JsClass;
import org.eclipse.scout.migration.ecma6.model.old.JsConstant;
import org.eclipse.scout.migration.ecma6.model.old.JsEnum;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.old.JsFunction;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(500)
public class T500_CreateClasses extends AbstractTask {

  private static final Logger LOG = LoggerFactory.getLogger(T500_CreateClasses.class);

  public static String END_CLASS_MARKER = "//MARKER_CLASS_END";
  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"), PathFilters.isClass());

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    try {
      createClasses(workingCopy, context);
    }
    catch (VetoException e) {
      MigrationUtility.prependTodo(workingCopy, e.getMessage());
      LOG.error("Could not create class [" + pathInfo.getPath().getFileName() + "]. Appended TODO for manual migration.");
    }

  }

  protected void createClasses(WorkingCopy workingCopy, Context context) {
    JsFile jsFile = context.ensureJsFile(workingCopy);
    List<JsClass> jsClasses = jsFile.getJsClasses();
    // reverse
    for (int i = jsClasses.size() - 1; i > -1; i--) {
      JsClass jsClazz = jsClasses.get(i);
      createClazzBlock(jsClazz, jsClasses.size() == 1, jsFile, workingCopy, context);
      updateFunctions(jsClazz, jsFile, workingCopy);
      updateConstants(jsClazz, jsFile, workingCopy);
      updateEnums(jsClazz, jsFile, workingCopy);
    }
  }

  protected void createClazzBlock(JsClass clazz, boolean onlyOneClazz, JsFile jsFile, WorkingCopy workingCopy, Context context) {
    //    export default class Menu extends Action {
    //      constructor() {
    // close classblock after last element
    String source = workingCopy.getSource();
    StringBuilder sourceBuilder = new StringBuilder(workingCopy.getSource());

    List<ISourceElement> elements = new ArrayList<>();
    elements.addAll(clazz.getFunctions());
    elements.add(clazz.getConstructor());
    elements.add(clazz.getSuperCall());
    elements.addAll(clazz.getConstants());
    elements.addAll(clazz.getEnums());
    if (elements.isEmpty()) {
      return;
    }

    //find first element start position
    int startPos = elements
        .stream()
        .filter(e -> e != null)
        .filter(e -> e.getSource() != null)
        .mapToInt(e -> {
          int i = source.indexOf(e.getSource());
          return i;
        })
        .filter(i -> i >= 0)
        .min()
        .orElse(-1);
    Assertions.assertTrue(startPos >= 0, "Class must have at least one element. Check '" + clazz.getFullyQualifiedName() + "'.");

    //find last element end position
    int endPos = elements
        .stream()
        .filter(e -> e != null)
        .filter(e -> e.getSource() != null)
        .mapToInt(e -> {
          int i = source.indexOf(e.getSource());
          if (i < 0) return -1;
          return i + e.getSource().length();
        })
        .filter(i -> i >= 0)
        .max()
        .orElse(-1);
    Assertions.assertTrue(endPos >= 0, "Class must have at least one element. Check '" + clazz.getFullyQualifiedName() + "'.");

    // end class marker is used to find the end of a class and will be removed in T70000_RemoveEndClassMarkers.
    sourceBuilder.insert(endPos, workingCopy.getLineDelimiter() + "}" + END_CLASS_MARKER);

    // remove scout inherits
    if (clazz.getSuperCall() != null) {
      Matcher matcher = Pattern.compile(Pattern.quote(clazz.getSuperCall().getSource())).matcher(sourceBuilder.toString());
      if (matcher.find()) {
        sourceBuilder.replace(matcher.start(), matcher.end(), "");
      }
      else {
        sourceBuilder.insert(0, MigrationUtility.prependTodo("", "Remove 'scout.inhertits(...' manual", workingCopy.getLineDelimiter()));
        LOG.warn("Could not remove 'scout.inhertits(...'  in '" + clazz.getFullyQualifiedName() + "'");

      }
    }
    // open class block
    StringBuilder classBuilder = new StringBuilder();
    classBuilder.append("export ");
    if (onlyOneClazz) {
      classBuilder.append("default ");
    }
    classBuilder.append("class ").append(clazz.getName()).append(" ");
    if (clazz.getSuperCall() != null) {

      String alias = clazz.getSuperCall().getName();
      if (alias.equalsIgnoreCase(clazz.getName())) {
        alias = StringUtility.uppercaseFirst(clazz.getSuperCall().getNamespace()) + clazz.getSuperCall().getName();
      }
      classBuilder.append("extends ")
          .append(jsFile.getOrCreateImport(clazz.getSuperCall().getFullyQualifiedName(), context).getReferenceName())
          .append(" ");

    }
    classBuilder.append("{").append(workingCopy.getLineDelimiter()).append(workingCopy.getLineDelimiter());

    sourceBuilder.insert(startPos, classBuilder.toString());
    if (!sourceBuilder.toString().endsWith(workingCopy.getLineDelimiter())) {
      sourceBuilder.append(workingCopy.getLineDelimiter());
    }
    workingCopy.setSource(sourceBuilder.toString());
  }

  protected void updateFunctions(JsClass clazz, JsFile jsFile, WorkingCopy workingCopy) {
    String source = workingCopy.getSource();
    for (JsFunction f : clazz.getFunctions()) {
      if (!f.isConstructor()) {
        source = updateFunction(f, jsFile, source);
      }
      else {
        source = updateConstructor(f, jsFile, source);
      }
    }
    workingCopy.setSource(source);
  }

  protected String updateFunction(JsFunction function, JsFile jsFile, String source) {
    StringBuilder patternBuilder = new StringBuilder();
    patternBuilder.append(function.getJsClass().getNamespace())
        .append("\\.")
        .append(function.getJsClass().getName());
    if (!function.isStatic()) {
      patternBuilder.append("\\.prototype");
    }
    patternBuilder.append("\\.").append(Pattern.quote(function.getName()));
    patternBuilder.append("\\ \\=\\s*function");

    Pattern pattern = Pattern.compile(patternBuilder.toString());
    Matcher matcher = pattern.matcher(source);
    if (matcher.find()) {
      StringBuilder replacement = new StringBuilder();
      if (function.isStatic()) {
        replacement.append("static ");
      }
      replacement.append(function.getName().replace("$", "\\$"));
      source = matcher.replaceFirst(replacement.toString());
    }
    // super call

    // group 1 function name
    // group 2 (optional) arguments with leading semicolumn
    // group 3 (inner optional) agruments to use
    patternBuilder = new StringBuilder();
    patternBuilder.append(Pattern.quote(function.getJsClass().getFullyQualifiedName()))
        .append("\\.parent\\.prototype\\.(").append(Pattern.quote(function.getName())).append(")\\.call\\(\\s*this\\s*(\\,\\s*([^\\)]))?");
    pattern = Pattern.compile(patternBuilder.toString());
    matcher = pattern.matcher(source);
    if (matcher.find()) {
      StringBuilder replacement = new StringBuilder();
      replacement.append("super.").append(matcher.group(1)).append("(");
      if (matcher.group(2) != null) {
        replacement.append(matcher.group(3));
      }
      source = matcher.replaceFirst(Matcher.quoteReplacement(replacement.toString()));
    }

    return source;
  }

  protected String updateConstructor(JsFunction function, JsFile jsFile, String source) {
    StringBuilder patternBuilder = new StringBuilder();
    patternBuilder.append(function.getJsClass().getNamespace())
        .append("\\.")
        .append(function.getJsClass().getName())
        .append("\\s*\\=\\s*function");

    Pattern pattern = Pattern.compile(patternBuilder.toString());
    Matcher matcher = pattern.matcher(source);
    if (matcher.find()) {
      StringBuilder replacement = new StringBuilder();
      replacement.append("constructor");
      replacement.append(function.getName());
      source = matcher.replaceFirst(replacement.toString());
    }
    // super call
    if (function.getJsClass().getSuperCall() != null) {

      patternBuilder = new StringBuilder();
      patternBuilder.append(function.getJsClass().getNamespace())
          .append("\\.")
          .append(function.getJsClass().getName())
          .append("\\.parent\\.call\\(this(\\,\\s*([^\\)]+))?\\)\\;");

      // group 1 arguments with leading ','
      // group 2 arguments to use for mig
      pattern = Pattern.compile(patternBuilder.toString());
      matcher = pattern.matcher(source);
      if (matcher.find()) {
        StringBuilder replacement = new StringBuilder();
        replacement.append("super(");
        if (matcher.group(1) != null) {
          replacement.append(matcher.group(2));
        }
        replacement.append(");");
        source = matcher.replaceFirst(Matcher.quoteReplacement(replacement.toString()));
      }
    }
    return source;
  }

  protected void updateConstants(JsClass clazz, JsFile jsFile, WorkingCopy workingCopy) {
    String source = workingCopy.getSource();
    String lineDelimiter = workingCopy.getLineDelimiter();
    List<JsConstant> constants = clazz.getConstants();
    if (constants.size() == 0) {
      return;
    }
    for (JsConstant constant : constants) {
      source = updateConstant(source, constant, lineDelimiter);
    }
    workingCopy.setSource(source);
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
      StringBuilder replacement = new StringBuilder();
      replacement.append(matcher.group(1));

      if (constant.hasParseErrors()) {
        replacement.append(constant.toTodoText(lineDelimiter)).append(lineDelimiter);
        replacement.append(matcher.group());
      }
      else {
        replacement.append("static");
        replacement.append(" ").append(constant.getName())
            .append(matcher.group(3).replace("\\", "\\\\").replace("$", "\\$"));

      }
      source = matcher.replaceFirst(replacement.toString());
    }
    return source;
  }

  protected void updateEnums(JsClass clazz, JsFile jsFile, WorkingCopy workingCopy) {
    String source = workingCopy.getSource();
    String lineDelimiter = workingCopy.getLineDelimiter();
    List<JsEnum> jsEnums = clazz.getEnums();
    if (jsEnums.size() == 0) {
      return;
    }
    for (JsEnum jsEnum : jsEnums) {
      source = updateEnum(source, jsEnum, lineDelimiter);
    }
    workingCopy.setSource(source);
  }

  protected String updateEnum(String source, JsEnum jsEnum, String lineDelimiter) {
    StringBuilder patternBuilder = new StringBuilder();
    patternBuilder.append(jsEnum.getJsClass().getNamespace())
        .append("\\.")
        .append(jsEnum.getJsClass().getName());

    patternBuilder.append("\\.").append(jsEnum.getName());
    patternBuilder.append("(\\ \\=\\s*\\{)");

    Pattern pattern = Pattern.compile(patternBuilder.toString());
    Matcher matcher = pattern.matcher(source);
    if (matcher.find()) {

      StringBuilder replacement = new StringBuilder();
      if (jsEnum.hasParseErrors()) {
        replacement.append(jsEnum.toTodoText(lineDelimiter)).append(lineDelimiter);
        replacement.append(matcher.group());
      }
      else {
        replacement.append("static ").append(jsEnum.getName())
            .append(matcher.group(1));
      }
      source = matcher.replaceFirst(replacement.toString());
    }
    return source;
  }

}
