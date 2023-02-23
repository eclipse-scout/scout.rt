/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, numbers, RoundingMode} from '../index';

/**
 * Checks if the combined total size of the given files does not exceed the maximum upload size.
 *
 * @param {File[]} files array of files
 * @param {Number} maximumUploadSize maximum combined file size. null disables the size check
 * @returns {boolean} true if the total size does not exceed maximumUploadSize, otherwise false
 */
export function validateMaximumUploadSize(files, maximumUploadSize) {
  files = arrays.ensure(files);
  if (files.length === 0 || maximumUploadSize == null) {
    return true;
  }

  let totalSize = files.reduce((total, file) => {
    return total + file.size;
  }, 0);

  return totalSize <= maximumUploadSize;
}

export function fileListToArray(fileList) {
  let files = [];
  for (let i = 0; i < fileList.length; i++) {
    files.push(fileList[i]);
  }
  return files;
}

export function getErrorMessageMaximumUploadSizeExceeded(session, maximumUploadSize) {
  if (maximumUploadSize < 1024) {
    return session.text('ui.FileSizeLimit', maximumUploadSize, session.text('ui.Bytes'));
  }
  let maximumUploadSizeKB = maximumUploadSize / 1024;
  if (maximumUploadSizeKB < 1024) {
    return session.text('ui.FileSizeLimit', numbers.round(maximumUploadSizeKB, RoundingMode.HALF_UP, 2), 'KB');
  }
  let maximumUploadSizeMB = maximumUploadSizeKB / 1024;
  if (maximumUploadSizeMB < 1024) {
    return session.text('ui.FileSizeLimit', numbers.round(maximumUploadSizeMB, RoundingMode.HALF_UP, 2), 'MB');
  }
  return session.text('ui.FileSizeLimit', numbers.round(maximumUploadSizeMB / 1024, RoundingMode.HALF_UP, 2), 'GB');
}

export default {
  fileListToArray,
  validateMaximumUploadSize,
  getErrorMessageMaximumUploadSizeExceeded
};
