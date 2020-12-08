package root.configure;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;
import root.report.interceptor.RestInterceptor;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

@Configuration
public class RestWebMvcConfigurationSupport extends WebMvcConfigurationSupport {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        StringHttpMessageConverter converter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        converters.add(converter);
        FastJsonHttpMessageConverter4 fastConverter = new FastJsonHttpMessageConverter4();
        FastJsonConfig fastConfig =  new FastJsonConfig();
        fastConfig.setCharset(Charset.forName("UTF-8"));
        converter.setWriteAcceptCharset(false);
        fastConfig.setSerializerFeatures(SerializerFeature.PrettyFormat);
        fastConverter.setFastJsonConfig(fastConfig);
        converters.add(fastConverter);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false);
    }

    @Bean
    public MultipartResolver multipartResolver(){
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("UTF-8");
        resolver.setMaxInMemorySize(40960);
        resolver.setMaxUploadSize(50*1024*1024);//50M
        return resolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/report/static/**").addResourceLocations("file:"+AppConstants.getStaticReportPath()+ File.separator);
        registry.addResourceHandler("/report/dynamic/**").addResourceLocations("file:"+AppConstants.getDynamicReportPath()+File.separator);
        registry.addResourceHandler("/ibas2/**").addResourceLocations("file:"+AppConstants.getClientInstallFile()+File.separator);
        registry.addResourceHandler("/**").addResourceLocations("file:"+AppConstants.getReport2()+File.separator);
        registry.addResourceHandler("/app/**").addResourceLocations("file:"+AppConstants.getPhoneapp()+File.separator);
        registry.addResourceHandler("/report/upload/**").addResourceLocations("file:"+AppConstants.getUploadPath()+ File.separator);

    }


    @Override
    protected void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/reportServer/**")
                .allowedOrigins("*")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "DELETE", "PUT")
                .maxAge(36000);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] EXCLUDE_URL = {"/reportServer/fonts/*",
                                "/reportServer/css/*",
                                "/reportServer/js/*",
                                "/reportServer/upload/*",
                                "/reportServer/DBConnection/test",
                                "/reportServer/DBConnection/save",
                                "/reportServer/user/encodePwd",
                                "/reportServer/user/encodePwdReact",
                                "/reportServer/user/login",
                                "/reportServer/user/Reactlogin",
                                "/wechat",
                                "/reportServer/dataCollect/saveTaskInfo"};
        registry.addInterceptor(new RestInterceptor())
                .addPathPatterns("/reportServer/**")
                .excludePathPatterns(EXCLUDE_URL);
    }

//    @Bean
//    public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
//        return new ByteArrayHttpMessageConverter();
//    }
}
