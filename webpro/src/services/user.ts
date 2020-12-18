
import HttpService from "@/utils/HttpService"
export async function queryMenu(params: number) {
  return HttpService.post("/reportServer/auth/getMenuListNew", { userId: params });
}
