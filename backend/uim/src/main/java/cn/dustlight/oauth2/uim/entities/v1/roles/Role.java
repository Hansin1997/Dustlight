package cn.dustlight.oauth2.uim.entities.v1.roles;

import java.io.Serializable;
import java.util.Date;

/**
 * 角色信息
 */
public interface Role extends Serializable {

    /**
     * 获取角色id
     *
     * @return 角色id
     */
    Long getRoleId();

    /**
     * 获取角色名
     *
     * @return 角色名
     */
    String getRoleName();

    /**
     * 获取角色描述
     *
     * @return 角色描述
     */
    String getRoleDescription();

    /**
     * 获取创建时间
     *
     * @return
     */
    Date getCreatedAt();

    /**
     * 获取更新时间
     *
     * @return
     */
    Date getUpdatedAt();
}
