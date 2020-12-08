package root.report.common;

import org.springframework.web.bind.annotation.RequestBody;

public interface IControl {
	String getInputOutputParas(@RequestBody String pJson);
	
	String saveMetaData(@RequestBody String pJson);
}
