package com.finn.stock.timed;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finn.stock.exception.ApiException;
import com.finn.stock.repository.dao.StockInfoDao;
import com.finn.stock.repository.dao.UserReturnDao;
import com.finn.stock.repository.entity.StockInfoDO;
import com.finn.stock.repository.entity.UserReturnDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/*
 * @description: 每日计算收益率
 * @author: Finn
 * @create: 2022/07/19 17:01
 */
@Slf4j
@Component
public class CalculateReturnTask {

    @Autowired
    private UserReturnDao userReturnDao;

    @Autowired
    private StockInfoDao stockInfoDao;

    // 测试用
//    @Scheduled(cron = "0/20 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    // 每天中午12点，选出股票
    @Scheduled(cron = "0 0 12 * * ?")
    public void getStockList() {
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date(System.currentTimeMillis());
        Date yest = new Date(System.currentTimeMillis() - 86400000L);;
        String today = null;
        String yesterday = null;
        try {
            // 今天
            today = formatter.format(now);
            // 昨天
            yesterday = formatter.format(yest);
            List<UserReturnDO> userReturnDOS = null;
            int tryTimes = 1;
            while (userReturnDOS == null) {
                if (tryTimes == 10) {
                    throw new ApiException("获取不到用户信息");
                }
                userReturnDOS = userReturnDao.selectList(new LambdaQueryWrapper<UserReturnDO>()
                        .gt(UserReturnDO::getDate, yesterday)
                        .le(UserReturnDO::getDate, today)
                );
                tryTimes++;
            }

            for (UserReturnDO userReturn : userReturnDOS) {
                if(!judgeIsTradeDay(today)) {
                    userReturn.setDate(today);
                    userReturnDao.insert(userReturn);
                } else {
                     String stockList = getStockList(formatter.format(userReturn.getDate()), userReturn.getSelectedStock());
                    UserReturnDO build = UserReturnDO.builder()
                            .userInfoId(userReturn.getUserInfoId())
                            .date(today)
                            .selectedStock(stockList)
                            .userReturn(null)
                            .cumuReturn(null)
                            .selectedStockYest(userReturn.getSelectedStock())
                            .lastCumuReturn(userReturn.getCumuReturn())
                            .build();
                    userReturnDao.insert(build);
                }

            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    // 每天下午四点执行，算出 user_return
    @Scheduled(cron = "0 0 16 * * ?")
    public void calculateReturnDaily() {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 获取中午12点的时间
        Date todayNoon = new Date(getTodayStartTime());
        Date todayNoon1 = new Date(getTodayStartTime() - 10*60*1000);
        Date todayNoon2 = new Date(getTodayStartTime() - 10*60*1000);
        String today = null;
        String today1 = null;
        String today2 = null;
        try {
            // 今天：前后10分钟误差
            today = formatter.format(todayNoon);
            today1 = formatter.format(todayNoon1);
            today2 = formatter.format(todayNoon2);
            // 用户今天的信息
            List<UserReturnDO> todayUserReturns = null;
            int tryTimes = 1;
            while (todayUserReturns == null) {
                if (tryTimes == 10) {
                    throw new ApiException("获取不到用户信息");
                }
                todayUserReturns = userReturnDao.selectList(new LambdaQueryWrapper<UserReturnDO>()
                        .gt(UserReturnDO::getDate, today1)
                        .le(UserReturnDO::getDate, today2)
                );
                tryTimes++;
            }

            for (UserReturnDO userReturnDO : todayUserReturns) {
                if(!judgeIsTradeDay(today)) {
                    userReturnDO.setDate(today);
                    userReturnDao.insert(userReturnDO);
                } else {
                   BigDecimal userReturn = new BigDecimal(getUserReturn(today, userReturnDO.getSelectedStockYest(),
                        userReturnDO.getSelectedStock(), getCurMonthStock()));
                   userReturnDO.setUserReturn(userReturn);
                   userReturnDO.setCumuReturn(userReturn.multiply(userReturnDO.getLastCumuReturn()));
                   userReturnDao.updateById(userReturnDO);
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /*
    * @Description: 获得当天中午12点的时间戳
    * @Param: []
    * @return: long
    * @Author: Finn
    * @Date: 2022/07/24 10:12
    */
    private long getTodayStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime().getTime();
    }

    /*
    * @Description: 获取当前月的股票
    * @Param: []
    * @return: long
    * @Author: Finn
    * @Date: 2022/07/24 10:24
    */
    private String getCurMonthStock() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH)+1; //得到月
        StockInfoDO stockInfoDO = stockInfoDao
                .selectOne(new LambdaQueryWrapper<StockInfoDO>().eq(StockInfoDO::getMonth, month));

        return stockInfoDO.getStockList();
    }

    /* 
    * @Description: 判断是否是交易日 
    * @Param: [today] 
    * @return: java.lang.Boolean 
    * @Author: Finn
    * @Date: 2022/07/24 20:25
    */
    private Boolean judgeIsTradeDay(String today) {
        BufferedReader in = null;
        BufferedReader err = null;
        try {
            // #传递参数为{日期}
            String[] argvs = new String[]{"python", "\\stock\\src\\main\\resources\\python\\is_trade_day.py", today};
            Process proc = Runtime.getRuntime().exec(argvs);// 执行py文件
            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line = null;
            StringBuilder error = new StringBuilder();
            if (!Objects.isNull(line = in.readLine())) {
                if (Integer.parseInt(line) == 1) return true;
                if (Integer.parseInt(line) == 0) return false;
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
            throw new ApiException("调用 is_trade_day.py 失败！");
        } finally {
            try {
                if (!Objects.isNull(in))
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    

    /*
    * @Description: 调用 user_return.py
    * @Param: [today, yestStock, todayStock, stocks]
    * @return: java.lang.String
    * @Author: Finn
    * @Date: 2022/07/24 10:23
    */
    public String getUserReturn(String today, String yestStock, String todayStock, String stocks) {
        BufferedReader in = null;
        BufferedReader err = null;
        try {
            // #传递参数为{日期，十只股票昨天状态字符 e.g. 101110101，十只股票今天状态e.g. 1010101101，股票名称 e.g. 0000001.sz0000002.sz000003.sz}
            String[] argvs = new String[]{"python", "\\stock\\src\\main\\resources\\python\\daily_return_calc.py",
                    today, yestStock, todayStock, stocks};
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
            throw new ApiException("调用 user_return.py 失败！");
        } finally {
            try {
                if (!Objects.isNull(in))
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    * @Description: 调用 get_stock_list.py
     * @Param: [time, stockList]
    * @return: java.lang.String
    * @Author: Finn
    * @Date: 2022/07/24 10:23
    */
    public String getStockList(String time, String stockList) {
        BufferedReader in = null;
        BufferedReader err = null;
        try {
            // #传递参数为{日期，十只股票昨天状态字符 e.g. 101110101}
            String[] argvs = new String[]{"python", "\\stock\\src\\main\\resources\\python\\get_stock_list.py",
                    time, stockList};
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
            throw new ApiException("调用 get_stock_list.py 失败！");
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
