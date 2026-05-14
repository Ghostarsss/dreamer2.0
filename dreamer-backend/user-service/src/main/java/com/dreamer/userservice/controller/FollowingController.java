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
     *
     * @param userId
     * @return
     */
    @PostMapping("/me/following/{userId}")
    public SaResult followByUserId(@PathVariable String userId) {
        return followingService.followByUserId(userId);
    }

    /**
     * 取消关注用户
     *
     * @param userId
     * @return
     */
    @DeleteMapping("/me/following/{userId}")
    public SaResult unFollowByUserId(@PathVariable String userId) {
        return followingService.unFollowByUserId(userId);
    }

    /**
     * 滚动查询我的关注
     *
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
     *
     * @param cursor
     * @param offset
     * @return
     */
    @GetMapping("/me/fans")
    public SaResult listMeFans(@RequestParam(required = false) Long cursor,
                               @RequestParam(required = false) Integer offset) {

        return followingService.listMeFans(cursor, offset);
    }

    /**
     * 指定用户滚动查询关注
     *
     * @return
     */
    @GetMapping("/{userId}/following")
    public SaResult listFollowingByUserId(@PathVariable Long userId, @RequestParam(required = false) Long cursor
            , @RequestParam(required = false) Integer offset) {
        return followingService.listFollowingByUserId(userId, cursor, offset);
    }

    /**
     * 指定用户滚动查询粉丝
     *
     * @return
     */
    @GetMapping("/{userId}/fans")
    public SaResult listFansByUserId(@PathVariable Long userId, @RequestParam(required = false) Long cursor
            , @RequestParam(required = false) Integer offset) {
        return followingService.listFansByUserId(userId, cursor, offset);
    }

    /**
     * 移除粉丝
     *
     * @param userId
     * @return
     */
    @DeleteMapping("/me/fans/{userId}")
    public SaResult removeFansByUserId(@PathVariable Long userId) {
        return followingService.removeFansByUserId(userId);
    }

    /**
     * 查询当前用户是否关注指定用户
     *
     * @param userId
     * @return
     */
    @GetMapping("/is-followed/{userId}")
    public SaResult checkFollowStatus(@PathVariable Long userId) {
        return followingService.checkFollowStatus(userId);
    }
}
