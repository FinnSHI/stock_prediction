package com.finn.stock.timed;

import com.finn.stock.exception.ApiException;
import com.finn.stock.repository.dao.StockInfoDao;
import com.finn.stock.repository.entity.StockInfoDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/*
 * @description: 获取月股票列表
 * @author: Finn
 * @create: 2022/07/24 22:07
 */
@Slf4j
@Component
public class StcokTask {

    @Autowired
    private StockInfoDao stockInfoDao;

    // 每个月1号0点执行
    @Scheduled(cron = "0 0 0 1 * ?")
    public void getMonthStockList() {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date(System.currentTimeMillis());
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        try {
            String today = formatter.format(now);
            String StockList = getStockByInvokePython(today);
            StockInfoDO build = StockInfoDO.builder()
                    .stockList(StockList)
                    .month(month)
                    .build();
            stockInfoDao.insert(build);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private String getStockByInvokePython(String today) {
        BufferedReader in = null;
        BufferedReader err = null;
        try {
            // #传递参数为{日期}
            String[] argvs = new String[]{"python",
                    "\\stock\\src\\main\\resources\\python\\get_stock_monthly.py", today};
            Process proc = Runtime.getRuntime().exec(argvs);// 执行py文件
            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line = null;
            StringBuilder error = new StringBuilder();
            if (!Objects.isNull(line = in.readLine())) {
                return line;
            }
            while (!Objects.isNull(line = err.readLine())) {
                System.out.println(line);
                error.append(line);
            }
            if (error.length() > 0) {
                throw new ApiException(error.toString());
            }
            proc.waitFor();
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApiException("调用 get_stock_monthly.py 失败！");
        } finally {
            try {
                if (!Objects.isNull(in))
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
