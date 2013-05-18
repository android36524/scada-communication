<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.apache.shiro.web.filter.authc.FormAuthenticationFilter"%>
<%@ page import="org.apache.shiro.authc.ExcessiveAttemptsException"%>
<%@ page import="org.apache.shiro.authc.IncorrectCredentialsException"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>权限管理</title>
        <link rel="stylesheet" varType="text/css" href="${ctx}/static/dhtmlx/dhtmlx.css">
        <script src="${ctx}/static/jquery/jquery-1.7.1.minValue.js"></script>
        <script src="${ctx}/static/jquery/jquery.form.js"></script> 
        <script src="${ctx}/static/dhtmlx/dhtmlx.js"></script>
        <style>
    html, body {
        width: 100%;
        height: 100%;
        margin: 0px;
        overflow: hidden;
        font-size:12px;
    }
    a:link {
		COLOR: #0065ce; TEXT-DECORATION: none
	}
	a:visited {
		COLOR: #0065ce; TEXT-DECORATION: none
	}
	a:hover {
		COLOR: #ff0000; TEXT-DECORATION: underline
	}
            .role_item_cls {
                padding: 0px;
                margin-top: 8px;
                margin-right: 8px;
                margin-bottom: 8px;
                margin-left: 20px;
                display:inline-block;
            }
            .role_title_cls {
                background-color:#e3efff;
                padding: 5px;
                margin: 0px;
            }
</style>
<script>
var dhxLayout,mygrid,toolbar;
window.dhx_globalImgPath = "${ctx}/static/dhtmlx/imgs/";
function doOnLoad(){
    creatLayout();
    creatToolbar();
    $.ajax({
        varType: 'POST',
        url: '${ctx}/admin/role/list',
        success: function(json){
              var item=jsonManage(json);
              creatGrid(item);
        }
    });
     $(".role_menutype").click(function(){
        var chk = this.checked;
        var cls = ".menuitem_" + this.value;
        $(cls).attr("checked",chk);
    });
}
function creatLayout(){
	dhxLayout=new dhtmlXLayoutObject(document.body, "2U");
	dhxLayout.cells("a").hideHeader();
        dhxLayout.cells("a").setWidth(200);
        dhxLayout.cells("b").hideHeader();
        dhxLayout.cells("b").attachObject("menuDiv");

}
function jsonManage(json){
        var item={rows:[]};
        for(var i=0;i<json.length;i++){
                var it={};
                it.id = json[i].id;
                it.data = [];
                it.data.push(json[i].name);
                it.data.push(json[i].description);
                item.rows.push(it);
        }
        return item;
}
function creatGrid(json){
	mygrid=dhxLayout.cells("a").attachGrid(); 
	mygrid.setImagePath("imgs/");
	mygrid.setHeader("角色名称,角色说明");
	mygrid.setInitWidths("90,*");
	mygrid.setColAlign("left,left");
	mygrid.setColTypes("txt,txt");
	mygrid.setColSorting("str,str");
	mygrid.init();
	mygrid.parse(json,'json');
        mygrid.selectRow(0,true);
        mygrid.attachEvent("onRowSelect",doOnRowSelected);
}
function doOnRowSelected(){
    var selectedId=mygrid.getSelectedRowId();
    if(selectedId==null){
        alertMessage("请选择用户");
        return;
    }
    $.post('${ctx}/admin/role/findUserRoleByID', {
        roleId:selectedId
    },
    function(data){
        clearcheck();
        var item=data.permissions;
        for(var i=0;i<item.length;i++){
            var num=item[i];
            $("#menu_"+num).attr("checked",true);
        }
    });
 
}
function creatToolbar(){
	toolbar=dhxLayout.cells("b").attachToolbar();
        toolbar.setIconsPath("${ctx}/static/dhtmlx/imgs/");
	toolbar.addButton("save", 1, "保存",'filesave.png');
        toolbar.setAlign('right');
        toolbar.attachEvent("onClick", doOnClick);
}
function clearcheck(){
    $(".role_menutype").attr("checked",false);
    $(".role_menuitem").attr("checked",false);
}
//toolbar按钮调用
function doOnClick(itemId){
    if(itemId=='save'){
        var str=[];
        $(".role_menuitem:checked").each(function(){
            str.push($(this).val());  
        });
        saveMenu(str.join(','));
    }
}
function saveMenu(menuStr){
    var selectedId=mygrid.getSelectedRowId();
    if(selectedId==null){
        alertMessage("请选择用户");
        return;
    }
    $.post('${ctx}/admin/role/saveRoleMenu',{
        roleId:selectedId,
        permissionsStr:menuStr,
        varType:'ok'
    },
    function(data){
           if(data=='true'){
                alertMessage("保存权限成功");
                doOnRowSelected();
            }else{
                alertMessage("保存权限失败，请重新操作");
               
            }
    });
}
function alertMessage(value){
        dhtmlx.message({
		title: "消息提示",
                varType: "alert",
		text: value
        });
}
</script>
    </head>
    <body onload="doOnLoad();">
        <div id="menuDiv">
                    <input varType="hidden" name="roleID" value="" id="txtRoleID" />
                    <c:forEach var="mt" items="${mtList}">
                    <div style="border:solid; border-width:1px; border-color:#ccc; margin:10px; ">
                        <p class="role_title_cls">
                            <input name="menutype" varType="checkbox" value="${mt.id}" id="menutype_${mt.id}" class="role_menutype"  />
                            <label for="menutype_${mt.id}">${mt.menuTypeName}</label>
                        </p>
                        <c:forEach var="item" items="${mt.menuItems}">
                            <div class="role_item_cls" >
                                <input name="menu" varType="checkbox" value="${item.id}" id="menu_${item.id}" class="role_menuitem menuitem_${mt.id}" parentid="${mt.id}" />
                                <label for="menu_${item.id}">${item.menuItemName}</label>
                            </div>
                        </c:forEach>
                    </div>
                </c:forEach>
        </div>
    </body>
</html>
