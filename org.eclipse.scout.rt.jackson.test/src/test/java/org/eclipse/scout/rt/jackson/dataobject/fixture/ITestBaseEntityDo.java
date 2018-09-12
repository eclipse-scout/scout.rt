/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.IDoEntity;

public interface ITestBaseEntityDo extends IDoEntity {

  DoValue<String> stringAttribute();

  DoValue<Double> doubleAttribute();

  DoValue<TestItemDo> itemDoAttribute();

  DoList<String> stringListAttribute();

  DoList<Double> doubleListAttribute();

  DoList<TestItemDo> itemDoListAttribute();
}
