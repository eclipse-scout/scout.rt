/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security.csp;

import java.io.Serial;

import jakarta.annotation.PostConstruct;

/**
 * CSP blocking everything. Can be used as starting point to create a policy as restrictive as possible.
 */
public class BlockAllContentSecurityPolicy extends ContentSecurityPolicy {
  @Serial
  private static final long serialVersionUID = -6438009746915370378L;

  @PostConstruct
  protected void initBlockAll() {
    withBaseUri(EXPRESSION_NONE);
    withDefaultSrc(EXPRESSION_NONE); // covers all sources including 'plugin-types'
    withFormAction(EXPRESSION_NONE);
    withFrameAncestors(EXPRESSION_NONE);
    withReportUri(REPORT_URL); // see also ContentSecurityPolicyReportHandler
  }
}
