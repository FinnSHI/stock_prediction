package com.finn.stock.timed;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finn.stock.exception.ApiException;
import com.finn.stock.repository.dao.StockInfoDao;
import com.finn.stock.repository.dao.UserReturnDao;
import com.finn.stock.repository.entity.UserReturnDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/*
 * @description: 每日计算收益率
 * @author: Finn
 * @create: 2022/07/19 17:01
 */
@Slf4j
public class CalculateReturnTask {

    @Autowired
    private StockInfoDao userInfoDao;

    @Autowired
    private UserReturnDao userReturnDao;

    @Transactional(rollbackFor = Exception.class)
    @Scheduled(cron = "0 0 16 * * ?")
    public void calculateReturnDaily() {

        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date(System.currentTimeMillis());
        Date yest = new Date(System.currentTimeMillis() - 86400000L);
        Date today = null;
        Date yesterDay = null;
        try {
            today = formatter.parse(formatter.format(now));
            yesterDay = formatter.parse(formatter.format(yest));
            List<UserReturnDO> userReturnDOS = userReturnDao.selectList(new LambdaQueryWrapper<UserReturnDO>()
                    .gt(UserReturnDO::getDate, yesterDay)
                    .le(UserReturnDO::getDate, today)
            );
            for (UserReturnDO userReturn : userReturnDOS) {

            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    public String getUserReturn(String today, UserReturnDO userReturn) {
        BufferedReader in = null;
        BufferedReader err = null;
        try {
            // #传递参数为{日期，十只股票昨天状态字符 e.g. 101110101，十只股票今天状态e.g. 1010101101，股票名称 e.g. 0000001.sz0000002.sz000003.sz}
            String[] argvs = new String[] { "python", "\\stock\\src\\main\\resources\\python\\daily_return_calc.py.py", today, };
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
            throw new ApiException("调用Python算法失败！");
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