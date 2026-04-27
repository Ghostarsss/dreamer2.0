package com.dreamer.adminservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "letter-service")
public interface LetterFeignClient {

    @GetMapping("/letters/admin/count")
    Long countLetters();
}
