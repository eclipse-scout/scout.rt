/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared;

import java.io.Serializable;

/**
 * See also icons.css.
 */
//SONAR:OFF
@SuppressWarnings("FieldNamingConvention")
public abstract class AbstractIcons implements Serializable {
  private static final long serialVersionUID = 1L;

  protected AbstractIcons() {
  }

  /**
   * marker icon for 'no icon'
   */
  public static final String Null = "null";

  /* default font icons (sans-serif, arial) */
  public static final String TableSortAsc = "font:\u2191";
  public static final String TableSortDesc = "font:\u2193";

  /* custom icons */
  public static final String ExclamationMarkCircle = "font:\uE001";
  public static final String Info = "font:\uE002";
  public static final String Calendar = "font:\uE029";
  public static final String CalendarBold = "font:\uE003";
  public static final String ClockBold = "font:\uE004";
  public static final String CheckedBold = "font:\uE005";
  public static final String Group = "font:\uE006";
  public static final String GroupPlus = "font:\uE007";
  public static final String GroupRemove = "font:\uE009";
  public static final String AngleDoubleLeft = "font:\uE010";
  public static final String AngleDoubleRight = "font:\uE011";
  public static final String AngleLeft = "font:\uE012";
  public static final String AngleRight = "font:\uE013";
  public static final String AngleDown = "font:\uE014";
  public static final String AngleUp = "font:\uE015";
  public static final String LongArrowDown = "font:\uE016";
  public static final String LongArrowUp = "font:\uE017";
  public static final String LongArrowDownPlus = "font:\uE018";
  public static final String LongArrowUpPlus = "font:\uE019";
  public static final String Minus = "font:\uE01A";
  public static final String Plus = "font:\uE01B";
  public static final String List = "font:\uE01C";
  public static final String LongArrowDownRemove = "font:\uE01D";
  public static final String LongArrowUpRemove = "font:\uE01E";
  public static final String FilterRemove = "font:\uE01F";
  public static final String Target = "font:\uE020";
  public static final String World = "font:\uE021";
  public static final String Chart = "font:\uE022";
  public static final String Gear = "font:\uE031";
  public static final String Star = "font:\uE02D";
  public static final String StarMarked = "font:\uE02E";
  public static final String StarBold = "font:\uE032";
  public static final String StarSolid = "font:\uE033";
  public static final String PersonSolid = "font:\uE034";
  public static final String Remove = "font:\uE035";
  public static final String ExpandAll = "font:\uE036";
  public static final String CollapseAll = "font:\uE037";
  public static final String Min = "font:\uE038";
  public static final String Max = "font:\uE039";
  public static final String EllipsisV = "font:\uE041";
  public static final String Search = "font:\uE02A";
  public static final String SearchBold = "font:\uE042";
  public static final String Folder = "font:\uE02B";
  public static final String FolderBold = "font:\uE043";
  public static final String RemoveBold = "font:\uE045";
  public static final String Sum = "font:\ue02C";
  public static final String SumBold = "font:\ue025";
  public static final String Pencil = "font:\uE02F";
  public static final String PencilBold = "font:\uE04B";
  public static final String PencilSolid = "font:\uE04F";
  public static final String ExclamationMarkBold = "font:\uE060";
  public static final String Slippery = "font:\uE044";

  /* awesome font icons */
  public static final String RotateLeftBold = "font:\uF0E2";
  public static final String RotateRightBold = "font:\uF01E";
  public static final String ChevronLeftBold = "font:\uF053";
  public static final String ChevronRightBold = "font:\uF054";
  public static final String ArrowRightBold = "font:\uF061";
  public static final String PlusBold = "font:\uF067";
  public static final String MinusBold = "font:\uF068";
  public static final String ChevronUpBold = "font:\uF077";
  public static final String ChevronDownBold = "font:\uF078";
  public static final String SquareSolid = "font:\uF0C8";
  public static final String CircleSolid = "font:\uF111";
  public static final String CaretDown = "font:\uF0D7";
  public static final String CaretUp = "font:\uF0D8";
  public static final String CaretLeft = "font:\uF0D9";
  public static final String CaretRight = "font:\uF0DA";
  public static final String AngleLeftBold = "font:\uF104";
  public static final String AngleRightBold = "font:\uF105";
  public static final String AngleUpBold = "font:\uF106";
  public static final String AngleDownBold = "font:\uF107";
  public static final String FileSolid = "font:\uF15B";
  public static final String LongArrowDownBold = "font:\uF175";
  public static final String LongArrowUpBold = "font:\uF176";
  public static final String LongArrowLeftBold = "font:\uF177";
  public static final String LongArrowRightBold = "font:\uF178";

}
