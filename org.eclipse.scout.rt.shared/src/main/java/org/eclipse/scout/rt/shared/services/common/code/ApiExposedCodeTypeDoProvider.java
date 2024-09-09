/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.api.data.code.CodeTypeDo;
import org.eclipse.scout.rt.api.data.code.IApiExposedCodeTypeDoProvider;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.services.common.code.mapping.CodeTypeToDoFunction;

public class ApiExposedCodeTypeDoProvider implements IApiExposedCodeTypeDoProvider {

  @Override
  public Set<CodeTypeDo> provide() {
    return provide(null); // all exposed code types
  }

  @Override
  public Set<CodeTypeDo> provide(Set<String> ids) {
    Stream<ICodeType> codeTypeStream = getExposedCodeTypes().stream();
    if (CollectionUtility.hasElements(ids)) {
      Function<Object, String> codeTypeIdConverter = BEANS.get(CodeTypeToDoFunction.class).getIdConverter();
      codeTypeStream = codeTypeStream.filter(codeType -> ids.contains(codeTypeIdConverter.apply(codeType.getId())));
    }
    return codeTypeStream
        .map(ICodeType::toDo)
        .filter(Objects::nonNull)
        .filter(codeType -> codeType.getId() != null) // id is mandatory
        .collect(Collectors.toSet());
  }

  public Set<ICodeType> getExposedCodeTypes() {
    Set<ICodeType> codeTypes = new HashSet<>();
    BEANS.all(IApiExposedCodeTypeContributor.class).forEach(contributor -> contributor.contribute(codeTypes));
    return codeTypes;
  }
}
