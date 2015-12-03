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
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * Marker interface for a form used as search form.
 * <p>
 * A search form is started by {@link IForm#start()}. Thereto, the search handler must be installed during
 * initialization, or by overriding {@link #start()}.
 */
public interface ISearchForm extends IForm {
}
