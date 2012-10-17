function constExpression(x) {
	return x;
}

function simplifyCSSExpression() {
	try {
		var ss,sl, rs, rl;
		ss = document.styleSheets;
		sl = ss.length
	
		for (var i = 0; i < sl; i++) {
			simplifyCSSBlock(ss[i]);
		}
	}
	catch (exc) {
		alert("Got an error while processing css. The page should still work but might be a bit slower");
		throw exc;
	}
}

function simplifyCSSBlock(ss) {
	var rs, rl;
	
	for (var i = 0; i < ss.imports.length; i++)
		simplifyCSSBlock(ss.imports[i]);
	
	if (ss.cssText.indexOf("expression(constExpression(") == -1)
		return;

	rs = ss.rules;
	rl = rs.length;
	for (var j = 0; j < rl; j++)
		simplifyCSSRule(rs[j]);
	
}

function simplifyCSSRule(r) {
	var str = r.style.cssText;
	var str2 = str;
	var lastStr;
	do {
		lastStr = str2;
		str2 = simplifyCSSRuleHelper(lastStr);
	} while (str2 != lastStr)

	if (str2 != str)
		r.style.cssText = str2;
}

function simplifyCSSRuleHelper(str) {
	var i, i2;
	i = str.indexOf("expression(constExpression(");
	if (i == -1) return str;
	i2 = str.indexOf("))", i);
	var hd = str.substring(0, i);
	var tl = str.substring(i2 + 2);
	var exp = str.substring(i + 27, i2);
	var val = eval(exp)
	return hd + val + tl;
}

if (/msie/i.test(navigator.userAgent) && window.attachEvent != null) {
	window.attachEvent("onload", function () {
		simplifyCSSExpression();
	});
}