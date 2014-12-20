// Define the __include as using the eval tag for jasmine tests
function __include(path) {
  //console.log('__include '+path);
  var scriptContent = "";
  var errorText = "";
  $.ajax({
    async: false,
    type: 'GET',
    dataType: 'text',
    contentType: 'application/javascript; charset=UTF-8',
    cache: false,
    url: '/src/main/js/' + path
  }).done(function(data) {
    scriptContent = data;
  }).fail(function(jqXHR, textStatus, errorThrown) {
    errorText = textStatus;
  });
  if (errorText !== "") {
    throw errorText;
  }
  eval(scriptContent);
}
