// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.MapTableControl = function() {
  scout.MapTableControl.parent.call(this);
  this.cssClass = 'map';
  this.countryColumns;
};
scout.inherits(scout.MapTableControl, scout.TableControl);

scout.MapTableControl.FILTER_KEY = 'MAP';

scout.MapTableControl.prototype._renderContent = function($parent) {
  var that = this,
    countries = this.map.objects.countries.geometries,
    tableCountries;

  this.$contentContainer = $parent
    .appendSVG('svg', '', 'map-container')
    .attrSVG('viewBox', '5000 -100000 200000 83000')
    .attrSVG('preserveAspectRatio', 'xMidYMid');

  if (!this.countryColumns) {
    this.countryColumns = this._resolveColumnIds();
  }
  tableCountries = this._findCountries();

  this._filterResetListener = this.table.events.on(scout.Table.GUI_EVENT_FILTER_RESETTED, function(event) {
    that.$contentContainer.find('.map-item.selected').removeClassSVG('selected');
  });

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
          arc = reverse ? -1 - border[a] : border[a],
          localArray = [],
          x, y;

        // loop all points of arc
        for (var s = 0; s < this.map.arcs[arc].length; s++) {
          var line = this.map.arcs[arc][s];

          // first point is absolute, all other delta
          if (s === 0) {
            // TODU CRU: alaska and russia have overlap
            if ((countries[c].id === 'Russland') && (line[0] < 3000)) {
              line[0] += 100000;
            }
            x = line[0];
            y = line[1];
          } else {
            // TODO CRU: some pacific islands
            if (Math.abs(line[0]) > 8000) {
              line[0] = 0;
            }
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
    var $country = this.$contentContainer.appendSVG('path', countries[c].id, 'map-item')
      .attr('d', pathString)
      .click(onMapClick);

    if (tableCountries.indexOf(countries[c].id) > -1) {
      $country.addClassSVG('has-data');
    }
  }

  function onMapClick(event) {
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
    that.$contentContainer.find('.map-item.selected').each(function() {
      countries.push($(this).attr('id'));
    });

    if (countries.length) {
      var filterFunc = function($row) {
        for (var c = 0; c < that.countryColumns.length; c++) {
          var column = that.countryColumns[c];
          var row = $row.data('row');
          var cellText = that.table.cellText(column, row);
          if (countries.indexOf(cellText) > -1) {
            return true;
          }
        }
        return false;
      };

      var filter = that.table.getFilter(scout.MapTableControl.FILTER_KEY) || {};
      filter.label = that.tooltipText;
      filter.accept = filterFunc;
      that.table.registerFilter(scout.MapTableControl.FILTER_KEY, filter);
    } else {
      that.table.unregisterFilter(scout.MapTableControl.FILTER_KEY);
    }

    that.table.filter();
  }
};

scout.MapTableControl.prototype._removeContent = function() {
  this.$contentContainer.remove();
  this.table.events.removeListener(this._filterResetListener);
};

scout.MapTableControl.prototype._removeMap = function() {
  this.removeContent();
};

scout.MapTableControl.prototype._renderMap = function(map) {
  this.renderContent();
};

scout.MapTableControl.prototype.isContentAvailable = function() {
  return !!this.map;
};

scout.MapTableControl.prototype._resolveColumnIds = function() {
  var i, j, column, countryColumns = [];
  for (i = 0; i < this.table.columns.length; i++) {
    for (j = 0; j < this.columnIds.length; j++) {
      column = this.table.columns[i];
      if (column.id === this.columnIds[j]) {
        countryColumns.push(column);
      }
    }
  }
  return countryColumns;
};

/**
 * @return all countries in the table
 */
scout.MapTableControl.prototype._findCountries = function() {
  var column, row, i, j, countryName,
    tableCountries = [];

  for (i = 0; i < this.countryColumns.length; i++) {
    column = this.countryColumns[i];
    for (j = 0; j < this.table.rows.length; j++) {
      row = this.table.rows[j];
      countryName = this.table.cellText(column, row);
      if (tableCountries.indexOf(countryName) === -1) {
        tableCountries.push(countryName);
      }
    }
  }

  return tableCountries;
};
