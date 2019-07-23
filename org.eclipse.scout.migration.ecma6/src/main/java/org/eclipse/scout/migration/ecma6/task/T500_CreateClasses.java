//package org.eclipse.scout.migration.ecma6.task;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Predicate;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//import org.eclipse.scout.migration.ecma6.MigrationUtility;
//import org.eclipse.scout.migration.ecma6.PathFilters;
//import org.eclipse.scout.migration.ecma6.PathInfo;
//import org.eclipse.scout.migration.ecma6.WorkingCopy;
//import org.eclipse.scout.migration.ecma6.context.Context;
//import org.eclipse.scout.migration.ecma6.model.old.JsClass;
//import org.eclipse.scout.migration.ecma6.model.old.JsConstant;
//import org.eclipse.scout.migration.ecma6.model.old.JsFile;
//import org.eclipse.scout.migration.ecma6.model.old.JsFunction;
//import org.eclipse.scout.migration.ecma6.model.target.EJsImport;
//import org.eclipse.scout.rt.platform.Order;
//import org.eclipse.scout.rt.platform.exception.VetoException;
//
//@Order(500)
//public class T500_CreateClasses extends AbstractTask {
//
//  public static Path ACCEPTED_CLAZZ = Paths.get("src/main/js/scout/form/fields/FormField.js");
////  public static Path ACCEPTED_CLAZZ = Paths.get("src/main/js/scout/messagebox/MessageBoxes.js");
//  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"));
//
//  @Override
//  public boolean accept(PathInfo pathInfo, Context context) {
//    Predicate<PathInfo> debugFilter = PathFilters.and(m_filter, PathFilters.oneOf(ACCEPTED_CLAZZ));
//    return debugFilter.test(pathInfo);
////    return m_filter.test(pathInfo);
//  }
//
//  @Override
//  public void process(PathInfo pathInfo, Context context) {
//    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
//    try {
//      createClasses(workingCopy, context);
//
//    }
//    catch (VetoException e) {
//      MigrationUtility.prependTodo(workingCopy, e.getMessage());
//      System.out.println("ERROR [" + pathInfo.getPath().getFileName() + "]: " + e.getMessage());
//    }
//
//  }
//
//  protected void createClasses(WorkingCopy workingCopy, Context context) {
//    List<EJsImport> importDeclarations = new ArrayList<>();
//    JsFile jsFile = context.ensureJsFile(workingCopy);
//    List<JsClass> jsClasses = jsFile.getJsClasses();
//    // reverse
//    for (int i = jsClasses.size() - 1; i > -1; i--) {
//
//      JsClass jsClazz = jsClasses.get(i);
//      createClazzBlock(jsClazz, jsClasses.size() == 1, importDeclarations, workingCopy);
//      updateFunctions(jsClazz, importDeclarations, workingCopy);
//      updateConstants(jsClazz, importDeclarations, workingCopy);
//    }
//    // create imports
//    String impots = importDeclarations.stream()
//        .map(imp -> imp.toSource(context, jsFile))
//        .collect(Collectors.joining(workingCopy.getLineSeparator()));
//
//    StringBuilder sourceBuilder = new StringBuilder(workingCopy.getSource());
//    if (jsFile.getCopyRight() == null) {
//      sourceBuilder.insert(0, impots);
//    }
//    else {
//      sourceBuilder.insert(jsFile.getCopyRight().getEndOffset() + workingCopy.getLineSeparator().length(), impots);
//    }
//    workingCopy.setSource(sourceBuilder.toString());
//  }
//
//  protected void createClazzBlock(JsClass clzz, boolean onlyOneClazz, List<EJsImport> importDeclarations, WorkingCopy workingCopy) {
////    export default class Menu extends Action {
////      constructor() {
//    List<JsFunction> functions = clzz.getFunctions();
//    if (functions.size() == 0) {
//      throw new VetoException("Clazz without functions '" + clzz.getFullyQuallifiedName() + "' !");
//    }
//    // close classblock after last function
//    functions.get(functions.size() - 1).getEndOffset();
//    StringBuilder sourceBuilder = new StringBuilder(workingCopy.getSource());
//    sourceBuilder.insert(functions.get(functions.size() - 1).getEndOffset() + 1, workingCopy.getLineSeparator() + "};");
//    // remove scout inherits
//    if (clzz.getSuperCall() != null) {
//      sourceBuilder.replace(clzz.getSuperCall().getStartOffset(), clzz.getSuperCall().getEndOffset(), "");
//    }
//    // open class block
//    StringBuilder classBuilder = new StringBuilder();
//    classBuilder.append("export ");
//    if (onlyOneClazz) {
//      classBuilder.append("default ");
//    }
//    classBuilder.append(clzz.getName()).append(" ");
//    if (clzz.getSuperCall() != null) {
//      importDeclarations.add(new EJsImport(clzz.getSuperCall().getNamespace(), clzz.getSuperCall().getName()));
//      classBuilder.append("extends ").append(clzz.getSuperCall().getName()).append(" ");
//
//    }
//    classBuilder.append("{").append(workingCopy.getLineSeparator());
//    sourceBuilder.insert(functions.get(0).getStartOffset(), classBuilder.toString());
//    workingCopy.setSource(sourceBuilder.toString());
//  }
//
//  protected void updateFunctions(JsClass clazz, List<EJsImport> importDeclarations, WorkingCopy workingCopy) {
//    String source = workingCopy.getSource();
//    for (JsFunction f : clazz.getFunctions()) {
//      if (!f.isConstructor()) {
//        source = updateFunction(f, importDeclarations, source);
//      }
//      else {
//        source = updateConstructor(f, importDeclarations, source);
//      }
//    }
//    workingCopy.setSource(source);
//  }
//
//  private String updateConstructor(JsFunction function, List<EJsImport> importDeclarations, String source) {
//    StringBuilder patternBuilder = new StringBuilder();
//    patternBuilder.append(function.getJsClass().getNamespace())
//        .append("\\.")
//        .append(function.getJsClass().getName())
//        .append("\\ \\=\\s*function");
//
//    Pattern pattern = Pattern.compile(patternBuilder.toString());
//    Matcher matcher = pattern.matcher(source);
//    if (matcher.find()) {
//      StringBuilder replacement = new StringBuilder();
//      replacement.append("constructor");
//      replacement.append(function.getName());
//      source = matcher.replaceFirst(replacement.toString());
//    }
//    // super call
//    if (function.getJsClass().getSuperCall() != null) {
//
//      patternBuilder = new StringBuilder();
//      patternBuilder.append(function.getJsClass().getNamespace())
//          .append("\\.")
//          .append(function.getJsClass().getName())
//          .append("\\.parent\\.call\\(this(\\,\\s*[^\\)]+)?\\)\\;");
//      pattern = Pattern.compile(patternBuilder.toString());
//      matcher = pattern.matcher(source);
//      if (matcher.find()) {
//        StringBuilder replacement = new StringBuilder();
//        replacement.append("super(");
//        if (matcher.group(1) != null) {
//          replacement.append(matcher.group(1));
//        }
//        replacement.append(");");
//        source = matcher.replaceFirst(replacement.toString());
//      }
//    }
//    return source;
//  }
//
//  protected String updateFunction(JsFunction function, List<EJsImport> importDeclarations, String source) {
//    StringBuilder patternBuilder = new StringBuilder();
//    patternBuilder.append(function.getJsClass().getNamespace())
//        .append("\\.")
//        .append(function.getJsClass().getName());
//    if (!function.isStatic()) {
//      patternBuilder.append("\\.prototype");
//    }
//    patternBuilder.append("\\.").append(function.getName());
//    patternBuilder.append("\\ \\=\\s*function");
//
//    Pattern pattern = Pattern.compile(patternBuilder.toString());
//    Matcher matcher = pattern.matcher(source);
//    if (matcher.find()) {
//      StringBuilder replacement = new StringBuilder();
//      if (function.isStatic()) {
//        replacement.append("static ");
//      }
//      replacement.append(function.getName());
//      source = matcher.replaceFirst(replacement.toString());
//    }
//    return source;
//  }
//
//  protected void updateConstants(JsClass clazz, List<EJsImport> importDeclarations, WorkingCopy workingCopy) {
//    List<JsConstant> constants = clazz.getConstants();
//    if (constants.size() == 0) {
//      return;
//    }
//    String source = workingCopy.getSource();
//    for (JsConstant constant : constants) {
//      source = updateConstant(constant, importDeclarations, source, workingCopy);
//    }
//    workingCopy.setSource(source);
//  }
//
//  private String updateConstant(JsConstant constant, List<EJsImport> importDeclarations, String source, WorkingCopy workingCopy) {
//
//    StringBuilder patternBuilder = new StringBuilder();
//    patternBuilder.append(constant.getJsClass().getNamespace())
//        .append("\\.")
//        .append(constant.getJsClass().getName());
//
//    patternBuilder.append("\\.").append(constant.getName());
//    patternBuilder.append("(\\ \\=\\s*\\{)");
//
//    Pattern pattern = Pattern.compile(patternBuilder.toString());
//    Matcher matcher = pattern.matcher(source);
//    if (matcher.find()) {
//
//      StringBuilder replacement = new StringBuilder();
//      if (constant.hasParseErrors()) {
//        replacement.append(constant.toTodoText(workingCopy.getLineSeparator())).append(workingCopy.getLineSeparator());
//        replacement.append(matcher.group());
//      }
//      else {
////        export const MenuStyle
//        replacement.append("export const ").append(constant.getName())
//            .append(matcher.group(1));
//      }
//      source = matcher.replaceFirst(replacement.toString());
//    }
//    return source;
//  }
//
//}
