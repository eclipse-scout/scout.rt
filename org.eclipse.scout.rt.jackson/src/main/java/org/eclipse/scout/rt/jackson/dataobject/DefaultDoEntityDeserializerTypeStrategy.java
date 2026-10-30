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

import java.util.Collection;
import java.util.Optional;

import org.eclipse.scout.rt.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.LazyValue;

import tools.jackson.core.JsonToken;
import tools.jackson.databind.DatabindContext;

@Bean
public class DefaultDoEntityDeserializerTypeStrategy implements IDoEntityDeserializerTypeStrategy {

  protected final LazyValue<DataObjectInventory> m_dataObjectInventory = new LazyValue<>(DataObjectInventory.class);

  @Override
  public Class<? extends IDoEntity> resolveTypeName(String entityType) {
    return m_dataObjectInventory.get().fromTypeName(entityType);
  }

  @Override
  public String resolveTypeVersion(Class<? extends IDoEntity> entityClass) {
    NamespaceVersion typeVersion = m_dataObjectInventory.get().getTypeVersion(entityClass);
    return typeVersion == null ? null : typeVersion.unwrap();
  }

  @Override
  public Optional<AttributeType> resolveAttributeType(Class<? extends IDoEntity> entityClass, String attributeName, JsonToken currentToken, DatabindContext ctxt) {
    return m_dataObjectInventory.get().getAttributeDescription(entityClass, attributeName)
        .map(a -> TypeFactoryUtility.toAttributeType(a.getType(), currentToken, ctxt))
        .filter(AttributeType::isKnown);
  }

  @Override
  public void putContributions(IDoEntity doEntity, String attributeName, Collection<?> contributions) {
    if (CollectionUtility.isEmpty(contributions)) {
      return;
    }

    if (doEntity.getClass() == DoEntity.class) {
      // doEntity itself has an unknown type name -> assume that contributions might be unknown too and don't put in typed contributions list,
      // otherwise serialization might fail due to a ClassCastException in DoEntitySerializer#validateContributions because IDoEntity (unknown contribution) is cast to IDoEntityContribution.
      doEntity.putList(attributeName, CollectionUtility.arrayList(contributions));
    }
    else {
      // add contributions to corresponding list in do entity
      //noinspection deprecation,unchecked
      doEntity.getAllContributions()
          .addAll((Collection<? extends IDoEntity>) contributions);
    }
  }
}
