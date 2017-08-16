/**
 * Created by BYM on 2016/8/29.
 */
//初次加载username到cookie当中
(function(){
	//向cookie中写死一个用户，因为现在前端登录界面没有开发出来
	document.cookie ="vincent";
})();

var second = 0;
window.setInterval(function () {
    second ++;
}, 1000);

//关闭、刷新页面之前，页面上埋点 - vincent - 2017年8月15日
window.onbeforeunload = function() {

    var dataArr = {
            'url' : location.href,
            'time' : second,
            'refer' : getReferrer(),
            'timeIn' : Date.parse(new Date()),
            'timeOut' : Date.parse(new Date()) + (second * 1000)
    };
    
    var params = {};
    if(dataArr){
    	params.url = location.href || '';
    	params.time = dataArr.time || '';
    	params.refer = getReferrer() || '';
    	params.timeIn = dataArr.timeIn || '';
    	params.timeOut = dataArr.timeOut || '';
    }
    //Document对象数据
    if (document) {
    	//每次拿取用户名称
    	params.username = document.cookie || '';
        params.domain = document.domain || '';
        params.title = document.title || '';
    }
    //navigator对象数据,获取用户的默认语言
    if (navigator) {
        params.lang = navigator.language || '';
    }
 
    //拼接参数串，内置函数查询到的信息
    var args = '';
    for (var i in params) {
        if (args != '') {
            args += '&';
        }
        args += i + '=' + params[i];
    }
  
    //通过Image对象请求后端脚本
    var img = new Image(1, 1);
    var src = 'http://192.168.##￥.#@:8091/data/dataCollection/log.gif?args=' + encodeURIComponent(args);
    img.src = src; 
};

function getReferrer() {
    var referrer = '';
    try {
        referrer = window.top.document.referrer;
    } catch(e) {
        if(window.parent) {
            try {
                referrer = window.parent.document.referrer;
            } catch(e2) {
                referrer = '';
            }
        }
    }
    if(referrer === '') {
        referrer = document.referrer;
    }
    return referrer;
}
