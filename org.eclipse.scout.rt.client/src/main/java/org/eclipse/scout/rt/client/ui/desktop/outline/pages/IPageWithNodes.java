/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
