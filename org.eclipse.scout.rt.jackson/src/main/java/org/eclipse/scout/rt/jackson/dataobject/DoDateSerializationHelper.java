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

import java.text.SimpleDateFormat;

import org.eclipse.scout.rt.dataobject.DataObjectAttributeDescriptor;
import org.eclipse.scout.rt.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.date.StrictSimpleDateFormat;

import com.fasterxml.jackson.core.JsonStreamContext;

/**
 * Common helper for {@link DoDateSerializer} and {@link DoDateDeserializer}
 */
@ApplicationScoped
public class DoDateSerializationHelper {

  protected final LazyValue<DataObjectInventory> m_dataObjectInventory = new LazyValue<>(DataObjectInventory.class);

  public SimpleDateFormat findFormatter(JsonStreamContext ctx) {
    // if current element is a DoList, switch to parent context (which is defining the DoList attribute)
    if (ctx != null && ctx.getCurrentValue() instanceof DoList) {
      ctx = ctx.getParent();
    }
    // if current value is a IDoEntity, and the current field has a name, try to find an annotated custom formatter
    if (ctx != null && ctx.getCurrentValue() != null && ctx.getCurrentName() != null && ctx.getCurrentValue() instanceof IDoEntity) {
      Class<? extends IDoEntity> entityClass = ctx.getCurrentValue().getClass().asSubclass(IDoEntity.class);
      return lookupFormatter(entityClass, ctx.getCurrentName());
    }
    return null;
  }

  protected SimpleDateFormat lookupFormatter(Class<? extends IDoEntity> entityClass, String name) {
    String pattern = m_dataObjectInventory.get().getAttributeDescription(entityClass, name)
        .flatMap(DataObjectAttributeDescriptor::getFormatPattern)
        .orElse(IValueFormatConstants.DEFAULT_DATE_PATTERN);
    return new StrictSimpleDateFormat(pattern);
  }
}
