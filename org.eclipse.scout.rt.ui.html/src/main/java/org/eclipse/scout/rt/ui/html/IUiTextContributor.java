/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import java.util.Set;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Implementations of this interface contribute to the list of text keys that are sent as (translated) text sent to the
 * UI on session startup (i.e. static UI texts that are required by JavaScript classes). Dynamic texts are always sent
 * as regular (form-)data and must <i>not</i> be contributed here.
 */
@FunctionalInterface
@ApplicationScoped
public interface IUiTextContributor {

  /**
   * @param textKeys
   *          live set to add the contributed text keys to (never <code>null</code>)
   */
  void contributeUiTextKeys(Set<String> textKeys);
}
