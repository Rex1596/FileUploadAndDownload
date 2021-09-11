package com.rex.api.util;


import com.alibaba.fastjson.JSONObject;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Author lzw
 * Create 2021/9/7
 * Description
 */
public class HttpUtils {
    private final static String BOUNDARY = UUID.randomUUID().toString()
            .toLowerCase().replaceAll("-", "");// 边界标识
    private final static String PREFIX = "--";// 必须存在
    private final static String LINE_END = "\r\n";



    public static String doPost(String postUrl, InputStream inputStream) throws IOException {
        OutputStream out = null;
        HttpURLConnection conn = null;
        InputStream ins = null;
        ByteArrayOutputStream outStream = null;
        try {
            URL url = new URL(postUrl);

            conn = (HttpURLConnection) url.openConnection();
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.connect();
            conn.setConnectTimeout(10000);
            out = conn.getOutputStream();

            int bytes;
            byte[] buffer = new byte[1024];
            while ((bytes = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytes);
            }
            out.flush();
            // 返回流
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                ins = conn.getInputStream();
                outStream = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int count = -1;
                while ((count = ins.read(data, 0, 1024)) != -1) {
                    outStream.write(data, 0, count);
                }
                return JSONObject.parseObject(outStream.toString("UTF-8")).toString();
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (out != null) {
                out.close();
            }
            if (ins != null) {
                ins.close();
            }
            if (outStream != null) {
                outStream.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static void inputStreamUpload(String url, String fileName, InputStream inputStream) {
        //创建HttpClient对象
        CloseableHttpClient client = HttpClients.createDefault();
        //构建POST请求   请求地址请更换为自己的。
        //1)
        HttpPost post = new HttpPost(url);
        try {
            //文件路径请换成自己的
            //2)
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            //第一个参数为 相当于 Form表单提交的file框的name值 第二个参数就是我们要发送的InputStream对象了
            //第三个参数是文件名
            //3)
            builder.addBinaryBody("file", inputStream, ContentType.create("application/octet-stream"), fileName);
            //4)构建请求参数 普通表单项
            StringBody stringBody = new StringBody(fileName, ContentType.MULTIPART_FORM_DATA);
            builder.addPart("fileName", stringBody);

            HttpEntity entity = builder.build();
            post.setEntity(entity);
            //发送请求
            CloseableHttpResponse httpResponse = client.execute(post);
            entity = httpResponse.getEntity();
            if (entity != null) {
                inputStream = entity.getContent();
                //转换为字节输入流
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, Consts.UTF_8));
                String body = null;
                while ((body = br.readLine()) != null) {
                    System.out.println(body);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String sendRequest(String requestUrl,
                                     Map<String, String> requestText, InputStream requestFile) throws Exception{
        HttpURLConnection conn = null;
        InputStream input = null;
        OutputStream os = null;
        BufferedReader br = null;
        StringBuffer buffer = null;
        try {
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(1000 * 10);
            conn.setReadTimeout(1000 * 10);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            conn.connect();

            // 往服务器端写内容 也就是发起http请求需要带的参数
            os = new DataOutputStream(conn.getOutputStream());
            // 请求参数部分
            writeParams(requestText, os);
            // 请求上传文件部分
            int bytes = 0;
            byte[] buff = new byte[1024];
            while ((bytes = requestFile.read(buff)) != -1) {
                os.write(buff, 0, bytes);
            }
            os.flush();

            // 请求结束标志
            String endTarget = PREFIX + BOUNDARY + PREFIX + LINE_END;
            os.write(endTarget.getBytes());
            os.flush();

            // 读取服务器端返回的内容
            System.out.println("======================响应体=========================");
            System.out.println("ResponseCode:" + conn.getResponseCode()
                    + ",ResponseMessage:" + conn.getResponseMessage());
            if(conn.getResponseCode()==200){
                input = conn.getInputStream();
            }else{
                input = conn.getErrorStream();
            }

            br = new BufferedReader(new InputStreamReader( input, "UTF-8"));
            buffer = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
            //......
            System.out.println("返回报文:" + buffer.toString());

        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            try {
                if (conn != null) {
                    conn.disconnect();
                    conn = null;
                }

                if (os != null) {
                    os.close();
                    os = null;
                }

                if (br != null) {
                    br.close();
                    br = null;
                }
            } catch (IOException ex) {
                throw new Exception(ex);
            }
        }
        return buffer.toString();
    }
    /**
     * 对post参数进行编码处理并写入数据流中
     * @throws Exception
     *
     * @throws IOException
     *
     * */
    private static void writeParams(Map<String, String> requestText,
                                    OutputStream os) throws Exception {
        try{
            String msg = "请求参数部分:\n";
            if (requestText == null || requestText.isEmpty()) {
                msg += "空";
            } else {
                StringBuilder requestParams = new StringBuilder();
                Set<Map.Entry<String, String>> set = requestText.entrySet();
                Iterator<Map.Entry<String, String>> it = set.iterator();
                while (it.hasNext()) {
                        Map.Entry<String, String> entry = it.next();
                    requestParams.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    requestParams.append("Content-Disposition: form-data; name=\"")
                            .append(entry.getKey()).append("\"").append(LINE_END);
                    requestParams.append("Content-Type: text/plain; charset=utf-8")
                            .append(LINE_END);
                    requestParams.append("Content-Transfer-Encoding: 8bit").append(
                            LINE_END);
                    requestParams.append(LINE_END);// 参数头设置完以后需要两个换行，然后才是参数内容
                    requestParams.append(entry.getValue());
                    requestParams.append(LINE_END);
                }
                os.write(requestParams.toString().getBytes());
                os.flush();

                msg += requestParams.toString();
            }

            //System.out.println(msg);
        }catch(Exception e){
            throw new Exception(e);
        }
    }

    /**
     * 对post上传的文件进行编码处理并写入数据流中
     *
     * @throws IOException
     *
     * */
    private static void writeFile(Map<String, MultipartFile> requestFile,
                                  OutputStream os) throws Exception {
        InputStream is = null;
        try{
            String msg = "请求上传文件部分:\n";
            if (requestFile == null || requestFile.isEmpty()) {
                msg += "空";
            } else {
                StringBuilder requestParams = new StringBuilder();
                Set<Map.Entry<String, MultipartFile>> set = requestFile.entrySet();
                Iterator<Map.Entry<String, MultipartFile>> it = set.iterator();
                while (it.hasNext()) {
                    Map.Entry<String, MultipartFile> entry = it.next();
                    if(entry.getValue() == null){//剔除value为空的键值对
                        continue;
                    }
                    requestParams.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    requestParams.append("Content-Disposition: form-data; name=\"")
                            .append(entry.getKey()).append("\"; filename=\"")
                            .append(entry.getValue().getName()).append("\"")
                            .append(LINE_END);
                    requestParams.append("Content-Type:")
                            .append(entry.getValue().getContentType())
                            .append(LINE_END);
                    requestParams.append("Content-Transfer-Encoding: 8bit").append(
                            LINE_END);
                    requestParams.append(LINE_END);// 参数头设置完以后需要两个换行，然后才是参数内容

                    os.write(requestParams.toString().getBytes());
                    os.write(entry.getValue().getBytes());

                    os.write(LINE_END.getBytes());
                    os.flush();

                    msg += requestParams.toString();
                }
            }
            //System.out.println(msg);
        }catch(Exception e){
            throw new Exception(e);
        }finally{
            try{
                if(is!=null){
                    is.close();
                }
            }catch(Exception e){
                throw new Exception(e);
            }
        }
    }
    public static void main(String[] args) throws Exception {
        String str = "我是一個文件流";
        InputStream inputStream = new ByteArrayInputStream(str.getBytes());
        InputStream stream = new FileInputStream("d:/1.zip");
//        System.out.println(doPost("http://localhost:31001/publicFiles/v1/uploadTemplateFile", stream));
//        inputStreamUpload("http://localhost:31001/publicFiles/v1/uploadTemplateFile", "abc", stream);
        Map<String,String> map = new HashMap<>();
        map.put("fileName","abc");
        sendRequest("http://localhost:31001/publicFiles/v1/uploadTemplateFile",map,inputStream);
    }
}

