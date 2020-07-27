package com.emis.test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

//import org.bytedeco.javacpp.avcodec;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder.Exception;
import org.bytedeco.javacv.OpenCVFrameGrabber;

/**
 * 来源：javacv-1.2-bin\javacv-bin\samples\WebcamAndMicrophoneCapture.java
 * 此类是广播视频捕获设备（例如，网络摄像头）和音频捕获设备（例如，麦克风）的简单示例
 * 使用FFmpegFrameRecorder。FFmpegFrameRecorder允许输出目标是FILE或RTMP端点（Wowza，FMS等）
 * 重要提示：线程间的音频/视频同步存在潜在的时序问题，我正在努力寻找解决方案，但是如果您能解决问题，请拨入：o）
 */
public class OpenCVFrameGrabberTest {
    final private static int WEBCAM_DEVICE_INDEX = 1;
    final private static int AUDIO_DEVICE_INDEX = 4;

    final private static int FRAME_RATE = 30;
    final private static int GOP_LENGTH_IN_FRAMES = 60;

    private static long startTime = 0;
    private static long videoTS = 0;

    public static void main(String[] args) throws Exception, org.bytedeco.javacv.FrameGrabber.Exception {
        int captureWidth = 1024;
        int captureHeight = 720;

        //可用的FrameGrabber类包括OpenCVFrameGrabber（opencv_videoio）...
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        //grabber.setImageWidth(captureWidth);
        //grabber.setImageHeight(captureHeight);
        grabber.start();

        /*// org.bytedeco.javacv.FFmpegFrameRecorder.FFmpegFrameRecorder(String
        // filename, int imageWidth, int imageHeight, int audioChannels)
        //对于每个参数，我们传入...
        // filename =我们要创建的本地文件的路径，或者RTMP网址到FMS / Wowza服务器
        // imageWidth =我们为采集卡指定的宽度
        // imageHeight =我们为采集卡指定的高度
        // audioChannels = 2，因为我们喜欢立体声
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                "stream_name",
                captureWidth, captureHeight, 2);
        recorder.setInterleaved(true);

        //减少FFMPEG中的“启动”延迟(请参阅：https://trac.ffmpeg.org/wiki/StreamingGuide)
        recorder.setVideoOption("tune", "zerolatency");
        //在质量和编码速度之间进行权衡
        //可能的值是超快，超快，非常快，更快，快，中，慢，慢，非常慢
        // ultrafast为我们提供最少的压缩量（较低的编码器CPU），但要占用更大的流
        //在另一端，veryslow提供最佳压缩率（高编码器CPU），同时减小流的大小
        // (请参阅: https://trac.ffmpeg.org/wiki/Encode/H.264)
        recorder.setVideoOption("preset", "ultrafast");
        //恒定速率因子 (请参阅: https://trac.ffmpeg.org/wiki/Encode/H.264)
        recorder.setVideoOption("crf", "28");
        // 2000 kb / s，720的合理“合理”区域
        recorder.setVideoBitrate(2000000);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("flv");
        // FPS（每秒帧数）
        recorder.setFrameRate(FRAME_RATE);
        //关键帧间隔，在本例中为每2秒-> 30（fps）* 2 = 60（长度）
        recorder.setGopSize(GOP_LENGTH_IN_FRAMES);

        //我们不想要可变比特率的音频
        recorder.setAudioOption("crf", "0");
        // 最好的质量
        recorder.setAudioQuality(0);
        // 192 Kbps
        recorder.setAudioBitrate(192000);
        recorder.setSampleRate(44100);
        recorder.setAudioChannels(2);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);

        //杰克可乐...做...
        recorder.start();

        //音频捕获线程，如果您愿意，可以在嵌套的私有类中...
        new Thread(new Runnable() {
            @Override
            public void run() {
                //选择一种格式...
                //注意：最好枚举系统支持的格式，因为getLine（）可能会以任何特定格式出错...
                //对于我们：44.1采样率，16位，立体声，带符号，小端
                AudioFormat audioFormat = new AudioFormat(44100.0F, 16, 2, true, false);

                //获取具有该格式的TargetDataLine
                Mixer.Info[] minfoSet = AudioSystem.getMixerInfo();
                Mixer mixer = AudioSystem.getMixer(minfoSet[AUDIO_DEVICE_INDEX]);
                DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

                try {
                    //打开并开始捕获音频
                    //通过此行可以对所选音频设备进行更多控制：
                    // TargetDataLine line = (TargetDataLine)mixer.getLine(dataLineInfo);
                    TargetDataLine line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
                    line.open(audioFormat);
                    line.start();

                    int sampleRate = (int) audioFormat.getSampleRate();
                    int numChannels = audioFormat.getChannels();

                    //让我们初始化音频缓冲区...
                    int audioBufferSize = sampleRate * numChannels;
                    byte[] audioBytes = new byte[audioBufferSize];

                    //使用ScheduledThreadPoolExecutor与while循环进行比较
                    //Thread.sleep将允许我们解决一些特定于操作系统的计时问题，并保持
                    //更精确 时钟作为固定速率进行垃圾收集 时间等
                    //类似的方法可用于网络摄像头捕获 同样，如果您愿意
                    ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                    exec.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //从行中读取...非阻塞
                                int nBytesRead = line.read(audioBytes, 0, line.available());

                                //由于我们在AudioFormat中指定了16位，我们需要将读取的byte []转换为short []
                                //（请参阅来自FFmpegFrameRecorder.recordSamples的AV_SAMPLE_FMT_S16的源代码）
                                //让我们初始化short []数组
                                int nSamplesRead = nBytesRead / 2;
                                short[] samples = new short[nSamplesRead];

                                //让我们将short []包装到ShortBuffer中，将其传递给recordSamples
                                ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                                ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);

                                //记录器是 org.bytedeco.javacv.FFmpegFrameRecorder
                                recorder.recordSamples(sampleRate, numChannels, sBuff);
                            } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 0, (long) 1000 / FRAME_RATE, TimeUnit.MILLISECONDS);
                } catch (LineUnavailableException e1) {
                    e1.printStackTrace();
                }
            }
        }).start();*/

        //一个非常不错的硬件加速组件，用于我们的预览...
        CanvasFrame cFrame = new CanvasFrame("Capture Preview", CanvasFrame.getDefaultGamma() / grabber.getGamma());

        Frame capturedFrame = null;

        //当我们捕获...
        while ((capturedFrame = grabber.grab()) != null) {
            if (cFrame.isVisible()) {
                //在预览中显示我们的框架
                cFrame.showImage(capturedFrame);
            }

            //让我们定义开始时间...
            //需要将其初始化为接近于 可能 因为从赋值到计算时间的增量可能太高
            if (startTime == 0)
                startTime = System.currentTimeMillis();

            //为此帧创建时间戳
            videoTS = 1000 * (System.currentTimeMillis() - startTime);

            /*//检查AV漂移
            if (videoTS > recorder.getTimestamp()) {
                System.out.println(
                        "Lip-flap correction: "
                                + videoTS + " : "
                                + recorder.getTimestamp() + " -> "
                                + (videoTS - recorder.getTimestamp()));

                //我们告诉记录器在此时间戳记下写此帧
                recorder.setTimestamp(videoTS);
            }

            //将框架发送到 org.bytedeco.javacv.FFmpegFrameRecorder
            recorder.record(capturedFrame);*/
        }

        cFrame.dispose();
        //recorder.stop();
        grabber.stop();
    }
}
