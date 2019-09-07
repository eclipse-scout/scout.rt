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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.old.JsUtility;
import org.eclipse.scout.migration.ecma6.model.old.JsUtilityFunction;
import org.eclipse.scout.migration.ecma6.model.old.JsUtilityVariable;
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
      String r = rewriteSource(workingCopy, context);
      workingCopy.setSource(r);
    }
    catch (VetoException e) {
      MigrationUtility.prependTodo(workingCopy, e.getMessage());
      LOG.error("Could not create utility [" + pathInfo.getPath().getFileName() + "]. Appended TODO for manual migration.");
    }
  }

  /**
   * <pre>
   *   scout.string = {
   *     foo: function... ,
   *     bar: function...,
   *     var1 : 1,
   *   }
   * </pre>
   */
  protected String rewriteSource(WorkingCopy workingCopy, Context context) {
    String source = workingCopy.getSource();
    JsFile jsFile = context.ensureJsFile(workingCopy);
    String ln = workingCopy.getLineSeparator();

    for (JsUtility util : jsFile.getJsUtilities()) {
      String sourceBefore;
      String s;
      if (util.getStartTag() != null) {
        sourceBefore = util.getSource();
        s = rewriteBlockStyleUtility(util, sourceBefore, ln);
      }
      else {
        sourceBefore = source;
        s = rewritePartStyleUtility(util, sourceBefore, ln);
      }

      //remove all 'this.'
      s = s.replaceAll("(?<!\\w)this\\.", "");

      //remove all self-references
      s = s.replaceAll("(?<!\\w)" + util.getFullyQualifiedName() + "\\.", "");

      //create default export
      String exportedNames = Stream.concat(
          util.getFunctions().stream().filter(f -> f.isExported()).map(f -> "  " + f.getName()),
          util.getVariables().stream().filter(v -> v.isExported()).map(v -> "  " + v.getName()))
          .sorted()
          .collect(Collectors.joining("," + ln));
      s += ln;
      s += "export default {";
      s += ln;
      s += exportedNames;
      s += ln;
      s += "};";

      //apply
      source = source.replace(sourceBefore, s);
    }

    //remove duplicate ';;'
    source = source.replace(";;", ";");

    return source;
  }

  private String rewriteBlockStyleUtility(JsUtility util, String s, String ln) {
    s = s.replace(util.getStartTag(), "");
    s = s.substring(0, s.lastIndexOf("}"));

    for (JsUtilityFunction f : util.getFunctions()) {
      if (f.isExported()) {
        s = replaceFirstNoRegex(s, f.getTag(), "export function " + f.getName());
      }
      else {
        //change and annotate private functions
        s = replaceFirstNoRegex(s, f.getTag(), "//private\"+ln+\"function " + f.getName());
      }
    }

    for (JsUtilityVariable v : util.getVariables()) {
      String prefix;
      if (v.isExported()) {
        prefix = "export ";
      }
      else {
        prefix = "//private" + ln;
      }
      if (v.isConst()) {
        prefix += "const ";
      }
      else {
        prefix += "let ";
      }
      s = replaceFirstNoRegex(s, v.getTag(), prefix + v.getName() + " = " + v.getValueOrFirstLine() + (v.getTag().endsWith(",") ? ";" : ""));
    }

    //remove all ',' after function body '}'
    s = s.replaceAll("(?m)^  \\},", "\\}");

    //reduce indent by 2
    s = s.replaceAll("(?m)^  ", "");

    return s;
  }

  private String rewritePartStyleUtility(JsUtility util, String s, String ln) {
    for (JsUtilityFunction f : util.getFunctions()) {
      if (f.isExported()) {
        s = replaceFirstNoRegex(s, f.getTag(), "export function " + f.getName());
      }
      else {
        //change and annotate private functions
        s = replaceFirstNoRegex(s, f.getTag(), "//private" + ln + "function " + f.getName());
      }
    }

    for (JsUtilityVariable v : util.getVariables()) {
      String prefix;
      if (v.isExported()) {
        prefix = "export ";
      }
      else {
        prefix = "//private" + ln;
      }
      if (v.isConst()) {
        prefix += "const ";
      }
      else {
        prefix += "let ";
      }
      s = replaceFirstNoRegex(s, v.getTag(), prefix + v.getName() + " =");
    }
    return s;
  }

  private static String replaceFirstNoRegex(String s, String oldValue, String newValue) {
    return s.replaceFirst(Pattern.quote(oldValue), Matcher.quoteReplacement(newValue));
  }
}
