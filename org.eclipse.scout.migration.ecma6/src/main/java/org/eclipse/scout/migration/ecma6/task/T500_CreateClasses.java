package org.eclipse.scout.migration.ecma6.task;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsClass;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.old.JsFunction;
import org.eclipse.scout.migration.ecma6.model.old.JsImport;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.StringUtility;

@Order(500)
public class T500_CreateClasses extends AbstractTask {

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
      System.out.println("ERROR [" + pathInfo.getPath().getFileName() + "]: " + e.getMessage());
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
    }
  }

  protected void createClazzBlock(JsClass clzz, boolean onlyOneClazz, JsFile jsFile, WorkingCopy workingCopy, Context context) {
    //    export default class Menu extends Action {
    //      constructor() {
    List<JsFunction> functions = clzz.getFunctions();
    if (functions.size() == 0) {
      throw new VetoException("Clazz without functions '" + clzz.getFullyQuallifiedName() + "' !");
    }
    // close classblock after last function
    functions.get(functions.size() - 1).getEndOffset();
    StringBuilder sourceBuilder = new StringBuilder(workingCopy.getSource());
    sourceBuilder.insert(functions.get(functions.size() - 1).getEndOffset() + 1, workingCopy.getLineSeparator() + "};");
    // remove scout inherits
    if (clzz.getSuperCall() != null) {
      sourceBuilder.replace(clzz.getSuperCall().getStartOffset(), clzz.getSuperCall().getEndOffset(), "");
    }
    // open class block
    StringBuilder classBuilder = new StringBuilder();
    classBuilder.append("export ");
    if (onlyOneClazz) {
      classBuilder.append("default ");
    }
    classBuilder.append(clzz.getName()).append(" ");
    if (clzz.getSuperCall() != null) {

      String alias = clzz.getSuperCall().getName();
      if(alias.equalsIgnoreCase(clzz.getName())){
        alias = StringUtility.uppercaseFirst(clzz.getSuperCall().getNamespace())+clzz.getSuperCall().getName();
      }
      classBuilder.append("extends ")
        .append(jsFile.getOrCreateImport(context.getJsClass(clzz.getSuperCall().getFullyQuallifiedName())).getReferenceName())
        .append(" ");

    }
    classBuilder.append("{").append(workingCopy.getLineSeparator());
    sourceBuilder.insert(functions.get(0).getStartOffset(), classBuilder.toString());
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

  private String updateConstructor(JsFunction function, JsFile jsFile, String source) {
    StringBuilder patternBuilder = new StringBuilder();
    patternBuilder.append(function.getJsClass().getNamespace())
      .append("\\.")
      .append(function.getJsClass().getName())
      .append("\\ \\=\\s*function");

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
        .append("\\.parent\\.call\\(this(\\,\\s*[^\\)]+)?\\)\\;");
      pattern = Pattern.compile(patternBuilder.toString());
      matcher = pattern.matcher(source);
      if (matcher.find()) {
        StringBuilder replacement = new StringBuilder();
        replacement.append("super(");
        if (matcher.group(1) != null) {
          replacement.append(matcher.group(1).replace("$","\\$"));
        }
        replacement.append(");");
        try {
          source = matcher.replaceFirst(replacement.toString());
        }catch (RuntimeException e){
          e.printStackTrace();
        }
      }
    }
    return source;
  }

  protected String updateFunction(JsFunction function, JsFile jsFile, String source) {
    StringBuilder patternBuilder = new StringBuilder();
    patternBuilder.append(function.getJsClass().getNamespace())
      .append("\\.")
      .append(function.getJsClass().getName());
    if (!function.isStatic()) {
      patternBuilder.append("\\.prototype");
    }
    patternBuilder.append("\\.").append(function.getName());
    patternBuilder.append("\\ \\=\\s*function");

    Pattern pattern = Pattern.compile(patternBuilder.toString());
    Matcher matcher = pattern.matcher(source);
    if (matcher.find()) {
      StringBuilder replacement = new StringBuilder();
      if (function.isStatic()) {
        replacement.append("static ");
      }
      replacement.append(function.getName());
      source = matcher.replaceFirst(replacement.toString());
    }
    return source;
  }



}
