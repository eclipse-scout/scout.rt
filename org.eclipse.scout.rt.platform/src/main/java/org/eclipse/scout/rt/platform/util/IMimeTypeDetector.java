/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;

/**
 * Participant of mime type detectors
 * <p>
 * Do not use beans of this type directly, use {@link FileUtility#getMimeType(Path)}
 * <p>
 * In order to find the mime type of a stream or file there are various tools which may be used in all possible
 * combinations.
 * <ul>
 * <li>{@link Files#probeContentType(Path)}
 * <li>
 * <li>Apache Tika org.apache.tika:tika-core</li>
 * <li>ServletContext#getMimeType(String)</li>
 * <li>project specific preferences</li>
 * </ul>
 * <p>
 * Scout solves this dilemma by defining this {@link IMimeTypeDetector} {@link Bean} interface that can be implemented
 * using the {@link Order} annotation.
 * <p>
 * The scout platform module provides two pre-defined {@link IMimeTypeDetector}
 * <ul>
 * <li>{@link PrimaryMimeTypeDetector} with order 0 (first to be asked)</li>
 * <li>{@link JavaNioMimeTypeDetector} with {@link IOrdered#DEFAULT_ORDER} (last to be asked)</li>
 * </ul>
 * Projects are free to replace these beans or add additional beans with other orders.
 *
 * @author BSI AG
 * @since 5.2
 */
@FunctionalInterface
@Bean
public interface IMimeTypeDetector {

  /**
   * @param path
   * @return the mime type for the specified path (including content if necessary) or null if none is defined in this
   *         bean
   */
  String getMimeType(Path path);

}
