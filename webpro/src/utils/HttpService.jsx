import { request } from 'umi';

import LocalStorge from './LogcalStorge.jsx';
const localStorge = new LocalStorge();
class BreakSignal { }

export default class HttpService {


    static getBaseUrl() {

        if (process.env.NODE_ENV === 'development') {
            return '/api';
        } else {
            return window.getServerUrl();
        }
    }

    static isLogin(url) {
        return (undefined == localStorge.getStorage('userInfo') && url != '/reportServer/user/encodePwd' && url != '/reportServer/user/Reactlogin') || ('' == localStorge.getStorage('userInfo') && url != '/reportServer/user/encodePwd' && url != '/reportServer/user/Reactlogin');
    }

    static post(url, params) {
        if (this.isLogin(url)) {
            window.location.href = '#/user/login';
            return new Promise((resolve, reject) => { });
        } else {
            const fullUrl = HttpService.getBaseUrl() + (url.substring(0, 1) == '/' ? '' : '/') + url;
            //console.log('fullUrl', fullUrl)
            let opts = {
                method: 'POST',
                data: params || '',
                headers: {
                    'credentials': JSON.stringify(localStorge.getStorage('userInfo') || ''),
                    'Content-Type': 'text/plain;charset=UTF-8',
                    'Accept': '*/*'
                }
            }
            return new Promise(function (resolve, reject) {
                request(fullUrl, opts).then((response) => {
                    //console.log('HttpService then', response);
                    if (typeof response == 'undefined') {
                        reject('网络异常')
                    } else {
                        resolve(response)
                    }
                }).catch(BreakSignal, () => { })
            });
        }
    }

    // static getFile(url) {
    //     if (this.isLogin(url)) {
    //         window.location.href = '#login';
    //         return new Promise((resolve, reject) => { });
    //     } else {
    //         const fullUrl = HttpService.getBaseUrl() + url;
    //         let opts = {
    //             method: 'GET',
    //             headers: {
    //                 credentials: JSON.stringify(localStorge.getStorage('userInfo') || '')
    //             },
    //         };

    //         return fetch(fullUrl, opts);
    //     }
    // }

    // static uploadImage(url, imageFile, params) {
    //     if (this.isLogin(url)) {
    //         window.location.href = '#login';
    //         return new Promise((resolve, reject) => { });
    //     } else {
    //         return new Promise(function (resolve, reject) {
    //             let formData = new FormData();
    //             if (params != null && typeof params != 'undefinde') {
    //                 for (var key in params) {
    //                     formData.append(key, params[key]);
    //                 }
    //             }

    //             formData.append("file", imageFile);
    //             const fullUrl = HttpService.getBaseUrl() + url;
    //             fetch(fullUrl, {
    //                 method: 'POST',
    //                 headers: {
    //                     'Content-Type': 'multipart/form-data;charset=utf-8',
    //                     credentials: JSON.stringify(localStorge.getStorage('userInfo') || '')
    //                 },
    //                 body: formData,
    //             }).then((response) => response.json())
    //                 .then((responseData) => {
    //                     console.log('uploadImage', responseData);
    //                     resolve(responseData);
    //                 })
    //                 .catch((err) => {
    //                     console.log('err', err);
    //                     reject(err);
    //                 });
    //         });
    //     }

    // }


}
