package cn.dustlight.oauth2.uim.controllers.users;

import cn.dustlight.oauth2.uim.Constants;
import cn.dustlight.oauth2.uim.entities.results.QueryResults;
import cn.dustlight.oauth2.uim.entities.v1.users.UimUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RequestMapping(value = Constants.V1.API_ROOT,
        produces = Constants.ContentType.APPLICATION_JSON)
@Tag(name = "用户拓展业务",
        description = "负责用户搜索、信息修改、密码更改、头像更新等业务。")
public interface UserExtendController {

    @Operation(summary = "查找用户")
    @GetMapping("users")
    QueryResults<? extends UimUser, ? extends Number> getUsers(@RequestParam(required = false, value = "q") String query,
                                                               @RequestParam Integer offset, @RequestParam Integer limit,
                                                               @RequestParam(required = false) Collection<String> order);

    /**
     * 更新用户密码
     *
     * @param uid      用户id
     * @param password 新密码
     */
    @Operation(summary = "更新用户密码")
    @PutMapping("user/{uid}/password")
    @PreAuthorize("#user.matchUid(#uid) and hasAnyAuthority('WRITE_USER') or hasAuthority('WRITE_USER_ANY')")
    void updatePassword(@PathVariable Long uid, @RequestParam String password);

    /**
     * 更新用户邮箱
     *
     * @param uid   用户id
     * @param email 新邮箱
     */
    @Operation(summary = "更新用户邮箱")
    @PutMapping("user/{uid}/email")
    @PreAuthorize("#user.matchUid(#uid) and hasAnyAuthority('WRITE_USER') or hasAuthority('WRITE_USER_ANY')")
    void updateEmail(@PathVariable Long uid, @RequestParam String email);

    /**
     * 更新用户性别
     *
     * @param uid    用户id
     * @param gender 性别
     */
    @Operation(summary = "更新用户性别")
    @PutMapping("user/{uid}/gender")
    @PreAuthorize("#user.matchUid(#uid) and hasAnyAuthority('WRITE_USER') or hasAuthority('WRITE_USER_ANY')")
    void updateGender(@PathVariable Long uid, @RequestParam int gender);

    /**
     * 更新用户头像
     *
     * @param uid 用户id
     */
    @Operation(summary = "更新用户头像",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "image/*",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ))
    @PutMapping(value = "user/{uid}/avatar", consumes = "image/*")
    @PreAuthorize("#user.matchUid(#uid) and hasAnyAuthority('WRITE_USER') or hasAuthority('WRITE_USER_ANY')")
    void updateAvatar(@PathVariable Long uid);

    /**
     * 获取用户头像
     *
     * @param uid  用户id
     * @param size 图片尺寸
     * @param t    时间戳
     */
    @Operation(summary = "获取用户头像")
    @GetMapping("user/{uid}/avatar")
    void getAvatar(@PathVariable Long uid, @RequestParam(required = false) Integer size, @RequestParam(required = false) Long t);

}
