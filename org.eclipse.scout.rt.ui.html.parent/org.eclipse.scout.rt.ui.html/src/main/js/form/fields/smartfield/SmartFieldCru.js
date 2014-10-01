// open, smart, tree, code field
$.fn.smart_box = function(field){
  function set(nr, text, error){
    field.data('value', nr);
    field.data('old-text', text);
    field.data('error', error);
    field.val(text);
    field.trigger('change');
    $('body').trigger('update');
  }

  $(".proposal_box").remove();
  var box = $('<div class="proposal_box"></div>');
  field.after(box);

  box.append('<div class="proposal_rows"></div><div class="proposal_footer"><div class="proposal_footer_command">Neu</div><div class="proposal_footer_command">Details</div><div class="proposal_footer_command">Suchen</div></div>');

  var type = field.data('type');
  var text = field.val();
  var select_nr = field.data('value');
  var infinity = field.data('infinity');

  if (!infinity || text || select_nr) {
    var select = $.fn.select(type, select_nr, text, infinity);
    for(var i in select){
      var nr = select[i][0];
      var line_1 = select[i][1];
      var line_2 = select[i][2];
      var inset = new Array(select[i][4] * 3 + 1).join("&nbsp;")

      var cls = (nr == select_nr ? 'proposal_row_focus' : '')
      var html_line_2 = (line_2 ? '<div class="proposal_line_2">' + inset + line_2 + '</div>' : '');
      var row = $('<div class="proposal_row ' + cls + '" data-nr="' + nr + '" data-text="' + line_1 + '"><div class="proposal_line_1">' + inset + line_1  + '</div>' + html_line_2 + '</div>');
      $(".proposal_rows", box).append(row);
    }

    if (select.length == 1) {
      $(".proposal_row").first().addClass("proposal_row_focus");
    }

    if (select_nr) {
      var cancel = $('<div class="proposal_footer_cancel">X</div>');
      $('.proposal_footer', box).append(cancel);
      cancel.mousedown(function(e){
        set(0, '', 0);
        $.fn.smart_box(field);
        e.preventDefault();
        return false;
      });
    }
    else {
      $('.proposal_footer', box).append('<div class="proposal_footer_count">' + select.length + '</div>');
    }
  }
  else {
    $(".proposal_rows", box).append('<div class="proposal_info">Es exisiteren viele Datensätze, bitte schränken sie die Daten weiter ein.</div>');
  }

  $('.proposal_row', box).mousedown(function(){
    $('.proposal_row_focus', box).removeClass("proposal_row_focus");
    $(this).addClass("proposal_row_focus");
  });

  $.fn.scrollbar($('.proposal_rows'));

  field.off();
  field.keydown(function(e){
    if (e.which == 13) {
      var tabindex = $(this).attr('tabindex');
            tabindex++;
            $('[tabindex=' + tabindex + ']').focus();
    }

    if (e.which == 27) {
      field.blur();
    }

    if (e.which == 33 || e.which == 34 || e.which == 38 || e.which == 40) {
      var diff;
      if (e.which == 33) diff = -10;
      if (e.which == 34) diff = 10;
      if (e.which == 38) diff = -1;
      if (e.which == 40) diff = 1;

      var focus = $(".proposal_row").index($(".proposal_row_focus"));
      focus = Math.min(Math.max(0, focus + diff), $(".proposal_row").length - 1);

      $(".proposal_row_focus", box).removeClass("proposal_row_focus");
      $($(".proposal_row").get(focus)).addClass("proposal_row_focus");

      $('.proposal_rows').triggerHandler('show', {row: $(".proposal_row_focus")});
      e.preventDefault();
      return false;
    }

    setTimeout(function(e){
      if (field.val() == field.data('old-text')) return;
      field.data('old-text', field.val());
      field.data('value', null);
      $.fn.smart_box(field);
    }, 1)
  });

  field.blur(function(event){
    var f = $(".proposal_row_focus");
    if (f.length) {
      set(f.data('nr'), f.data('text'), 0);
    } else if (field.val() == '')
      set(0, '', 0);
    else {
      set(0, field.val(), 1);
    }

    box.remove();
  });
}
