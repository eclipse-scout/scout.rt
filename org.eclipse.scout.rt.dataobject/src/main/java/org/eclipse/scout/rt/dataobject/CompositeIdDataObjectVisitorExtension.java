/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.dataobject.id.ICompositeId;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IIds;
import org.eclipse.scout.rt.dataobject.id.IRootId;
import org.eclipse.scout.rt.dataobject.id.IdCodec;
import org.eclipse.scout.rt.dataobject.id.IdCodec.IIdCodecFlag;
import org.eclipse.scout.rt.platform.exception.PlatformException;

/**
 * Visitor extension for {@link ICompositeId}.
 */
public class CompositeIdDataObjectVisitorExtension extends AbstractDataObjectVisitorExtension<ICompositeId> {

  @Override
  public void visit(ICompositeId value, Consumer<Object> chain) {
    value.unwrap().forEach(chain);
  }

  @Override
  public ICompositeId replaceOrVisit(ICompositeId value, UnaryOperator<Object> chain) {
    List<IId> replacedComponents = value.unwrap().stream()
        .map(chain)
        .map(IId.class::cast)
        .collect(Collectors.toList());

    if (replacedComponents.equals(value.unwrap())) {
      return value;
    }

    // create a new instance of the composite ID with the replaced unwrapped components
    List<Object> unwrappedComponents = new ArrayList<>();
    replacedComponents.forEach(o -> unwrap(o, unwrappedComponents));
    return IIds.create(value.getClass(), unwrappedComponents.toArray(new Object[0]));
  }

  /**
   * Similar as in {@link IdCodec#toUnqualified(IId, IIdCodecFlag...)}.
   */
  protected void unwrap(IId component, List<Object> unwrappedComponents) {
    if (component instanceof IRootId) {
      unwrappedComponents.add(component.unwrap());
    }
    else if (component instanceof ICompositeId) {
      ((ICompositeId) component).unwrap().forEach(id -> unwrap(id, unwrappedComponents));
    }
    else {
      handleUnknownIdTypeUnwrap(component, unwrappedComponents);
    }
  }

  protected void handleUnknownIdTypeUnwrap(IId id, List<Object> unwrappedComponents) {
    throw new PlatformException("Unsupported id type {}, cannot unwrap id {}", id.getClass(), id);
  }
}
