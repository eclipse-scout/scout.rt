/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class annotation on a table column used in scout sdk as a marker to manage the table data.
 * <p>
 * The following properties are supported.
 * <table border="1">
 * <tr>
 * <th width="10%">Property</th>
 * <th width="10%">Property Value</th>
 * <th width="80%">Description</th>
 * </tr>
 * <tr>
 * <td rowspan="2" vAlign="top"><code>value</code></td>
 * <td>{@link SdkColumnCommand#CREATE}</td>
 * <td>Scout SDK creates a property for the given column in the table data.</td>
 * </tr>
 * <tr>
 * <td>{@link SdkColumnCommand#IGNORE}</td>
 * <td>Scout SDK ignores the column in the table data.</td>
 * </tr>
 * <tr>
 * </table>
 * <h3>Examples</h3>
 * <h4>Ignore a table column</h4> <blockquote>
 *
 * <pre>
 * &#ColumnData(SdkColumnCommand.IGNORE)
 * public class DisplayColumn extends AbstractStringColumn { ... }
 * </pre>
 *
 * </blockquote> The NameField will not be considered in the form data. The NameField is an inner type in a
 * form.</blockquote>
 *
 * @since 3.10.0-M5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ColumnData {

  SdkColumnCommand value();

  enum SdkColumnCommand {
    CREATE, IGNORE
  }
}
