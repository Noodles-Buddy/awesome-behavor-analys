function oncli(){
    //接收用户输入的信息
	//var nameId = document.getElementById('nameId').value;
	//承接 用户输入的信息
	var inputInfo = document.getElementById('inputInfo').value;
	
	//向 缓存 中 添加 用户信息
	//document.cookie =nameId;
	var params = {};
	//params.cookieInfo=document.cookie;
	params.userInput=inputInfo;
	
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
	var src = 'http://192.168.21.18:8091/data/dataCollection/log.gif?args=' + encodeURIComponent(args);
	img.src = src; 	
}