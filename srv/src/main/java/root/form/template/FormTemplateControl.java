package root.form.template;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;



@RestController
@RequestMapping("/reportServer/formTemplate")
public class FormTemplateControl {
	
	
	//返回所有的填报模板
	@RequestMapping(value = "/getAll", produces = "text/plain;charset=UTF-8")
	public String getAll() {
	
		//查询模板表中的模板，并返回
		
		
		return "";
		
	}
	
    //上传并创建模板
	@RequestMapping("/create")
	public String create(HttpServletRequest request) {
	
		//上传文件
		//新增模板表中的记录
		
		return "";
		
	}
	
	//删除模板
	@RequestMapping(value = "/delete", produces = "text/plain;charset=UTF-8")
	public String delete() {
	
		return "";
		
	}

}
