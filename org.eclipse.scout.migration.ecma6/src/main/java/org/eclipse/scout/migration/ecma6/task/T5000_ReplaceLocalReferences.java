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
import org.eclipse.scout.migration.ecma6.model.old.JsConstant;
import org.eclipse.scout.migration.ecma6.model.old.JsEnum;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.rt.platform.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(5000)
public class T5000_ReplaceLocalReferences extends AbstractTask {
  private static final Logger LOG = LoggerFactory.getLogger(T5000_ReplaceLocalReferences.class);

  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"));

  @Override
  public void setup(Context context) {
  }

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    JsFile jsFile = context.ensureJsFile(workingCopy);

    List<JsClass> jsClasses = jsFile.getJsClasses();
    if (jsClasses.size() > 1) {
      MigrationUtility.prependTodo(workingCopy, "Replace local references (static function calls, enums, constants).");
    }
    if (jsClasses.size() != 1) {
      // log is done in parsing
      return;
    }
    JsClass jsClass = jsClasses.get(0);
    String source = workingCopy.getSource();
    for (JsConstant constant : jsClass.getConstants()) {
      String pattern = Pattern.quote(constant.getFqn()) + "([^a-zA-Z0-9]{1})";
      String replacement = constant.getName() + "$1";
      source = replaceReferences1(pattern, replacement, source, pathInfo.getPath().getFileName().toString());
    }
    for (JsEnum jsEnum : jsClass.getEnums()) {
      String pattern = Pattern.quote(jsEnum.getFqn()) + "([^a-zA-Z0-9]{1})";
      String replacement = jsEnum.getName() + "$1";
      source = replaceReferences1(pattern, replacement, source, pathInfo.getPath().getFileName().toString());
    }
    workingCopy.setSource(source);
  }

  protected String replaceReferences1(String regex, String replacement, String source, String fileName) {
    Matcher matcher = Pattern.compile(regex).matcher(source);
    boolean result = matcher.find();
    if (result) {
      StringBuffer sb = new StringBuffer();
      // loop over all because of logging reasons
      do {
        matcher.appendReplacement(sb, replacement);
        LOG.debug("Local reference replacement[" + fileName + "]: '" + matcher.group() + "' -> '" + replacement + "'");
        result = matcher.find();
      }
      while (result);
      matcher.appendTail(sb);
      source = sb.toString();
    }
    return source;
  }
}
