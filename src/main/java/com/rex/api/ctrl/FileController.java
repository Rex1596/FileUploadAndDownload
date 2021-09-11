package com.rex.api.ctrl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import com.rex.api.entity.FileResult;
import com.rex.api.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Author lzw
 * Create 2021/8/23
 * Description 文件上传下载
 */
@RestController
@RequestMapping("/publicFiles/v1")
@Api(value = "参数查询模板上传下载接口")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Value("${file.path}")
    private String filePath;

    @ApiOperation("参数查询模板上传: 上传文件名、文件")
    @ApiResponses({
            @ApiResponse(code = 200, message = "上传成功", response = String.class),
            @ApiResponse(code = 400, message = "请求错误，请检查参数"),
            @ApiResponse(code = 500, message = "系统错误，请联系管理员")
    })
    // 单个文件上传
    @PostMapping(value = "/uploadTemplateFile")
    public FileResult uploadTemplateFile(@RequestParam("fileName") String fileName,
                                         @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return new FileResult(0, "文件为空", "");
            }
            log.info("文件大小: " + file.getSize());
            if (file.getSize() > 1073741824) {
                return new FileResult(0, "上传失败，文件不能超过1GB", "");
            }
            // 设置文件存储路径
            // 放在当他日期文件夹内 例如/20210901
            Integer dateValueOfInteger = DateUtil.dateValueOfInteger(new Date());
            String path = filePath + dateValueOfInteger + "/" + fileName;
            log.info("上传路径为: " + path);
            File dest = new File(new File(path).getAbsolutePath());// dist为文件，有多级目录的文件
            // 检测是否存在目录
            if (!dest.getParentFile().exists()) {//因此这里使用.getParentFile()，目的就是取文件前面目录的路径
                boolean mkdirs = dest.getParentFile().mkdirs();// 新建文件夹
                log.info("创建文件夹: " + mkdirs);
            }
            // 检测是否存在同名文件 1、同名文件存在是覆盖 2、同名文件存在不覆盖 3、不存在同名文件
            file.transferTo(dest);// 文件写入
            return new FileResult(1, "上传成功", fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return new FileResult(0, "上传失败，文件被占用读写文件失败", "");
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return new FileResult(0, "上传失败，客户端链接失败", "");
        }
    }

    @ApiOperation("参数查询模板上传: 上传文件名、输入流")
    @ApiResponses({
            @ApiResponse(code = 200, message = "上传成功", response = String.class),
            @ApiResponse(code = 400, message = "请求错误，请检查参数"),
            @ApiResponse(code = 500, message = "系统错误，请联系管理员")
    })
    // 单个文件上传
    @RequestMapping(value = "/uploadTemplateInputStream")
    public FileResult uploadTemplateInputStream(HttpServletRequest request) throws IOException {
        Integer dateInteger = DateUtil.dateValueOfInteger(new Date());
        String fileName = request.getParameter("fileName");
        String fileFullPath = filePath + dateInteger + "/" + fileName+".csv";
        log.info("fileFullPath:" + fileFullPath);
        InputStream input = null;
        FileOutputStream fos = null;
        try {
//            input = request.getInputStream();
            input = request.getInputStream();
            File newFile = new File(fileFullPath);
            if (!newFile.getParentFile().exists()) {
                log.info("创建文件夹" + newFile.getParentFile().mkdirs());
            }
            fos = new FileOutputStream(fileFullPath);
            fos.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
            int size;
            byte[] buffer = new byte[1024];
            while ((size = input.read(buffer, 0, 1024)) != -1) {
                fos.write(buffer, 0, size);
            }
            log.info(fileFullPath + " 上传成功");
            return new FileResult(1, "上传成功", fileName);
        } catch (IOException e) {
            //响应信息 json字符串格式
            log.error("上传失败 " + e.getLocalizedMessage());
            e.printStackTrace();
            return new FileResult(0, e.getLocalizedMessage(), "");
        } finally {
            if (input != null) {
                input.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    @ApiOperation("文件下载: 文件名，1-参数查询模板（ID_日期）、0-其他文件下载（日期_ID）")
    @ApiResponses({
            @ApiResponse(code = 200, message = "下载成功", response = String.class),
            @ApiResponse(code = 400, message = "请求错误，请检查参数"),
            @ApiResponse(code = 500, message = "系统错误，请联系管理员")
    })
    @PostMapping("/downloadFile")
    public void downloadFile(@RequestParam("fileName") String fileName,
                             @RequestParam("type") int type,
                             HttpServletResponse response) throws IOException {
        try {
            // 获取文件名
            if (type == 1) {//ID_日期
                String s = fileName.split("_")[1].replace(".csv","");
                if (s.length() != 8) {
                    log.info("文件名"+s+"下载文件后缀异常或者不等于8位，后缀格式为yyyyMMdd");
                } else {
                    log.info("文件名OK");
                    fileName = s +"/" + fileName;
                }
            } else {//日期_ID
                String s = fileName.split("_")[0];
                if (s.length() != 8) {
                    log.info("下载文件前缀异常或者不等于8位，前缀格式为yyyyMMdd");
                } else {
                    log.info("文件名OK");
                    fileName = s +"/" + fileName;
                }
            }
            // path是指想要下载的文件的路径
            File file = new File(filePath + fileName);
            log.info(file.getAbsolutePath());
            // 将文件写入输入流
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStream fis = new BufferedInputStream(fileInputStream);
            byte[] buffer = new byte[fis.available()];
            log.info(fis.read(buffer) + "");
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            response.setCharacterEncoding("utf-8");
            //Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
            //attachment表示以附件方式下载 inline表示在线打开 "Content-Disposition: inline; filename=文件名.mp3"
            // filename表示文件的默认名称，因为网络传输只支持URL编码的相关支付，因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            // 告知浏览器文件的大小
            response.addHeader("Content-Length", "" + file.length());
            response.setContentType("application/octet-stream");
            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            outputStream.write(buffer);
            outputStream.flush();
            log.info(file.getAbsolutePath() + " 下载成功");
        } catch (Exception ex) {
            log.error("下载失败" + ex.getLocalizedMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    @ApiOperation("查询文件是否存在: 文件名")
    @ApiResponses({
            @ApiResponse(code = 200, message = "下载成功", response = String.class),
            @ApiResponse(code = 400, message = "请求错误，请检查参数"),
            @ApiResponse(code = 500, message = "系统错误，请联系管理员")
    })
    @GetMapping("/fileIsExists")
    public FileResult fileIsExists(@RequestParam("fileName") String fileName,
                                   @RequestParam("type") int type) {
        // path是指想要下载的文件的路径
        if (type == 1) {//ID_20210908
            String s = fileName.split("_")[1].replace(".csv","");
            if (s.length() != 8) {
                log.error("文件名"+s+"下载文件前缀异常或者不等于8位，前缀格式为yyyyMMdd");
                return new FileResult(0, "下载文件后缀异常或者不等于8位，后缀格式为yyyyMMdd", fileName);
            } else {
                log.info("文件名OK");
                fileName = s + "/" + fileName;
            }
        } else { //20210908_UUID
            String s = fileName.split("_")[0];
            if (s.length() != 8) {
                log.info("下载文件前缀异常或者不等于8位，前缀格式为yyyyMMdd");
                return new FileResult(0, "下载文件前缀异常或者不等于8位，前缀格式为yyyyMMdd", fileName);
            } else {
                log.info("文件名OK");
                fileName = s +"/" + fileName;
            }
        }
        // path是指想要下载的文件的路径
        File file = new File(filePath  + fileName);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            log.info(file.getAbsolutePath() + "文件存在");
            fileInputStream.close();
            return new FileResult(1, "文件存在", fileName);
        } catch (IOException e) {
            log.info(file.getAbsolutePath() + "文件不存在");
            log.error(e.getLocalizedMessage());
            return new FileResult(0, e.getLocalizedMessage(), fileName);
        }
    }

    @ApiOperation("文件上传: 文件流")
    @ApiResponses({
            @ApiResponse(code = 200, message = "上传成功", response = String.class),
            @ApiResponse(code = 400, message = "请求错误，请检查参数"),
            @ApiResponse(code = 500, message = "系统错误，请联系管理员")
    })
    @PostMapping("/uploadInputStream")
    public FileResult uploadInputStream(HttpServletRequest request) throws Exception {
        String uuid = UUID.randomUUID().toString().toUpperCase(Locale.ROOT).replace("-", "");
        //20210908/20210908_UUID
        Integer dateInteger = DateUtil.dateValueOfInteger(new Date());
        String fileName = dateInteger + "_" + uuid;

        String fileFullPath = filePath + dateInteger + "/" + fileName;
        log.info("fileFullPath:" + fileFullPath);
        InputStream input = null;
        FileOutputStream fos = null;
        try {
            input = request.getInputStream();
            File file = new File(fileFullPath);
            if (!file.getParentFile().exists()) {
                log.info("创建文件夹" + file.getParentFile().mkdirs());
            }
            fos = new FileOutputStream(fileFullPath);
            int size;
            byte[] buffer = new byte[1024];
            while ((size = input.read(buffer, 0, 1024)) != -1) {
                fos.write(buffer, 0, size);
            }
            log.info(fileFullPath + " 上传成功");
            return new FileResult(1, "上传成功", fileName);
        } catch (IOException e) {
            //响应信息 json字符串格式
            log.error("上传失败 " + e.getLocalizedMessage());
            e.printStackTrace();
            return new FileResult(0, e.getLocalizedMessage(), "");
        } finally {
            if (input != null) {
                input.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }


}


