;(function($){
	function create(elem,appendTo) {
		var $elem = elem
		,$append = appendTo
		,$tr = $($elem[0].rows)
		,selfTR,i=0;

		if ($tr.length > 0) {
			while (selfTR = $tr[i++]) {
				if ('tr' === selfTR.tagName.toLowerCase()) {
					var tr = document.createElement('tr')
					,th = document.createElement('th');
					th.style.cssText='margin:0;padding:0;width:16px';
					$(th).appendTo(selfTR);

					if ('' !== selfTR.className)
						$(tr).addClass(selfTR.className);

					//ths
					var cell,j=0;
					while (cell = selfTR.cells[j++]) {
						$(cell).css({
							width: $(cell).width()
							,backgroundColor: $(cell).css('backgroundColor')
						}).clone(true).attr({
							rowSpan: cell.rowSpan,
							colSpan: cell.colSpan
						}).appendTo(tr);
					}
					$(tr).appendTo($append);
				}
			}
			$elem[0].style.visibility = 'hidden'
		}
	};

	$.fn.scrollTable = function(options){
		var options = $.extend({},$.fn.scrollTable.defaults,options);
		return this.each(function(){
			var $self = $(this)
			,selfDOM = this
			,selfWidth = $self.width()
			,$tableMain = $self.children('table')
			,tableMainDOM = $tableMain[0];

			if (!$self.hasClass('hasGrid')) {
				$self.addClass('hasGrid').width('100%');
				
				if (0 === $tableMain.length)
					return;
				
				$tableMain.width(selfWidth);
					
				var divTableTemplate = '<div><table class="'+options.tableClassName+'"><thead></thead></table></div>'
				,$divHeader = $(divTableTemplate).addClass('divHeader').insertBefore(selfDOM)
				,$tableThead = $(tableMainDOM.tHead)
				,$tableTfoot = (null === tableMainDOM.tFoot)
					? false
					: $(tableMainDOM.tFoot)
				,attrTable = {
					cellSpacing: tableMainDOM.cellSpacing||'',
					cellPadding: tableMainDOM.cellPadding||''
				}
				,$tableHeaderThead = $divHeader.children('table').each(function(){
					var $caption = $('caption',tableMainDOM)
					if ($caption.length) {
						$(this).append($caption[0]);
					}
				}).attr(attrTable).width(selfWidth).children('thead');
			
				// Create
				create($tableThead,$tableHeaderThead);
				
				if ($tableTfoot) {
					var $divFooter = $(divTableTemplate).addClass('divFooter').insertAfter(selfDOM),
					$tableFooterThead = $divFooter.children('table').attr(attrTable).children('thead');
					create($tableTfoot,$tableFooterThead);
				}

				$self.css({
					height: options.height
					,overflowY: 'scroll',overflowX: 'hidden'
				});

				$tableMain.css({marginTop: parseInt($divHeader.height())*(-1)});

				($.isFunction(options.onComplete) && options.onComplete.call(selfDOM,$divHeader[0],(($tableTfoot) ? $divFooter[0] : null)));
			};
		});
	};
	
	$.fn.scrollTable.defaults = {
		height:200
		,tableClassName:''
		,onComplete:null
	};
})(jQuery);
