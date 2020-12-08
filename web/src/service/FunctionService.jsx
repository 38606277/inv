import HttpService from '../util/HttpService.jsx';
import  LocalStorge  from '../util/LogcalStorge.jsx';
const localStorge = new LocalStorge();


export default class FunctionService {
   
    constructor(){
      
    }
    // 获取函数列表
    getFunctionList() {

        let url = "reportServer/function1/getAllFunctionName";
        let param = {
            name: 'Hubot',
            login: 'hubot'
        };
  
        return HttpService.post(url,null);
    }


    // 获取函数列表
    getFunctionByID(funcid) {

        let url = "reportServer/function1/getFunctionByID/"+funcid;
        let param = {
            
        };
  
        return HttpService.post(url,param);
    }

      // 获取SQL的输入输出参数
      getSqlInOut(aSQL) {

        let url = "reportServer/sql/getInputOutputParas";
        let param = {
            sqlType: "sql",
            sql:aSQL
        };
  
        return HttpService.post(url, JSON.stringify(param));
    }

    getAllFunctionClass(){
        let url = "reportServer/function1/getAllFunctionClass";
        
  
        return HttpService.post(url, '');
    }
    saveFunctionClass(param){
        let url = "reportServer/function1/createFunctionClassInfo";
        return HttpService.post(url, JSON.stringify(param));
    }
    updateFunctionClass(param){
        let url = "reportServer/function1/updateFunctionClassInfo";
        return HttpService.post(url, JSON.stringify(param));
    }
    deleteFunctionClss(param){
        let url = "reportServer/function1/deleteFunctionClassInfo";
        return HttpService.post(url, JSON.stringify(param));
    }
    // 保存一个函数定义
    CreateFunction(jFunc) {

        let url = "reportServer/function1/saveUserSql";
        // let param = {
        //     sqlType: "sql",
        //     sql:aSQL
        // };
  
        return HttpService.post(url, JSON.stringify(jFunc));
    }

}
