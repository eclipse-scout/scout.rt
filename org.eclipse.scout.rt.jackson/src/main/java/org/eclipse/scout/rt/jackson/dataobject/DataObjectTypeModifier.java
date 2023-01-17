/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.lang.reflect.Type;

import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.platform.Bean;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;

/**
 * Type modifier used to add contained type information for {@link DoValue} reference type.
 */
@Bean
public class DataObjectTypeModifier extends TypeModifier {

  protected ScoutDataObjectModuleContext m_moduleContext;

  public DataObjectTypeModifier withModuleContext(ScoutDataObjectModuleContext moduleContext) {
    m_moduleContext = moduleContext;
    return this;
  }

  @Override
  public JavaType modifyType(JavaType type, Type jdkType, TypeBindings bindings, TypeFactory typeFactory) {
    if (type.isReferenceType() || type.isContainerType()) {
      return type;
    }
    // Upgrade simple type DoValue to a reference type by adding contained type
    if (type.getRawClass() == DoValue.class) {
      return ReferenceType.upgradeFrom(type, type.containedTypeOrUnknown(0));
    }
    return type;
  }
}
