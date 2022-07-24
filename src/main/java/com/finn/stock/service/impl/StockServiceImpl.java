package com.finn.stock.service.impl;

import com.finn.stock.service.StockService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


/*
 * @description: 调用python相关算法
 * @author: Finn
 * @create: 2022/07/15 11:10
 */
public class StockServiceImpl implements StockService {

    public static void main(String[] args) {
        int a = 18;
        int b = 23;
        try {
            String[] args1 = new String[] { "python", "C:\\Users\\shifa\\Desktop\\test.py", String.valueOf(a), String.valueOf(b) };
            Process proc = Runtime.getRuntime().exec(args1);// 执行py文件

            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> invokePython() {
        return null;
    }
}
