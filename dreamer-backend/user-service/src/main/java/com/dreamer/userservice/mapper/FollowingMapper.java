package com.dreamer.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dreamer.userservice.entity.pojo.UserFollow;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FollowingMapper extends BaseMapper<UserFollow> {
}
