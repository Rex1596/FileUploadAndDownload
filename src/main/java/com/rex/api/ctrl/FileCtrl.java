package com.rex.api.ctrl;

import com.rex.api.entity.FileResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

/**
 * Author lzw
 * Create 2021/8/23
 * Description 文件上传下载
 */
@RestController
@RequestMapping("/publicFiles/v1")
@Api(value = "参数查询模板上传下载接口")
public class FileCtrl {

    private static final Logger log = LoggerFactory.getLogger(FileCtrl.class);

    // 配置文件路径
    @Value("${file.path}")
    private String filePath;

    @ApiOperation("参数查询模板上传: 上传文件名、是否覆盖(1/0)、文件")
    @ApiResponses({
            @ApiResponse(code = 200, message = "上传成功", response = String.class),
            @ApiResponse(code = 400, message = "请求错误，请检查参数"),
            @ApiResponse(code = 500, message = "系统错误，请联系管理员")
    })
    // 单个文件上传
    @PostMapping(value = "/upload")
    public FileResult upload(@RequestParam("fileName") String fileName,
                             @RequestParam("isOverwrite") int isOverwrite,
                             @RequestParam("file") MultipartFile file) {
        try {
            // 1-覆盖，0-不覆盖
            if (file.isEmpty()) {
                return new FileResult(0, "文件为空");
            }
            // 获取文件名
            log.info("上传的文件名为：" + fileName);//写日志
            // 设置文件存储路径
            String path = filePath + fileName;
            File dest = new File(new File(path).getAbsolutePath());// dist为文件，有多级目录的文件
            // 检测是否存在目录
            if (!dest.getParentFile().exists()) {//因此这里使用.getParentFile()，目的就是取文件前面目录的路径
                dest.getParentFile().mkdirs();// 新建文件夹
            }
            // 检测是否存在同名文件 1、同名文件存在是覆盖 2、同名文件存在不覆盖 3、不存在同名文件
            if (dest.getAbsoluteFile().exists() && isOverwrite == 0) {
                return new FileResult(0, "存在同名文件，不需要覆盖");
            } else {
                file.transferTo(dest);// 文件写入
                return new FileResult(1, "上传成功");
            }
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        return new FileResult(0, "上传失败");
    }

    @ApiOperation("参数查询模板下载: 文件名")
    @ApiResponses({
            @ApiResponse(code = 200, message = "下载成功", response = String.class),
            @ApiResponse(code = 400, message = "请求错误，请检查参数"),
            @ApiResponse(code = 500, message = "系统错误，请联系管理员")
    })
    @PostMapping("/download")
    public String download(@RequestParam("fileName") String fileName,
                           HttpServletResponse response) {
        try {
            // path是指想要下载的文件的路径
            File file = new File(filePath + fileName);
            log.info(file.getPath());
            // 获取文件名
            String filename = file.getName();
            // 获取文件后缀名
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            log.info("文件后缀名：" + ext);
            // 将文件写入输入流
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStream fis = new BufferedInputStream(fileInputStream);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            response.setCharacterEncoding("UTF-8");
            //Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
            //attachment表示以附件方式下载 inline表示在线打开 "Content-Disposition: inline; filename=文件名.mp3"
            // filename表示文件的默认名称，因为网络传输只支持URL编码的相关支付，因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
            // 告知浏览器文件的大小
            response.addHeader("Content-Length", "" + file.length());
            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            outputStream.write(buffer);
            outputStream.flush();
            return "下载成功";
        } catch (IOException ex) {
            ex.printStackTrace();
            return "下载失败";
        }
    }
}
