/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, Dimension} from '../index';

export default class ImageLayout extends AbstractLayout {

  constructor(image) {
    super();
    this.image = image;
  }

  preferredLayoutSize($container, options) {
    let img = $container[0];
    if (img && img.complete && img.naturalWidth > 0 && img.naturalHeight > 0) {
      let prefHeight = img.naturalHeight;
      let prefWidth = img.naturalWidth;
      if (options.widthHint > 0 && options.widthHint < img.naturalWidth) {
        prefHeight = options.widthHint / img.naturalWidth * img.naturalHeight;
        prefWidth = options.widthHint;
      } else if (options.heightHint > 0 && options.heightHint < img.naturalHeight) {
        prefHeight = options.heightHint;
        prefWidth = options.heightHint / img.naturalHeight * img.naturalWidth;
      }
      return new Dimension(prefWidth, prefHeight);
    }
    return super.preferredLayoutSize($container, options);
  }
}
