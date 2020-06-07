package com.lyg.hero.entity;

import lombok.*;

/**
 * @author lyg
 * @create 2020-06-06-16:18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class User {
    /**
     * 用户 Id这是一个demo
     */
    public int userId;

    /**
     * 英雄形象,再来一个demo,现在我要删除分支了
     *
     */
    public String heroAvatar;

}
