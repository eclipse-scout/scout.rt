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
package org.eclipse.scout.migration.ecma6.model.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement.Type;
import org.eclipse.scout.migration.ecma6.model.old.FrameworkExtensionMarker;
import org.eclipse.scout.migration.ecma6.model.old.JsClass;
import org.eclipse.scout.migration.ecma6.model.old.JsClassVariable;
import org.eclipse.scout.migration.ecma6.model.old.JsEnum;
import org.eclipse.scout.migration.ecma6.model.old.JsFunction;
import org.eclipse.scout.migration.ecma6.model.old.JsTopLevelEnum;
import org.eclipse.scout.migration.ecma6.model.old.JsUtility;
import org.eclipse.scout.migration.ecma6.model.old.JsUtilityFunction;
import org.eclipse.scout.migration.ecma6.model.old.JsUtilityVariable;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ApiWriter {
  private static final ObjectMapper jacksonObjectMapper =new ObjectMapper()
    .setSerializationInclusion(Include.NON_DEFAULT)
    .enable(SerializationFeature.INDENT_OUTPUT)
    .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
    .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

  public void writeLibrary(Path libraryFile, String libName, Context context) throws IOException {
    writeLibrary(libraryFile,  createLibraryFromCurrentModule(libName, context));
  }

  public static void writeLibrary(Path libFile, INamedElement library ) throws IOException {
    jacksonObjectMapper.writeValue(Files.newBufferedWriter(libFile), library);
  }

  public INamedElement createLibraryFromCurrentModule(String libName, Context context) {
    NamedElement lib = context.getLibraries().getChildren().stream()
        .filter(l -> ObjectUtility.equals(l.getCustomAttribute(INamedElement.LIBRARY_MODULE_NAME), libName))
        .findFirst().map(l -> (NamedElement) l).orElse(null);
    if (lib == null) {
      lib = new NamedElement(Type.Library, Configuration.get().getNamespace());
      lib.addCustomAttribute(INamedElement.LIBRARY_MODULE_NAME, libName);
    }
    return fillLibraryFromCurrentModule(lib, context);
  }

  @FrameworkExtensionMarker
  public INamedElement fillLibraryFromCurrentModule(NamedElement lib, Context context) {
    List<INamedElement> allElements = new ArrayList<>();
    allElements.addAll(context
        .getAllJsClasses()
        .stream()
        .map(jsClass -> createClazz(jsClass, lib))
        .collect(Collectors.toList()));

    allElements.addAll(context
        .getAllJsUtilities()
        .stream()
        .map(jsUtil -> createUtility(jsUtil, lib))
        .collect(Collectors.toList()));

    allElements.addAll(context
        .getAllTopLevelEnums()
        .stream()
        .map(jsEnum -> createTopLevelEnum(jsEnum, lib))
        .collect(Collectors.toList()));
    lib.addChildren(allElements);
    return lib;
  }

  protected INamedElement createClazz(JsClass jsClass, INamedElement library) {
    NamedElement cz = new NamedElement(Type.Class, jsClass.getName(), jsClass.getFullyQualifiedName(), library);
    cz.addChildren(jsClass.getFunctions()
        .stream()
        .filter(JsFunction::isConstructor)
        .map(jsFun -> createConstructor(jsFun, cz))
        .collect(Collectors.toList()));
    cz.addChildren(jsClass.getFunctions()
        .stream()
        .filter(jsFun -> !jsFun.isConstructor() && jsFun.isStatic())
        .map(jsFun -> createStaticFunction(jsFun, cz))
        .collect(Collectors.toList()));

    cz.addChildren(jsClass.getFunctions()
        .stream()
        .filter(jsFun -> !jsFun.isConstructor() && !jsFun.isStatic())
        .map(jsFun -> createFunction(jsFun, cz))
        .collect(Collectors.toList()));
    cz.addChildren(jsClass.getVariables()
        .stream()
        .filter(JsClassVariable::isConst)
        .map(v -> createConstant(v, cz))
        .collect(Collectors.toList()));
    cz.addChildren(jsClass.getEnums()
        .stream()
        .map(en -> createEnum(en, cz))
        .collect(Collectors.toList()));
    return cz;
  }

  protected INamedElement createConstructor(JsFunction jsFun, INamedElement cz) {
    NamedElement fun = new NamedElement(Type.Constructor, jsFun.getName(), cz);
    List<String> singletonReferences = jsFun.getSingletonReferences();
    if (singletonReferences.size() > 0) {
      fun.addCustomAttribute(INamedElement.SINGLETON_REFERENCES, singletonReferences);
    }
    return fun;
  }

  protected INamedElement createStaticFunction(JsFunction jsFun, INamedElement cz) {
    NamedElement fun = new NamedElement(Type.StaticFunction, jsFun.getName(), cz);
    List<String> singletonReferences = jsFun.getSingletonReferences();
    if (singletonReferences.size() > 0) {
      fun.addCustomAttribute(INamedElement.SINGLETON_REFERENCES, singletonReferences);
    }
    return fun;
  }

  protected INamedElement createFunction(JsFunction jsFun, INamedElement cz) {
    NamedElement fun = new NamedElement(Type.Function, jsFun.getName(), cz);
    List<String> singletonReferences = jsFun.getSingletonReferences();
    if (singletonReferences.size() > 0) {
      fun.addCustomAttribute(INamedElement.SINGLETON_REFERENCES, singletonReferences);
    }
    return fun;
  }

  private INamedElement createConstant(JsClassVariable v, NamedElement cz) {
    return new NamedElement(Type.Constant, v.getName(), cz);
  }

  private INamedElement createEnum(JsEnum en, NamedElement cz) {
    return new NamedElement(Type.Enum, en.getName(), cz);
  }

  @FrameworkExtensionMarker
  protected INamedElement createUtility(JsUtility jsUtility, INamedElement library) {
    NamedElement u = new NamedElement(Type.Utility, jsUtility.getName(), jsUtility.getFullyQualifiedName(), library);
    u.addChildren(jsUtility.getFunctions()
        .stream()
        .map(jsFun -> createUtilityFunction(jsFun, u))
        .collect(Collectors.toList()));
    u.addChildren(jsUtility.getVariables()
        .stream()
        .map(jsVar -> createUtilityVariable(jsVar, u))
        .collect(Collectors.toList()));
    return u;
  }

  protected INamedElement createUtilityFunction(JsUtilityFunction jsFun, INamedElement u) {
    NamedElement fun = new NamedElement(Type.UtilityFunction, jsFun.getName(), u);
    return fun;
  }

  protected INamedElement createUtilityVariable(JsUtilityVariable jsVar, INamedElement u) {
    NamedElement fun = new NamedElement(Type.UtilityVariable, jsVar.getName(), u);
    return fun;
  }

  private INamedElement createTopLevelEnum(JsTopLevelEnum en, INamedElement lib) {
    return new NamedElement(Type.TopLevelEnum, en.getName(), en.getFqn(), lib);
  }
}
