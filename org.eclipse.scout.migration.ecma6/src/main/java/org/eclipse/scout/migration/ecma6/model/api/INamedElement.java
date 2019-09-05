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

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = NamedElement.class)
public interface INamedElement {

  String LIBRARY_MODULE_NAME = "lib-moduleName";
  String SINGLETON_REFERENCES = "singletonReferences";

  enum Type {
    AllLibraries,
    Library,
    Class,
    Utility,
    UtilityFunction,
    UtilityVariable,
    Constructor,
    StaticFunction,
    Function,
    Enum,
    TopLevelEnum,
    Constant,
    LessMixin,
    LessVariable;
  }

  Type getType();

  String getName();

  String getFullyQualifiedName();

  Map<String, Object> getCustomAttributes();

  Object getCustomAttribute(String key);

  String getCustomAttributeString(String key);

  INamedElement getParent();

  INamedElement getAncestor(Predicate<INamedElement> filter);

  void setParent(INamedElement parent);

  List<INamedElement> getChildren();

  void visit(INamedElementVisitor visitor);

  List<INamedElement> getElements(INamedElement.Type type);

  List<INamedElement> getElements(INamedElement.Type type, Predicate<INamedElement> filter);

}
