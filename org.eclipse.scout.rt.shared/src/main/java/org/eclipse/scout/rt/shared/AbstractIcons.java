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

/**
 * See also icons.css.
 */
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

  /* default font icons (sans-serif, arial) */
  public static final String TableSortAsc = "font:\u2191";
  public static final String TableSortDesc = "font:\u2193";

  /* custom icons */
  public static final String ExclamationMark = "font:\uE001";
  public static final String Info = "font:\uE002";
  public static final String Calendar = "font:\uE003";
  public static final String Clock = "font:\uE004";
  public static final String Checked = "font:\uE005";
  public static final String Group = "font:\uE006";
  public static final String Target = "font:\uE020";
  public static final String World = "font:\uE021";
  public static final String Chart = "font:\uE022";
  public static final String Graph = "font:\uE023";
  public static final String Category = "font:\uE024";
  public static final String Gear = "font:\uE031";
  public static final String Star = "font:\uE032";
  public static final String Person = "font:\uE034";
  public static final String VerticalDots = "font:\uE040";
  public static final String Search = "font:\uE042";
  public static final String Folder = "font:\uE043";
  public static final String Sum = "font:\ue025";

  /* awesome font icons */
  public static final String Remove = "font:\uF00D";
  public static final String RotateRight = "font:\uF01E";
  public static final String Pencil = "font:\uF040";
  public static final String ChevronLeft = "font:\uF053";
  public static final String ChevronRight = "font:\uF054";
  public static final String ArrowRight = "font:\uF061";
  public static final String Plus = "font:\uF067";
  public static final String Minus = "font:\uF068";
  public static final String ChevronUp = "font:\uF077";
  public static final String ChevronDown = "font:\uF078";
  public static final String Square = "font:\uF0C8";
  public static final String Menu = "font:\uF0C9";
  public static final String List = "font:\uF0CA";
  public static final String ListNumbered = "font:\uF0CB";
  public static final String ListThick = "font:\uF00B";
  public static final String CaretDown = "font:\uF0D7";
  public static final String CaretUp = "font:\uF0D8";
  public static final String CaretLeft = "font:\uF0D9";
  public static final String CaretRight = "font:\uF0DA";
  public static final String RotateLeft = "font:\uF0E2";
  public static final String AngleDoubleLeft = "font:\uF100";
  public static final String AngleDoubleRight = "font:\uF101";
  public static final String AngleDoubleUp = "font:\uF102";
  public static final String AngleDoubleDown = "font:\uF103";
  public static final String AngleLeft = "font:\uF104";
  public static final String AngleRight = "font:\uF105";
  public static final String AngleUp = "font:\uF106";
  public static final String AngleDown = "font:\uF107";
  public static final String Circle = "font:\uF111";
  public static final String File = "font:\uF15B";
  public static final String LongArrowDown = "font:\uF175";
  public static final String LongArrowUp = "font:\uF176";
  public static final String LongArrowLeft = "font:\uF177";
  public static final String LongArrowRight = "font:\uF178";

}
