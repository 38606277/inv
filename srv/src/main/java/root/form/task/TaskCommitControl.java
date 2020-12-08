package root.form.task;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;




@RestController
@RequestMapping("/reportServer/formTask")
public class TaskCommitControl {
		
		//******************以下为填报人的任务API***********************
		//得到当前用户的填报任务
		@RequestMapping(value = "/getMyTask", produces = "text/plain;charset=UTF-8")
		public String getMyTask() {
		
			//根据用户名，查找当前需要填报的任务
			//select * from fnd_user fu,form_task_lines ftl where fu.user_id=ftl.user_id and stata=0;
			return "";
			
		}
		
		//得到当前用户已经完成的填报任务
		@RequestMapping(value = "/getComlateTask", produces = "text/plain;charset=UTF-8")
		public String getComlateTask() {
		
			//根据用户名，查找已经填报完成的任务需要填报任务
			return "";
			
		}
		
		//接收任务
		@RequestMapping(value = "/ReceiveTask", produces = "text/plain;charset=UTF-8")
		public String ReceiveTask() {
		
			//修改当前任务状态为已经接收
			return "";
			
		}
		
		//提交任务
		@RequestMapping(value = "/CommitTask", produces = "text/plain;charset=UTF-8")
		public @ResponseBody String CommitTask() {
		
			//提交当前任务
			//修改当前任务状态为，已经提交
			//上传excel文件到对应的目录下，目录名为form/template.x/task.x/user
			//修改任务模板提交路径
	
			return "";
			
		}
		
		


}
