import pandas as pd
import pymysql

if __name__=='__main__':
    #写自己的数据库信息#
    db = pymysql.connect(host="127.0.0.1", user="admin", port=3306, passwd= "123456", db= "stock")
    cursor = db.cursor()

    #输入文件地址#
    data_set = pd.read_csv('C:/Users/shifa/Desktop/all_return.xlsx')

    for i in range(len(data_set)):
        #输入要加入的数据列#
        row = [data_set.iloc[i, 0], data_set.iloc[i, 2]]
        #修改表名，表内属性和value#
        sql="insert into total_return(date, benchmark_return, total_return) values(%s,%s,%s)"
        cursor.execute(sql,row)
        db.commit()
    db.close()