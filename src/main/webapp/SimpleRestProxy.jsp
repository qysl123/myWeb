<%@page session="false" %>
<%@page import=
                "java.io.*,
                 java.net.HttpURLConnection,
                 java.net.URL,
                 java.net.URLDecoder,
                 java.nio.charset.Charset,
                 java.text.SimpleDateFormat,
                 java.util.*,
                 java.util.logging.FileHandler,
                 java.util.logging.Level,
                 java.util.logging.Logger,
                 java.util.logging.SimpleFormatter" %>

<!-- ----------------------------------------------------------
*
* Simple Rest Proxy - as a JSP : deloyed to each webapp in which
* the javascripts need to call backend rest service
* 暂不处理cookie和header
* Version 1.0 SNAPSHOT
*
----------------------------------------------------------- -->

<%! final String version = "1.0 SNAPSHOT"; %>

<%!
    private String PROXY_REFERER = "http://www.yichengshequ.com:6060/SimpleRestProxy.jsp";
	private String REST_SERVER_BASE = "https://demo-openapi.wjjr.cc";

    private byte[] readRequestPostBody(HttpServletRequest request) throws IOException {
        int clength = request.getContentLength();
        if (clength > 0) {
            byte[] bytes = new byte[clength];
            DataInputStream dataIs = new DataInputStream(request.getInputStream());

            dataIs.readFully(bytes);
            dataIs.close();
            return bytes;
        }

        return new byte[0];
    }

    private HttpURLConnection forwardToServer(HttpServletRequest request, String httpMethod, String uri, byte[] postBody) throws IOException {
        return doHTTPRequest(uri, postBody, httpMethod, request.getHeader("Referer"), "application/json");
    }
    
    private String webResponseToString(InputStream in) throws IOException{
        Reader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
        StringBuffer content = new StringBuffer();
        char[] buffer = new char[5000];
        int n;
    
        while ( ( n = reader.read(buffer)) != -1 ) {
            content.append(buffer,0,n);
        }
        reader.close();
    
        String strResponse = content.toString();
    
        return strResponse;
    }


    private boolean fetchAndPassBackToClient(HttpURLConnection con, HttpServletResponse clientResponse, boolean ignoreAuthenticationErrors) throws IOException {
        if (con != null) {
            Map<String, List<String>> headerFields = con.getHeaderFields();

            Set<String> headerFieldsSet = headerFields.keySet();
            Iterator<String> hearerFieldsIter = headerFieldsSet.iterator();

            while (hearerFieldsIter.hasNext()) {
                String headerFieldKey = hearerFieldsIter.next();
                List<String> headerFieldValue = headerFields.get(headerFieldKey);
                StringBuilder sb = new StringBuilder();
                for (String value : headerFieldValue) {
                    sb.append(value);
                    sb.append("");
                }
                if (headerFieldKey != null) clientResponse.addHeader(headerFieldKey, sb.toString());
            }

            String respStr = null;
            if (con.getResponseCode() >= 400 && con.getErrorStream() != null) {
                if (ignoreAuthenticationErrors && (con.getResponseCode() == 498 || con.getResponseCode() == 499))
                    return true;
                respStr = webResponseToString(con.getErrorStream());
            } else {
                respStr = webResponseToString(con.getInputStream());
            }                   
            
            _log(Level.INFO, "rest response:\n" + respStr); 

            clientResponse.setStatus(con.getResponseCode());

            OutputStream ostream = clientResponse.getOutputStream();
            ostream.write(respStr.getBytes(Charset.forName("UTF-8")));
            ostream.flush();
            ostream.close();
        }
        return false;
    }


    private HttpURLConnection doHTTPRequest(String uri, byte[] bytes, String method, String referer, String contentType) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setConnectTimeout(5000);
        con.setReadTimeout(60000);

        con.setRequestProperty("Referer", referer);
        con.setRequestMethod(method);

        if (bytes != null && bytes.length > 0) {
            con.setDoOutput(true);
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/x-www-form-urlencoded";
            }

            con.setRequestProperty("Content-Type", contentType);

            OutputStream os = con.getOutputStream();
            os.write(bytes);
        }

        return con;
    }

    private String addTokenToUri(String uri, String token) {
        if (token != null && !token.isEmpty())
            uri += uri.contains("?") ? "&token=" + token : "?token=" + token;
        return uri;
    }

    private String getJsonValue(String text, String key) {
        int i = text.indexOf(key + ":");
        if(i == -1) {
            i = text.indexOf(key + "\":");
        }
        String value = "";
        if (i > -1) {
            value = text.substring(text.indexOf(':', i) + 1).trim();
            if(value.length() == 0) {
                return "";
            }

            if(value.charAt(0) == '"' || value.charAt(0) == '\'') {
                int index = value.indexOf(value.charAt(0), 1);
                value = value.substring(1, index).trim();
            } else if(value.charAt(0) == '{' || value.charAt(0) == '[') {
                value = value.trim();
            } else {
                value = value.substring(0, value.indexOf(',')).trim();
            }
        }
        _log(Level.FINE, "Extracted " + key + " Value: " + value);
        return value;
    }

    //writing Log file
    private static Object _lockobject = new Object();
    private static Logger logger = Logger.getLogger("SIMPLE_REST_PROXY_LOGGER");
    private static String logFileName = "simplerestproxy.log";
    private static boolean okToLog = true;
    private static String defaultLogLevel = "INFO";

    private static void _log(Level level, String s, Throwable thrown) {
        try {
            System.out.println(s);
            String filename = logFileName;
            boolean okToLog = filename != null && !filename.isEmpty() && logger != null;
            synchronized (_lockobject) {

                if (okToLog) {

                    if (logger.getUseParentHandlers()) {
                        FileHandler fh = new FileHandler(filename, true);
                        logger.addHandler(fh);
                        SimpleFormatter formatter = new SimpleFormatter();
                        fh.setFormatter(formatter);
                        logger.setUseParentHandlers(false);

                        String logLevelStr = defaultLogLevel;
                        Level logLevel = Level.SEVERE;

                        if (logLevelStr != null) {
                            try {
                                logLevel = Level.parse(logLevelStr);
                            } catch (IllegalArgumentException e) {
                                SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                System.err.println(dt.format(new Date()) + ": " + logLevelStr + " is not a valid logging level.  Defaulting to SEVERE.");
                            }
                        }

                        logger.setLevel(logLevel);

                        logger.info("Log handler configured and initialized.");
                    }

                    if (thrown != null) {
                        logger.log(level, s, thrown);
                    } else {
                        logger.log(level, s);
                    }
                }
            }
        } catch (Exception e) {
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.err.println("Error writing to log: ");
            System.err.println(dt.format(new Date()) + " " + s);
            e.printStackTrace();
        }
    }

    private static void _log(String s, Throwable thrown) {
        _log(Level.SEVERE, s, thrown);
    }

    private static void _log(Level level, String s) {
        _log(level, s, null);
    }


    private static void sendErrorResponse(HttpServletResponse response, String errorDetails, String errorMessage, int errorCode) throws IOException {
        String message = "{" +
                "\"error\": {" +
                "\"code\": " + errorCode + "," +
                "\"details\": [" +
                "\"" + errorDetails + "\"" +
                "], \"message\": \"" + errorMessage + "\"}}";

        response.setStatus(errorCode);
        OutputStream output = response.getOutputStream();

        output.write(message.getBytes());

        output.flush();
    }

    private static void _sendURLMismatchError(HttpServletResponse response) throws IOException {
        sendErrorResponse(response, "The proxy tried to resolve a prohibited or malformed URL. The server does not meet one of the preconditions that the requester put on the request.",
                "403 - Forbidden: Access is denied.", HttpServletResponse.SC_FORBIDDEN);
    }
