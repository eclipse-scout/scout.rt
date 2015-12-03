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
package org.eclipse.scout.rt.client.ui.basic.userfilter;

/**
 * This marker interface indicates that the filtering happens in the ui by the user. This is mainly necessary to decide
 * whether an object (table row, tree node) is allowed to be sent to the ui or not. Objects filtered by a regular (not
 * user) filter are never sent to the ui.
 *
 * @since 5.1
 */
public interface IUserFilter {

}
