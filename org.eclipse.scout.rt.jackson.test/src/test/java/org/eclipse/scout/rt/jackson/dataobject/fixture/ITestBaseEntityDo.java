/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;

public interface ITestBaseEntityDo extends IDoEntity {

  DoValue<String> stringAttribute();

  DoValue<Double> doubleAttribute();

  DoValue<TestItemDo> itemDoAttribute();

  DoList<String> stringListAttribute();

  DoList<Double> doubleListAttribute();

  DoList<TestItemDo> itemDoListAttribute();
}
