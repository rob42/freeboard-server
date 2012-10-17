/*
 * Copyright 2004-2009 Luciano Vernaschi
 *
 * This file is part of MeshCMS.
 *
 * MeshCMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MeshCMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MeshCMS.  If not, see <http://www.gnu.org/licenses/>.
 */

(function($){
  $.fn.progressBar = function(userOptions) {
    var defaults = {
      width: 300,
      height: 20,
      backgroundColor: 'navy',
      textColor: 'white',
      interval: 500,
      url: 'progress.jsp',
      completedCallback: null,
      hideAtEnd: false
    };

    var options = $.extend(defaults, userOptions);

    return this.each(function() {
      var $cont = $(this);
      var width = options.width - 2;
      var height = options.height - 2;

      $cont.html("<div class='pbEmpty'></div><div class='pbProgress'><div class='pbColored'></div></div>")
      .css('position', 'relative')
      .css('border', '1px inset')
      .css('width', options.width + 'px')
      .css('height', options.height + 'px');

      $cont.find('.pbEmpty')
      .css('position', 'absolute')
      .css('top', '1px')
      .css('left', '1px')
      .css('height', height + 'px')
      .css('line-height', height + 'px')
      .css('width', width + 'px')
      .css('text-align', 'center');

      $cont.find('.pbProgress')
      .css('position', 'absolute')
      .css('top', '1px')
      .css('left', '1px')
      .css('width', '0')
      .css('height', height + 'px')
      .css('line-height', height + 'px')
      .css('overflow', 'hidden')
      .css('text-align', 'center');

      $cont.find('.pbColored')
      .css('width', width + 'px')
      .css('height', height + 'px')
      .css('line-height', height + 'px')
      .css('text-align', 'center')
      .css('background-color', options.backgroundColor)
      .css('color', options.textColor);

      var progress = -1;

      var tmr = setInterval(function() {
        $.getJSON(options.url + '?pbRnd=' + Math.random(), null, function(data) {
          if (data.progress > progress) {
            progress = data.progress;
            $cont.find('.pbEmpty, .pbColored').text(data.text);
            $cont.find('.pbProgress').width(progress * width);
          }

          if (data.completed) {
            clearInterval(tmr);

            if (options.completedCallback) {
              options.completedCallback();
            }

            if (options.hideAtEnd) {
              $cont.hide();
            }
          }
        });
      },
      options.interval);
    });
  };
})(jQuery);
