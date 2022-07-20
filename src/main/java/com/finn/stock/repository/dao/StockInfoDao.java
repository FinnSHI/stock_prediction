package com.finn.stock.repository.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finn.stock.repository.entity.StockInfoDO;
import com.finn.stock.repository.entity.UserInfoDO;
import org.apache.ibatis.annotations.Mapper;

/*
 * @description: stock_info
 * @author: Finn
 * @create: 2022/07/16 16:18
 */
@Mapper
public interface StockInfoDao extends BaseMapper<StockInfoDO> {
}
