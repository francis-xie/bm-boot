package com.emis.test;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.IplImage;

/**
 * 使用javacv 1.2 获取摄像头图像，并识别图像中的二维码，
 * https://gitee.com/frogchou/FiwanQRCode/blob/master/src/com/fiwan/qrgenerator/ZxingHandler.java
 */
public class ScanCode {
    static OpenCVFrameGrabber grabber;
    static String retStr = null;

    public static void main(String[] args) throws Exception {
        grabber = new OpenCVFrameGrabber(0);
        grabber.start(); // 开始获取摄像头数据
        // Frame frame = grabber.grab();
        new Thread1().start();
    }

    /*接受一个IplImage 对象，转换为BufferedImage
     * @param IplImage
     * @return BufferedImage
     * */
    public static BufferedImage iplToBufImgData(IplImage mat) {
        if (mat.height() > 0 && mat.width() > 0) {
            BufferedImage image = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_3BYTE_BGR);
            WritableRaster raster = image.getRaster();
            DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
            byte[] data = dataBuffer.getData();
            mat.getByteBuffer().get(data);
            return image;
        }
        return null;
    }

    /**
     * 自定义一个Thread1，处理图像显示，防止界面被卡死
     */
    static class Thread1 extends Thread {
        @Override
        public void run() {
            //一个非常不错的硬件加速组件，用于我们的预览...
            CanvasFrame cFrame = new CanvasFrame("摄像预览", CanvasFrame.getDefaultGamma() / grabber.getGamma());
            Frame frame = null;
            int i = 0;
            try {
                while (cFrame.isVisible() && (frame = grabber.grab()) != null) {
                    if (cFrame.isVisible()) {
                        //在预览中显示我们的框架
                        cFrame.showImage(frame);
                    }

                    OpenCVFrameConverter.ToIplImage toIplImage = new OpenCVFrameConverter.ToIplImage();
                    IplImage image = toIplImage.convert(frame);
                    BufferedImage bufferedImage = iplToBufImgData(image);
				
				/* //测试IplImage转BufferedImage是否成功 
				 try { File file=new File("D://tpm111//"+i+++".jpg");
				 file.createNewFile();
				  ImageIO.write(bufferedImage, "jpg", file); } catch 
				 (IOException
				  e1) { e1.printStackTrace(); }*/

                    retStr = ZxingHandler.getQrcodeFromPic(bufferedImage);
                    if (retStr != null && !"".equals(retStr)) {
                        System.out.println(retStr);
                        // 是否获取到内容，是则退出循环
                        break;
                    }
                    try {
                        Thread.sleep(10);// 10毫秒刷新一次图像
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            try {
                cFrame.dispose();
                grabber.stop();
                grabber.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
