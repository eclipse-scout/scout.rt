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
package org.eclipse.scout.rt.platform.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as configuration property that is considered to be public API and may be overridden by clients to
 * change default properties.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConfigProperty {
  String value();

  /**
   * Boolean
   */
  String BOOLEAN = "BOOLEAN";
  /**
   * Double
   */
  String DOUBLE = "DOUBLE";
  /**
   * BigDecimal
   */
  String BIG_DECIMAL = "BIG_DECIMAL";
  /**
   * String
   */
  String DRAG_AND_DROP_TYPE = "DRAG_AND_DROP_TYPE";
  /**
   * Integer
   */
  String INTEGER = "INTEGER";
  /**
   * Long
   */
  String LONG = "LONG";
  /**
   * BigInteger
   */
  String BIG_INTEGER = "BIG_INTEGER";
  /**
   * Plain-String
   */
  String STRING = "STRING";
  /**
   * e.g. arial,bold,11
   */
  String FONT = "FONT";

  String FORM_DATA = "FORM_DATA";

  String ABSTRACT_FORM_DATA = "ABSTRACT_FORM_DATA";

  /**
   * HEX COLOR e.g. FFFFFF
   */
  String COLOR = "COLOR";
  /**
   * Object
   */
  String OBJECT = "OBJECT";
  /**
   * int
   */
  String BUTTON_DISPLAY_STYLE = "BUTTON_DISPLAY_STYLE";
  /**
   * int
   */
  String BUTTON_SYSTEM_TYPE = "BUTTON_SYSTEM_TYPE";
  /**
   * Class&lt;? extends LookupCall&gt;
   */
  String GROUP_BOX_BODY_GRID = "GROUP_BOX_BODY_GRID";

  /**
   * Class&lt;? extends ICodeType&gt;
   */
  String CODE_TYPE = "CODE_TYPE";

  /**
   * Class&lt;? extends ICodeRow&lt;CODE_ID&gt;&gt;
   */
  String CODE_ROW = "CODE_ROW";
  /**
   * int
   */
  String COMPOSER_ATTRIBUTE_TYPE = "COMPOSER_ATTRIBUTE_TYPE";
  /**
   * Lists&lt;String&gt;
   */
  String FILE_EXTENSIONS = "FILE_EXTENSIONS";
  /**
   * Lists&lt;String&gt;
   */
  String MIME_TYPES = "MIME_TYPES";
  /**
   * int
   */
  String FORM_DISPLAY_HINT = "FORM_DISPLAY_HINT";

  /**
   * String
   */
  String FORM_VIEW_ID = "FORM_VIEW_ID";

  /**
   * int
   */
  String HORIZONTAL_ALIGNMENT = "HORIZONTAL_ALIGNMENT";
  /**
   * String
   */
  String ICON_ID = "ICON_ID";
  /**
   * Class&lt;? extends IKeyStroke&gt;
   */
  String KEY_STROKE = "KEY_STROKE";
  /**
   * Class&lt;? extends LookupCall&gt;
   */
  String LOOKUP_CALL = "LOOKUP_CALL";
  /**
   * Class&lt;? extends ILookupService&gt;
   */
  String LOOKUP_SERVICE = "LOOKUP_SERVICE";
  /**
   * Class&lt;? extends IValueField&gt;
   */
  String MASTER_FIELD = "MASTER_FIELD";
  /**
   * Class&lt;? extends IPage&gt;
   */
  String OUTLINE_ROOT_PAGE = "OUTLINE_ROOT_PAGE";
  /**
   * Class&lt;? extends IOutline&gt;
   */
  String OUTLINE = "OUTLINE";
  /**
   * Class&lt;? extends IOutline&gt;[]
   */
  String OUTLINES = "OUTLINES";
  /**
   * Class&lt;? extends IForm&gt;
   */
  String FORM = "FORM";
  /**
   * Class&lt;? extends ISearchForm&gt;
   */
  String SEARCH_FORM = "SEARCH_FORM";
  /**
   * Class&lt;? extends DynamicNls;
   */
  String NLS_PROVIDER = "NLS_PROVIDER";
  /**
   * Class&lt;? extends ISqlStyle&gt;
   */
  String SQL_STYLE = "SQL_STYLE";
  /**
   * Class&lt;? extends IColumn&gt;
   */
  String TABLE_COLUMN = "TABLE_COLUMN";
  /**
   * an String representing an SQL statement
   */
  String SQL = "SQL";
  /**
   * NLS translated String referencing normal text provider services.
   */
  String TEXT = "TEXT";
  /**
   * int
   */
  String VERTICAL_ALIGNMENT = "VERTICAL_ALIGNMENT";
  /**
   * Fully qualified class name of a ISwingChartProvider class with bundle symbolic name prefix<br>
   */
  String CHART_QNAME = "CHART_QNAME";
  /**
   * {@link java.util.Calendar#MONDAY} ... {@link java.util.Calendar#SUNDAY}
   */
  String HOUR_OF_DAY = "HOUR_OF_DAY";
  /**
   * duration as type Long in minutes
   */
  String DURATION_MINUTES = "DURATION_MINUTES";
  /**
   * class of a local IMenu (used in table and tree)<br>
   * for example <code>NewCompanyMenu</code>
   */
  String MENU_CLASS = "MENU_CLASS";
  /**
   * Class&lt;?&gt; but must be a primitive wrapper type: String, Double, Float, Long, Integer, Byte
   */
  String PRIMITIVE_TYPE = "PRIMITIVE_TYPE";
  /**
   * Position of the label of a form field.
   */
  String LABEL_POSITION = "LABEL_POSITION";
  /**
   * Horizontal alignment of the label of a form field.
   */
  String LABEL_HORIZONTAL_ALIGNMENT = "LABEL_HORIZONTAL_ALIGNMENT";
  /**
   * Border decoration enumeration according to the BORDER_DECORATION_* constants
   */
  String BORDER_DECORATION = "BORDER_DECORATION";
  /**
   * java.math.RoundingMode
   */
  String ROUNDING_MODE = "ROUNDING_MODE";
  /**
   * int one of [TOOLBAR_FORM_HEADER, TOOLBAR_VIEW_PART] of IForm
   */
  String TOOLBAR_LOCATION = "TOOLBAR_LOCATION";
  /**
   * Set of menu type enum values
   */
  String MENU_TYPE = "MENU_TYPE";
}
