/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject;

import java.text.SimpleDateFormat;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.dataobject.DataObjectAttributeDescriptor;
import org.eclipse.scout.rt.platform.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.platform.util.LazyValue;

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
