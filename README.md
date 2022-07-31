# README

## Create User

![image-20220731160948774](https://finn-typora.oss-cn-shanghai.aliyuncs.com/pic/202207311609897.png)

This interface can be used to create a user with an id  and the total cash of the user.

![image-20220731161337308](https://finn-typora.oss-cn-shanghai.aliyuncs.com/pic/202207311613349.png)

After creating, a record will be inserted into the database.

![image-20220731161351122](https://finn-typora.oss-cn-shanghai.aliyuncs.com/pic/202207311613154.png)

![image-20220731161411792](https://finn-typora.oss-cn-shanghai.aliyuncs.com/pic/202207311614820.png)



## Get Cumulative Return

![image-20220731161457906](https://finn-typora.oss-cn-shanghai.aliyuncs.com/pic/202207311614943.png)

This interface can be used to get the cumulative return of the user with the user's id.

![image-20220731161630819](https://finn-typora.oss-cn-shanghai.aliyuncs.com/pic/202207311616866.png)

## Timed Task

### Stock List

![image-20220731163942682](https://finn-typora.oss-cn-shanghai.aliyuncs.com/pic/202207311639744.png)

This timed task can be used to select the list of stocks that the user should choose that day, at 12 noon every day. The list of stock selected will be recorded in the database.

![image-20220731165132259](https://finn-typora.oss-cn-shanghai.aliyuncs.com/pic/202207311651287.png)





### Calculate User Return Daily

![image-20220731165202791](https://finn-typora.oss-cn-shanghai.aliyuncs.com/pic/202207311652855.png)

This task is used at 4:00 pm every day. According to the stocks selected by the user, the user's rate of return for the day is calculated, and the user's cumulative rate of return is calculated through the previous daily rate of return.



## Get Backtest

![image-20220731165441932](https://finn-typora.oss-cn-shanghai.aliyuncs.com/pic/202207311654965.png)

This interface can calculate the backtest data in the time period according to the incoming start date and end date.





## Get Bench Mark Return

![image-20220731165530336](https://finn-typora.oss-cn-shanghai.aliyuncs.com/pic/202207311655373.png)

This interface can calculate the bench mark return based on the incoming start date and end date.