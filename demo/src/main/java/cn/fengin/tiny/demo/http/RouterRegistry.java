package cn.fengin.tiny.demo.http;

import cn.fengin.tiny.http.Router;
import cn.fengin.tiny.demo.http.controller.admin.*;
import cn.fengin.tiny.demo.http.interceptor.ApiAuthInterceptor;
import cn.fengin.tiny.demo.http.interceptor.UserAuthInterceptor;

import java.util.Arrays;


/**
 * 注册全局拦截器、路由等信息
 *
 * @author fengin
 * @since 1.0.0
 */
public class RouterRegistry {

    public static void register() {
        //1.注册拦截器
        Router.addInterceptor(new ApiAuthInterceptor());

        UserAuthInterceptor userAuthInterceptor = new UserAuthInterceptor();
        userAuthInterceptor.setIgnorePaths(Arrays.asList("/api/admin/login","/api/open/**"));
        Router.addInterceptor(userAuthInterceptor);

        //2.注册路由
        // 初始化服务和控制器
        UserController userController = new UserController();
        DeviceController deviceController = new DeviceController();
        SystemController systemController = new SystemController();
        CenterController centerController = new CenterController();
        AmmeterController ammeterController = new AmmeterController();
        //登录接口
        Router.post("/api/admin/login", userController::login);

        //系统接口
        Router.get("/api/admin/system/info", systemController::getInfo);
        Router.get("/api/admin/system/config", systemController::getConfig);
        Router.post("/api/admin/system/config/update", systemController::updateConfig);

        //设备统计接口
        Router.get("/api/admin/device/info", deviceController::getInfo);
        Router.post("/api/admin/device/config/pull", deviceController::pullConfig);

        //集中器接口
        Router.get("/api/admin/center/list", centerController::list);

        //电表接口
        Router.get("/api/admin/ammeter/list", ammeterController::list);
        Router.get("/api/admin/ammeter/detail", ammeterController::detail);
        Router.post("/api/admin/ammeter/read", ammeterController::read);
    }
}
