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
 * manage the FormData.
 * <p>
 * The following properties are supported.
 * <table border="1">
 * <tr>
 * <th width="10%">Property</th>
 * <th width="10%">Property</th>
 * <th width="80%">Description</th>
 * </tr>
 * <tr>
 * <td rowspan="1" vAlign="top"><code>value</code></td>
 * <td>*.class</td>
 * <td>A form data class (e.g. <code>AbstractMyGroupboxData.class</code>).
 * <li>The form data class to generate for a <code>? extends AbstractForm</code> when {@link SdkCommand#CREATE}</li>
 * <li>The super class of the managed form data when {@link SdkCommand#USE}</li></td>
 * </tr>
 * <tr>
 * <td rowspan="4" vAlign="top"><code>sdkCommand</code></td>
 * <td> {@link SdkCommand#CREATE}</td>
 * <td>Scout SDK will create an manage the form data.</td>
 * </tr>
 * <tr>
 * <td> {@link SdkCommand#USE}</td>
 * <td>All subclasses will use the formdata (<code>value</code>) as a supertype of its formdata.</td>
 * </tr>
 * <tr>
 * <td> {@link SdkCommand#IGNORE}</td>
 * <td>The annotated class will be ignored in the formdata.</td>
 * </tr>
 * <tr>
 * <td> {@link SdkCommand#DEFAULT}</td>
 * <td>Not to use in the code.</td>
 * </tr>
 * <tr>
 * <td rowspan="4" vAlign="top"><code>defaultSubtypeSdkCommand</code></td>
 * <td> {@link DefaultSubtypeSdkCommand#CREATE}</td>
 * <td>All subtypes will be included in the formdata.</td>
 * </tr>
 * <tr>
 * <td> {@link DefaultSubtypeSdkCommand#IGNORE}</td>
 * <td>All subtypes will be ignored in the formdata.</td>
 * </tr>
 * <tr>
 * <td> {@link DefaultSubtypeSdkCommand#DEFAULT}</td>
 * <td>Not to use in the code.</td>
 * </tr>
 * </table>
 * <h3>Examples</h3>
 * <h4>Ignore on form fields</h4> <blockquote>
 * 
 * <pre>
 * &#64FormData(sdkCommand=SdkCommand.IGNORE)
 * public class NameField extends AbstractStringField{...
 *</pre>
 * 
 * </blockquote> The NameField will not be considered in the form data. The NameField is an inner type in a
 * form.</blockquote>
 * <h4>Ignore on abstract form fields</h4> <blockquote>
 * 
 * <pre>
 * &#64FormData(defaultSubtypeSdkCommand=DefaultSubtypeSdkCommand.IGNORE)
 * public abstract class AbstractNameField extends AbstractStringField{...
 *</pre>
 * 
 * </blockquote> Any subtype of AbstractFormField will be ignored in its form data. The AbstractNameField is a primary
 * type.
 * <h4>Template Groupbox</h4> <blockquote>
 * 
 * <pre>
 * &#64FormData(value=AbstractTemplateGroupBoxData, defaultSubtypeSdkCommand=DefaultSubtypeSdkCommand.CREATE, sdkCommand=SdkCommand.CREATE)
 * public abstract class AbstractTemplateGroupBox extends AbstractGroupBox{...
 *</pre>
 * 
 * </blockquote> The <code>DefaultSubtypeSdkCommand.CREATE</code> ensures the creation of a FormData class for every
 * subclass of this groupbox. The value <code>AbstractTemplateGroupBoxData</code> ensures every
 * <h3>Existing Annotations</h3>
 * 
 * <pre>
 * &#64FormData(AbstractFormData.class)
 * public abstract class <b>AbstractForm</b> extends AbstractPropertyObserver implements IForm { ...
 * </pre>
 * 
 * <pre>
 * &#64FormData(value = AbstractFormFieldData.class, sdkCommand = SdkCommand.USE)
 * public abstract class <b>AbstractFormField</b> extends AbstractPropertyObserver implements IFormField {...
 * </pre>
 * 
 * <pre>
 * &#64FormData(value = AbstractValueFieldData.class, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE, sdkCommand = SdkCommand.USE, genericOrdinal = 0)
 * public abstract class <b>AbstractValueField<T></b> extends AbstractFormField implements IValueField<T> { ...
 * </pre>
 * 
 * <pre>
 * &#64FormData(value = AbstractComposerData.class, sdkCommand = SdkCommand.USE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
 * public abstract class <b>AbstractComposerField</b> extends AbstractFormField implements IComposerField { ...
 * </pre>
 * 
 * <pre>
 * &#64FormData(value = AbstractUTCFieldData.class, sdkCommand = SdkCommand.USE)
 * public abstract class <b>AbstractUTCDateField</b> extends AbstractDateField implements IUTCDateField { ...
 * </pre>
 * 
 * <pre>
 * &#64FormData(value = AbstractTableFieldData.class, sdkCommand = SdkCommand.USE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
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

  public static enum SdkCommand {
    CREATE, USE, IGNORE, DEFAULT
  }

  public static enum DefaultSubtypeSdkCommand {
    CREATE, IGNORE, DEFAULT
  }
}
