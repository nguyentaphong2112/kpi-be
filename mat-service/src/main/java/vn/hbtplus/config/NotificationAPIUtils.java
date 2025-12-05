//package vn.hbtplus.config;
//
//import com.google.gson.Gson;
//import com.sun.jersey.api.client.Client;
//import com.sun.jersey.api.client.ClientResponse;
//import com.sun.jersey.api.client.WebResource;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import vn.hbtplus.dto.request.sendNotify.SendRequest;
//import vn.hbtplus.models.response.BaseResponseEntity;
//import vn.hbtplus.utils.ResponseUtils;
//
//import javax.net.ssl.*;
//import javax.ws.rs.core.MediaType;
//import java.security.KeyManagementException;
//import java.security.NoSuchAlgorithmException;
//import java.security.cert.X509Certificate;
//
///**
// * NotificationUtils
// * @author hieuhc
// */
//@Component
//@Slf4j
//public class NotificationAPIUtils {
//    @Value("${notification.serviceUrl}")
//    private String serviceUrl;
//
//    @Value("${notification.appCode}")
//    private String appCode;
//
//    @Value("${notification.appPass}")
//    private String apiKey;
//
//    @Value("${notification.headerApiName}")
//    private String headerName;
//    public static Gson gson = new Gson();
//    static {
//        disableSslVerification();
//    }
//
//    /**
//     *
//     * @param sendRequest
//     * @return
//     */
//    public BaseResponseEntity<Object> send(SendRequest sendRequest) {
//        if (StringUtils.isBlank(sendRequest.getAppCode())) {
//            sendRequest.setAppCode(appCode);
//        }
//        if (StringUtils.isBlank(sendRequest.getAppPass())) {
//            sendRequest.setAppPass(apiKey);
//        }
//        String urlPost = serviceUrl + "/api-ver2/send";
//        Client client = Client.create();
//        WebResource webResource = client.resource(urlPost);
//        final ClientResponse res;
//        String json = "";
//        try {
//            res = webResource.type(MediaType.APPLICATION_JSON_TYPE).header(headerName, apiKey)
//                    .post(ClientResponse.class, gson.toJson(sendRequest));
//            if (res != null) {
//                json = res.getEntity(String.class);
//            }
//            log.info("Call service POST " + urlPost + " fromData: " + gson.toJson(sendRequest) + " return: " + json);
//        } catch (Exception ex) {
//            log.error("Call service POST " + urlPost + " fromData: " + gson.toJson(sendRequest));
//            json = "";
//            log.error(urlPost + " ERROR", ex);
//        }
//
//        return ResponseUtils.ok(json);
//    }
//
//    /**
//     * disableSslVerification
//     */
//    private static void disableSslVerification() {
//        try {
//            // Create a trust manager that does not validate certificate chains
//            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
//                public X509Certificate[] getAcceptedIssuers() {
//                    return null;
//                }
//
//                public void checkClientTrusted(X509Certificate[] certs, String authType) {
//                }
//
//                public void checkServerTrusted(X509Certificate[] certs, String authType) {
//                }
//            } };
//
//            // Install the all-trusting trust manager
//            SSLContext sc = SSLContext.getInstance("SSL");
//            sc.init(null, trustAllCerts, new java.security.SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//
//            // Create all-trusting host name verifier
//            HostnameVerifier allHostsValid = new HostnameVerifier() {
//                public boolean verify(String hostname, SSLSession session) {
//                    return true;
//                }
//            };
//
//            // Install the all-trusting host verifier
//            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        }
//    }
//}
