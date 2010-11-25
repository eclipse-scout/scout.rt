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
package org.eclipse.scout.commons.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class annotation on a form or form field used in scout sdk as a marker to
 * automatically manage the FormData.
 * <p>
 * For an IForm with this annotation a java class with "Data" suffix is created (super type {@link AbstractFormData})
 * with all marked fields as top-level inner types (super type {@link AbstractFormFieldData}).
 * <p>
 * With an optional value attribute the detailed behavior in auto-creating form data can be customized.
 * <p>
 * Valid values are
 * <ul>
 * <li>IGNORE or empty string excludes this form or field from the form data</li>
 * <li>CREATE infers the correct type as {@link AbstractFormData}, {@link AbstractFormFieldData},
 * {@link AbstractValueFieldData}, {@link AbstractTableFieldData}.</li>
 * <li>on a IFormField: CREATE | CREATE EXTERNAL | USING fully-qualified-type-name subclassing
 * {@link AbstractFormFieldData}</li>
 * </ul>
 * <p>
 * A form without a form data annotation is assumed "IGNORE".<br>
 * A table field or value field without a form data annotation is assumed "CREATE".<br>
 * All other fields without a form data annotation are assumed "IGNORE".
 * <p>
 * Examples on a IForm:<br>
 * &#064;FormData<br>
 * &#064;FormData("IGNORE")<br>
 * <p>
 * Examples on a IFormField:<br>
 * &#064;FormData("IGNORE")<br>
 * &#064;FormData("CREATE")<br>
 * &#064;FormData("CREATE EXTERNAL")<br>
 * &#064;FormData("USING com.bsiag.crm.shared.services.process.AbstractCustomPersonFieldData" )<br>
 * <p>
 * USING class-name simply uses the given class as the field-data superclass.<br>
 * CREATE EXTERNAL creates an additional external abstract java file for the abstract field-data of the models supertype
 * (this abstract template is a subclass of {@link AbstractExternalFieldData}).
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface FormData {

  String value() default "CREATE";

}
