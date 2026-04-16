package com.dreamer.letterservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.letterservice.entity.dto.FutureLetterDto;
import com.dreamer.letterservice.service.ILetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/letters")
@RequiredArgsConstructor
public class LetterController {

    private final ILetterService letterService;

    @GetMapping("/queryOpenLettersByUserId")
    public SaResult queryOpenLettersByUserId(String userId) {

        return letterService.queryOpenLettersByUserId(userId);
    }

    /**
     * 发布未来信封
     * @param futureLetterDto
     * @return
     */
    @PostMapping
    public SaResult addLetter(@RequestBody FutureLetterDto futureLetterDto) {
        return letterService.addLetter(futureLetterDto);
    }

    /**
     * 上传图片
     * @param img
     * @return
     */
    @PostMapping("/images")
    public SaResult uploadImages(@RequestParam("img") MultipartFile img) {
        return null;
    }
}
