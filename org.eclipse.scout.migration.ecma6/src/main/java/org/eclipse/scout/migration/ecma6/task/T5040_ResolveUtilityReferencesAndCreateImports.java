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

import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement.Type;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.rt.platform.Order;

@Order(5040)
public class T5040_ResolveUtilityReferencesAndCreateImports extends AbstractResolveReferencesAndCreateImportTask {

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());

    String source = workingCopy.getSource();
    JsFile jsFile = context.ensureJsFile(workingCopy);

    List<INamedElement> elems = context.getApi().getElements(Type.UtilityFunction);
    elems.addAll(context.getLibraries().getElements(Type.UtilityFunction));
    for (INamedElement e : elems) {
      source = createImportForReferences(e.getFullyQualifiedName(), e.getParent().getFullyQualifiedName(), e.getParent().getName() + "." + e.getName(), source, jsFile, context);
    }

    elems = context.getApi().getElements(Type.UtilityVariable);
    elems.addAll(context.getLibraries().getElements(Type.UtilityVariable));
    for (INamedElement e : elems) {
      source = createImportForReferences(e.getFullyQualifiedName(), e.getParent().getFullyQualifiedName(), e.getParent().getName() + "." + e.getName(), source, jsFile, context);
    }

    workingCopy.setSource(source);
  }
}
