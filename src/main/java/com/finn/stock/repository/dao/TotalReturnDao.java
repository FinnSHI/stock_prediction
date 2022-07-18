package com.finn.stock.repository.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finn.stock.repository.entity.TotalReturnDO;
import org.apache.ibatis.annotations.Mapper;

/*
 * @description: total_return
 * @author: Finn
 * @create: 2022/07/16 16:18
 */
@Mapper
public interface TotalReturnDao extends BaseMapper<TotalReturnDO> {
}
