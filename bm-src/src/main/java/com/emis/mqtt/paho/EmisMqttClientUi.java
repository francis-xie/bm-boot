package com.emis.mqtt.paho;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class EmisMqttClientUi {

	private JFrame frmApolloMqttDemo;
	private JTextField txtServer;
	private JTextField txtPort;
	private JTextField txtClientid;
	private JTextField txtUsername;
	private JTextField txtPassword;
	private JTextField txtSubTopic;
	private JTextField txtPubTopic;

	private EmisMqttClient client;
	private JRadioButton rbSslOn;
	private JRadioButton rbSslOff;
	private JLabel lblStatus;
	private JTextArea txtSubResult;
	private JTextArea txtPubMsg;
	private JRadioButton rbRetainedOff;
	private JRadioButton rbRetainedOn;
	private JList listSubTopic;
	private JComboBox cmbSubQos;
	private JComboBox cmbPubQos;
	private JButton btnConnect;
	private JButton btnDisconnect;

	private final PushCallback connectionLostCallback = new PushCallback() {
		public void connectionLost(Throwable cause) {
			client.startReconnect();
		}
	};

	private final PushCallback deliveryCompleteCallback = new PushCallback() {
		public void deliveryComplete(IMqttDeliveryToken token) {
			System.out
					.println("deliveryComplete---------" + token.isComplete());
		}
	};
	private final PushCallback messageArrivedCallback = new PushCallback() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
		StringBuffer sb = new StringBuffer();

		public void messageArrived(String topic, MqttMessage message)
				throws Exception {
			sb.setLength(0);
			sb.append("======").append(sdf.format(System.currentTimeMillis()))
					.append("======\n");
			sb.append("接收消息主题:\t").append(topic).append("\n");
			sb.append("接收消息Qos:\t").append(message.getQos()).append("\n");
			sb.append("接收消息内容:\t").append(new String(message.getPayload()))
					.append("\n");
			txtSubResult.insert(sb.toString(), 0);
		}
	};

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EmisMqttClientUi window = new EmisMqttClientUi();
					window.frmApolloMqttDemo.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public EmisMqttClientUi() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmApolloMqttDemo = new JFrame();
		frmApolloMqttDemo.setTitle("Apollo MQTT Demo  for Venus ");
		frmApolloMqttDemo.setResizable(false);
		frmApolloMqttDemo.setBounds(100, 100, 450, 335);
		frmApolloMqttDemo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmApolloMqttDemo.getContentPane().setLayout(new BorderLayout(0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmApolloMqttDemo.getContentPane().add(tabbedPane);

		createConnectPanel(tabbedPane);

		createSubscribePanel(tabbedPane);

		careatePublishPanel(tabbedPane);

		lblStatus = new JLabel("Status：");
		frmApolloMqttDemo.getContentPane().add(lblStatus, BorderLayout.SOUTH);
	}

	protected void createConnectPanel(JTabbedPane tabbedPane) {
		JPanel connectPanel = new JPanel();
		tabbedPane.addTab("Connect", null, connectPanel, null);
		connectPanel.setLayout(null);

		JLabel lblPort = new JLabel("Port");
		lblPort.setBounds(10, 58, 42, 15);
		connectPanel.add(lblPort);

		JLabel lblServer = new JLabel("Server");
		lblServer.setBounds(10, 25, 54, 15);
		connectPanel.add(lblServer);

		txtServer = new JTextField();
		// txtServer.setText("211.149.169.63");
		txtServer.setText("iot.eclipse.org");
		txtServer.setBounds(55, 22, 359, 21);
		connectPanel.add(txtServer);
		txtServer.setColumns(10);

		txtPort = new JTextField();
		// txtPort.setText("61613");
		txtPort.setText("1883");
		txtPort.setBounds(55, 55, 359, 21);
		connectPanel.add(txtPort);
		txtPort.setColumns(10);

		JLabel lblClientId = new JLabel("Client ID");
		lblClientId.setBounds(10, 128, 62, 15);
		connectPanel.add(lblClientId);

		txtClientid = new JTextField();

		txtClientid.setText("Client"
				+ (Double.valueOf(Math.random() * 100000)).intValue());
		txtClientid.setColumns(10);
		txtClientid.setBounds(82, 125, 332, 21);
		connectPanel.add(txtClientid);

		JLabel lblUsername = new JLabel("Username");
		lblUsername.setBounds(10, 156, 62, 15);
		connectPanel.add(lblUsername);

		txtUsername = new JTextField();
		// txtUsername.setText("venus");
		txtUsername.setColumns(10);
		txtUsername.setBounds(82, 153, 332, 21);
		connectPanel.add(txtUsername);

		JLabel lblPassword = new JLabel("Password");
		lblPassword.setBounds(10, 185, 62, 15);
		connectPanel.add(lblPassword);

		txtPassword = new JTextField();
		// txtPassword.setText("venus");
		txtPassword.setColumns(10);
		txtPassword.setBounds(82, 182, 332, 21);
		connectPanel.add(txtPassword);

		JLabel lblCleanSession = new JLabel("Clean Session");
		lblCleanSession.setBounds(10, 213, 94, 15);
		connectPanel.add(lblCleanSession);

		JRadioButton rbCleanSessionOff = new JRadioButton("OFF");
		rbCleanSessionOff.setBounds(105, 209, 49, 23);
		connectPanel.add(rbCleanSessionOff);

		JRadioButton rbCleanSessionOn = new JRadioButton("ON");
		rbCleanSessionOn.setSelected(true);
		rbCleanSessionOn.setBounds(156, 209, 62, 23);
		connectPanel.add(rbCleanSessionOn);

		ButtonGroup cleanSession = new ButtonGroup();
		cleanSession.add(rbCleanSessionOff);
		cleanSession.add(rbCleanSessionOn);

		JLabel lblSsl = new JLabel("SSL");
		lblSsl.setBounds(69, 238, 30, 15);
		connectPanel.add(lblSsl);

		rbSslOff = new JRadioButton("OFF");
		rbSslOff.setSelected(true);
		rbSslOff.setBounds(105, 234, 49, 23);
		connectPanel.add(rbSslOff);

		rbSslOn = new JRadioButton("ON");
		rbSslOn.setBounds(156, 234, 61, 23);
		connectPanel.add(rbSslOn);

		ButtonGroup ssl = new ButtonGroup();
		ssl.add(rbSslOn);
		ssl.add(rbSslOn);

		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doConnect();
			}
		});
		btnConnect.setBounds(112, 86, 93, 23);
		connectPanel.add(btnConnect);

		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (client != null && doDisconnect()) {
					btnConnect.setEnabled(true);
					btnDisconnect.setEnabled(false);
					showStatus("Disconnected!");
				} else {
					showStatus("Failed to disconnect!");
				}
			}
		});
		btnDisconnect.setBounds(239, 86, 108, 23);
		btnDisconnect.setEnabled(false);
		connectPanel.add(btnDisconnect);
	}

	protected void createSubscribePanel(JTabbedPane tabbedPane) {
		JPanel subscribePanel = new JPanel();
		tabbedPane.addTab("Subscribe", null, subscribePanel, null);
		subscribePanel.setLayout(null);

		JLabel lblSubTopic = new JLabel("Topic");
		lblSubTopic.setBounds(25, 13, 42, 15);
		subscribePanel.add(lblSubTopic);

		JLabel lblSubQos = new JLabel("QOS");
		lblSubQos.setBounds(25, 46, 42, 15);
		subscribePanel.add(lblSubQos);

		JButton btnSubscribe = new JButton("Subscribe");
		btnSubscribe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSubscribe();
			}
		});
		btnSubscribe.setBounds(175, 42, 121, 23);
		subscribePanel.add(btnSubscribe);

		txtSubTopic = new JTextField();
		txtSubTopic.setText("test/topic");
		txtSubTopic.setColumns(10);
		txtSubTopic.setBounds(70, 10, 359, 21);
		subscribePanel.add(txtSubTopic);

		txtSubResult = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(txtSubResult);
		scrollPane.setBounds(10, 75, 286, 177);
		subscribePanel.add(scrollPane);

		JButton btnUnsubscribe = new JButton("UnSubscribe");
		btnUnsubscribe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doUnSubscribe();
			}
		});
		btnUnsubscribe.setBounds(308, 42, 121, 23);
		subscribePanel.add(btnUnsubscribe);

		JScrollPane scrollPaneSubTopic = new JScrollPane();
		scrollPaneSubTopic.setBounds(306, 75, 123, 177);
		subscribePanel.add(scrollPaneSubTopic);

		listSubTopic = new JList();
		listSubTopic.setModel(new DefaultListModel());
		scrollPaneSubTopic.setViewportView(listSubTopic);

		cmbSubQos = new JComboBox();
		cmbSubQos.setModel(new DefaultComboBoxModel(new String[] { "0", "1",
				"2" }));
		cmbSubQos.setBounds(70, 43, 95, 21);
		cmbSubQos.setSelectedIndex(1);
		subscribePanel.add(cmbSubQos);
	}

	protected void careatePublishPanel(JTabbedPane tabbedPane) {
		JPanel publishPanel = new JPanel();
		publishPanel.setLayout(null);
		tabbedPane.addTab("Publish", null, publishPanel, null);

		JLabel lblPubTopic = new JLabel("Topic");
		lblPubTopic.setBounds(25, 13, 42, 15);
		publishPanel.add(lblPubTopic);

		JLabel lblPubQos = new JLabel("QOS");
		lblPubQos.setBounds(25, 46, 42, 15);
		publishPanel.add(lblPubQos);

		JButton btnPublish = new JButton("Publish");
		btnPublish.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doPublish();
			}
		});
		btnPublish.setBounds(170, 74, 93, 23);
		publishPanel.add(btnPublish);

		txtPubTopic = new JTextField();
		txtPubTopic.setText("test/topic");
		txtPubTopic.setColumns(10);
		txtPubTopic.setBounds(70, 10, 359, 21);
		publishPanel.add(txtPubTopic);

		JLabel lblRetained = new JLabel("Retained");
		lblRetained.setBounds(251, 46, 60, 15);
		publishPanel.add(lblRetained);

		rbRetainedOff = new JRadioButton("OFF");
		rbRetainedOff.setBounds(317, 42, 49, 23);
		publishPanel.add(rbRetainedOff);

		rbRetainedOn = new JRadioButton("ON");
		rbRetainedOn.setSelected(true);
		rbRetainedOn.setBounds(368, 42, 61, 23);
		publishPanel.add(rbRetainedOn);

		ButtonGroup retained = new ButtonGroup();
		retained.add(rbRetainedOff);
		retained.add(rbRetainedOn);

		JLabel lblMessage = new JLabel("Message");
		lblMessage.setBounds(25, 97, 100, 15);
		publishPanel.add(lblMessage);

		JScrollPane scrollPanePubMsg = new JScrollPane();
		scrollPanePubMsg.setBounds(10, 122, 419, 131);
		publishPanel.add(scrollPanePubMsg);

		txtPubMsg = new JTextArea();
		scrollPanePubMsg.setViewportView(txtPubMsg);

		cmbPubQos = new JComboBox();
		cmbPubQos.setModel(new DefaultComboBoxModel(new String[] { "0", "1",
				"2" }));
		cmbPubQos.setBounds(70, 43, 95, 21);
		cmbPubQos.setSelectedIndex(1);
		publishPanel.add(cmbPubQos);
	}

	protected void showStatus(String msg) {
		lblStatus.setText("Status: " + msg);
	}

	/**
	 * 连接
	 */
	protected void doConnect() {
		String host = (rbSslOff.isSelected() ? "tcp://" : "tls://")
				+ txtServer.getText().trim() + ":" + txtPort.getText().trim();
		String username = txtUsername.getText().trim();
		String password = txtPassword.getText().trim();
		String clientid = txtClientid.getText().trim();

		client = new EmisMqttClient(host, username, password, clientid);
		client.setConnectLostCallback(connectionLostCallback);
		if (client.connect()) {
			showStatus("Connected!");
			btnConnect.setEnabled(false);
			btnDisconnect.setEnabled(true);
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					doDisconnect();
				}
			});
		} else {
			showStatus("Failed to connect!");
		}
	}

	/**
	 * 断开连接
	 * 
	 * @return
	 */
	protected boolean doDisconnect() {
		if (client != null) {
			try {
				int size = listSubTopic.getModel().getSize();
				for (int i = 0; i < size; i++) {
					client.unsubscribe((String) listSubTopic.getModel()
							.getElementAt(i));
				}
				client.disconnect();
				return true;
			} catch (MqttException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/**
	 * 订阅
	 */
	protected void doSubscribe() {
		try {
			if (client != null) {
				String topic = txtSubTopic.getText().trim();
				int qos = cmbSubQos.getSelectedIndex();
				client.setMessageArrivedCallback(messageArrivedCallback);
				client.subscribe(topic, qos);
				DefaultListModel model = (DefaultListModel) listSubTopic
						.getModel();
				if (!(model).contains(topic))
					model.addElement(topic);
				showStatus(" Subscribe " + topic + " Qos Level:" + qos);
			} else {
				showStatus(" Failed to Subscribe, Please connect firstly !");
			}
		} catch (MqttException ex) {
			showStatus(" Failed to Subscribe !");
			ex.printStackTrace();
		}
	}

	/**
	 * 取消订阅
	 */
	protected void doUnSubscribe() {
		try {
			if (client != null) {
				if (listSubTopic.getSelectedIndex() != -1) {
					String topic = (String) listSubTopic.getSelectedValue();
					client.unsubscribe(topic);

					((DefaultListModel) listSubTopic.getModel())
							.remove(listSubTopic.getSelectedIndex());
					showStatus(" Unsubscribe " + topic);
				}
			} else {
				showStatus(" Failed to UnSubscribe, Please connect firstly !");
			}
		} catch (MqttException e1) {
			showStatus(" Failed to Subscribe !");
			e1.printStackTrace();
		}
	}

	/**
	 * 发布
	 */
	protected void doPublish() {
		if (client != null) {
			client.setDeliveryCompleteCallback(deliveryCompleteCallback);
			String topic = txtPubTopic.getText().trim();
			int qos = cmbPubQos.getSelectedIndex();
			String msg = txtPubMsg.getText();
			if (topic.length() == 0 || msg.length() == 0)
				return;
			boolean retained = rbRetainedOff.isSelected() ? false : true;
			try {
				client.publish(topic, msg, qos, retained);
				showStatus(" Published topic:" + topic + " qos:" + qos);
			} catch (MqttException ex) {
				ex.printStackTrace();
			}
		} else {
			showStatus(" Failed to Publish, Please connect firstly !");
		}
	}
}
