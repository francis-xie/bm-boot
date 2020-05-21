window.showTips  =function(text){
    var tmplate = '<div id="toast" style="display: none;">\
        <div class="weui-mask_transparent"></div>\
        <div class="weui-toast">\
            <div class="table-wrapper" style="width:100%;    min-height: 7.6em;text-align:center">\
                <div class="table-cell">\
                    <p>%text%</p>\
                </div>\
            </div>\
        </div>\
    </div>';
    var html = tmplate.replace("%text%",text);
    var show = function(str){
        $(html).appendTo($("body")).fadeIn();
        setTimeout(function(){
            $("#toast").fadeOut("fast");
            setTimeout(function(){
                $("#toast").remove();
            },500);
        },3000);
    };
    if($("#toast").length>0){
        return;
    }
    show(html);
}

window.showLoading = function(){
    var tmplate ='<div id="loadingToast" style="display: none;">\
        <div class="weui-mask_transparent"></div>\
        <div class="weui-toast">\
            <i class="weui-loading weui-icon_toast"></i>\
            <p class="weui-toast__content">数据加载中</p>\
        </div>\
    </div>';

    if($("#loadingToast").length>0){
        return;
    }
    $(tmplate).appendTo($("body")).fadeIn();
}

window.hideLoading = function(){
    if($("#loadingToast").length>0){
        $("#loadingToast").fadeOut("fast");
        setTimeout(function(){
            $("#loadingToast").remove();
        },500);
        return;
    }
}


window.showDialog=function(title,text,successCb){
    title = title || '';
    text = text||'';
    successCb = successCb || function(){};

    var tmp = '<div class="js_dialog" id="iosDialog1">\
        <div class="weui-mask"></div>\
        <div class="weui-dialog">\
            <div class="weui-dialog__hd"><strong class="weui-dialog__title">'+title+'</strong></div>\
            <div class="weui-dialog__bd">'+text+'</div>\
            <div class="weui-dialog__ft">\
                <a href="javascript:;" class="weui-dialog__btn weui-dialog__btn_default cancel" >关闭</a>\
                <a href="javascript:;" class="weui-dialog__btn weui-dialog__btn_primary confirm">确定</a>\
            </div>\
        </div>\
    </div>';
    $(tmp).appendTo($("body")).fadeIn();
    $("#iosDialog1 .cancel").on(tap,function(e){
        if($("#iosDialog1").length>0){
            $("#iosDialog1").fadeOut("fast");
            setTimeout(function(){
                $("#iosDialog1").remove();
            },500);
            return;
        }
    });
    $("#iosDialog1 .confirm").on(tap,function(e){
        successCb();
        if($("#iosDialog1").length>0){
            $("#iosDialog1").fadeOut("fast");
            setTimeout(function(){
                $("#iosDialog1").remove();
            },500);
            return;
        }
    });


}
