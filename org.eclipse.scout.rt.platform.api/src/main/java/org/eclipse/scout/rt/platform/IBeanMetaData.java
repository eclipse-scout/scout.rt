/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface IBeanMetaData {

  IBeanMetaData removeAnnotation(Class<? extends Annotation> annotationType);

  IBeanMetaData addAnnotation(Annotation annotation);

  void setBeanAnnotations(Map<Class<? extends Annotation>, Annotation> annotations);

  Map<Class<? extends Annotation>, Annotation> getBeanAnnotations();

  <ANNOTATION extends Annotation> ANNOTATION getBeanAnnotation(Class<ANNOTATION> annotationClazz);

  IBeanMetaData applicationScoped(boolean set);

  IBeanMetaData order(double order);

  IBeanMetaData replace(boolean set);

  IBeanInstanceProducer<?> getProducer();

  IBeanMetaData producer(IBeanInstanceProducer<?> producer);

  IBeanMetaData initialInstance(Object initialInstance);

  Object getInitialInstance();

  Class<?> getBeanClazz();

}
