package me.xjn;

import java.util.Timer;
import java.util.TimerTask;

import me.xjn.service.FlightChecker;

/**
 * 检查机票价格变动
 *
 */
public class App {
    public static void main(String[] args) {
        System.out.println("App starting(测试字符编码)...");
        Timer timer = new Timer();
        // 每隔5分钟检查一次机票
        int period = 5 * 60 * 1000;
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    FlightChecker.execute();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 1, period);
    }
}
