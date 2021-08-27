package com.rex.api.util;

import com.rex.api.entity.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Author lzw
 * Create 2021/8/26
 * Description
 */
public class HttpUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
    /**
     * multipart/form-data 格式发送数据时各个部分分隔符的前缀,必须为 --
     */
    private static final String BOUNDARY_PREFIX = "--";
    /**
     * 回车换行,用于一行的结尾
     */
    private static final String LINE_END = "\r\n";

    /**
     * post 请求：以表单方式提交数据
     * <p>
     * 由于 multipart/form-data 不是 http 标准内容，而是属于扩展类型，
     * 因此需要自己构造数据结构，具体如下：
     * <p>
     * 1、首先，设置 Content-Type
     * <p>
     * Content-Type: multipart/form-data; boundary=${bound}
     * <p>
     * 其中${bound} 是一个占位符，代表我们规定的分割符，可以自己任意规定，
     * 但为了避免和正常文本重复了，尽量要使用复杂一点的内容
     * <p>
     * 2、设置主体内容
     * <p>
     * --${bound}
     * Content-Disposition: form-data; name="userName"
     * <p>
     * Andy
     * --${bound}
     * Content-Disposition: form-data; name="file"; filename="测试.excel"
     * Content-Type: application/octet-stream
     * <p>
     * 文件内容
     * --${bound}--
     * <p>
     * 其中${bound}是之前头信息中的分隔符，如果头信息中规定是123，那这里也要是123；
     * 可以很容易看到，这个请求提是多个相同部分组成的：
     * 每一部分都是以--加分隔符开始的，然后是该部分内容的描述信息，然后一个回车换行，然后是描述信息的具体内容；
     * 如果传送的内容是一个文件的话，那么还会包含文件名信息以及文件内容类型。
     * 上面第二部分是一个文件体的结构，最后以--分隔符--结尾，表示请求体结束
     * <p>
     * urlStr      请求的url
     * filePathMap key 参数名，value 文件的路径
     * keyValues   普通参数的键值对
     */
    public static HttpResponse postFormData(String urlStr, Map<String, String> filePathMap, Map<String, Object> keyValues, Map<String, Object> headers) throws IOException {
        HttpResponse response;
        HttpURLConnection conn = getHttpURLConnection(urlStr, headers);
        //分隔符，可以任意设置，这里设置为 MyBoundary+ 时间戳（尽量复杂点，避免和正文重复）
        String boundary = "MyBoundary" + System.currentTimeMillis();
        //设置 Content-Type 为 multipart/form-data; boundary=${boundary}
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        //发送参数数据
        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            //发送普通参数
            if (keyValues != null && !keyValues.isEmpty()) {
                for (Map.Entry<String, Object> entry : keyValues.entrySet()) {
                    writeSimpleFormField(boundary, out, entry);
                }
            }
            //发送文件类型参数
            if (filePathMap != null && !filePathMap.isEmpty()) {
                for (Map.Entry<String, String> filePath : filePathMap.entrySet()) {
                    writeFile(filePath.getKey(), filePath.getValue(), boundary, out);
                }
            }

            //写结尾的分隔符--${boundary}--,然后回车换行
            String endStr = BOUNDARY_PREFIX + boundary + BOUNDARY_PREFIX + LINE_END;
            out.write(endStr.getBytes());
        } catch (Exception e) {
            LOGGER.error("HttpUtils.postFormData 请求异常！", e);
            response = new HttpResponse(500, e.getMessage());
            return response;
        }

        return getHttpResponse(conn);
    }

    /**
     * 获得连接对象
     */
    private static HttpURLConnection getHttpURLConnection(String urlStr, Map<String, Object> headers) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //设置超时时间
        conn.setConnectTimeout(50000);
        conn.setReadTimeout(50000);
        //允许输入流
        conn.setDoInput(true);
        //允许输出流
        conn.setDoOutput(true);
        //不允许使用缓存
        conn.setUseCaches(false);
        //请求方式
        conn.setRequestMethod("POST");
        //设置编码 utf-8
        conn.setRequestProperty("Charset", "UTF-8");
        //设置为长连接
        conn.setRequestProperty("connection", "keep-alive");

        //设置其他自定义 headers
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue().toString());
            }
        }

        return conn;
    }

    private static HttpResponse getHttpResponse(HttpURLConnection conn) {
        HttpResponse response;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            int responseCode = conn.getResponseCode();
            StringBuilder responseContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
            response = new HttpResponse(responseCode, responseContent.toString());
        } catch (Exception e) {
            LOGGER.error("获取 HTTP 响应异常！", e);
            response = new HttpResponse(500, e.getMessage());
        }
        return response;
    }

    /**
     * 写文件类型的表单参数
     * <p>
     * paramName 参数名
     * filePath  文件路径
     * boundary  分隔符
     */
    private static void writeFile(String paramName, String filePath, String boundary,
                                  DataOutputStream out) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
            //  写分隔符--${boundary}，并回车换行
            String boundaryStr = BOUNDARY_PREFIX + boundary + LINE_END;
            out.write(boundaryStr.getBytes());
            // 写描述信息(文件名设置为上传文件的文件名)：
            // 写 Content-Disposition: form-data; name="参数名"; filename="文件名"，并回车换行
            // 写 Content-Type: application/octet-stream，并两个回车换行
            String fileName = new File(filePath).getName();
            String contentDispositionStr = String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"", paramName, fileName) + LINE_END;
            out.write(contentDispositionStr.getBytes());
            String contentType = "Content-Type: application/octet-stream" + LINE_END + LINE_END;
            out.write(contentType.getBytes());

            String line;
            while ((line = fileReader.readLine()) != null) {
                out.write(line.getBytes());
            }
            //回车换行
            out.write(LINE_END.getBytes());
        } catch (Exception e) {
            LOGGER.error("写文件类型的表单参数异常", e);
        }
    }

    /**
     * 写普通的表单参数
     * <p>
     * boundary 分隔符
     * entry    参数的键值对
     */
    private static void writeSimpleFormField(String boundary, DataOutputStream out, Map.Entry<String, Object> entry) throws IOException {
        //写分隔符--${boundary}，并回车换行
        String boundaryStr = BOUNDARY_PREFIX + boundary + LINE_END;
        out.write(boundaryStr.getBytes());
        //写描述信息：Content-Disposition: form-data; name="参数名"，并两个回车换行
        String contentDispositionStr = String.format("Content-Disposition: form-data; name=\"%s\"", entry.getKey()) + LINE_END + LINE_END;
        out.write(contentDispositionStr.getBytes());
        //写具体内容：参数值，并回车换行
        String valueStr = entry.getValue().toString() + LINE_END;
        out.write(valueStr.getBytes());
    }

    public static void main(String[] args) throws IOException {
        //请求 uploadUrl
        String uploadUrl = "http://localhost:33001/publicFiles/v1/upload";

        // keyValues 保存普通参数
        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put("fileName", "abc");
        keyValues.put("isOverwrite", 1);

        // filePathMap 保存文件类型的参数名和文件路径
        Map<String, String> filePathMap = new HashMap<>();
        String paramName = "file";
        String filePath = "E:\\abc";
        filePathMap.put(paramName, filePath);

        //headers
        Map<String, Object> headers = new HashMap<>();

        HttpResponse response = postFormData(uploadUrl, filePathMap, keyValues, headers);
        System.out.println(response);


        /*
         * Author lzw
         * Description 下载请求
         **/
        String downloadUrl = "http://localhost:33001/publicFiles/v1/download";

        // keyValues 保存普通参数
        keyValues = new HashMap<>();
        keyValues.put("fileName", "abc");


        //headers
        headers = new HashMap<>();

        response = postFormData(downloadUrl, filePathMap, keyValues, headers);

        System.out.println(response);
        // 输出到文件
        String filePath1 = "e:/test.txt";
        String content = response.getContent();

        string2File(content, filePath1);

    }

    /**
     * 发送文本内容
     */
    public static HttpResponse postText(String urlStr, String filePath) throws IOException {
        HttpResponse response;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/plain");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
             BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                writer.write(line);
            }

        } catch (Exception e) {
            LOGGER.error("HttpUtils.postText 请求异常！", e);
            response = new HttpResponse(500, e.getMessage());
            return response;
        }

        return getHttpResponse(conn);
    }

    /**
     * 将字符串写入指定文件(当指定的父路径中文件夹不存在时，会最大限度去创建，以保证保存成功！)
     *
     * @param res            原字符串
     * @param filePath 文件路径
     * @return 成功标记
     */
    public  static  boolean string2File(String res, String filePath) {
        boolean flag =  true;
        BufferedReader bufferedReader =  null;
        BufferedWriter bufferedWriter;
        try {
            File distFile =  new File(filePath);
            if (!distFile.getParentFile().exists()) System.out.println(distFile.getParentFile().mkdirs());
            bufferedReader =  new BufferedReader( new StringReader(res));
            bufferedWriter =  new BufferedWriter( new FileWriter(distFile));
            char[] buf =  new  char[1024];          //字符缓冲区
            int len;
            while ((len = bufferedReader.read(buf)) != -1) {
                bufferedWriter.write(buf, 0, len);
            }
            bufferedWriter.flush();
            bufferedReader.close();
            bufferedWriter.close();
        }  catch (IOException e) {
            e.printStackTrace();
            flag =  false;
            return flag;
        }  finally {
            if (bufferedReader !=  null) {
                try {
                    bufferedReader.close();
                }  catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }
}
