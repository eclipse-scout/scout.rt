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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(5030)
public class T5030_ResolveClassConstructorReferencesAndCreateImports extends AbstractResolveReferencesAndCreateImportTask {
  private static final Logger LOG = LoggerFactory.getLogger(T5030_ResolveClassConstructorReferencesAndCreateImports.class);

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
    List<JsFunction> constructors = jsClasses
        .stream()
        .filter(jsClass -> jsClass.getConstructor() != null)
        .map(JsClass::getConstructor)
        .collect(Collectors.toList());
    if (constructors.size() == 0) {
      return source;
    }

    for (JsFunction constructor : constructors) {
      source = createImportForReferences(constructor.getJsClass().getFullyQualifiedName(), null, constructor.getJsClass().getName(), source, jsFile, context);
    }
    return source;
  }

  protected String updateForeignReferences(JsFile jsFile, String source, Context context) {
    Set<String> currentClassesFqn = jsFile.getJsClasses().stream().map(JsClass::getFullyQualifiedName).collect(Collectors.toSet());

    List<INamedElement> constructors = context.getApi().getElements(Type.Constructor, fun -> !currentClassesFqn.contains(fun.getAncestor(Type.Class).getFullyQualifiedName()));
    constructors.addAll(context.getLibraries().getElements(Type.Constructor));

    for (INamedElement constructor : constructors) {
      String replacement = constructor.getAncestor(Type.Class).getName();
      source = createImportForReferences(constructor.getAncestor(Type.Class).getFullyQualifiedName(), constructor.getAncestor(Type.Class).getFullyQualifiedName(), replacement, source, jsFile, context);
    }
    return source;
  }

}
