package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestProcessingUtils {
	
	private static final Logger log = LoggerFactory.getLogger(HttpRequestProcessingUtils.class);
	
	private BufferedReader br;
	
	public BufferedReader getBr() {
		return br;
	}
	
	public HttpRequestProcessingUtils(InputStream inputStream) {
		br = new BufferedReader(new InputStreamReader(inputStream));
	}
	
	
	public String[] getHeaderInfo() throws IOException {
	
		return br.readLine().split(" ");
	}
	
	
	public Map<String, String> getDetailHeaderInfo() throws IOException {
		Map<String, String> header = new HashMap<>();
		String line = "";
		
		String[] tokens; 
		
		while (!"".equals(line = br.readLine())) {
			if (line == null) {
				break;
			}
			
			// header 정보
			log.info("header 정보: " + line);
			
			tokens = line.split(": ");
			
			header.put(tokens[0], tokens[1]);
			
		}
		
		return header;
	}
	
	
	public Map<String, String> getQueryStringKeyValue(String values, String delimiter) {
		Map<String, String> queryString = new HashMap<>();
		
		String[] params = null;
		String[] keyValue = null;
		
		if (values != null & values != "") {
			params = values.split(delimiter);
		}
		
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				keyValue = params[i].split("=");
				
				queryString.put(keyValue[0], keyValue[1]);
			}
		}
		
		
		return queryString;
	}
	
	

}
