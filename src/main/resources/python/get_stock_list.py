import pandas as pd
import numpy as np
import talib as ta
import tushare as ts
import stockstats
from datetime import datetime, timedelta
import datetime
from pandas import to_datetime
# import akshare as ak
from sklearn.preprocessing import  MinMaxScaler
from sklearn.linear_model import Lasso
from sklearn.model_selection import train_test_split 
import sklearn.model_selection as ms
# from sklearn import metrics
import lightgbm as lgb
import sys

pro = ts.pro_api('b8063ba5dc239f3af72477772623d0c95b570f0285b29550d1f86c48')

def factor_cal(code,start,end):
    start =  datetime.datetime.strptime(start, '%Y%m%d') 
    start = start - timedelta(days=119)
    start=datetime.datetime.strftime(start, "%Y%m%d")

    #Technical factor
    df = pro.daily(ts_code=code, start_date=start, end_date=end)
    time01=to_datetime(df.trade_date,format="%Y%m%d")
    df.trade_date=time01
    df=df.sort_values(by='trade_date')
    stockStat = stockstats.StockDataFrame.retype(df)
    stockStat['volume']=stockStat['vol']
    open=df['open']
    close=df['close']
    high=df['high']
    low=df['low']
    volume=df['vol']
    amount=df['amount']
    df_tech=pd.DataFrame()
    df_tech['datetime']=df['trade_date']
    df_tech['open']=open
    df_tech['close']=close
    df_tech['high']=high
    df_tech['low']=low
    df_tech['volume']=volume
    df_tech['amount']=amount
    def arbr(high,low,open,close):
        HO=high-open
        OL=open-low
        HCY=high-close.shift(1)
        CYL=close.shift(1)-low
    #计算AR、BR指标
        AR=ta.SUM(HO, timeperiod=26)/ta.SUM(OL, timeperiod=26)*100
        BR=ta.SUM(HCY, timeperiod=26)/ta.SUM(CYL, timeperiod=26)*100

        return AR,BR
    AR,BR=arbr(high,low,open,close)
    df_tech['AR']=AR
    df_tech['BR']=BR
    CR=stockStat[['cr','cr-ma1','cr-ma2','cr-ma3']]
    df_tech=pd.concat([df_tech, CR], axis=1)
    VR=stockStat[['vr','vr_6_sma'] ]
    df_tech=pd.concat([df_tech, VR], axis=1)
    def PSY(priceData, period):
        difference = priceData[1:] - priceData[:-1]
        difference = np.append(0, difference)
        difference_dir = np.where(difference > 0, 1, 0)
        psy = np.zeros((len(priceData),))
        psy[:period] *= np.nan
        for i in range(period, len(priceData)):
            psy[i] = (difference_dir[i-period+1:i+1].sum()) / period
        return psy*100
    PSY=PSY(close, 12)
    PSYMA=ta.MA(PSY, timeperiod=6)
    df_tech['PSY']=PSY
    df_tech['PSYMA']=PSYMA
    CCI = ta.CCI(high, low, close, timeperiod=14)
    df_tech['CCI']=CCI
    MFI=ta.MFI(high, low, close, volume, timeperiod=14)
    df_tech['MFI']=MFI
    ROC=ta.ROC(close, timeperiod=10)
    df_tech['ROC']=ROC
    KDJ=stockStat[['kdjk','kdjd','kdjj']]
    df_tech=pd.concat([df_tech, KDJ], axis=1)
    WR=stockStat[['wr_10','wr_6'] ]
    df_tech=pd.concat([df_tech, WR], axis=1)
    RSI=stockStat[['rsi_6','rsi_12'] ]
    df_tech=pd.concat([df_tech, RSI], axis=1)
    def RVI(close,open,high,low):
        X = close-open+2*(close.shift()-open.shift())+2*(close.shift(periods=2)-open.shift(periods=2))*(close.shift(periods=3)-open.shift(periods=3))/6
        Y = high-low+2*(high.shift()-low.shift())+2*(high.shift(periods=2)-low.shift(periods=2))*(high.shift(periods=3)-low.shift(periods=3))/6
        Z = ta.MA(X, timeperiod=10)*10
        D = ta.MA(Y, timeperiod=10)*10
        return Z/D 
    RVI=RVI(close,open,high,low)
    df_tech['RVI']=RVI
    TR=stockStat[['tr','atr'] ]
    df_tech=pd.concat([df_tech, TR], axis=1)
    def BIAS(close, timeperiod):
        if isinstance(close,np.ndarray):
            pass
        else:
            close = np.array(close)
        MA = ta.MA(close,timeperiod=timeperiod)
        return (close-MA)/MA
    BIAS6=BIAS(close,6)
    BIAS12=BIAS(close,12)
    BIAS24=BIAS(close,24)
    df_tech['BIAS6']=BIAS6
    df_tech['BIAS12']=BIAS12
    df_tech['BIAS24']=BIAS24
    MACD_dif, MACD_dea, MACD_hist =ta.MACD(close, fastperiod=12, slowperiod=26, signalperiod=9)
    df_tech['MACD_dif']=MACD_dif
    df_tech['MACD_dea']=MACD_dea
    df_tech['MACD_hist']=MACD_hist
    TRIX=stockStat[['trix','trix_9_sma'] ]
    df_tech=pd.concat([df_tech, TRIX], axis=1)
    DMI=stockStat[['pdi','mdi','dx','adx','adxr'] ]
    df_tech=pd.concat([df_tech, DMI], axis=1)
    DMA=stockStat[['dma'] ]
    df_tech=pd.concat([df_tech, DMA], axis=1)
    AMA=ta.MA(df_tech['dma'],timeperiod=10)
    df_tech['AMA']=AMA
    def DPO(close,timeperiod):
        p = ta.MA(close, timeperiod=timeperiod)
        p.shift()
        return close-p
    DPO=DPO(close,timeperiod=20)
    MADPO=ta.MA(DPO, timeperiod=6)
    df_tech['DPO']=DPO
    df_tech['MADPO']=MADPO
    def VHF(close):
        LCP = ta.MIN(close, timeperiod=28)
        HCP = ta.MAX(close, timeperiod=28)
        NUM = HCP - LCP
        pre = close.copy()
        pre = pre.shift()
        DEN = abs(close-close.shift())
        DEN = ta.MA(DEN, timeperiod=28)*28
        return NUM.div(DEN)
    VHF=VHF(close)
    df_tech['VHF']=VHF
    MTM = ta.MOM(close, timeperiod=6)
    MTMMA=ta.MA(MTM,timeperiod=12)
    df_tech['MTM']=MTM
    df_tech['MTMMA']=MTMMA
    OBV=ta.OBV(close, volume)
    df_tech['OBV']=OBV
    AD=ta.AD(high, low, close, volume)
    df_tech['AD']=AD
    MAVOL5=ta.MA(volume,timeperiod=5)
    MAVOL10=ta.MA(volume,timeperiod=10)
    df_tech['MAVOL5']=MAVOL5
    df_tech['MAVOL10']=MAVOL10
    AMO6=ta.MA(amount,timeperiod=6)
    AMO12=ta.MA(amount,timeperiod=12)
    AMO24=ta.MA(amount,timeperiod=24)
    df_tech['AMO6']=AMO6
    df_tech['AMO12']=AMO12
    df_tech['AMO24']=AMO24
    MA10=ta.MA(close, timeperiod=10, matype=0)
    MA120=ta.MA(close, timeperiod=120, matype=0)
    df_tech['MA10']=MA10
    df_tech['MA120']=MA120
    EXPMA12=ta.EMA(close, timeperiod=12)
    EXPMA50=ta.EMA(close, timeperiod=50)
    df_tech['EXPMA12']=EXPMA12
    df_tech['EXPMA50']=EXPMA50
    upperband, middleband, lowerband = ta.BBANDS(close, timeperiod=20, nbdevup=2, nbdevdn=2, matype=0)
    df_tech['BBANDS_UP']=upperband
    df_tech['BBANDS_MID']=middleband
    df_tech['BBANDS_LOW']=lowerband
    df_tech=df_tech.fillna(method='ffill')
    df_tech=df_tech.fillna(method='bfill')
    
    #fundamental factor
    df1 = pro.fina_indicator(ts_code=code)
    df2 = pro.balancesheet(ts_code=code)
    df3 = pro.income(ts_code=code)
    df1.drop_duplicates(subset=['ann_date'],keep='first',inplace=True)
    df2.drop_duplicates(subset=['ann_date'],keep='first',inplace=True)
    df3.drop_duplicates(subset=['ann_date'],keep='first',inplace=True)
    df4=pd.merge(pd.merge(df1,df2,on='ann_date'),df3,on='ann_date')
    df_fund=pro.daily_basic(ts_code=code, start_date=start, end_date=end)
    df_fund.drop(['close','volume_ratio'], axis=1, inplace=True)
    roc=df4['op_income']/df4['total_hldr_eqy_inc_min_int']
    doe=(df4['total_profit']-df4['income_tax'])/df4['total_share']
    roe=df4['roe']
    bps=df4['bps']
    eps=df4['eps']
    ros=(df4['total_profit']-df4['income_tax'])/df4['revenue']
    ior=df4['total_revenue']/df4['total_cogs']
    qr=df1['quick_ratio']
    eod=df4['total_hldr_eqy_inc_min_int']/df4['total_liab']
    doe=1/eod
    lr=df4['total_liab']/df4['total_assets']
    dte=df4['debt_to_eqt']
    fr=df4['fix_assets']/df4['total_hldr_eqy_inc_min_int']
    ipm=df4['total_profit']/df4['income_tax']
    prr=(df4['total_profit']-df4['income_tax'])-(df4['prfshare_payable_dvd']+df4['comshare_payable_dvd']+df4['capit_comstock_div'])/(df4['total_profit']-df4['income_tax'])
    rtr=df4['ar_turn']
    ft=df4['fa_turn']
    at=df4['assets_turn']
    df_report=pd.concat([df4['ann_date'],roc,doe,roe,bps,eps,ros,ior,doe,eod,lr,dte,fr,at],axis=1)
    df_report.columns=['ann_date','roc','doe','roe','bps','eps','ros','ior','doe','eod','lr','dte','fr','at']
