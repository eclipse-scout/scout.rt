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
package org.eclipse.scout.rt.client.ui.action.menu;

/**
 * All possible menu types of a tile menu. These menu types are used by {@link AbstractMenu#getConfiguredMenuTypes()}.
 */
public enum TilesMenuType implements IMenuType {
  EmptySpace,
  SingleSelection,
  MultiSelection
}
