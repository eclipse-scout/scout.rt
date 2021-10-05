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
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

import com.fasterxml.jackson.databind.JavaType;

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
  public Optional<JavaType> resolveAttributeType(Class<? extends IDoEntity> entityClass, String attributeName) {
    return Optional.empty();
  }

  @Override
  public void putContributions(IDoEntity doEntity, String attributeName, Collection<?> contributions) {
    if (!CollectionUtility.isEmpty(contributions)) {
      doEntity.putList(attributeName, CollectionUtility.arrayList(contributions)); // for raw do entity, handle contributions as regular node (DoList)
    }
  }
}
