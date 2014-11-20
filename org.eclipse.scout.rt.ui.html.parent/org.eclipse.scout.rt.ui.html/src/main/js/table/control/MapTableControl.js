// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.MapTableControl = function() {
  scout.MapTableControl.parent.call(this);
};
scout.inherits(scout.MapTableControl, scout.TableControl);

scout.MapTableControl.FILTER_KEY = 'MAP';

scout.MapTableControl.prototype._renderContent = function($parent) {
  this.$container = $parent
    .appendSVG('svg', '', 'map-container')
    .attrSVG('viewBox', '5000 -100000 200000 83000')
    .attrSVG('preserveAspectRatio', 'xMidYMid');

  var that = this,
    countries = this.map.objects.countries.geometries;

  this._filterResetListener = this.table.events.on(scout.Table.GUI_EVENT_FILTER_RESETTED, function(event) {
    that.$container.find('.map-item.selected').removeClassSVG('selected');
  });

  // find all countries in table
  var tableCountries = [];
  for (var i = 0; i < this.table.columns.length; i++) {
    for (var j = 0; j < this.columnIds.length; j++) {
      if (this.table.columns[i].id === this.columnIds[j]) {
        for (var r = 0; r < this.table.rows.length; r++) {
          var value = this.table.rows[r].cells[i];
          if (tableCountries.indexOf(value) === -1) tableCountries.push(value);
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
      var border = (typeof borders[b][0] !== 'number') ? borders[b][0] : borders[b],
        mainArray = [];

      // build arcs of every boundary
      for (var a = 0; a < border.length; a++) {
        // negative arc-numbers are in reverse order
        var reverse = (border[a] < 0),
          arc = reverse ? ~border[a] : border[a],
          localArray = [],
          x, y;

        // loop all points of arc
        for (var s = 0; s < this.map.arcs[arc].length; s++) {
          var line = this.map.arcs[arc][s];

          // first point is absolute, all other delta
          if (s === 0) {
            // todo: alaska and russia have overlap
            if ((countries[c].id === 'Russland') && (line[0] < 3000)) line[0] += 100000;
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
    var $country = this.$container.appendSVG('path', countries[c].id, 'map-item')
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
      for (var c = 0; c < that.table.columns.length; c++) {
        var text = $row.children().eq(c).text();
        if (countries.indexOf(text) > -1) return true;
      }
      return false;
    };

    // callback to table
    // set filter function
    var filter = that.table.getFilter(scout.MapTableControl.FILTER_KEY) || {};
    filter.label = that.label;
    filter.accept = filterFunc;
    that.table.registerFilter(scout.MapTableControl.FILTER_KEY, filter);
    that.table.filter();
  }
};

scout.MapTableControl.prototype._removeContent = function() {
  if (this.$container) {
    this.$container.remove();
  }
};

scout.MapTableControl.prototype.dispose = function() {
  this.table.events.removeListener(this._filterResetListener);
};

scout.MapTableControl.prototype._removeMap = function() {
  this.removeContent();
};

scout.MapTableControl.prototype._renderMap = function(map) {
  this.renderContent();
};

scout.MapTableControl.prototype.isContentAvailable = function() {
  return this.map;
};
