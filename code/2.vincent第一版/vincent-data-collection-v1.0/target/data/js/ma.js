(function () {
    
    //************************************
    var a = localStorage.getItem('jsArr');
    
    var b = [{},{"url":"http://localhost:8091/data/page1.html","time":"304","refer":"http://localhost:8091/data/","timeIn":"1500969100000","timeOut":"1500969404000"}];
    //alert("json数组的长度：" + a.length);
    alert("拿到的数据" + a);
  //************************************

    var params = {};
    //Document对象数据
    if (document) {
        params.domain = document.domain || '';
        params.url = document.URL || '';
        params.title = document.title || '';
        params.referrer = document.referrer || '';
    }
    //Window对象数据
    if (window && window.screen) {
        params.sh = window.screen.height || 0;
        params.sw = window.screen.width || 0;
        params.cd = window.screen.colorDepth || 0;
    }
    //navigator对象数据
    if (navigator) {
        params.lang = navigator.language || '';
    }
    
    //拼接参数串，内置函数查询到的信息
    var args = '';
    for (var i in params) {
        // alert(i);
        if (args != '') {
            args += '&';
        }
        args += i + '=' + params[i];
    }
    //补充前台的页面信息
    alert("arg中的数据是：" + args);
    
    //通过Image对象请求后端脚本
    var img = new Image(1, 1);
    //var src = 'http://localhost:8091/data/dataCollection/log.gif?args=' + encodeURIComponent(args);
    var src = 'http://192.168.21.44:8080/data/dataCollection/log.gif?args=' + encodeURIComponent(args);
    img.src = src;
    
})();