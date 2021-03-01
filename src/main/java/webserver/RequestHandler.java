package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
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
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            String requestUrl = null;
            String requestMethod = null;
            int contentLength = 0;
            while (!"".equals(line = bufferedReader.readLine())) {
                log.debug(line);
                if (line == null) {
                    return;
                }

                if (HttpRequestUtils.isURLRequest(line)) {
                    requestMethod = HttpRequestUtils.getRequestMethod(line);
                    requestUrl = HttpRequestUtils.getRequestUrl(line);
                    log.debug("Request URL : {}", requestUrl);
                }

                HttpRequestUtils.Pair header = HttpRequestUtils.parseHeader(line);
                if(header != null && header.getKey().equals("Content-Length")) {
                    contentLength = Integer.parseInt(header.getValue());
                }
            }

            String requestBody = getRequestBody(bufferedReader, contentLength);
            log.debug("requestBody : {}", requestBody);

            byte[] body = "Hello Peter".getBytes();
            if (isHtmlFileRequestUrl(requestUrl)) {
                body = HttpRequestUtils.readDataFromUrl(requestUrl);
            }
            if (isGetUserCreateUrl(requestMethod, requestUrl)) {
                handleCreateUserFromGetRequest(requestUrl);
            }
            if (isPostUserCreateUrl(requestMethod, requestUrl)) {
                handleCreateUserFromPostRequest(requestBody);
            }

            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void handleCreateUserFromPostRequest(String requestBody) {
        Map<String,String> paramsMap = HttpRequestUtils.parseQueryString(requestBody);
        User user = newUserFromParamsMap(paramsMap);
        log.debug("user : {}", user.toString());
    }

    private boolean isPostUserCreateUrl(String requestMethod, String requestUrl) {
        return requestMethod != null
                && requestUrl != null
                && requestMethod.equals("POST")
                && requestUrl.equals("/user/create");
    }

    private String getRequestBody(BufferedReader bufferedReader, int contentLength) throws IOException {
        if(contentLength > 0) {
            return IOUtils.readData(bufferedReader, contentLength);
        }
        return null;
    }

    private void handleCreateUserFromGetRequest(String requestUrl) {
        String params = getQueryStringFromUrl(requestUrl);
        Map<String,String> paramsMap = HttpRequestUtils.parseQueryString(params);
        User user = newUserFromParamsMap(paramsMap);
        log.debug("user : {}", user.toString());
    }

    private User newUserFromParamsMap(Map<String, String> paramsMap) {
        return new User(paramsMap.get("userId"),
                paramsMap.get("password"),
                paramsMap.get("name"),
                paramsMap.get("email"));
    }

    private String getQueryStringFromUrl(String requestUrl) {
        int index = requestUrl.indexOf("?");
        String requestPath = requestUrl.substring(0, index);
        return requestUrl.substring(index+1);
    }

    private boolean isGetUserCreateUrl(String requestMethod, String requestUrl) {
        return requestMethod != null
                && requestMethod.equals("GET")
                && requestUrl != null
                && requestUrl.contains("/user/create");
    }

    private boolean isHtmlFileRequestUrl(String requestUrl) {
        return requestUrl != null && !requestUrl.equals("/") && requestUrl.indexOf(".html") > 0;
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
