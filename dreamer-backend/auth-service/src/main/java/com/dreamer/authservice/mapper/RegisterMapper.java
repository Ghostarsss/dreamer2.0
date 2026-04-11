package com.dreamer.authservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dreamer.common.entity.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RegisterMapper extends BaseMapper<User> {
}
