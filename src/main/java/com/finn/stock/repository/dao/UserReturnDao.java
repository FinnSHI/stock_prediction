package com.finn.stock.repository.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finn.stock.repository.entity.UserReturnDO;
import org.apache.ibatis.annotations.Mapper;

/*
 * @description:
 * @author: Finn
 * @create: 2022/07/19 14:09
 */
@Mapper
public interface UserReturnDao extends BaseMapper<UserReturnDO>  {
}
