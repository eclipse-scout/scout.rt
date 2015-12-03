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

import org.eclipse.scout.rt.client.ui.basic.table.ITable;

/**
 * Node-oriented page (one of the two types of IPage @see IPage)<br>
 * <p>
 * Contains a set of child pages (defined in execCreateChildPages() function)<br>
 * This page is suitable if you want to define the tree (the table is derived from the tree)<br>
 * If the note is not marked as leaf, it is possible to drill-down the child pages in the outline <br>
 * <p>
 * contains a table as data holder<br>
 * table events are handled by the configured inner table<br>
 * tree events are delegated to the table<br>
 */
public interface IPageWithNodes extends IPage<ITable> {

  void rebuildTableInternal();

}
