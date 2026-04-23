package com.dreamer.postservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dreamer.postservice.entity.pojo.Likes;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LikesMapper extends BaseMapper<Likes> {
}
