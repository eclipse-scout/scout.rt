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
package org.eclipse.scout.rt.shared;

import java.io.Serializable;

public abstract class AbstractIcons implements Serializable {
  private static final long serialVersionUID = 1L;

  protected AbstractIcons() {
  }

  /**
   * marker icon for 'no icon'
   */
  public static final String Null = "null";

  /**
   * marker icon for an empty (transparent white) icon
   */
  public static final String ApplicationLogo = "application_logo_large";
  public static final String Empty = "empty";
  public static final String Bookmark = "bookmark";
  public static final String ComposerFieldAggregation = "composerfield_aggregation";
  public static final String ComposerFieldAttribute = "composerfield_attribute";
  public static final String ComposerFieldEitherOrNode = "composerfield_eitheror";
  public static final String ComposerFieldEntity = "composerfield_entity";
  public static final String ComposerFieldRoot = "composerfield_root";
  public static final String DateFieldDate = "datefield_date";
  public static final String DateFieldTime = "datefield_time";
  public static final String FileChooserFieldFile = "filechooserfield_file";
  /**
   * Use {@link AbstractIcons#TreeNode} instead due to icon semantic
   */
  @Deprecated
  public static final String Folder = AbstractIcons.TreeNode;
  /**
   * Use {@link AbstractIcons#TreeNodeOpen} instead due to icon semantic
   */
  @Deprecated
  public static final String FolderOpen = AbstractIcons.TreeNodeOpen;
  public static final String Gears = "gears";
  public static final String NavigationCurrent = "navigation_current";
  public static final String SmartFieldBrowse = "smartfield_browse";
  public static final String StatusError = "status_error";
  public static final String StatusInfo = "status_info";
  public static final String StatusWarning = "status_warning";
  public static final String TableColumnFilterActive = "table_column_filter_active";
  public static final String TableCustomColumn = "table_custom_column";
  public static final String TableSortAsc = "table_sort_asc";
  public static final String TableSortDesc = "table_sort_desc";
  public static final String TreeNode = "tree_node";
  public static final String TreeNodeOpen = "tree_node_open";
  public static final String WizardBackButton = "wizard_back";
  public static final String WizardBullet = "wizard_bullet";
  public static final String WizardNextButton = "wizard_next";

}
