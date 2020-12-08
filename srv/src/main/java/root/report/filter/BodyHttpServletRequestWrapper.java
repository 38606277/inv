package root.report.filter;

import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class BodyHttpServletRequestWrapper extends HttpServletRequestWrapper{
	private byte[] requestBody;
	
	public BodyHttpServletRequestWrapper(HttpServletRequest request)throws IOException{
		super(request);
		//从流中获取数据
		requestBody = IOUtils.toByteArray(request.getInputStream());
	}
	
	@Override  
    public BufferedReader getReader() throws IOException{  
        return new BufferedReader(new InputStreamReader(getInputStream()));  
    }  
  
    @Override  
    public ServletInputStream getInputStream() throws IOException{  
        final ByteArrayInputStream bais = new ByteArrayInputStream(requestBody);  
        return new ServletInputStream() {  
            @Override  
            public int read() throws IOException{ 
                return bais.read();
            }

			@Override
			public boolean isFinished(){
				return bais.available() == 0;
			}

			@Override
			public boolean isReady(){
				return true;
			}

			@Override
			public void setReadListener(ReadListener readListener){
				throw new RuntimeException("Not implemented");
			}  
        };  
    } 
    
    public byte[] getRequestBody(){
    	return this.requestBody;
    }
}
