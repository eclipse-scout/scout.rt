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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement.Type;
import org.eclipse.scout.migration.ecma6.model.old.JsClass;
import org.eclipse.scout.migration.ecma6.model.old.JsClassVariable;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.rt.platform.Order;

@Order(5010)
public class T5010_ResolveClassConstantsReferencesAndCreateImports extends AbstractResolveReferencesAndCreateImportTask {

  @SuppressWarnings("DuplicatedCode")
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
    List<JsClassVariable> vars = jsClasses
        .stream()
        .flatMap(jsClass -> jsClass.getVariables().stream())
        .collect(Collectors.toList());
    if (vars.size() == 0) {
      return source;
    }

    for (JsClassVariable v : vars) {
      JsClass jsClass = v.getJsClass();
      source = createImportForReferences(jsClass.getFullyQualifiedName() + "." + v.getName(), null, jsClass.getName() + "." + v.getName(), source, jsFile, context);
    }
    return source;
  }

  protected String updateForeignReferences(JsFile jsFile, String source, Context context) {
    Set<String> currentClassesFqn = jsFile.getJsClasses().stream().map(JsClass::getFullyQualifiedName).collect(Collectors.toSet());

    Collection<INamedElement> constants = context.getApi().getElements(Type.Constant, fun -> !currentClassesFqn.contains(fun.getParent().getFullyQualifiedName()));
    constants.addAll(context.getLibraries().getElements(Type.Constant));

    for (INamedElement constant : constants) {
      String replacement = constant.getParent().getName() + "." + constant.getName();
      source = createImportForReferences(constant.getFullyQualifiedName(), constant.getAncestor(Type.Class).getFullyQualifiedName(), replacement, source, jsFile, context);
    }
    return source;
  }

}
