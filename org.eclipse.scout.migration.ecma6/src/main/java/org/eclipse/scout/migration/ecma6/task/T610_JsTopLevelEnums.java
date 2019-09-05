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

import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.old.JsTopLevelEnum;
import org.eclipse.scout.rt.platform.Order;

@Order(610)
public class T610_JsTopLevelEnums extends AbstractTask {
  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"), PathFilters.isTopLevelEnum());

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    String source = workingCopy.getSource();
    JsFile jsFile = context.ensureJsFile(workingCopy);

    for (JsTopLevelEnum e : jsFile.getJsTopLevelEnums()) {
      source = createEnum(source, e, workingCopy.getLineSeparator());
    }

    workingCopy.setSource(source);
  }

  protected String createEnum(String source, JsTopLevelEnum jsEnum, String lineDelimiter) {
    StringBuilder patternBuilder = new StringBuilder();
    patternBuilder.append(jsEnum.getNamespace());

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
        replacement.append("export const ").append(jsEnum.getName())
            .append(matcher.group(1));
      }
      source = matcher.replaceFirst(replacement.toString());
    }

    //replace all state variables
    // scout.keys.codesToKeys = {
    // export let codesToKeys = {
    source = source.replaceAll("" + Pattern.quote(jsEnum.getFqn()) + "\\.(\\w+)(\\s*=)", "export let $1$2");

    return source;
  }
}
