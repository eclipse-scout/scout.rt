/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.Serial;

import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.AnnotatedClass;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * Jackson {@link AnnotationIntrospector} implementation adding type resolver for all {@link IDataObject} data object
 * instances.
 */
@Bean
public class DataObjectAnnotationIntrospector extends JacksonAnnotationIntrospector {

  @Serial
  private static final long serialVersionUID = 1L;

  protected ScoutDataObjectModuleContext m_moduleContext;

  public DataObjectAnnotationIntrospector withModuleContext(ScoutDataObjectModuleContext moduleContext) {
    m_moduleContext = moduleContext;
    return this;
  }

  @Override
  public JsonTypeInfo.Value findPolymorphicTypeInfo(MapperConfig<?> config, Annotated ann) {
    if (ann instanceof AnnotatedClass ac && IDataObject.class.isAssignableFrom(ac.getRawType())) {
      if (m_moduleContext.isSuppressTypeAttribute()) {
        return JsonTypeInfo.Value.construct(
            JsonTypeInfo.Id.NONE,
            JsonTypeInfo.As.NOTHING,
            null,
            null,
            false,
            null,
            null);
      }

      return JsonTypeInfo.Value.construct(
          JsonTypeInfo.Id.NAME,
          JsonTypeInfo.As.PROPERTY,
          m_moduleContext.getTypeAttributeName(),
          null,
          false,
          null,
          null);
    }

    return super.findPolymorphicTypeInfo(config, ann);
  }

  @Override
  public Object findTypeResolverBuilder(MapperConfig<?> config, Annotated ann) {
    if (ann instanceof AnnotatedClass ac && IDataObject.class.isAssignableFrom(ac.getRawType())) {
      return BEANS.get(DataObjectTypeResolverBuilder.class);
    }

    return super.findTypeResolverBuilder(config, ann);
  }

  @Override
  public Object findTypeIdResolver(MapperConfig<?> config, Annotated ann) {
    if (ann instanceof AnnotatedClass ac && IDataObject.class.isAssignableFrom(ac.getRawType())) {
      return DataObjectTypeIdResolver.class;
    }

    return super.findTypeIdResolver(config, ann);
  }
}
