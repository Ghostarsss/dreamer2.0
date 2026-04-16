package com.dreamer.userservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.userservice.service.IFollowingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follows")
public class FollowingController {

    @Autowired
    private IFollowingService followingService;

    /**
     * 关注用户
     * @param userId
     * @return
     */
    @PostMapping("/me/following/{userId}")
    public SaResult followByUserId(@PathVariable String userId) {
        return followingService.followByUserId(userId);
    }

    /**
     * 取消关注用户
     * @param userId
     * @return
     */
    @DeleteMapping("/me/following/{userId}")
    public SaResult unFollowByUserId(@PathVariable String userId) {
        return followingService.unFollowByUserId(userId);
    }

    /**
     * 滚动查询我的关注
     * @param cursor
     * @param offset
     * @return
     */
    @GetMapping("/me/following")
    public SaResult listMeFollowing(@RequestParam(required = false) Long cursor,
                                 @RequestParam(required = false) Integer offset) {

        return followingService.listMeFollowing(cursor, offset);

    }

    /**
     * 滚动查询我的粉丝
     * @param cursor
     * @param offset
     * @return
     */
    @GetMapping("/me/fans")
    public SaResult listMeFans(@RequestParam(required = false) Long cursor,
                               @RequestParam(required = false) Integer offset) {

        return followingService.listMeFans(cursor, offset);
    }

    //TODO 指定用户滚动查询关注
    @GetMapping("/{userId}/following")
    public SaResult listFollowingByUserId() {
        return null;
    }

    //TODO 指定用户滚动查询粉丝
    @GetMapping("/{userId}/fans")
    public SaResult listFansByUserId() {
        return null;
    }

    /**
     * 移除粉丝
     * @param userId
     * @return
     */
    @DeleteMapping("/me/fans/{userId}")
    public SaResult removeFansByUserId(@PathVariable Long userId) {
        return followingService.removeFansByUserId(userId);
    }

}
