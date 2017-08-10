package com.huawei.osm.knowledge.searchmachine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import com.huawei.osm.common.log.OSMCommonLog;
import com.huawei.osm.knowledge.disconf.util.ElasticConfigUtil;
import com.huawei.osm.knowledge.utils.ConfigUtil;

public class ElasticSearchClientFactory
{
    private enum ElasticSearchClientSingleton
    {
        elasticSearchClientSingleton;
        
        private RestClient esRestClient;
        
        private ElasticSearchClientSingleton()
        {
            String host = ElasticConfigUtil.getElasticHost().replaceAll("/",
                    "");
            String[] hosts = host.split(",");
            List<HttpHost> hostList = new ArrayList<HttpHost>();
            for (int i = 0; i < hosts.length; i++)
            {
                String[] split = hosts[i].split(":");
                HttpHost httpHost = new HttpHost(split[1],
                        Integer.parseInt(split[2]), split[0]);
                hostList.add(httpHost);
            }
            HttpHost[] httpHosArr = new HttpHost[hostList.size()];
            httpHosArr = hostList.toArray(httpHosArr);
            
            String userName = ElasticConfigUtil.getElasticUser();
            String pwd = ElasticConfigUtil.getElasticPwd();
            
            if (!StringUtils.isBlank(userName))
            {
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(userName, pwd));
                
                KeyStore keystore = null;
                InputStream is = null;
                SSLContext context = null;
                try
                {
                    keystore = KeyStore.getInstance("jks");
                    String confPath = ConfigUtil.getConfPath();
                    String keystorePath = confPath + ElasticConfigUtil.getEsCerPath();
                    Path path = Paths.get(pathValidate(keystorePath));
                    is = Files.newInputStream(path);
                    keystore.load(is, "changeit".toCharArray());
                    TrustManagerFactory tmf = TrustManagerFactory
                            .getInstance("SunX509");
                    tmf.init(keystore);
                    TrustManager[] tm = tmf.getTrustManagers();
                    context = SSLContext.getInstance("TLSv1.2");
                    context.init(null, tm, null);
                }
                catch (KeyStoreException e)
                {
                    OSMCommonLog.error("Failed to obtain certificate.", e);
                }
                catch (IOException e)
                {
                    OSMCommonLog.error("Failed to obtain certificate.", e);
                }
                catch (NoSuchAlgorithmException e)
                {
                    OSMCommonLog.error("Failed to obtain certificate.", e);
                }
                catch (CertificateException e)
                {
                    OSMCommonLog.error("Failed to obtain certificate.", e);
                }
                catch (KeyManagementException e)
                {
                    OSMCommonLog.error("Failed to obtain certificate.", e);
                }
                finally {
                    if (is != null)
                    {
                        try
                        {
                            is.close();
                        }
                        catch (IOException e)
                        {
                            OSMCommonLog.error("keystore closed erro.",e);
                            is = null;
                        }
                    }
                }
                final SSLContext contextfinal = context;
                
                RestClient restClient = RestClient.builder(httpHosArr)
                        .setHttpClientConfigCallback(
                                new RestClientBuilder.HttpClientConfigCallback()
                                {
                                    @Override
                                    public HttpAsyncClientBuilder customizeHttpClient(
                                            HttpAsyncClientBuilder httpClientBuilder)
                                    {
                                        return httpClientBuilder
                                                .setDefaultCredentialsProvider(
                                                        credentialsProvider)
                                                .setSSLContext(contextfinal);
                                    }
                                })
                        .build();
                this.esRestClient = restClient;
            }
            else
            {
                RestClient restClient = RestClient.builder(httpHosArr).build();
                this.esRestClient = restClient;
            }
        }
        
        public void reloadRestClient()
        {
            
        }
        
        public RestClient getInstance()
        {
            return esRestClient;
        }
    }
    
    public static RestClient getInstance()
    {
        RestClient esRestClient = ElasticSearchClientSingleton.elasticSearchClientSingleton
                .getInstance();
        
        return esRestClient;
    }
    
    public static void refreshESRestclient()
    {
        ElasticSearchClientSingleton.elasticSearchClientSingleton
                .reloadRestClient();
    }
    
    private static String pathValidate(String path)
    {
        String p = path;
        return p;
    }
}
