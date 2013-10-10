/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

/**
 * Interface extension to {@link ITable} that was used with version [3.8.2, 3.10.0) to provide additional methods for
 * importing and exporting the table's contents into {@link AbstractTableFieldBeanData}s.
 * Was merged with 3.10.0-M3 to the ITable interface.
 * 
 * @since 3.8.2
 * @deprecated will be removed with M-Release. Use {@link ITable} instead.
 */
@Deprecated
public interface ITable2 extends ITable {
}
