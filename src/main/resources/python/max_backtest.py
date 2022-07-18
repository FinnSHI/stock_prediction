import ffn
import sys
import pandas as pd

if __name__=='__main__':
    # sys.argv = ['', '1.00', '1.09', '1.18', '1.27']
    lis = []
    for i in range(1, len(sys.argv)):
        lis.append((float(sys.argv[i])))
    # lis = [1.00, 1.09, 1.18, 1.27]
    li = pd.DataFrame(lis)
    x = ffn.calc_max_drawdown(li)
    result = x[0]
    print(result)