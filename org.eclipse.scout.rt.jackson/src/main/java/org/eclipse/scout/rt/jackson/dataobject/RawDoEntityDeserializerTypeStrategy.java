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
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

import com.fasterxml.jackson.core.JsonToken;

@Bean
public class RawDoEntityDeserializerTypeStrategy implements IDoEntityDeserializerTypeStrategy {

  @Override
  public Class<? extends IDoEntity> resolveTypeName(String entityType) {
    return null;
  }

  @Override
  public String resolveTypeVersion(Class<? extends IDoEntity> entityClass) {
    return null;
  }

  @Override
  public Optional<AttributeType> resolveAttributeType(Class<? extends IDoEntity> entityClass, String attributeName, JsonToken currentToken) {
    return Optional.empty();
  }

  @Override
  public void putContributions(IDoEntity doEntity, String attributeName, Collection<?> contributions) {
    if (!CollectionUtility.isEmpty(contributions)) {
      doEntity.putList(attributeName, CollectionUtility.arrayList(contributions)); // for raw do entity, handle contributions as regular node (DoList)
    }
  }
}
