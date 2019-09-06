package org.eclipse.scout.migration.ecma6.task;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsClass;
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
      LOG.error("Could not create class ["+pathInfo.getPath().getFileName()+"]. Appended TODO for manual migration.");
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
      throw new VetoException("Clazz without functions '" + clzz.getFullyQualifiedName() + "' !");
    }
    // close classblock after last function

    String source = workingCopy.getSource();
    StringBuilder sourceBuilder = new StringBuilder(workingCopy.getSource());
    JsFunction lastFunction = null;
    for(int i = functions.size() -1; i > -1; i--){
      lastFunction = functions.get(i);
      if(lastFunction.isMemoryOnly()){
        lastFunction = null;
      }else{
        break;
      }
    }
    Assertions.assertNotNull(lastFunction,"Class must have at least one function check '"+clzz.getFullyQualifiedName()+"'.");

    Matcher matcher = Pattern.compile(Pattern.quote(lastFunction.getSource())).matcher(source);
    if(matcher.find()){
      // end class marker is used to find the end of a class and will be removed in T70000_RemoveEndClassMarkers.
      sourceBuilder.insert(matcher.end(),workingCopy.getLineSeparator() + "}"+END_CLASS_MARKER);
    }else{
      sourceBuilder.insert(0, MigrationUtility.prependTodo("", "Close class body with '}'  manual.", workingCopy.getLineSeparator()));
      LOG.warn("Could not close class body in '"+clzz.getFullyQualifiedName()+"'");
    }

    // remove scout inherits
    if (clzz.getSuperCall() != null) {
      matcher = Pattern.compile(Pattern.quote(clzz.getSuperCall().getSource())).matcher(sourceBuilder.toString());
      if(matcher.find()){
        sourceBuilder.replace(matcher.start(), matcher.end(),"");
      }else{
        sourceBuilder.insert(0, MigrationUtility.prependTodo("", "Remove 'scout.inhertits(...' manual", workingCopy.getLineSeparator()));
        LOG.warn("Could not remove 'scout.inhertits(...'  in '"+clzz.getFullyQualifiedName()+"'");

      }
    }
    // open class block
    StringBuilder classBuilder = new StringBuilder();
    classBuilder.append("export ");
    if (onlyOneClazz) {
      classBuilder.append("default ");
    }
    classBuilder.append("class ").append(clzz.getName()).append(" ");
    if (clzz.getSuperCall() != null) {

      String alias = clzz.getSuperCall().getName();
      if (alias.equalsIgnoreCase(clzz.getName())) {
        alias = StringUtility.uppercaseFirst(clzz.getSuperCall().getNamespace()) + clzz.getSuperCall().getName();
      }
      classBuilder.append("extends ")
          .append(jsFile.getOrCreateImport(clzz.getSuperCall().getFullyQualifiedName(), context).getReferenceName())
          .append(" ");

    }
    classBuilder.append("{").append(workingCopy.getLineSeparator()).append(workingCopy.getLineSeparator());

    matcher = Pattern.compile(Pattern.quote(functions.get(0).getSource())).matcher(sourceBuilder.toString());
    if(matcher.find()){
      sourceBuilder.insert(matcher.start(),classBuilder.toString());
    }else{
      sourceBuilder.insert(0, MigrationUtility.prependTodo("", "Create class declaration manual (like: 'export default class FormField')", workingCopy.getLineSeparator()));
      LOG.warn("Could not create class declaration like (like: 'export default class FormField')  in '"+clzz.getFullyQualifiedName()+"'");
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
      replacement.append(function.getName().replace("$","\\$"));
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
      if(matcher.group(2) != null){
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
          replacement.append(matcher.group(1).replace("$", "\\$"));
        }
        replacement.append(");");
        source = matcher.replaceFirst(replacement.toString());
      }
    }
    return source;
  }



}
