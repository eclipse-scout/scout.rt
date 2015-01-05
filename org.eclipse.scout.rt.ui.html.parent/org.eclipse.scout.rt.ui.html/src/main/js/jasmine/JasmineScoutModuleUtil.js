// Load a module.js, replace all __include tags, and add it as a dynamic script tag to the head section
function createDynamicScript(modulePath) {
  var moduleText=loadAndResolveScriptContent(modulePath);

  var script=document.createElement('script');
  script.async=false;
  script.setAttribute('type', 'text/javascript');
  script.innerHTML = moduleText;
  document.head.appendChild(script);
}

function loadAndResolveScriptContent(path) {
  var content='';
  $.ajax({
    async: false,
    type: 'GET',
    dataType: 'text',
    contentType: 'application/javascript; charset=UTF-8',
    cache: false,
    url: path
  }).done(function(data) {
    content = data;
  }).fail(function(jqXHR, textStatus, errorThrown) {
    throw new Error(textStatus);
  });
  //replace includes
  content = content.replace(
      /(?:\/\/\s*@|__)include\s*\(\s*(?:\"([^\"]+)\"|'([^']+)')\s*\)(?:;)?/g,
      function(match, p1, p2, offset, string) {
        var includePrefix=path.replace(/^(.*\/)[^\/]*$/,'$1');
        var includePath=includePrefix+(p1 ? p1 : p2);
        var stars=includePath.replace(/./g,'*');
        return ''+
        '/***********'+stars+'***********\n'+
        ' ********** '+includePath+' **********\n'+
        ' ***********'+stars+'***********/\n'+
        loadAndResolveScriptContent(includePath);
      }
  );
  return content;
}



