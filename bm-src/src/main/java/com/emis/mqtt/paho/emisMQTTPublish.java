package com.emis.mqtt.paho;

import com.emis.db.emisProp;
import com.emis.server.emisServer;
import com.emis.server.emisServerFactory;
import com.emis.util.emisLogger;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import javax.servlet.ServletContext;
import java.util.Observable;

/**
 * $Id$
 * MQTT消息推送-发布
 */
public class emisMQTTPublish extends Observable {

  private volatile static emisMQTTPublish emisMQTTPublish = null;

  public static emisMQTTPublish getInstance() {
    if (emisMQTTPublish == null) {
      synchronized (emisMQTTPublish.class) {
        if (emisMQTTPublish == null) {
          emisMQTTPublish = new emisMQTTPublish();
        }
      }
    }
    return emisMQTTPublish;
  }

  private final PushCallback connectionLostCallback = new PushCallback() {
    public void connectionLost(Throwable cause) {
      client.startReconnect();
    }
  };

  private EmisMqttClient client;
  private boolean isConnect = false;
  private Logger oLogger_ = null;
  private String custID = "";

  /**
   * 连接
   */
  private void doConnect(ServletContext context_) {
    oLogger_.info("-- MQTT Connect --");

    /*String sServer = "211.149.169.63";
    String sPort = "61613";
    String username = "venus";
    String password = "venus";*/
    String sServer = "";
    String sPort = "";
    String username = "";
    String password = "";
    try {
      emisProp prop = emisProp.getInstance(context_);
      sServer = prop.get("MQTT_SERVER");
      sPort = prop.get("MQTT_SERVER_PORT");
      username = prop.get("MQTT_USERNAME");
      password = prop.get("MQTT_PASSWORD");
      if("2".equals(prop.get("MQTT_SERVER_TYPE"))) {
        custID = prop.get("MQTT_CUST_ID");
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      oLogger_.error(ex, ex);
    }
    emisServer _oServer = emisServerFactory.getServer(context_);
    String _sServerName = _oServer.getServerName();
    if(_sServerName == null || "".equals(_sServerName)) {
      _sServerName  = "SERVER01";
    }
    oLogger_.info("clientid:" + _sServerName);

    String clientid = _sServerName;

    String host = (true ? "tcp://" : "tls://") + sServer + ":" + sPort;
    client = new EmisMqttClient(host, username, password, clientid);
    client.setConnectLostCallback(connectionLostCallback);
    if (client.connect()) {
      isConnect = true;
      oLogger_.info("-- MQTT Connect successful --");
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          if (client != null) {
            client.disconnect();
          }
        }
      });
    } else {
      isConnect = false;
      oLogger_.info("-- MQTT Connect error --");
    }
  }

  private final PushCallback deliveryCompleteCallback = new PushCallback() {
    public void deliveryComplete(IMqttDeliveryToken token) {
      oLogger_.info("deliveryComplete: " + token.isComplete());
    }
  };

  /**
   * 发布
   */
  public void doPublish(ServletContext context_, String msg) {
    String topic = "".equals(custID) ? "venus/waimai" : custID + "/venus/waimai"; // 主题
    this.doPublish(context_, msg, topic);
  }

  /**
   * 发布
   * @param context_ sevletcontext
   * @param msg      发布信息
   * @param topic    发布主题
   */
  public void doPublish(ServletContext context_, String msg, String topic) {
    try {
      if (oLogger_ == null) {
        oLogger_ = emisLogger.getlog4j(context_, this.getClass().getName());
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    oLogger_.info("topic:" + topic + "publish: " + msg);

    if (client == null || !isConnect) {
      doConnect(context_);
    }
    if (client != null && isConnect) {
      if (topic != null && !"".equals(topic)) {
        client.setDeliveryCompleteCallback(deliveryCompleteCallback);
        int qos = 1;
        if (topic.length() == 0 || msg.length() == 0)
          return;
        boolean retained = true; // rbRetainedOff.isSelected() ? false : true;
        try {
          client.publish(topic, msg, qos, retained);
        } catch (MqttException ex) {
          ex.printStackTrace();
          oLogger_.error(ex, ex);
        }
      } else {
        oLogger_.error(" Topic is null, Please check it !");
      }
    } else {
      oLogger_.error(" Failed to Publish, Please connect firstly !");
    }
  }

}