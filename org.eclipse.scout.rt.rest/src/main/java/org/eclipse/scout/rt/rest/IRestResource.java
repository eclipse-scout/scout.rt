/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Marks a class as REST resource so that it can be found by {@link RestApplication} using jandex.
 */
@Bean
public interface IRestResource {
}
