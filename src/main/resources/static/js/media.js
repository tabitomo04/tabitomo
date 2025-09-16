 // youtube url 출력 js

    $(document).ready(function() {
		document.querySelectorAll('oembed[url]').forEach(element => {
			var url = element.attributes.url.value;
			var regExp = /^.*((youtu.be\/)|(v\/)|(\/u\/\w\/)|(embed\/)|(watch\?))\??v?=?([^#\&\?]*).*/;
			var matchs = url.match(regExp);
			element.parentElement.outerHTML = `
    <div style="position: relative; padding-bottom: 100%; height: 0; padding-bottom: 56.2493%;">
        <iframe src="https://www.youtube.com/embed/${matchs[7]}?rel=0" style="position: absolute; width: 100%; height: 100%; top: 0; left: 0;" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen=""></iframe>
    </div>`;
		});
	})

function convertYoutubeLinkToIframe(url) {
    if(!url) return "";

    var regExp = /^.*((youtu.be\/)|(v\/)|(\/u\/\w\/)|(embed\/)|(watch\?))\??v?=?([^#\&\?]*).*/;
    var matchs = url.match(regExp);
    if(!matchs || !matchs[7]) return "";

    return `<div style="position: relative; padding-bottom: 56.25%; height: 0;">
        <iframe src="https://www.youtube.com/embed/${matchs[7]}?rel=0"
                style="position: absolute; width: 100%; height: 100%; top: 0; left: 0;"
                frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>
    </div>`;
}