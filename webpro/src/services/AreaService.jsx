import HttpService from '@/utils/HttpService.jsx';

export default class AreaService {
    // 添加网关
    getArea(param) {
        let url = "/reportServer/area/getArea";
        return HttpService.post(url, JSON.stringify(param));
    }


    getGatewayArea(param) {
        let url = "/reportServer/area/getGatewayArea";
        return HttpService.post(url, JSON.stringify(param));
    }
}

