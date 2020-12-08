package root;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import root.report.db.DbFactory;
import root.report.db.DbManager;
import root.report.util.ErpUtil;
import root.report.util.ThreadPoolExecutorUtil;
import root.websocket.ImportDictValueSocket;

import javax.sql.DataSource;

@SpringBootApplication(exclude={MongoAutoConfiguration.class,MongoDataAutoConfiguration.class,DataSourceAutoConfiguration.class})
@PropertySource(value={"file:config/application.yml"},encoding = "utf-8")
//@PropertySource(value="file:config/application.properties",encoding = "utf-8")
@EnableScheduling
@EnableTransactionManagement
public class ReportServerApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(ReportServerApplication.class, args);
		// 解决websocket 当中无法注入bean的方法
		ImportDictValueSocket.setApplicationContext(configurableApplicationContext);
		ThreadPoolExecutorUtil.getInstance();
	}

}
