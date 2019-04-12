const fs = require('fs');
const errno = require('errno');

deleteFile('./dist/scout-theme.js');
deleteFile('./dist/scout-theme-dark.js');

function deleteFile(filename) {
  fs.unlink(filename, (err) => {
    if (err) {
      if (err.errorno === errno.ENOENT) {
        console.log('file does not exist', filename);
        return;
      } else {
        throw err;
      }
    }
    console.log('deleted ', filename);
  });
}
