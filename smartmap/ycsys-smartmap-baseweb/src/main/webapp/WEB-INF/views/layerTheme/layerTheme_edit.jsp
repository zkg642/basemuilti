<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false"%>
<%@include file="/WEB-INF/views/common/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<title>羽辰智慧林业综合管理平台-资源管理</title>
<!-- Tell the browser to be responsive to screen width -->
<meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
  <!-- Bootstrap 3.3.6 -->
  <link rel="stylesheet" href="${res}/bootstrap/css/bootstrap.css">
  <!-- iconfont -->
  <link rel="stylesheet" href="${res}/iconfont/iconfont.css">
  <!-- Theme style -->
  <link rel="stylesheet" href="${res}/dist/css/AdminLTE.css">
  <!-- AdminLTE Skins. Choose a skin from the css/skins
       folder instead of downloading all of them to reduce the load. -->
  <link rel="stylesheet" href="${res}/dist/css/skins/_all-skins.css">
  <!-- iCheck -->
  <link rel="stylesheet" href="${res}/plugins/iCheck/flat/blue.css">
  <!-- list -->
  <link href="${res}/plugins/ligerUI/skins/Aqua/css/ligerui-all.css" rel="stylesheet" type="text/css" />
  <!-- 弹出框 -->
  <link href="${res}/plugins/dialog/dialog.css" rel="stylesheet" type="text/css">

	<script src="${res}/js/common/form.js"></script>
    <script src="${res}/plugins/jQuery/jquery-2.2.3.min.js"></script>
	<script src="${res}/plugins/jquery-validation-1.15.1/lib/jquery.form.js"></script>
 	<script src="${res}/plugins/jquery-validation-1.15.1/dist/jquery.validate.min.js"></script>
	<!-- 封装弹出框dialog -->
	<script type="text/javascript" src="${res}/plugins/dialog/jquery.artDialog.source.js"></script>
	<script type="text/javascript" src="${res}/plugins/dialog/iframeTools.source.js"></script>
	<script type="text/javascript" src="${res}/plugins/dialog/unit.js"></script>
<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
<!--[if lt IE 9]>
  <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
  <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
  <![endif]-->
  <style>
        html,body{
            background-color: #ecf0f5
        }
        body{
            overflow-y: hidden;
        }
        </style>
</head>
<body>
	<%-- <form method="post" id="form_id">
		<input type="hidden" name="id" value="${layerThemeTheme.id}">
		<table width="100%" border="0" cellpadding="0" cellspacing="0" class="date_add_table">
			<tr>
				<td width="120" class="t_r">上级节点：</td>
				<td>
				<input type="text" name="parent.name" id="parent.name"
					disabled="disabled" class="text validate[required]"
					value="${layerThemeTheme.parent.name}" />
				</td>
				<input type="hidden" name="parent.id" id="parentId"
					value="${layerThemeTheme.parent.id}">
			</tr>
			<tr>
				<td class="t_r">结点名称：</td>
				<td><input type="text" name="name" id="name" size="15"
					value="${layerThemeTheme.name }" class="text validate[required]"
					validate="{required:true,maxlength:15,messages:{required:'必填',maxlength:'结点名称 的字符长度大于15个字符！'}}" /></td>
			</tr>
		</table>
	</form> --%>
	<form method="post" id="form_id">
		<input type="hidden" name="id" value="${layerTheme.id}">
		<table width="100%" border="0" cellpadding="0" cellspacing="0" class="date_add_table">
			<tr>
				<td width="120" class="t_r">上级节点：</td>
				<td>
				<%-- <input type="text" name="pId" id="layerThemeThemePid"
					disabled="disabled" class="text validate[required]"
					value="${layerThemeTheme.parentId}" /> --%>
					<input type="text" name="pid" id="layerThemePid"
					disabled="disabled" class="text validate[required]"
					value="${pid}" />
				</td>
				
				<input type="hidden" name="pId" id="pId"
					value="${layerTheme.pId}">
			</tr>
			<tr>
				<td class="t_r">结点名称：</td>
				<td><input type="text" name="name" id="name" size="15"
					value="${layerTheme.name }" class="text validate[required]"
					validate="{required:true,maxlength:15,messages:{required:'必填',maxlength:'结点名称 的字符长度大于15个字符！'}}" /></td>
			</tr>
		</table>
	</form>
</body>
<script type="text/javascript">
	$(function() {
		var form = $("#form_id");
		var val_obj = exec_validate(form);//方法在 ${res}/js/common/form.js
		form.validate(val_obj);
		
		var parentWin = window.parent;
		var dialog = parentWin.art.dialog.list["editLayerThemeDialog"];
		var dialog_div = dialog.DOM.wrap;
		
		dialog_div.on("ok", function() {
            var counts = $('div.l-exclamation'); //填的不对的记录数
			if(counts.length < 1) {
				$.ajax({
					type : "POST",
					url : "${ctx }/layerTheme/save",
					data : $('#form_id').serialize(),
					dataType:"json",
					async : false,
					error : function(request) {
						alert("Connection error");
					},
					success : function(ret) {
						dialog.close();
						if(ret.msg=="新增成功！"){
							$.Layer.confirm({
            	                msg:"保存成功！",
            	                fn:function(){
            	                 //treeManager.reload();
            	                 //dialog.close();
            	                 parentWin.treeManager.reload();
            	                 parentWin.gridManager.reload();
            	                }
            	            });
						}
						if(ret.msg=="修改成功！"){
							$.Layer.confirm({
            	                msg:"修改成功！",
            	                fn:function(){
            	                 parentWin.treeManager.reload();
            	                 parentWin.gridManager.reload();
            	                }
            	            });
						}
						if(ret.msg=="图层节点不能再新增节点！"){
							$.Layer.confirm({
            	                msg:"图层节点不能再新增节点！",
            	            });
						}
							//parentWin.treeManager.reload();
						/* if(ret.flag=="1" ||ret.flag=="3" ) {
							$.Layer.confirm({
            	                msg:"保存成功！",
            	                fn:function(){
            	                 treeManager.reload();
            	                 gridManager.reload();
            	                }
            	            });
							parentWin.treeManager.reload();
						} */
						//parentWin.layerThemeTheme.hideBtn(); //隐藏新增按钮
					}
				}); 
			}
			else {
				alert("请重新填写那些有误的信息！");
			}
		});
	});
</script>
</html>
