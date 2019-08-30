/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.migration.ecma6.task;

import java.util.function.Predicate;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * example strings.js:
 *
 * <pre>
 * import * as scout from '../scout';
 *
 * // private
 * function _changeFirstLetter(string, funcName) {
 *   ...
 * };
 *
 * export function uppercaseFirstLetter(string) {
 *   ...
 * };
 *
 * export default {
 *   ...,
 *   uppercaseFirstLetter,
 *   ...
 * };
 * </pre>
 */
@Order(800)
public class T800_Utilities extends AbstractTask {
  private static final Logger LOG = LoggerFactory.getLogger(T800_Utilities.class);
  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"), PathFilters.isUtility());

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    try {
      //XXX  createFunctions(workingCopy, context);
    }
    catch (VetoException e) {
      MigrationUtility.prependTodo(workingCopy, e.getMessage());
      LOG.error("Could not create utility [" + pathInfo.getPath().getFileName() + "]. Appended TODO for manual migration.");
    }
  }
/*
protected void createFunctions(WorkingCopy workingCopy, Context context) {
JsFile jsFile = context.ensureJsFile(workingCopy);

List<JsClass> jsClasses = jsFile.getJsFuncClasses();
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
if (alias.equalsIgnoreCase(clzz.getName())) {
alias = StringUtility.uppercaseFirst(clzz.getSuperCall().getNamespace()) + clzz.getSuperCall().getName();
}
classBuilder.append("extends ")
.append(jsFile.getOrCreateImport(clzz.getSuperCall().getFullyQualifiedName(), context).getReferenceName())
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
*/
}
