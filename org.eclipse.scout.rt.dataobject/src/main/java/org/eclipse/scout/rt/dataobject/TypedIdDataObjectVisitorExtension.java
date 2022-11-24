/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.TypedId;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

/**
 * Visitor extension for {@link TypedId}.
 */
public class TypedIdDataObjectVisitorExtension extends AbstractDataObjectVisitorExtension<TypedId> {

  @Override
  public void visit(TypedId value, Consumer<Object> chain) {
    chain.accept(value.getId());
  }

  @Override
  public TypedId replaceOrVisit(TypedId value, UnaryOperator<Object> chain) {
    IId id = (IId) chain.apply(value.getId());
    if (ObjectUtility.equals(value.getId(), id)) { // okay because IId is immutable
      return value; // no change
    }
    return TypedId.of(id);
  }
}
