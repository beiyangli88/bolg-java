package com.minzheng.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微博用户信息
 *
 * @author xiaoli
 * @date 2021/06/14
 * @since 1.0.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeiboUserInfoDTO {

    /**
     * 昵称
     */
    private String screen_name;

    /**
     * 头像
     */
    private String avatar_hd;

}
