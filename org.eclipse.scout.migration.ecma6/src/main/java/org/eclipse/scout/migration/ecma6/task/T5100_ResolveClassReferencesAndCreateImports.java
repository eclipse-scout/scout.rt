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
import org.eclipse.scout.migration.ecma6.model.old.JsTopLevelEnum;
import org.eclipse.scout.rt.platform.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(5100)
public class T5100_ResolveClassReferencesAndCreateImports extends AbstractTask {
  private static final Logger LOG = LoggerFactory.getLogger(T5100_ResolveClassReferencesAndCreateImports.class);

  @SuppressWarnings("unchecked")
  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"));


  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());

    String source = workingCopy.getSource();
    JsFile jsFile = context.ensureJsFile(workingCopy);

    Set<String> currentClassesFqn = jsFile.getJsClasses().stream().map(JsClass::getFullyQualifiedName).collect(Collectors.toSet());

    List<? extends INamedElement> staticFunctions = context.getApi().getElements(Type.StaticFunction, fun -> !currentClassesFqn.contains(fun.getParent().getFullyQualifiedName()));
    List<? extends INamedElement> constants = context.getApi().getElements(Type.Constant, fun -> !currentClassesFqn.contains(fun.getParent().getFullyQualifiedName()));
    List<? extends INamedElement> enums = context.getApi().getElements(Type.Enum, fun -> !currentClassesFqn.contains(fun.getParent().getFullyQualifiedName()));
    List<? extends INamedElement> constructors = context.getApi().getElements(Type.Constructor, fun -> !currentClassesFqn.contains(fun.getParent().getFullyQualifiedName()));

    for (INamedElement function : staticFunctions) {
      source = createImportForReferences(function, Pattern.quote(function.getFullyQualifiedName())+"([^\\']{1})", function.getParent().getName() + "." + function.getName()+"$1", source, jsFile, context);
      List<String> singletonRefs = (List<String>) function.getCustomAttribute(INamedElement.SINGLETON_REFERENCES);
      if(singletonRefs != null && singletonRefs.size() > 0){
        for (String singletonRef : singletonRefs) {
          source = createImportForReferences(function, Pattern.quote(singletonRef)+"([^\\']{1})", function.getParent().getName() + "." + function.getName()+"()$1", source, jsFile, context);
        }
      }
    }

    for (INamedElement constant : constants) {
      source = createImportForReferences(constant, Pattern.quote(constant.getFullyQualifiedName())+"([^\\']{1})", constant.getParent().getName() + "." + constant.getName()+"$1", source, jsFile, context);
    }

    for (INamedElement anEnum : enums) {
      source = createImportForReferences(anEnum, Pattern.quote(anEnum.getFullyQualifiedName())+"([^\\']{1})", anEnum.getParent().getName() + "." + anEnum.getName()+"$1", source, jsFile, context);
    }
    // constructor must be last in order to not replace static calls with constructor prefix
    for (INamedElement constructor : constructors) {
      //noinspection StringBufferReplaceableByString
      StringBuilder patternBuilder = new StringBuilder();
      patternBuilder.append("new\\s*")
          .append(Pattern.quote(constructor.getFullyQualifiedName()))
          .append("\\(");
      source = createImportForReferences(constructor, patternBuilder.toString(), "new " + constructor.getParent().getName() + "(", source, jsFile, context);
    }
    workingCopy.setSource(source);
  }

  private String createImportForReferences(INamedElement element, String pattern, String replacement, String source, JsFile jsFile, Context context) {

    Matcher matcher = Pattern.compile(pattern).matcher(source);

    boolean result = matcher.find();
    if (result) {
      String filename = jsFile.getPath().getFileName().toString();
      String fqn = element.getAncestor(ne -> ne.getType() == Type.Class || ne.getType() == Type.TopLevelEnum).getFullyQualifiedName();
      StringBuffer sb = new StringBuffer();
      // loop over all because of logging reasons
      do {
        matcher.appendReplacement(sb, replacement);
        LOG.debug("Reference replacement[" + filename + "]: '" + matcher.group() + "' -> '" + replacement + "'");
        result = matcher.find();
      }
      while (result);
      // create import
      LOG.debug("[" + filename + "] Create import for '" + fqn + "'.");
      JsTopLevelEnum definingEnum = context.getJsTopLevelEnum(fqn);
      JsClass definingClass = context.getJsClass(fqn);
      if (definingEnum != null) {
        jsFile.getOrCreateImport(definingEnum.getName(), definingEnum.getJsFile().getPath(), true);
      }
      else {
        jsFile.getOrCreateImport(definingClass);
      }

      matcher.appendTail(sb);
      source = sb.toString();
    }

    return source;
  }

}
