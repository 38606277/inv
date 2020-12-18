package root.report.sys;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.configure.AppConstants;
import root.form.user.UserModel;

@RestController
@RequestMapping("/reportServer")
public class SysContext {

    private static ThreadLocal<UserModel> map = new ThreadLocal<UserModel>();
    
    public static void setRequestUser(UserModel userModel)
    {
        map.set(userModel);
    }

    public static UserModel getRequestUser()
    {
        return map.get();
    }

    public static String getUserCode()
    {
        return map.get().getUserName();
    }
	@RequestMapping(value = "/getLambdaUrl", produces = "text/plain;charset=UTF-8")
	public String getLambdaUrl() {
		return AppConstants.getLambdaUrl();
	}
	
}
