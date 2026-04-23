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

    /**
     * 查询指定用户公开信件
     * @param userId
     * @return
     */
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
        return letterService.uploadImages(img);
    }

    /**
     * 删除已开启的信件
     * @param letterId
     * @return
     */
    @DeleteMapping("/{letterId}")
    public SaResult removeLetter(@PathVariable Long letterId) {
        return letterService.removeLetter(letterId);
    }

    /**
     * 查询是否存在未开启的信件
     * @return
     */
    @GetMapping("/exist-unopened")
    public SaResult UnopenedLetterIsExist() {
        return letterService.unopenedLetterIsExist();
    }

    /**
     * 查看待开启信件内容
     * @return
     */
    @GetMapping("/to-be-opened")
    public SaResult queryLetterToBeOpened() {
        return letterService.queryLetterToBeOpened();
    }

    /**
     * 查询已开启信件
     * @return
     */
    @GetMapping("/opened")
    public SaResult listOpenedLetters(Integer page,Integer size) {
        return letterService.listOpenedLetters(page,size);
    }
}
