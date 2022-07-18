import sys
from datetime import datetime

def annual_f(fdate,frate,edate,erate):
    
    time1 = datetime.strptime(fdate, '%Y-%m-%d %H:%M:%S')
    time2 = datetime.strptime(edate, '%Y-%m-%d %H:%M:%S')
    days = (time2 - time1).days

    f = pow((erate/frate),(365/int(days)))- 1
    return f

if __name__ == "__main__":
    fdate = sys.argv[1]
    frate = float(sys.argv[2])
    edate = sys.argv[3]
    erate = float(sys.argv[4])
    # fdate = '2022-06-02 00:00:00'
    # frate = 1.00
    # edate = '2022-06-05 00:00:00'
    # erate = 1.27
    print(annual_f(fdate, frate, edate, erate))



