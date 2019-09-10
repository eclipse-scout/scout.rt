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

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.rt.platform.Order;

@Order(25000)
public class T25000_ModelsGetModelToImport extends AbstractTask {

  @SuppressWarnings("unchecked")
  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"));

  // use the "new" pattern (without scout) because the utility migration has already changed the static utility from scout.models to models.
  private final Pattern GET_MODEL_PAT = Pattern.compile("models\\.getModel\\('([\\w.]+)'");
  private final Pattern EXTEND_MODEL_PAT = Pattern.compile("models\\.extend\\('([\\w.]+)',");

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    JsFile jsFile = context.ensureJsFile(workingCopy);
    String source = workingCopy.getSource();

    String step1 = replace(GET_MODEL_PAT, source, (m, r) -> insertModelFunctionCall(m, r, pathInfo, jsFile));
    String step2 = replace(EXTEND_MODEL_PAT, step1, (m, r) -> insertExtendFunctionCall(m, r, pathInfo, jsFile));

    workingCopy.setSource(step2);
  }

  protected void insertModelFunctionCall(Matcher matcher, StringBuilder result, PathInfo pathInfo, JsFile jsFile) {
    String importRef = createImportFor(getJsModelPath(matcher.group(1), pathInfo), jsFile);
    result.append("models.get(").append(importRef);
  }

  protected void insertExtendFunctionCall(Matcher matcher, StringBuilder result, PathInfo pathInfo, JsFile jsFile) {
    String importRef = createImportFor(getJsModelPath(matcher.group(1), pathInfo), jsFile);
    result.append("models.extend(").append(importRef).append("(this),");
  }

  protected Path getJsModelPath(String modelKey, PathInfo pathInfo) {
    String modelFileName = legacyJsonModelNameToJsModuleName(modelKey);
    return pathInfo.getPath().getParent().resolve(modelFileName);
  }

  protected String legacyJsonModelNameToJsModuleName(String modelKey) {
    int namespaceEnd = modelKey.lastIndexOf('.');
    if (namespaceEnd >= 0) {
      modelKey = modelKey.substring(namespaceEnd + 1);
    }
    return modelKey + T30000_JsonToJsModule.JSON_MODEL_NAME_SUFFIX + ".js";
  }

  public static String replace(Pattern pat, CharSequence inputString, BiConsumer<Matcher, StringBuilder> replacementFunction) {
    Matcher matcher = pat.matcher(inputString);
    StringBuilder result = new StringBuilder(inputString.length() * 2);
    int lastPos = 0;
    while (matcher.find()) {
      result.append(inputString, lastPos, matcher.start());
      replacementFunction.accept(matcher, result);
      lastPos = matcher.end();
    }
    result.append(inputString, lastPos, inputString.length());
    return result.toString();
  }

  protected String createImportFor(Path jsModelFile, JsFile jsFile) {
    String fileName = jsModelFile.getFileName().toString();
    if (fileName.endsWith(".js")) {
      fileName = fileName.substring(0, fileName.length() - 3);
    }

    return jsFile.getOrCreateImport(fileName, jsModelFile, false, true).getReferenceName();
  }
}
