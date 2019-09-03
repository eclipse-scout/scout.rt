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

import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
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
      rewriteSource(workingCopy, context);
    }
    catch (VetoException e) {
      MigrationUtility.prependTodo(workingCopy, e.getMessage());
      LOG.error("Could not create utility [" + pathInfo.getPath().getFileName() + "]. Appended TODO for manual migration.");
    }
  }

  protected void rewriteSource(WorkingCopy workingCopy, Context context) {
    String source = workingCopy.getSource();

    Matcher m = Pattern.compile("(?m)^([a-z]\\w+)\\.([a-z]\\w+)\\s*=\\s*\\{").matcher(source);
    if (!m.find()) throw new VetoException("This is no utility class");
    String namespace = m.group(1);
    String utilityName = m.group(2);
    if (m.find()) throw new VetoException("There are multiple utility classes in one file");

    JsFile jsFile = context.ensureJsFile(workingCopy);

    //remove wrapper block and replace by an import
    //XXX source = source.replaceAll("(?m)^([a-z]\\w+)\\.[a-z]\\w+\\s*=\\s*\\{", "import * as $1 from '../$1';");
    source = source.replaceAll("(?m)^([a-z]\\w+)\\.[a-z]\\w+\\s*=\\s*\\{", "");
    source = source.substring(0, source.lastIndexOf("}"));

    //change and export public functions
    source = source.replaceAll("(?m)^[ ]*([a-z]\\w*)\\s*:\\s*function", "export function $1");

    //change and annotate private functions
    source = source.replaceAll("(?m)^[ ]*([_]\\w*)\\s*:\\s*function", "//private\nfunction $1");

    //remove all ',' after function body '}'
    source = source.replaceAll("(?m)^  \\},", "\\}");

    //change state variables
    source = source.replaceAll("(?m)^[ ]*([_\\w]+)\\s*:\\s*([^,]+),?$", "let $1 = $2;");

    //clean all references to utility functions
    source = source.replace(namespace + "." + utilityName + ".", "");
    source = source.replaceAll("(?<!\\w)this\\.", "");

    //reduce indent by 2
    source = source.replaceAll("(?m)^  ", "");

    //create default export
    Set<String> names = new TreeSet<>();
    m = Pattern.compile("export function (\\w+)").matcher(source);
    while (m.find()) {
      names.add(m.group(1));
    }
    source += workingCopy.getLineSeparator();
    source += "export default {";
    source += "\n";
    source += names.stream().map(s -> "  " + s).collect(Collectors.joining(",\n"));
    source += "\n";
    source += "};";

    workingCopy.setSource(source);
  }
}
