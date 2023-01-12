/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The ModelVariant annotation is used to mark Scout model classes. The annotation is read by the JSON layer, where the
 * value of the annotation is added to the class-name of the model, like 'StringField:Custom' and returned by as
 * objectType (see: <code>IJsonAdapter#getObjectType</code>), which is used to look up a JSON adapter class in the
 * object factories on the client-side and also a JavaScript object in the registered object factories in the browser.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ModelVariant {

  String SEPARATOR = ":";

  String value();
}
