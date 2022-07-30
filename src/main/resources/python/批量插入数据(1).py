import datetime

import pandas as pd
import decimal
import datetime as dt
import pymysql



if __name__=='__main__':
    #写自己的数据库信息#
    db = pymysql.connect(host="127.0.0.1", user="root", port=3306, passwd= "admin", db= "stock", charset='utf8')
    cursor = db.cursor()

    #输入文件地址#
    data_set = pd.read_csv('C:/Users/shifa/Desktop/all_return.csv')

    for i in range(len(data_set)):
        #输入要加入的数据列#
        date_0 = str(data_set.iloc[i, 1])
        date_1 = date_0[0:4] + '-' + date_0[4:6] + '-' + date_0[6:8] + ' 00:00:00'
        data = datetime.datetime.strptime(date_1, '%Y-%m-%d %H:%M:%S')
        t1 = decimal.Decimal(str(data_set.iloc[i, 2]))
        # t1 = float(str(data_set.iloc[i, 2]))
        t2 = decimal.Decimal(str(data_set.iloc[i, 3]))
        # t2 = float(str(data_set.iloc[i, 3]))
        row = [data, t1, t2]
        print(row)
        #修改表名，表内属性和value#
        # sql = "insert into total_return(date, benchmark_return, total_return) values(str_to_date('\%s\','%%Y-%%m-%%d %%H:%%i:%%s.%%f')), %s, %s)"
        sql = "insert into total_return(date, benchmark_return, total_return) values(%s,%s,%s)"
        cursor.execute(sql, row)
        db.commit()
    db.close()
            