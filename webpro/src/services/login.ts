
import HttpService from "@/utils/HttpService"

export interface LoginParamsType {
  username: string;
  password: string;
  mobile: string;
  captcha: string;
  type: string;
}

export interface LoginParams {
  UserCode: string,
  Pwd: string
  import?: string
  isAdmin?: string
  encodePwd?: string
}

export async function encodePwd(params: string) {
  return HttpService.post("/reportServer/user/encodePwd", params);
}

export async function fakeAccountLogin(params: LoginParams) {
  return HttpService.post("/reportServer/user/Reactlogin", params);
}

