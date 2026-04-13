package com.dreamer.messageservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.common.entity.pojo.UserMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMessageMapper extends BaseMapper<UserMessage> {

    @Select("select count(*) from user_message where user_id = #{userId} and read_status = 0")
    int messageNotReadCount(Integer userId);

    List<MessageDto> listMessage(int userId);

    @Update("update user_message set read_status = 1 where user_id = #{userId}")
    void isRead(int userId);
}
