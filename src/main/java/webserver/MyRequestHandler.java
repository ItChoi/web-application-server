/*package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class MyRequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(MyRequestHandler.class);

    private Socket connection;

    public MyRequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	DataOutputStream dos = new DataOutputStream(out);
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
        	
        	byte[] body = null;
        	String line;
        	
        	String cookieCheck = "";
        	
        	int i = 0;
        	String uris = "";
        	String length = "";
        	
        	String cookieLogined = "";
        	String cookieLoginedValue = "";
        	
        	String extensionCheck = "";
        	
        	String[] urlTest = br.readLine().split(" ");
        	
        	Map<String, String> getHeader = new HashMap<>();
    		while (!"".equals(line = br.readLine())) {
    			if (i == 0) {
    				uris += line + " ";
    			}
    			
    			if (line == null) {
    				return;
    			}
    			
    			log.info("line::: " + line);
    			String[] header = line.split(": ");
    			
    			log.info("header key : " + header[0]);
    			log.info("header value : " + header[1]);
    			
    			getHeader.put(header[0], header[1]);
    			
				if (line.indexOf("Content-Length") != -1) {
					length = line.substring(line.indexOf(" ") + 1);
				}
    			
    			i++;
    		}
        	String realPath = urlTest[1];
        	log.info("realPath: " + realPath);
        	
    		log.info("uris: " + uris);
    		extensionCheck = realPath.substring(realPath.indexOf(".") + 1);
    		String extension = extensionCheck.indexOf("css") != -1 ? "css" : "html";
    		
    		int queryStringIndex = realPath.indexOf("?");
    		if (realPath.indexOf("?") != -1) {
    			realPath = realPath.substring(0, realPath.indexOf("?"));
    		}
    		
    		log.info("queryStringIndex: " + queryStringIndex);
    		log.info("extensionCheck: " + extensionCheck);
    		log.info("urlTest[0]: " + urlTest[0]);
    		log.info("urlTest[1]: " + urlTest[1]);
    		
    		User user = null;
    		if ("POST".equals(urlTest[0]) && urlTest[1].indexOf("login") != -1) {
    			
    			String queryString = IOUtils.readData(br, Integer.parseInt(length));
    			log.info("queryString: " + queryString);
    			Map<String, String> userInfo = HttpRequestUtils.parseQueryString(queryString);
    			
    			log.info("userInfo id: " + userInfo.get("password"));
    			log.info("userInfo pw: " + userInfo.get("password"));
    			
    			user = DataBase.findUserById(userInfo.get("userId"));
    			log.info("user::: " + user);
    			if (user != null && user.getUserId() != null && user.getUserId() != "") {
    				log.info("회원가입 성공");
    				urlTest[1] = "/index.html";
    				cookieCheck = "true";
    				cookieLoginedValue = "true";
    			} else {
    				log.info("회원가입 실패");
    				urlTest[1] = "/user/login_failed.html";
    				cookieCheck = "false";
    				cookieLoginedValue = "false";
    			}
    			
    		}
    		
    		if (getHeader.get("Cookie") != null) {
				log.info("getHeader.get(Cookie): " + getHeader.get("Cookie"));
				if (getHeader.get("Cookie").indexOf("logined=") != -1) {
					cookieLogined = getHeader.get("Cookie").substring(
																getHeader.get("Cookie").indexOf("logined="),
																getHeader.get("Cookie").indexOf(";"));
					
					log.info("cookie2: " + cookieLogined);
					if (cookieLogined != "" && cookieLogined != null) {
						String[] loginCheck = cookieLogined.split("=");  
						cookieLoginedValue = loginCheck[1];
						log.info("cookie3-1: " + loginCheck[0]);
						log.info("cookie3-2: " + loginCheck[1]);
					}
				}
				
			}
    		
    		if (queryStringIndex != -1) {
    			
    			String uri = urlTest[1].substring(0, urlTest[1].indexOf("?"));
    			String param = urlTest[1].substring(urlTest[1].indexOf("?") + 1);
    			
    			log.info("uri::: " + uri);
    			log.info("param::: " + param);
    					
    		
    			if (uri.indexOf("create") != -1) {
    				Map<String, String> userInfo = HttpRequestUtils.parseQueryString(param);
    				User user1 = new User(userInfo.get("userId"),
    						userInfo.get("password"),
    						userInfo.get("name"), 
    						userInfo.get("email"));
    				
    				DataBase.addUser(user1);
    			}
    		}
    		
    		
            if (queryStringIndex != -1) {
            	response302Header(dos, "/index.html");
            } else {
            	
            	if ("true".equals(cookieCheck)) {
            		realPath = "/index.html";
            	}
            	
            	File file = new File("./webapp" + realPath);
            	body = Files.readAllBytes(file.toPath());
            			
            	log.info("cookieCheck: " + cookieCheck);
        		log.info("cookieLoginedValue: " + cookieLoginedValue);
        		
    			if ("true".equals(cookieCheck)) {
    				response200HeaderCookie(dos, body.length, true, extension);
    				
    			} else if ("false".equals(cookieCheck)) {
    				log.info("body.length: " + body.length);
    				log.info("extension: " + extension);
    				
    				response200HeaderCookie(dos, body.length, false, extension);
    			} else {
    				response200Header(dos, body.length, extension);
    				
    			}
    			
        		
        		if ("/user/list.html".equals(realPath)) {
        			StringBuilder sb = new StringBuilder();
            		if (cookieLoginedValue != null && !"".equals(cookieLoginedValue) && !"false".equals(cookieLoginedValue)) {
            			sb.append("<tr>")
            			  .append("<th scope=\"row\">3</th>")
            			  .append("<td>" + user.getUserId() + "</td>")
            			  .append("<td>" + user.getName() + "</td>")
            			  .append("<td>" + user.getEmail() + "</td>")
            			  .append("<td>" + "testUserId" + "</td>")
            			  .append("<td>" + "testName" + "</td>")
            			  .append("<td>" + "testEmail" + "</td>")
            			  .append("<td><a href=\"#\" class=\"btn btn-success\" role=\"button\">수정</a></td>");
            			
            			FileReader fr = new FileReader(file);
            			BufferedReader brr = new BufferedReader(fr);
            			
            			String findTag = "";
            			
            			String result = "";
            			while((findTag = brr.readLine()) != null) {
            				result += findTag; 
        					if (findTag.indexOf("<tbody>") != -1) {
        						result += sb.toString();
        					}
            			}

            			body = result.getBytes();
            		}
        			
        		}
        		
        			
            }
            
            responseBody(dos, body);
            
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (InvalidPathException e) {
        	log.error("해당 경로에 적당한 파일이 없네유..... " + e.getMessage());
		}
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String extension) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/" + extension + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    
    private void response200HeaderCookie(DataOutputStream dos, int lengthOfBodyContent, boolean cookie, String extension) {
        try {
        	dos.writeBytes("HTTP/1.1 200 OK \r\n");
        	dos.writeBytes("Content-Type: text/" + extension + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + cookie + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
        	log.error("IOEXception0: " + e.getMessage());
        }
    }
    
    
    
    private void response302Header(DataOutputStream dos, String location) {
        try {
        	dos.writeBytes("HTTP/1.1 302 Found \r\n");
        	dos.writeBytes("Location: " + location + "\r\n");
        } catch (IOException e) {
        	log.error("IOEXception1: " + e.getMessage());
        }
    }
    
    

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error("IOEXception2: " + e.getMessage());
        } catch (NullPointerException e) {
        	log.error("Null 포인트 익셉션..... " + e.getMessage());
        }
    }
}
*/