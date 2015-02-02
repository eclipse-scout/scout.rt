scout.jsonHelper =  {

  removeCommentsFromJson: function(input) {
    if (!input || typeof input !== 'string') {
      return input;
    }
    var result = '';
    var whitespaceBuffer = '';
    for (var i = 0; i < input.length; i++) {
      var previousCharacter = input.charAt(i - 1);
      var currentCharacter = input.charAt(i);
      var nextCharacter = input.charAt(i + 1);

      // Add whitespace to a buffer (because me might want to ignore it at the end of a line)
      if (currentCharacter === ' ' || currentCharacter === '\t') {
        whitespaceBuffer += currentCharacter;
        continue;
      }
      // Handle end of line
      if (currentCharacter == '\r') {
        if (nextCharacter == '\n') {
          // Handle \r\n as \n
          continue;
        }
        // Handle \r as \n
        currentCharacter = '\n';
      }
      if (currentCharacter === '\n') {
        whitespaceBuffer = ''; // discard whitespace
        // Add line break (but not at the begin and not after another line break)
        if (result.charAt(result.length - 1) !== '\n') {
          result += currentCharacter;
        }
        continue;
      }

      // Handle strings
      if (currentCharacter === '"' && previousCharacter !== '\\') {
        // Flush whitespace to result
        result += whitespaceBuffer;
        whitespaceBuffer = '';
        result += currentCharacter;
        for (i++; i < input.length; i++) {
          previousCharacter = input.charAt(i - 1);
          currentCharacter = input.charAt(i);
          nextCharacter = input.charAt(i + 1);
          result += currentCharacter;
          if (currentCharacter === '"' && previousCharacter !== '\\') {
            break; // end of string
          }
        }
      }
      // Handle multi-line comments
      else if (currentCharacter === '/' && nextCharacter === '*') {
        for (i++; i < input.length; i++) {
          previousCharacter = input.charAt(i - 1);
          currentCharacter = input.charAt(i);
          nextCharacter = input.charAt(i + 1);
          if (currentCharacter === '/' && previousCharacter === '*') {
            break; // end of multi-line comment
          }
        }
      }
      // Handle single-line comment
      else if (currentCharacter === '/' && nextCharacter === '/') {
        for (i++; i < input.length; i++) {
          previousCharacter = input.charAt(i - 1);
          currentCharacter = input.charAt(i);
          nextCharacter = input.charAt(i + 1);
          if (nextCharacter === '\n' || nextCharacter === '\r') {
            break; // end of single-line comment
          }
        }
      }
      // regular character
      else {
        // Flush whitespace to result
        result += whitespaceBuffer;
        whitespaceBuffer = '';
        result += currentCharacter;
      }
    }
    return result;
  }

}
