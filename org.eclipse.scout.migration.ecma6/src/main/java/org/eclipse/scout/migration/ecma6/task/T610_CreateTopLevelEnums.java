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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.old.JsTopLevelEnum;
import org.eclipse.scout.rt.platform.Order;

@Order(610)
public class T610_CreateTopLevelEnums extends AbstractTask {
  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"), PathFilters.isTopLevelEnum());

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    String s = workingCopy.getSource();
    JsFile jsFile = context.ensureJsFile(workingCopy);
    String ln = workingCopy.getLineDelimiter();

    List<String> exportCollector = new ArrayList<>();
    for (JsTopLevelEnum e : jsFile.getJsTopLevelEnums()) {
      s = createEnum(s, e, ln, exportCollector);
    }

    //create default export
    if (exportCollector.size() == 1) {
      String exportedNames = exportCollector
          .stream()
          .collect(Collectors.joining("," + ln));
      s += ln;
      s += "export default ";
      s += exportedNames;
      s += ";";
    }
    else if (exportCollector.size() > 1) {
      String exportedNames = exportCollector
          .stream()
          .map(name -> "  " + name)
          .sorted()
          .collect(Collectors.joining("," + ln));
      s += ln;
      s += "export default {";
      s += ln;
      s += exportedNames;
      s += ln;
      s += "};";
    }

    workingCopy.setSource(s);
  }

  protected String createEnum(String source, JsTopLevelEnum jsEnum, String ln, List<String> exportCollector) {
    StringBuilder patternBuilder = new StringBuilder();
    patternBuilder.append(jsEnum.getNamespace());

    patternBuilder.append("\\.").append(jsEnum.getName());
    patternBuilder.append("(\\ \\=\\s*\\{)");

    Pattern pattern = Pattern.compile(patternBuilder.toString());
    Matcher matcher = pattern.matcher(source);
    if (matcher.find()) {

      StringBuilder replacement = new StringBuilder();
      if (jsEnum.hasParseErrors()) {
        replacement
            .append(jsEnum.toTodoText(ln))
            .append(ln)
            .append(matcher.group());
      }
      else {
        replacement
            .append("const ")
            .append(jsEnum.getName())
            .append(matcher.group(1));
        exportCollector.add(jsEnum.getName());
      }
      source = matcher.replaceFirst(replacement.toString());
    }

    return source;
  }
}
