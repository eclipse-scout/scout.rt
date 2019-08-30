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

import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement.Type;
import org.eclipse.scout.migration.ecma6.model.api.less.LessApiParser;
import org.eclipse.scout.migration.ecma6.model.old.JsClass;
import org.eclipse.scout.migration.ecma6.model.old.JsConstant;
import org.eclipse.scout.migration.ecma6.model.old.JsEnum;
import org.eclipse.scout.migration.ecma6.model.old.JsFunction;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ApiWriter {

  public void writeLibrary(Path libraryFile, String libName, Context context) throws IOException {
    ObjectMapper defaultJacksonObjectMapper = new ObjectMapper()
        .setSerializationInclusion(Include.NON_DEFAULT)
        .enable(SerializationFeature.INDENT_OUTPUT)
        .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    defaultJacksonObjectMapper.writeValue(Files.newBufferedWriter(libraryFile), createLibraryFromCurrentModule(libName, context, true));
  }

  public INamedElement createLibraryFromCurrentModule(String libName, Context context, boolean includeLess) {
    NamedElement lib = new NamedElement(Type.Library, libName);
    List<INamedElement> children = context.getAllJsClasses()
        .stream()
        .map(jsClass -> createClazz(jsClass, lib))
        .collect(Collectors.toList());

    if (includeLess) {
      LessApiParser lessApi = context.getLessApi();
      List<INamedElement> lessMixins = new ArrayList<>(lessApi.getMixins().values());
      List<INamedElement> lessVariables = lessApi
          .getGlobalVariables().values()
          .stream()
          .flatMap(entry -> entry.values().stream())
          .collect(Collectors.toList());
      children.addAll(lessMixins);
      children.addAll(lessVariables);
    }
    lib.setChildren(children);
    return lib;
  }

  protected INamedElement createClazz(JsClass jsClass, INamedElement library) {
    NamedElement cz = new NamedElement(Type.Class, jsClass.getName(), library);
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
    cz.addChildren(jsClass.getConstants()
        .stream()
        .map(cons -> createConstant(cons, cz))
        .collect(Collectors.toList()));
    cz.addChildren(jsClass.getEnums()
        .stream()
        .map(en -> createEnum(en, cz))
        .collect(Collectors.toList()));
    return cz;
  }

  protected INamedElement createConstructor(JsFunction jsFun, INamedElement cz) {
    return new NamedElement(Type.Constructor, jsFun.getName(), cz);
  }

  protected INamedElement createStaticFunction(JsFunction jsFun, INamedElement cz) {
    return new NamedElement(Type.StaticFunction, jsFun.getName(), cz);
  }

  protected INamedElement createFunction(JsFunction jsFun, INamedElement cz) {
    return new NamedElement(Type.Function, jsFun.getName(), cz);
  }

  private INamedElement createConstant(JsConstant cons, NamedElement cz) {
    return new NamedElement(Type.Constant, cons.getName(), cz);
  }

  private INamedElement createEnum(JsEnum en, NamedElement cz) {
    return new NamedElement(Type.Enum, en.getName(), cz);
  }
}
