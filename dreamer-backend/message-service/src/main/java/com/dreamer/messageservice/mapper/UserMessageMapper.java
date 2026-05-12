package com.dreamer.messageservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dreamer.messageservice.entity.pojo.UserMessage;
import com.dreamer.messageservice.entity.vo.MessageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMessageMapper extends BaseMapper<UserMessage> {

    @Select("select count(*) from user_message where user_id = #{userId} and read_status = 0")
    int messageNotReadCount(Long userId);

    List<MessageVo> listMessage(long userId);

    @Update("update user_message set read_status = 1 where user_id = #{userId}")
    void isRead(long userId);
}
