                                                   import sys
import tushare as ts

pro = ts.pro_api('b8063ba5dc239f3af72477772623d0c95b570f0285b29550d1f86c48')

#判定当天日期是否为交易日#
#输入当天日期#
if __name__=='__main__':
    t = sys.argv[1]
    t = t[0:4] + t[5:7] + t[8:10]
    try:
        df = pro.daily(ts_code='000001.SZ', start_date=t, end_date=t)
        e = df.iloc[0,0]
        print(1)
    except:
        print(0)