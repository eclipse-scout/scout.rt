/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
export default class Route {
  location: string;

  constructor() {
    this.location = null;
  }

  /**
   * @returns Whether or not this instance can handle the given location.
   */
  matches(location: string): boolean {
    return false;
  }

  /**
   * Called when the route is activated, stores the given location as instance variable.
   * This is useful for the case where a single instance of Route handles multiple locations.
   */
  activate(location: string) {
    this.location = location;
  }

  /**
   * Called when route is deactivated because another route is activated. This is the place
   * to perform clean-up operations.
   */
  deactivate() {
    // nop
  }
}
