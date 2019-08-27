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
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement.Type;
import org.eclipse.scout.migration.ecma6.model.old.JsClass;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.rt.platform.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Order(5100)
public class T5100_ResolveReferencesAndCreateImports extends AbstractTask {
  private static final Logger LOG = LoggerFactory.getLogger(T5100_ResolveReferencesAndCreateImports.class);

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

    String source = workingCopy.getSource();
    JsFile jsFile = context.ensureJsFile(workingCopy);

    Set<String> currentClassesFqn = jsFile.getJsClasses().stream().map(c -> c.getFullyQuallifiedName()).collect(Collectors.toSet());

    List<INamedElement> staticFunctions = context.getApi().getElements(Type.StaticFunction, fun -> !currentClassesFqn.contains(fun.getParent().getFullyQuallifiedName()));
    List<INamedElement> constants = context.getApi().getElements(Type.Constant, fun -> !currentClassesFqn.contains(fun.getParent().getFullyQuallifiedName()));
    List<INamedElement> enums = context.getApi().getElements(Type.Enum, fun -> !currentClassesFqn.contains(fun.getParent().getFullyQuallifiedName()));
    List<INamedElement> constructors = context.getApi().getElements(Type.Constructor, fun -> !currentClassesFqn.contains(fun.getParent().getFullyQuallifiedName()));


    for (INamedElement function : staticFunctions) {
      source = createImportForReferences(function, Pattern.quote(function.getFullyQuallifiedName()),function.getParent().getName()+"."+function.getName(), source, jsFile,context);
    }

    for (INamedElement constant : constants) {
      source = createImportForReferences(constant, Pattern.quote(constant.getFullyQuallifiedName()),constant.getParent().getName()+"."+constant.getName(), source, jsFile,context);
    }

    for (INamedElement anEnum : enums) {
      source = createImportForReferences(anEnum, Pattern.quote(anEnum.getFullyQuallifiedName()),anEnum.getParent().getName()+"."+anEnum.getName(), source, jsFile,context);
    }
    // constructor must be last in order to not replace static calls with constructor prefix
    for (INamedElement constructor : constructors) {
      StringBuilder patternBuilder = new StringBuilder();
      patternBuilder.append("new\\s*")
        .append(Pattern.quote(constructor.getFullyQuallifiedName()))
        .append("\\(");
      source = createImportForReferences(constructor, patternBuilder.toString(), "new "+constructor.getParent().getName()+"(", source, jsFile,context);
    }
    workingCopy.setSource(source);
  }

  private String createImportForReferences(INamedElement element,String pattern,String replacement,  String source, JsFile jsFile, Context context) {

    Matcher matcher = Pattern.compile(pattern).matcher(source);

    boolean result = matcher.find();
    if (result) {
      String filename = jsFile.getPath().getFileName().toString();
      JsClass definingClass = context.getJsClass(element.getAncestor(ne -> ne.getType() == Type.Class).getFullyQuallifiedName());
      StringBuffer sb = new StringBuffer();
      // loop over all because of logging reasons
      do {
        matcher.appendReplacement(sb, "TODO:" + replacement);
        LOG.debug("Reference replacement[" + filename + "]: '" + matcher.group() + "' -> '" + replacement + "'");
        result = matcher.find();
      }
      while (result);
      // create import
      LOG.debug("[" + filename + "] Create import for '" + definingClass.getFullyQuallifiedName() + "'.");
      jsFile.getOrCreateImport(definingClass);

      matcher.appendTail(sb);
      source = sb.toString();
    }

    return source;
  }

}