#     macro_china_gdp_df = ak.macro_china_gdp()
#     macro_cnbs = ak.macro_cnbs()
# #     macro_china_enterprise_boom_index_df = ak.macro_china_enterprise_boom_index()
#     macro_china_national_tax_receipts_df = ak.macro_china_national_tax_receipts()
#     macro_china_czsr_df = ak.macro_china_czsr()
    def dateprocess(df):
        df= pd.to_datetime(df)
        df = df.apply(lambda x : x.strftime('%Y-%m-%d'))
        return df
    df_fund['trade_date']=dateprocess(df_fund['trade_date'])
    df_report.dropna(axis=0,subset=['ann_date'],inplace=True)
    df_report['ann_date']=dateprocess(df_report['ann_date'])
    df_fund=pd.merge(df_fund,df_report,left_on='trade_date',right_on='ann_date',how='left')
    df_fund=df_fund.drop('ann_date',axis=1)
    df_fund=df_fund.fillna(method='ffill')
# #     df_fund=pd.merge(df_fund,macro_china_gdp_df,left_on='trade_date',right_on='季度',how='left')
# #     df_fund=df_fund.drop('季度',axis=1)
#     macro_cnbs['年份']=dateprocess(macro_cnbs['年份'])
#     df_fund=pd.merge(df_fund,macro_cnbs,left_on='trade_date',right_on='年份',how='left')
#     df_fund=df_fund.drop('年份',axis=1)
# #     macro_china_enterprise_boom_index_df['季度']=dateprocess(macro_china_enterprise_boom_index_df['季度'])
# #     df_fund=pd.merge(df_fund,macro_china_enterprise_boom_index_df,left_on='trade_date',right_on='季度',how='left')
#     df_fund=df_fund.drop('季度',axis=1)
#     macro_china_national_tax_receipts_df['季度']=dateprocess(macro_china_national_tax_receipts_df['季度'])
#     df_fund=pd.merge(df_fund,macro_china_national_tax_receipts_df,left_on='trade_date',right_on='季度',how='left')
#     df_fund=df_fund.drop('季度',axis=1)
#     macro_china_czsr_df['月份']=dateprocess(macro_china_czsr_df['月份'])
#     df_fund=pd.merge(df_fund,macro_china_czsr_df,left_on='trade_date',right_on='月份',how='left')
#     df_fund=df_fund.drop('月份',axis=1)
    df_fund=df_fund.fillna(method='bfill')
    
    #merge
    df_tech['datetime']= pd.to_datetime(df_tech['datetime'])
    df_tech['datetime'] = df_tech['datetime'].apply(lambda x : x.strftime('%Y-%m-%d'))
    df_all=pd.merge(df_tech,df_fund,left_on='datetime',right_on='trade_date')
    df_all=df_all[80:]
    df_all=df_all.dropna()
    
    return df_all

