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
  public static String Null = "null";

  /**
   * marker icon for an empty (transparent white) icon
   */
  public static String Bookmark = "bookmark";
  public static String CheckboxYes = "checkbox_yes";
  public static String CheckboxNo = "checkbox_no";
  public static String ComposerFieldAggregation = "composerfield_aggregation";
  public static String ComposerFieldAttribute = "composerfield_attribute";
  public static String ComposerFieldEitherOrNode = "composerfield_eitheror";
  public static String ComposerFieldEntity = "composerfield_entity";
  public static String ComposerFieldRoot = "composerfield_root";
  public static String DateFieldDate = "datefield_date";
  public static String DateFieldTime = "datefield_time";
  public static String Empty = "empty";
  public static String File = "file";
  public static String FileChooserFieldFile = "filechooserfield_file";
  public static String Folder = "folder";
  public static String FolderOpen = "folder_open";
  public static String Gears = "gears";
  public static String NavigationCurrent = "navigation_current";
  public static String SmartFieldBrowse = "smartfield_browse";
  public static String TimeFieldTime = "datefield_time";
  public static String WizardBackButton = "wizard_back";
  public static String WizardNextButton = "wizard_next";

  public static String StatusInfo = "status_info";
  public static String StatusError = "status_error";
  public static String StatusWarning = "status_warning";
  public static String StatusInterrupt = "status_interrupt";

}
