package pl.finsys.servlet.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

public final class HttpProxyServlet extends HttpServlet {

    public static final String UTF_8 = "UTF-8";
    public static final String PARAM_URL = "url";
    private URL url;
    private HttpClient httpClient;

    @Override
    public void init(final ServletConfig config) throws ServletException {

        super.init(config);

        try {
            url = new URL(config.getInitParameter(PARAM_URL));
        } catch (MalformedURLException me) {
            throw new ServletException("URL is invalid", me);
        }
        httpClient = new HttpClient();
        httpClient.getHostConfiguration().setHost(url.getHost());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Map<String, String[]> requestParams = request.getParameterMap();

        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            String name = URLEncoder.encode(entry.getKey(), UTF_8);

            for (String value : entry.getValue()) {
                value = URLEncoder.encode(value, UTF_8);
                query.append(query.length()==0? "?" : "&");
                query.append(String.format("&%s=%s", name, value));
            }
        }

        String uri = String.format("%s%s", url.toString(), query.toString());
        GetMethod getMethod = new GetMethod(uri);

        httpClient.executeMethod(getMethod);
        write(getMethod.getResponseBodyAsStream(), response.getOutputStream());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Map<String, String[]> requestParams = request.getParameterMap();

        String uri = url.toString();
        PostMethod postMethod = new PostMethod(uri);

        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            for (String value : entry.getValue()) {
                postMethod.addParameter(entry.getKey(), value);
            }
        }

        httpClient.executeMethod(postMethod);
        write(postMethod.getResponseBodyAsStream(), response.getOutputStream());
    }

    private void write(final InputStream inputStream, final OutputStream outStream) throws IOException {
        int b;
        while ((b = inputStream.read()) != -1) {
            outStream.write(b);
        }

        outStream.flush();
    }

}
