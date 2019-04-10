package me.xjn.service;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {
    public static void send(String subject, String content) {
        Properties properties = System.getProperties();
        String host = "smtp.126.com";
        String from = "xu_jiannan@126.com";
        String to = "xu_jiannan@126.com";
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.auth", "true");
        Session session = Session.getDefaultInstance(properties);
        try {
            // 创建默认的 MimeMessage 对象。
            MimeMessage message = new MimeMessage(session);

            // Set From: 头部头字段
            message.setFrom(new InternetAddress(from));

            // Set To: 头部头字段
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: 头字段
            message.setSubject(subject);

            // 发送 HTML 消息, 可以插入html标签
            message.setContent(content, "text/html; charset=UTF-8");

            // 发送消息
            Transport transport = session.getTransport("smtp");
            transport.connect(host, "xu_jiannan@126.com", "qjijklw123456");
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}