package com.dreamer.userservice.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.dreamer.userservice.service.IVPService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vp")
@RequiredArgsConstructor
public class VPController {

    private final IVPService VPService;

    /**
     * 签到
     * @return
     */
    @PostMapping("/sign")
    public SaResult sign() {
        return VPService.sign();
    }

    /**
     * 判断是否已签到
     * @return
     */
    @GetMapping("/sign/status")
    public SaResult isSign() {
        return VPService.isSign();
    }

    /**
     * 质子换购用户经验
     * @param proton
     * @return
     */
    @PutMapping("/buy-exp")
    public SaResult buyEXPByProton(Integer proton) {
        return VPService.buyEXPByProton(proton);
    }

    /**
     * 消耗质子，信件可以上传图片
     * @return
     */
    @PutMapping("/letter-upload-images")
    public SaResult uploadImagesWithProtonCost(long userId) {
        return VPService.uploadImagesWithProtonCost(userId);
    }

    /**
     * 扣减质子数
     * @param userId
     * @param protons
     * @return
     */
    @PutMapping("/deduct-proton/{userId}")
    public SaResult deductProton(@PathVariable Long userId, @RequestParam("protons") Integer protons) {
        return VPService.deductProton(userId, protons);
    }
}
