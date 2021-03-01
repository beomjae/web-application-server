package util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.junit.Test;

import util.HttpRequestUtils.Pair;

public class HttpRequestUtilsTest {
    @Test
    public void parseQueryString() {
        String queryString = "userId=javajigi";
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(queryString);
        assertThat(parameters.get("userId"), is("javajigi"));
        assertThat(parameters.get("password"), is(nullValue()));

        queryString = "userId=javajigi&password=password2";
        parameters = HttpRequestUtils.parseQueryString(queryString);
        assertThat(parameters.get("userId"), is("javajigi"));
        assertThat(parameters.get("password"), is("password2"));
    }

    @Test
    public void parseQueryString_null() {
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(null);
        assertThat(parameters.isEmpty(), is(true));

        parameters = HttpRequestUtils.parseQueryString("");
        assertThat(parameters.isEmpty(), is(true));

        parameters = HttpRequestUtils.parseQueryString(" ");
        assertThat(parameters.isEmpty(), is(true));
    }

    @Test
    public void parseQueryString_invalid() {
        String queryString = "userId=javajigi&password";
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(queryString);
        assertThat(parameters.get("userId"), is("javajigi"));
        assertThat(parameters.get("password"), is(nullValue()));
    }

    @Test
    public void parseCookies() {
        String cookies = "logined=true; JSessionId=1234";
        Map<String, String> parameters = HttpRequestUtils.parseCookies(cookies);
        assertThat(parameters.get("logined"), is("true"));
        assertThat(parameters.get("JSessionId"), is("1234"));
        assertThat(parameters.get("session"), is(nullValue()));
    }

    @Test
    public void getKeyValue() throws Exception {
        Pair pair = HttpRequestUtils.getKeyValue("userId=javajigi", "=");
        assertThat(pair, is(new Pair("userId", "javajigi")));
    }

    @Test
    public void getKeyValue_invalid() throws Exception {
        Pair pair = HttpRequestUtils.getKeyValue("userId", "=");
        assertThat(pair, is(nullValue()));
    }

    @Test
    public void parseHeader() throws Exception {
        String header = "Content-Length: 59";
        Pair pair = HttpRequestUtils.parseHeader(header);
        assertThat(pair, is(new Pair("Content-Length", "59")));
    }

    @Test
    public void testIsURLRequest() {
        assertTrue(HttpRequestUtils.isURLRequest("GET /index.html HTTP/1.1"));
        assertTrue(HttpRequestUtils.isURLRequest("POST /index.html HTTP/1.1"));
        assertTrue(HttpRequestUtils.isURLRequest("PUT /index.html HTTP/1.1"));
        assertTrue(HttpRequestUtils.isURLRequest("DELETE /index.html HTTP/1.1"));
        assertFalse(HttpRequestUtils.isURLRequest("Host: localhost:8080"));
        assertFalse(HttpRequestUtils.isURLRequest("Connection: keep-alive"));
        assertFalse(HttpRequestUtils.isURLRequest("Cache-Control: max-age=0"));
    }

    @Test
    public void testGetRequestMethod() {
        assertEquals("GET", HttpRequestUtils.getRequestMethod("GET /index.html HTTP/1.1"));
        assertEquals("POST", HttpRequestUtils.getRequestMethod("POST /index.html HTTP/1.1"));
        assertEquals("PUT", HttpRequestUtils.getRequestMethod("PUT /index.html HTTP/1.1"));
        assertEquals("DELETE", HttpRequestUtils.getRequestMethod("DELETE /index.html HTTP/1.1"));
    }

    @Test
    public void testRequestURL() {
        assertEquals("/index.html", HttpRequestUtils.getRequestUrl("GET /index.html HTTP/1.1"));
        assertEquals("/favicon.ico", HttpRequestUtils.getRequestUrl("GET /favicon.ico HTTP/1.1"));
        assertEquals("/", HttpRequestUtils.getRequestUrl("GET / HTTP/1.1"));
    }

    @Test
    public void testReadDataFromUrl() throws IOException {
        assertArrayEquals(Files.readAllBytes(new File("./webapp/index.html" ).toPath()),
                HttpRequestUtils.readDataFromUrl("/index.html"));
        assertArrayEquals(Files.readAllBytes(new File("./webapp/favicon.ico" ).toPath()),
                HttpRequestUtils.readDataFromUrl("/favicon.ico"));
    }


}
