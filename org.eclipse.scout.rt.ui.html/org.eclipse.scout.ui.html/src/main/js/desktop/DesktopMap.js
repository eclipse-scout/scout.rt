// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.DesktopMap = function($parent, table, model) {
  // create container
  var $mapContainer = $parent.empty()
    .appendSVG('svg', 'MapContainer')
    .attrSVG('viewBox', '5000 -100000 200000 83000')
    .attrSVG("preserveAspectRatio", "xMidYMid"),
    filter = {},
    countries = model.objects.countries.geometries;

  this._table = table;
  this._filterResetListener = table.events.on(scout.Table.GUI_EVENT_FILTER_RESETTED, function(event) {
    $mapContainer.find('.map-item.selected').removeClassSVG('selected');
  });

  // find all countries in table
  var tableCountries = [];
  for (var i = 0; i < table.model.columns.length; i++) {
    for (var j = 0; j < model.columnIds.length; j++) {
      if (table.model.columns[i].id == model.columnIds[j]) {
        for (var r = 0; r < table.model.rows.length; r++) {
          var value = table.model.rows[r].cells[i];
          if (tableCountries.indexOf(value) == -1) tableCountries.push(value);
        }
      }
    }
  }

  // loop all countries and draw path
  for (var c = 0; c < countries.length; c++) {
    var borders = countries[c].arcs,
      pathString = '';

    // per country: loop boundaries
    for (var b = 0; b < borders.length; b++) {
      // inconsistent: if any more than one boundary exists, hidden in sub array
      var border = (typeof borders[b][0] != 'number') ? borders[b][0] : borders[b],
        mainArray = [];

      // build arcs of every boundary
      for (var a = 0; a < border.length; a++) {
        // negative arc-numbers are in reverse order
        var reverse = (border[a] < 0),
          arc = reverse ? ~border[a] : border[a],
          localArray = [],
          x, y;

        // loop all points of arc
        for (var s = 0; s < model.arcs[arc].length; s++) {
          var line = model.arcs[arc][s];

          // first point is absolute, all other delta
          if (s === 0) {
            // todo: alaska and russia have overlap
            if ((countries[c].id == 'Russland') && (line[0] < 3000)) line[0] += 100000;
            x = line[0];
            y = line[1];
          } else {
            // todo: some pacific islands
            if (Math.abs(line[0]) > 8000) line[0] = 0;
            x += line[0];
            y += line[1];
          }

          // transform coordinates
          localArray.push((x * 2) + ',' + (-y));
        }

        // append array
        localArray = reverse ? localArray.reverse() : localArray;
        mainArray = $.merge(mainArray, localArray);
      }

      // build path per boundary
      pathString += 'M' + mainArray.join('L') + 'Z';
    }

    // finally: append country as svg path
    var $country = $mapContainer.appendSVG('path', countries[c].id, 'map-item')
      .attr('d', pathString)
      .click(clickMap);

    if (tableCountries.indexOf(countries[c].id) > -1) $country.addClassSVG('has-data');
  }

  function clickMap(event) {
    var $clicked = $(this);

    if (event.ctrlKey) {
      if ($clicked.hasClassSVG('selected')) {
        $clicked.removeClassSVG('selected');
      } else {
        $clicked.addClassSVG('selected');
      }
    } else {
      $clicked.addClassSVG('selected');
      $clicked.siblings('.selected').removeClassSVG('selected');
    }

    // find filter values
    var countries = [];
    $('.map-item.selected').each(function() {
      countries.push($(this).attr('id'));
    });

    //  filter function
    var filterFunc = function($row) {
      for (var c = 0; c < table.model.columns.length; c++) {
        var text = $row.children().eq(c).text();
        if (countries.indexOf(text) > -1) return true;
      }
      return false;
    };

    // callback to table
    // set filter function
    var filter = table.getFilter(scout.DesktopMap.FILTER_KEY) || {};
    filter.label = model.label;
    filter.accept = filterFunc;
    table.registerFilter(scout.DesktopMap.FILTER_KEY, filter);
    table.filter();
  }
};

scout.DesktopMap.FILTER_KEY = 'MAP';

scout.DesktopMap.prototype.dispose = function() {
  this._table.events.removeListener(this._filterResetListener);
};
