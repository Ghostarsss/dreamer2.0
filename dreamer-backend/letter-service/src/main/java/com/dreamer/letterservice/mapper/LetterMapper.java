package com.dreamer.letterservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dreamer.letterservice.entity.pojo.FutureLetter;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LetterMapper extends BaseMapper<FutureLetter> {
}
