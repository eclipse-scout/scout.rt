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
package org.eclipse.scout.rt.shared.services.common.code;

import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

/**
 *
 */
public interface ICodeRow<KEY_TYPE> extends ILookupRow<KEY_TYPE> {

  long getPartitionId();

  Number getValue();

  void setValue(Number value);

  String getExtKey();

  void setExtKey(String extKey);

}
