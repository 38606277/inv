import HttpService from '@/utils/HttpService.jsx';

export default class GatewayService {
    // 添加网关
    addGateway(param) {
        let url = "reportServer/gateway/addGateway";
        return HttpService.post(url, JSON.stringify(param));
    }

    //获取网关列表 分页
    getGatewayList(param) {
        let url = "reportServer/gateway/listEamGateway";
        return HttpService.post(url, JSON.stringify(param));
    }

    //获取网关列表 分页
    listGatewayStatus(param) {
        let url = "reportServer/gateway/listGatewayStatus";
        return HttpService.post(url, JSON.stringify(param));
    }

    //获取所有网关-地图
    listEamGatewayByMap(param) {
        let url = "reportServer/gateway/listEamGatewayByMap";
        return HttpService.post(url, JSON.stringify(param));
    }

    //删除网关
    deleteGateway(param) {
        let url = "reportServer/gateway/deleteGateway";
        return HttpService.post(url, JSON.stringify(param));
    }


    bindAssetList(param) {
        let url = "reportServer/gateway/bindAssetList";
        return HttpService.post(url, JSON.stringify(param));
    }

    //获取地点下的网关， 返回tree控件需要的格式
    treeGatewayByAddressId(param) {
        let url = "reportServer/gateway/treeGatewayByAddressId";
        return HttpService.post(url, JSON.stringify(param));
    }

}
