/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.testing;

import java.util.Objects;
import java.util.function.Function;

import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;

@Replace
@Order(BeanTestingHelper.TESTING_RESOURCE_ORDER)
public class TestingDataObjectHelper extends DataObjectHelper {

  /**
   * In a test setup there might be no [configured] {@link IDataObjectMapper}. Therefore usual {@code toString} method
   * is used in such cases.
   */
  private final LazyValue<Function<IDoEntity, String>> m_toStringFunction = new LazyValue<>(() -> {
    IPrettyPrintDataObjectMapper prettyPrinter = BEANS.opt(IPrettyPrintDataObjectMapper.class);
    if (prettyPrinter != null) {
      return prettyPrinter::writeValue;
    }
    else {
      return entity -> Objects.toString(entity.all());
    }
  });

  @Override
  public String toString(IDoEntity entity) {
    if (entity == null) {
      return Objects.toString(entity);
    }
    return m_toStringFunction.get().apply(entity);
  }
}
