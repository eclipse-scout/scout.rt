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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

/**
 * A smart field with a key type different from the value type. The default implementation of
 * {@link #convertKeyToValue(Object)} and {@link #convertValueToKey(Object)} methods works for any case where
 * <VALUE_TYPE extends LOOKUP_CALL_KEY_TYPE>. For all other cases provide your own conversion methods.
 *
 * @param <VALUE>
 * @param <LOOKUP_KEY>
 */
public interface IMixedSmartField<VALUE, LOOKUP_KEY> extends IContentAssistField<VALUE, LOOKUP_KEY> {
}
