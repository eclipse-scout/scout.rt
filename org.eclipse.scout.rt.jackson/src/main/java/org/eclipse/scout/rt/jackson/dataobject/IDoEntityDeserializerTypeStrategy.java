/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
