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

import java.util.Collection;
import java.util.Optional;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

import com.fasterxml.jackson.core.JsonToken;

/**
 * Type resolver used for type resolution by {@link DoEntityDeserializer}.
 */
public interface IDoEntityDeserializerTypeStrategy {

  /**
   * Resolves {@link IDoEntity} class for given {@code entityType}
   *
   * @see TypeName
   */
  Class<? extends IDoEntity> resolveTypeName(String entityType);

  /**
   * Resolves version for given {@code entityClass}
   *
   * @see TypeVersion
   */
  String resolveTypeVersion(Class<? extends IDoEntity> entityClass);

  /**
   * Resolves attribute type class for given {@code entityClass}, {@code attributeName} and the {@code currentToken}.
   */
  Optional<AttributeType> resolveAttributeType(Class<? extends IDoEntity> entityClass, String attributeName, JsonToken currentToken);

  /**
   * Adds the contributions to the given DO entity.
   */
  void putContributions(IDoEntity doEntity, String attributeName, Collection<?> contributions);
}
