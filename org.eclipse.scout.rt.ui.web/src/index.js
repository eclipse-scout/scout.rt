import Ocean from './scout/Ocean.js';

function main() {
  var output = document.getElementById('output');
  output.innerHTML = new Ocean().flow();
}

main();
