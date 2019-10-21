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
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.Configuration;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;

@Order(5500)
public class T5500_JasmineSpecs extends AbstractTask {
  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcTestJs(), PathFilters.withExtension("js"));
  private static final Pattern SCOUT_TESTING_IMP_PAT = Pattern.compile("} from '[./]+main/js/index';");

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return m_filter.test(pathInfo);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    BEANS.get(T5000_ResolveStaticFunctionReferencesAndCreateImports.class).process(pathInfo, context);
    BEANS.get(T5010_ResolveClassConstantsReferencesAndCreateImports.class).process(pathInfo, context);
    BEANS.get(T5020_ResolveClassEnumReferencesAndCreateImports.class).process(pathInfo, context);
    BEANS.get(T5030_ResolveClassConstructorReferencesAndCreateImports.class).process(pathInfo, context);
    BEANS.get(T5040_ResolveUtilityReferencesAndCreateImports.class).process(pathInfo, context);
    BEANS.get(T5050_ResolveTopLevelEnumReferencesAndCreateImports.class).process(pathInfo, context);
    WorkingCopy workingCopy = context.getWorkingCopy(pathInfo.getPath());

    // specs are migrated in the xyz.test maven module
    // but in the end they are moved into the same module as the non-test sources
    // therefore the import must be changed from an external module to the local modules index.js path.

    String curPath = pathInfo.getPath().toString().replace('\\', '/');
    Path newSpecDestination = Paths.get(curPath.replace("src/test/js/scout", "test")).getParent();
    Path locationOfIndex = Configuration.get().getSourceModuleDirectory().resolve("src");
    String fromSpecToIndex = newSpecDestination.relativize(locationOfIndex).toString().replace('\\', '/');
    String targetModuleName = Configuration.get().getPersistLibraryName().replace("/testing", "/core");

    String source = workingCopy.getSource();
    String newSource = source.replace("} from '" + targetModuleName + "';", "} from '" + fromSpecToIndex + "/index';");
    if ("scout".equals(Configuration.get().getNamespace())) {
      newSource = SCOUT_TESTING_IMP_PAT.matcher(newSource).replaceAll("} from '@eclipse-scout/testing';");
    }
    workingCopy.setSource(newSource);
  }
}
