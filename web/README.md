## 目录结构

-- webview                  //webview接入示例
    -- webview              //白板webview页面。用户应该将其部署到自己的静态服务器中
        -- sdk
            -- NIM_Web_NIM
            -- NIM_Web_WhiteBoard
            -- DrawPlugin
            -- PageControl
            -- Toolbar
            -- vconsole
            -- webview
        -- css
            -- webview.css          
        -- webview.html     //用户客户端应该接入此html
        -- webview.log.html //带日志的webview.html, 开发/调试阶段使用

    -- index.html           //模拟客户端接入白板webview的示例
    -- index.iframe.html    //使用iframe接入白板webview的示例

-- sdk                      //sdk接入示例
    -- sdk                  //sdk文件目录
        -- NIM_Web_NIM
        -- NIM_Web_WhiteBoard
        -- DrawPlugin
        -- PageControl
        -- Toolbar

## webview示例

```bash
1. cd webview
2. 修改config.js, 在`getParams`函数中填入必要参数
3. 开启静态服务器，例如：`npx serve ./`
```

## sdk接入示例

sdk接入适合于需要定制化部分功能，UI的用户。sdk接入时，需要用户自行处理文档转码相关逻辑

```bash
1. cd sdk
2. 修改config.js, 在`getParams`函数中填入必要参数
3. 开启静态服务器，例如：`npx serve ./`
```

## IM账号与参数设置

1. 用户先联系云信团队，询问appKey，然后申请若干个im账号进行测试
2. 测试时，先将account和ownerAccount设置为同一个。这样首次登录时，会根据channelName创建房间
3. 保持上一个设置打开的网页不动。修改account，打开另一个tab
4. 第一个tab和第二个tab之间应该能够互通。第一个tab是教师端的白板。第二个tab是学生端的白板。