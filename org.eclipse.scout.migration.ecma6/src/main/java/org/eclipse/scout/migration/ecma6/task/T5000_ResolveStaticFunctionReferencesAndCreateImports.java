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
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement.Type;
import org.eclipse.scout.migration.ecma6.model.old.JsClass;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.old.JsFunction;
import org.eclipse.scout.rt.platform.Order;

@Order(5000)
public class T5000_ResolveStaticFunctionReferencesAndCreateImports extends AbstractResolveReferencesAndCreateImportTask {

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());

    String source = workingCopy.getSource();
    JsFile jsFile = context.ensureJsFile(workingCopy);

    source = updateLocalReferences(jsFile, source, context, workingCopy.getLineDelimiter());
    source = updateForeignReferences(jsFile, source, context);

    workingCopy.setSource(source);
  }

  protected String updateLocalReferences(JsFile jsFile, String source, Context context, String lineDelimiter) {
    List<JsClass> jsClasses = jsFile.getJsClasses();
    List<JsFunction> staticFunctions = jsClasses
        .stream()
        .flatMap(jsClass -> jsClass.getFunctions()
            .stream()
            .filter(JsFunction::isStatic))
        .collect(Collectors.toList());
    if (staticFunctions.size() == 0) {
      return source;
    }

    for (JsFunction fun : staticFunctions) {
      JsClass jsClass = fun.getJsClass();
      source = createImportForReferences(fun.getFqn(), null, jsClass.getName() + "." + fun.getName(), source, jsFile, context);
      List<String> singletonRefs = fun.getSingletonReferences();
      if (singletonRefs != null && singletonRefs.size() > 0) {
        for (String singletonRef : singletonRefs) {
          source = createImportForReferences(singletonRef, jsClass.getFullyQualifiedName(), jsClass.getName() + "." + fun.getName() + "()", source, jsFile, context);
        }
      }
    }
    return source;
  }

  protected String updateForeignReferences(JsFile jsFile, String source, Context context) {
    Set<String> currentClassesFqn = jsFile.getJsClasses().stream().map(JsClass::getFullyQualifiedName).collect(Collectors.toSet());

    List<INamedElement> staticFunctions = context.getApi().getElements(Type.StaticFunction, fun -> !currentClassesFqn.contains(fun.getParent().getFullyQualifiedName()));
    staticFunctions.addAll(context.getLibraries().getElements(Type.StaticFunction));

    for (INamedElement function : staticFunctions) {
      String replacement = function.getParent().getName() + "." + function.getName();
      source = createImportForReferences(function.getFullyQualifiedName(), function.getAncestor(Type.Class).getFullyQualifiedName(), replacement, source, jsFile, context);
      @SuppressWarnings("unchecked")
      List<String> singletonRefs = (List<String>) function.getCustomAttribute(INamedElement.SINGLETON_REFERENCES);
      if (singletonRefs != null && singletonRefs.size() > 0) {
        for (String singletonRef : singletonRefs) {
          source = createImportForReferences(singletonRef, function.getAncestor(Type.Class).getFullyQualifiedName(), replacement + "()", source, jsFile, context);
        }
      }
    }
    return source;
  }

}
