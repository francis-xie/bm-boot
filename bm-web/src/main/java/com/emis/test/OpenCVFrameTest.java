package com.emis.test;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.bytedeco.javacv.*;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Hashtable;

/**
 * https://github.com/bytedeco/javacv
 * 调用windows平台的摄像头窗口视频
 * java通过javacv获取windows的摄像头视频
 */
public class OpenCVFrameTest {
    public static void main(String[] args) throws Exception, InterruptedException {
        //test1();
        test2();
    }

    /**
     * 用java通过javacv获取本机电脑摄像头
     * https://github.com/ATOOMs/JavaOpenCV/blob/master/JavaCamera/src/JavaCamera/JavacvCameraTest.java
     *
     * @throws FrameGrabber.Exception
     * @throws InterruptedException
     */
    public static void test1() throws FrameGrabber.Exception, InterruptedException {
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();   //开始获取摄像头数据
        CanvasFrame canvas = new CanvasFrame("摄像头");//新建一个窗口
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas.setAlwaysOnTop(true);

        while (true) {
            if (!canvas.isDisplayable()) {//窗口是否关闭
                grabber.stop();//停止抓取
                System.exit(2);//退出
            }
            canvas.showImage(grabber.grab());//获取摄像头图像并放到窗口上显示， 这里的Frame frame=grabber.grab(); frame是一帧视频图像

            Thread.sleep(50);//50毫秒刷新一次图像
        }
    }

    /**
     * 使用javaCV实现摄像头调用，并且完成二维码扫描
     * https://blog.csdn.net/qq_24172609/article/details/90347696
     *
     * @throws Exception
     */
    public static void test2() throws Exception {
        Java2DFrameConverter java2dConverter = new Java2DFrameConverter();  //用以完成Frame到BufferedImage的格式转换
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();

        while (true) {
            Frame frame = grabber.grabFrame();
            BufferedImage bImage = java2dConverter.getBufferedImage(frame);
            String url = qrParse(bImage);
            System.out.println(url);
            if (url != null) {
                grabber.stop();//停止抓取
                System.exit(2);//退出
            }

            Thread.sleep(50);//50毫秒刷新一次图像
        }
    }

    /**
     * 二维码解析
     *
     * @param bImage
     * @return
     */
    public static String qrParse(BufferedImage bImage) {
        MultiFormatReader reader = new MultiFormatReader();
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bImage)));
        Hashtable<DecodeHintType, Object> hints = new Hashtable<>();
        //设置编码格式
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        try {
            Result result = reader.decode(binaryBitmap, hints);
            //System.out.println("二维码文本内容:"+result.getText());
            return result.getText();
        } catch (NotFoundException e) {
            //图片中不包含二维码 do not
            //e.printStackTrace();
            return null;
        }
    }
}
