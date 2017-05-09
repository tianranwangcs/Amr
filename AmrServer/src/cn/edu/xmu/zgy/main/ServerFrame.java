package cn.edu.xmu.zgy.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import cn.edu.xmu.zgy.server.AmrServer;

//blog.csdn.net/zgyulongfei
//Email: zgyulongfei@gmail.com

public class ServerFrame extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	private JButton openButton;
	private JButton closeButton;
	private JPanel panel;
	private JTextArea textArea;

	public ServerFrame() {
		super("【amr音频实时传输】服务端");

		int width = 500, height = 400;
		this.setSize(width, height);

		panel = new JPanel();
		add(panel, BorderLayout.NORTH);

		openButton = new JButton("开启服务器");
		openButton.addActionListener(this);
		panel.add(openButton);

		closeButton = new JButton("关闭服务器");
		closeButton.setEnabled(false);
		closeButton.addActionListener(this);
		panel.add(closeButton);

		textArea = new JTextArea();
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);

		Toolkit tk = this.getToolkit();
		Dimension ds = tk.getScreenSize();
		this.setLocation((ds.width - width) / 2, (ds.height - height) / 2);
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent event) {
		Object obj = event.getSource();
		if (obj == openButton) {
			openButton.setEnabled(false);
			start();
			displayMessage("服务端已经开启");
			closeButton.setEnabled(true);
		} else if (obj == closeButton) {
			openButton.setEnabled(true);
			stop();
			displayMessage("服务端已经关闭");
			closeButton.setEnabled(false);
		}
	}

	private AmrServer amrServer;

	private void start() {
		amrServer = new AmrServer();
		amrServer.start();
	}

	private void stop() {
		try {
			if (amrServer != null) {
				amrServer.stop();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void displayMessage(final String messageToDisplay) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textArea.append(messageToDisplay + "\r\n");
			}
		});
	}
}
