/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class annotation on a form or form field used in Scout SDK as a marker to manage the FormData.
 * <p>
 * The following properties are supported:
 * <table border="1">
 * <tr>
 * <th width="10%">Property</th>
 * <th width="10%">Type</th>
 * <th width="80%">Description</th>
 * </tr>
 * <tr>
 * <td rowspan="1" vAlign="top"><code>value</code></td>
 * <td vAlign="top">*.class</td>
 * <td vAlign="top">A form data class (e.g. <code>AbstractMyGroupboxData.class</code>).
 * <li>The form data class to generate for a <code>? extends AbstractForm</code> when {@link SdkCommand#CREATE}</li>
 * <li>The super class of the managed form data when {@link SdkCommand#USE}</li></td>
 * </tr>
 * <tr>
 * <td rowspan="4" vAlign="top"><code>sdkCommand</code></td>
 * <td vAlign="top">{@link SdkCommand#CREATE}</td>
 * <td vAlign="top">Scout SDK will create and manage the form data.</td>
 * </tr>
 * <tr>
 * <td vAlign="top">{@link SdkCommand#USE}</td>
 * <td vAlign="top">All subclasses will use the formdata (<code>value</code>) as a supertype of its formdata.</td>
 * </tr>
 * <tr>
 * <td vAlign="top">{@link SdkCommand#IGNORE}</td>
 * <td>The annotated class will be ignored in the formdata.</td>
 * </tr>
 * <tr>
 * <td vAlign="top">{@link SdkCommand#DEFAULT}</td>
 * <td>Not to use in the code.</td>
 * </tr>
 * <tr>
 * <td rowspan="3" vAlign="top"><code>defaultSubtypeSdkCommand</code></td>
 * <td vAlign="top">{@link DefaultSubtypeSdkCommand#CREATE}</td>
 * <td vAlign="top">All subtypes will be included in the formdata.</td>
 * </tr>
 * <tr>
 * <td vAlign="top">{@link DefaultSubtypeSdkCommand#IGNORE}</td>
 * <td vAlign="top">All subtypes will be ignored in the formdata.</td>
 * </tr>
 * <tr>
 * <td>{@link DefaultSubtypeSdkCommand#DEFAULT}</td>
 * <td vAlign="top">Not to use in the code.</td>
 * </tr>
 * <tr>
 * <td vAlign="top"><code>genericOrdinal</code></td>
 * <td vAlign="top">int &gt;= 0</td>
 * <td vAlign="top"><i>Since Scout 4.1.</i><br>
 * If the class referenced in <code>value</code> has a type parameter and the annotation owner has type parameters as
 * well, the ordinal describes the zero-based index of the type parameter of the annotation owner that should be
 * transfered to the type parameter of the <code>value</code> class.</td>
 * </tr>
 * <tr>
 * <td vAlign="top"><code>interfaces</code></td>
 * <td vAlign="top">Class[]</td>
 * <td vAlign="top"><i>Since Scout 4.1.</i><br>
 * An array of interface classes that the formdata class referenced in <code>value</code> should implement. There is no
 * check done that the resulting formdata fulfills the interfaces provided in this array. They are just added to the
 * generated formdata class.</td>
 * </tr>
 * </table>
 * <h3>Examples</h3>
 * <h4>Ignore on form fields</h4>
 *
 * <pre>
 * &#64FormData(sdkCommand=FormData.SdkCommand.IGNORE)
 * public class NameField extends AbstractStringField{...
 * </pre>
 *
 * The NameField will not be considered in the form data. The NameField is an inner type in a form.
 * <h4>Ignore on abstract form fields</h4>
 *
 * <pre>
 * &#64FormData(defaultSubtypeSdkCommand=FormData.DefaultSubtypeSdkCommand.IGNORE)
 * public abstract class AbstractNameField extends AbstractStringField{...
 * </pre>
 *
 * Any subtype of AbstractFormField will be ignored in its form data. The AbstractNameField is a primary type.
 * <h4>Template Groupbox</h4>
 *
 * <pre>
 * &#64FormData(value=AbstractTemplateGroupBoxData.class, defaultSubtypeSdkCommand=FormData.DefaultSubtypeSdkCommand.CREATE, sdkCommand=FormData.SdkCommand.CREATE)
 * public abstract class AbstractTemplateGroupBox extends AbstractGroupBox{...
 * </pre>
 *
 * The <code>FormData.DefaultSubtypeSdkCommand.CREATE</code> ensures the creation of a FormData class for every subclass
 * of this groupbox. The value <code>AbstractTemplateGroupBoxData.class</code> ensures that every generated FormData
 * class of every subclass of this groupbox extends AbstractTemplateGroupBoxData (instead of the default
 * AbstractGroupBox).
 * <h3>Existing Annotations</h3>
 *
 * <pre>
 * &#64FormData(AbstractFormData.class)
 * public abstract class <b>AbstractForm</b> extends AbstractPropertyObserver implements IForm { ...
 * </pre>
 *
 * <pre>
 * &#64FormData(value = AbstractFormFieldData.class, sdkCommand = FormData.SdkCommand.USE)
 * public abstract class <b>AbstractFormField</b> extends AbstractPropertyObserver implements IFormField {...
 * </pre>
 *
 * <pre>
 * &#64FormData(value = AbstractValueFieldData.class, defaultSubtypeSdkCommand = FormData.DefaultSubtypeSdkCommand.CREATE, sdkCommand = FormData.SdkCommand.USE, genericOrdinal = 0)
 * public abstract class <b>AbstractValueField<T></b> extends AbstractFormField implements IValueField<T> { ...
 * </pre>
 *
 * <pre>
 * &#64FormData(value = AbstractComposerData.class, sdkCommand = FormData.SdkCommand.USE, defaultSubtypeSdkCommand = FormData.DefaultSubtypeSdkCommand.CREATE)
 * public abstract class <b>AbstractComposerField</b> extends AbstractFormField implements IComposerField { ...
 * </pre>
 *
 * <pre>
 * &#64FormData(value = AbstractUTCFieldData.class, sdkCommand = FormData.SdkCommand.USE)
 * public abstract class <b>AbstractUTCDateField</b> extends AbstractDateField implements IUTCDateField { ...
 * </pre>
 *
 * <pre>
 * &#64FormData(value = AbstractTableFieldData.class, sdkCommand = FormData.SdkCommand.USE, defaultSubtypeSdkCommand = FormData.DefaultSubtypeSdkCommand.CREATE)
 * public abstract class <b>AbstractTableField<T extends ITable></b> extends AbstractFormField implements ITableField<T> { ...
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface FormData {

  Class value() default Object.class;

  SdkCommand sdkCommand() default SdkCommand.DEFAULT;

  DefaultSubtypeSdkCommand defaultSubtypeSdkCommand() default DefaultSubtypeSdkCommand.DEFAULT;

  int genericOrdinal() default -1;

  Class[] interfaces() default {};

  enum SdkCommand {
    CREATE, USE, IGNORE, DEFAULT
  }

  enum DefaultSubtypeSdkCommand {
    CREATE, IGNORE, DEFAULT
  }
}