def preprocess(df):
    #对因子数据进行预处理和降维
    df_close=df['close']
    scaler = MinMaxScaler()
    x = scaler.fit_transform(df.drop(columns=['datetime','ts_code','trade_date']))
    data=pd.DataFrame(x)
    data.columns=df.drop(columns=['datetime','ts_code','trade_date']).columns
    df_close=data['close']

#     df_close=df.close
#     df=df.drop(columns=['datetime','ts_code','trade_date'])
#     for i in df.columns:
#         df[i]=df[i]/df[i].mean()
    
    X=data
    y=df_close.shift(-1).fillna(method='ffill')
    X_train,X_test,y_train,y_test = train_test_split(X,y,random_state=42)
    lasso0001 = Lasso(alpha=0.001).fit(X_train,y_train)
    lasso_list=lasso0001.coef_!=0
    i=0
    l=[]
    for item in lasso_list:
        if item==True:
            l.append(i)
        i=i+1
    df_lasso=X.iloc[:,l]
    
    return df_lasso,df_close 
def slide_window(data, n_in=1):
    #对因子数据构造滑动窗口
    n_vars = 1 if type(data) is list else data.shape[1]
    df = pd.DataFrame(data)
    cols, names = list(), list()
    for i in range(n_in, 0, -1):
        cols.append(df.shift(i))
        names += [('var%d(t-%d)' % (j+1, i)) for j in range(n_vars)]
    
    agg = pd.concat(cols, axis=1)
    agg.columns = names
    return agg

