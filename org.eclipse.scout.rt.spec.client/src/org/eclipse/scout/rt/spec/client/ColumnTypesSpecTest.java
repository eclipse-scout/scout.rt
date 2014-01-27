/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * Concrete implementation of {@link AbstractTypeSpecTest} for columns
 */
public class ColumnTypesSpecTest extends AbstractTypeSpecTest {

  public ColumnTypesSpecTest() {
    super("org.eclipse.scout.rt.spec.columntypes", TEXTS.get("org.eclipse.scout.rt.spec.columntypes"), IColumn.class);
  }
}
