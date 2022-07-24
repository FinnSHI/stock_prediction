import pandas as pd
import numpy as np
import talib as ta
import tushare as ts
import stockstats
from datetime import datetime, timedelta
import datetime
from pandas import to_datetime
import akshare as ak
import copy
import matplotlib.pyplot as plt
import seaborn as sns
import palettable
from sklearn.preprocessing import StandardScaler
from sklearn.decomposition import PCA
from sklearn.model_selection import train_test_split
from sklearn.svm import SVR,NuSVR
from sklearn.metrics import mean_squared_error as mse
from sklearn.metrics import r2_score
from sklearn.model_selection import GridSearchCV
from sklearn.cluster import AgglomerativeClustering,KMeans
import random as rn
import math
from sklearn import svm
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

def PCA_10(df_raw):
    indi_raw=pd.DataFrame(df_raw)
    corrmat=indi_raw.corr() 
    #indi_corr= del_by_corr(corrmat)
    indi_corr=indi_raw#补丁
    indi_array=np.array(indi_corr)
    
    data_sc= StandardScaler().fit_transform(indi_array)#标准化
    pca = PCA(copy=True,n_components=10,whiten=bool)#mle自动选取是49个成分
    pc = pca.fit_transform(data_sc)#训练pca模型
    pca_result = pd.DataFrame(pc)
    return pca_result

#用的是当前文件夹nlp.csv
def merge_nlp(df_raw,start,end):
    nlp_raw=pd.read_csv('nlp.csv')
    nlp_raw['datetime'] =to_datetime(nlp_raw.datetime)
    df_raw['datetime'] =to_datetime(df_raw.datetime)
    nlp_dated=nlp_raw.loc[(nlp_raw["datetime"] >= start)&(nlp_raw["datetime"] <= end)]
    merge=pd.merge(df_raw, nlp_dated, left_on="datetime", right_on="datetime",how='inner')
    return merge #结果返回：删掉了所有没有nlp的数据（30%没有）

def SVM_pred_return(code,start,end):#预测end之后一个月的平均利润率
    
    start2=to_datetime(end)
    start2=(start2-datetime.timedelta(days=1500)).strftime("%Y%m%d")
    df_test=pro.daily(code=code,start=start2,end=end)
    if df_test.shape[0]<1000:
        return -1
    df_raw=factor_cal(code,start,end)
    if df_raw.shape[0]<200:
        return -1
    df_raw=merge_nlp(df_raw,start,end)#加nlp
    df_raw.drop('datetime',axis=1,inplace=True)
    df_raw.drop(['PSY','ts_code','trade_date'],axis=1,inplace=True)
    #加收益率，并且保存出来
    df_raw2=df_raw.shift(-22)
    #print(df_raw2) close=close_x
    df_raw3=pd.DataFrame()
    df_raw3['return'] = df_raw2['close_x']/df_raw['close_x']
    df_raw3=df_raw3.dropna()
    df_return=copy.deepcopy(df_raw3['return'])#保存训练的收益率
#     df_raw3.drop('datetime',axis=1,inplace=True)
#     df_raw3.drop(['PSY','ts_code','trade_date'],axis=1,inplace=True)
    
    
    if df_raw3.isnull().sum().sum()>0:
        print('df_raw.isnull()>0')
    if df_raw3.isna().sum().sum()>0:
        print('df_raw.isnull()>0')
        
    #pca
    pca_result=PCA_10(df_raw)#pca降维
    pca_result2=pca_result.loc[df_raw3.index]
    #train model
    x_train, x_test, y_train, y_test = train_test_split(pca_result2,df_return,test_size=0.05)
    Stand_X = StandardScaler()  # 特征进行标准化
    Stand_Y = StandardScaler()  # 标签也是数值，也需要进行标准化
    x_train = Stand_X.fit_transform(x_train)
    x_test = Stand_X.transform(x_test)
    best_parameters = {'kernel': ['rbf'],'C': [1,5,10],'gamma': [0.1,0.2,0.5]}
    model = GridSearchCV(SVR(), param_grid=best_parameters, cv=4)
    model.fit(x_train, y_train)
    print ("Trainset R2:" , (model.score(x_train, y_train)))
    print ("Testset R2:" , (model.score(x_test, y_test)))#83%以上
    #predict
    y_predict = model.predict(np.array(pca_result.iloc[-1]).reshape(1, -1))
    
    return y_predict[0] #array

def stock_selection(start,end):#根据什么时间段的数据选股，间隔建议不超过一年，end为价格预测开始时间
    #沪深300范围选股
    sz_stocklist=pro.hs_const(hs_type='SZ')
    sh_stocklist=pro.hs_const(hs_type='SH')
    all_stocklist=sz_stocklist.append(sh_stocklist)
    stocklist=all_stocklist[-all_stocklist.ts_code.isin(['000040.SZ'])]#去掉数据不全的股票
    stocklist=stocklist['ts_code']#沪深300范围选股
    #预测stocklist对应的return
    return_pred =pd.DataFrame(columns=('ts_code','return'))
    for i in stocklist:
        print(i)
        #df_raw=factor_cal(i,start,end)
        #if df_raw.shape[0]>1:
        return_tem=SVM_pred_return(i,start,end)
        print(return_tem)
        df_combine=pd.DataFrame([[i, return_tem]], columns=['ts_code','return'])
        return_pred = return_pred.append(df_combine,ignore_index=True)
    return_sorted=return_pred.sort_values(by='return',ascending=False)
    return_sorted.reset_index()
    return_sorted.to_csv('return_sorted.csv')
    return return_sorted.iloc[0:10,0] 

#时间#
if __name__=='__main__':
    lis = []
    t = sys.argv[1]
    t1 = (datetime.datetime.strptime(t, '%Y-%m-%d %H:%M:%S') - datetime.timedelta(days=140)).strftime("%Y-%m-%d %H:%M:%S")
    t1 = str(t1)
    t = t[0:4] + t[5:7] + t[8:10]
    t1 = t1[0:4] + t1[5:7] + t1[8:10]
    stocklist=stock_selection(t1,t)
    for i in range(len(stocklist)):
        lis.append(stocklist.iloc[i,0])
    print(lis)
