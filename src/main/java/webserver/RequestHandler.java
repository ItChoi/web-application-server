package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestProcessingUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	DataOutputStream dos = new DataOutputStream(out);
        	HttpRequestProcessingUtils hrpu = new HttpRequestProcessingUtils(in);
        	
        	String[] headerInfo = hrpu.getHeaderInfo();
        	Map<String, String> headerDetailInfo = hrpu.getDetailHeaderInfo();
        	
        	String httpMethod = headerInfo[0];
        	String httpUri = headerInfo[1];
        	// String httpVersion = headerInfo[2];
        	
        	Map<String, String> queryString = null;
        	String values = "";
        	
        	
        	if (httpUri.indexOf("?") != -1) {
        		values = httpUri.substring(httpUri.indexOf("?") + 1);
        		httpUri = httpUri.substring(0, httpUri.indexOf("?"));
        		queryString = hrpu.getQueryStringKeyValue(values, "&");
        		
        		log.info("httpUri get: " + httpUri);
        	}

        	User user = null;
        	if ("post".equals(httpMethod.toLowerCase())) {
        		log.info("post로는 온다.: " + httpUri);
        		values = IOUtils.readData(hrpu.getBr(), Integer.parseInt(headerDetailInfo.get("Content-Length")));
        		queryString = hrpu.getQueryStringKeyValue(values, "&");
        		
        		if ("/user/create".equals(httpUri)) {
        			log.info("uri1::: " + httpUri);
        			httpUri = "/index.html";
        			
        			if (queryString != null) {
        				user = new User(queryString.get("userId"),
        						queryString.get("password"),
        						queryString.get("name"),
        						queryString.get("email"));
        				
        				DataBase.addUser(user);
        			}
        			
        		} else if (httpUri.indexOf("/user/login") != -1) {
        			log.info("uri2::: " + httpUri);
        			if (queryString != null) {
        				log.info("로그인해서 넘어온 아디: " + queryString.get("userId"));
        				log.info("로그인해서 넘어온 비번: " + queryString.get("password"));
        				log.info("저장된 아디: " + DataBase.findUserById("enffl").getUserId());
        				log.info("저장된 비번: " + DataBase.findUserById("enffl").getPassword());
        				
        			}
        			
        			
        			
        		}
        		
        	}
        	
        	log.info("1::: " + DataBase.findUserById("11"));
        	log.info("2::: " + DataBase.findUserById("22"));
        	
    		log.info("3::: " + DataBase.findAll());
        	
        	
        	
            
            byte[] body = Files.readAllBytes(new File("./webapp" + httpUri).toPath());
            
            
            if ("/user/create".equals(httpUri)) {
    			response302header(dos, httpUri);
    		} else {
    			response200Header(dos, body.length);
    		}
            
            // response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response200HeaderCookie(DataOutputStream dos, int lengthOfBodyContent, boolean cookie) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Cookie: logined=" + cookie + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response302header(DataOutputStream dos, String url) {
    	// HTTP/1.1 302 Found
    	// Location: http://www.iana.org/domains/example/
    	try {
    		log.info("302왔당....");
    		dos.writeBytes("HTTP/1.1 302 Found \r\n");
    		dos.writeBytes("Location: " + url + "\r\n");
    	} catch (IOException e) {
			e.getStackTrace();
		}
    	
    }
    
    
}