def sig_cal(df_close):
    #通过计算涨跌，对数据标注买入卖出信号
    df_sig=[]
    for i in range(1,df_close.shape[0]):
        if df_close.iloc[i]>df_close.iloc[i-1]:
            df_sig.append(1)
        else:
            df_sig.append(0)
    return df_sig

def logistic_obj(p, dtrain):
    #自定义目标函数
    gamma = 5
    alpha = 0.55
    y = p
    p = 1.0 / (1.0 + np.exp(-dtrain))
    grad = p * (1 - p) * (alpha * gamma * y * (1 - p) ** gamma * np.log(p) / (1 - p) - alpha * y * (
                1 - p) ** gamma / p - gamma * p ** gamma * (1 - alpha) * (1 - y) * np.log(1 - p) / p + p ** gamma * (
                                      1 - alpha) * (1 - y) / (1 - p))
    hess = p * (1 - p) * (p * (1 - p) * (
                -alpha * gamma ** 2 * y * (1 - p) ** gamma * np.log(p) / (1 - p) ** 2 + alpha * gamma * y * (
                    1 - p) ** gamma * np.log(p) / (1 - p) ** 2 + 2 * alpha * gamma * y * (1 - p) ** gamma / (
                            p * (1 - p)) + alpha * y * (1 - p) ** gamma / p ** 2 - gamma ** 2 * p ** gamma * (
                            1 - alpha) * (1 - y) * np.log(1 - p) / p ** 2 + 2 * gamma * p ** gamma * (1 - alpha) * (
                            1 - y) / (p * (1 - p)) + gamma * p ** gamma * (1 - alpha) * (1 - y) * np.log(
            1 - p) / p ** 2 + p ** gamma * (1 - alpha) * (1 - y) / (1 - p) ** 2) - p * (
                                      alpha * gamma * y * (1 - p) ** gamma * np.log(p) / (1 - p) - alpha * y * (
                                          1 - p) ** gamma / p - gamma * p ** gamma * (1 - alpha) * (1 - y) * np.log(
                                  1 - p) / p + p ** gamma * (1 - alpha) * (1 - y) / (1 - p)) + (1 - p) * (
                                      alpha * gamma * y * (1 - p) ** gamma * np.log(p) / (1 - p) - alpha * y * (
                                          1 - p) ** gamma / p - gamma * p ** gamma * (1 - alpha) * (1 - y) * np.log(
                                  1 - p) / p + p ** gamma * (1 - alpha) * (1 - y) / (1 - p)))
    return grad, hess

def model_LGBM(code,date):
    #建立预测模型(date为当前日期)
    
    end=date
    date=datetime.datetime.strptime(date, '%Y%m%d') 
    start = date - timedelta(days=1095)
    start=datetime.datetime.strftime(start, "%Y%m%d")
    
    df=factor_cal(code,start,end)
    df_lasso,df_close=preprocess(df)
    df_lasso_reframed=slide_window(df_lasso, n_in=5)
    df_pre=df_lasso_reframed.loc[df_lasso_reframed.index.max()]
    df_lasso_reframed.drop([df_lasso_reframed.index.max()],inplace=True)
    df_sig=sig_cal(df_close)
    
   
    model=lgb.LGBMClassifier()
    params = {'n_estimators': [150,200],
          'learning_rate':[0.05,0.1,0.2], 
          'max_depth':[8],
         'min_child_samples':[30,40,50],
         'feature_fraction':[0.5,0.6,0.8,1],
         'obj':['logistic_obj']
         }
    # 网格搜索 确定最优超参数

    # 网格搜索 确定最优超参数
    model = ms.GridSearchCV(model, params, cv=4)
    model.fit(np.array(df_lasso_reframed), df_sig)
    x=np.array(df_pre).reshape(1,-1)
    y=model.predict(x)
    return y[0]

#时间2022-01-10 11:11:11 股票票列表#
if __name__=='__main__':
    lis = []
    sum = '0'
    t = sys.argv[1]
    t1 = (datetime.datetime.strptime(t, '%Y-%m-%d %H:%M:%S') + datetime.timedelta(days=1)).strftime("%Y-%m-%d %H:%M:%S")
    t = str(t1)
    t = t[0:4] + t[5:7] + t[8:10]
    stock_list = sys.argv[2]
    try:
        for i in range(0,10):
            y = model_LGBM(stock_list[(9*i):(9*(i+1))],t)
            sum = sum + str(y)
        print(sum[1:])
    except:
        ori = sys.argv[3]
        print(ori)

