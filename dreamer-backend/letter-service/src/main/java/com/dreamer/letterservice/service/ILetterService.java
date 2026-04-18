package com.dreamer.letterservice.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreamer.letterservice.entity.dto.FutureLetterDto;
import com.dreamer.letterservice.entity.pojo.FutureLetter;
import org.springframework.web.multipart.MultipartFile;

public interface ILetterService extends IService<FutureLetter> {

    SaResult queryOpenLettersByUserId(String userId);

    SaResult addLetter(FutureLetterDto futureLetterDto);

    SaResult uploadImages(MultipartFile img);

    SaResult removeLetter(Long letterId);

    SaResult unopenedLetterIsExist();

    SaResult queryLetterToBeOpened();

    SaResult listOpenedLetters(Integer page, Integer size);
}
