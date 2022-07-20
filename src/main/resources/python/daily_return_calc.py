import pandas as pd
import tushare as ts
from datetime import datetime, timedelta
import datetime
from pandas import to_datetime
import sys
pro = ts.pro_api('b8063ba5dc239f3af72477772623d0c95b570f0285b29550d1f86c48')

def day_return(yes_status,status,date,code):
    #date需为交易日，否则报错
    end =  datetime.datetime.strptime(date, '%Y%m%d') 
    start = end - timedelta(days=1)
    start=datetime.datetime.strftime(start, "%Y%m%d")
    end=datetime.datetime.strftime(end, "%Y%m%d")
    try:
        df=pro.daily(ts_code=code, start_date=start, end_date=end)
        df['avg']=(df.high+df.low)/2
        ret1=df.close.iloc[0]/df.close.iloc[1]
        ret2=df.close.iloc[0]/df.avg.iloc[0]
        ret3=df.avg.iloc[0]/df.close.iloc[1]
        if yes_status==0 and status==0:
            day_return=1
        elif yes_status==1 and status==1:
            day_return=ret1
        elif yes_status==1 and status==0:
            day_return=ret3
        elif yes_status==0 and status==1:
            day_return=ret2

        return day_return
    except:
        return 1

#传递参数为{日期，十只股票昨天状态字符 e.g. 101110101，十只股票今天状态e.g. 1010101101，股票名称 e.g. 0000001.sz0000002.sz000003.sz}
if __name__=='__main__':
    lis = []
    sum = 0
    t = sys.argv[1]
    t = t[0:4] + t[5:7] + t[8:10]
    y_data = sys.argv[2]
    to_data = sys.argv[3]
    stock_list = sys.argv[4]
    for i in range(0,10):
        lis.append((day_return(y_data[i],to_data[i],t,stock_list[(9*i):(9*(i+1))])))
    for j in range(len(lis)):
        sum = sum + float(lis[i])
    ave = sum/len(lis)
    print(lis)