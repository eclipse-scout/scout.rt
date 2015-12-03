/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.MixedSmartColumnChains.MixedSmartColumnConvertKeyToValueChain;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractMixedSmartColumn;

public interface IMixedSmartColumnExtension<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE, OWNER extends AbstractMixedSmartColumn<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE>> extends IContentAssistColumnExtension<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE, OWNER> {

  VALUE_TYPE execConvertKeyToValue(MixedSmartColumnConvertKeyToValueChain<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE> chain, LOOKUP_CALL_KEY_TYPE key);
}
