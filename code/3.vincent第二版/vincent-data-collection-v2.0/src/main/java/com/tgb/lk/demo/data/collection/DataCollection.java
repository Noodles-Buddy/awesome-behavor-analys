package com.tgb.lk.demo.data.collection;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tgb.demo.util.ElasticSearchUtils;
import com.tgb.entity.EsUpload;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.UUID;

@Controller
@RequestMapping("/dataCollection")
public class DataCollection {

    @RequestMapping(value = "log.gif")
    public void analysis(String args, HttpServletResponse response) throws IOException {
        System.out.println(args);
         
		//日志收集 
        //1.Es需要通过实体上传
        EsUpload esUpload = new EsUpload();
        
        esUpload.setId(UUID.randomUUID().toString());
        esUpload.setArgs(args);
        
        //2.调用老盖封装的ES上传方法
        ElasticSearchUtils.addDoc("blog", esUpload.getId(),esUpload,"getId","getArgs");
        
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/gif");
        OutputStream out = response.getOutputStream();
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "gif", out);
        out.flush();
    }
}
