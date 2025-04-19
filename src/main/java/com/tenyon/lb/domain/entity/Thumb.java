package com.tenyon.lb.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@TableName(value = "thumb")
@Data
public class Thumb implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 博客Id
     */
    private Long blogId;

    /**
     * 创建时间
     */
    private Date createTime;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}