%><%
    _log(Level.INFO, "SimpleRestProxy took the control!");

    if (REST_SERVER_BASE == null) {
        //if no serverUrl found, send error message and get out.
        _sendURLMismatchError(response);
        return;
    }

    HttpURLConnection con = null;
    try {
        byte[] postBody = readRequestPostBody(request);
        String post = (new String(postBody, Charset.forName("UTF-8"))).trim();
        post = post.substring(0, post.length() - 1);
        System.out.println(post);

        String toServerUrl = REST_SERVER_BASE + getJsonValue(post, "path");
        String httpMethod = getJsonValue(post, "method").toUpperCase();
        String params = getJsonValue(post, "params");
        String headers = getJsonValue(post, "headers");
        String data = getJsonValue(post, "data");

        if (params != null && params.length() > 0) {
            toServerUrl += "?" + params;
        }

        String token = request.getParameter("token");
        if (token != null) {
            toServerUrl = addTokenToUri(toServerUrl, token);
        }
        _log(Level.INFO, "toServerUrl: " + toServerUrl);


        //todo
        //deal with header
        //deal with cookie by SESSION_ID_COOKIE_NAME, set get session data for the current user;

        //forwarding original request
        con = forwardToServer(request, httpMethod, toServerUrl, data != null ? data.getBytes(Charset.forName("UTF-8")) : null);
        _log(Level.INFO, "passing back to client");
        fetchAndPassBackToClient(con, response, true);
        response.flushBuffer(); 
        out.clear();  
        out = pageContext.pushBody();
    } catch (FileNotFoundException e) {
        try {
            _log("404 Not Found .", e);
            response.sendError(404, e.getLocalizedMessage() + " is NOT Found.");
            return;
        } catch (IOException finalErr) {
            _log("There was an error sending a response to the client.  Will not try again.", finalErr);
        }
    } catch (IOException e) {
        e.printStackTrace();
        try {
            _log("A fatal proxy error occurred.", e);
            response.sendError(500, e.getLocalizedMessage());
            return;
        } catch (IOException finalErr) {
            _log("There was an error sending a response to the client.  Will not try again.", finalErr);
        }
    } finally {
        con.disconnect();
    }
%>