package cn.dustlight.oauth2.uim.controllers;

import cn.dustlight.generator.snowflake.SnowflakeIdGenerator;
import cn.dustlight.oauth2.uim.configurations.UimProperties;
import cn.dustlight.oauth2.uim.entities.v1.users.UimUser;
import cn.dustlight.oauth2.uim.handlers.code.VerificationCodeGenerator;
import cn.dustlight.oauth2.uim.entities.*;
import cn.dustlight.oauth2.uim.services.*;
import cn.dustlight.oauth2.uim.RestfulConstants;
import cn.dustlight.oauth2.uim.RestfulResult;
import cn.dustlight.storage.core.Permission;
import cn.dustlight.storage.tencent.cos.TencentCloudObjectStorage;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

@RestController
public class ClientController implements IClientController {

    @Autowired
    private ClientDetailsMapper clientMapper;

    @Autowired
    private ScopeDetailsMapper scopeDetailsMapper;

    @Autowired
    private AuthorityDetailsMapper authorityDetailsMapper;

    @Autowired
    private RoleDetailsMapper roleDetailsMapper;

    @Autowired
    private GrantTypeMapper grantTypeMapper;

    @Autowired
    private SnowflakeIdGenerator snowflake;

    @Autowired
    private VerificationCodeGenerator verificationCodeGenerator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TencentCloudObjectStorage storage;

    @Autowired
    private UimProperties properties;

    private final static Base32 base32 = new Base32();

    protected String sha1(String str) {
        return DigestUtils.sha1Hex(str);
    }

    @Transactional
    @Override
    public RestfulResult createClient(String name, String redirectUri, String description, List<Long> scopes, List<Long> grantTypes, Authentication authentication) {
        UimUser userDetails = (UimUser) authentication.getPrincipal();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Long id = snowflake.generate();
        try (ObjectOutputStream os = new ObjectOutputStream(out)) {
            os.writeLong(id);
            os.writeByte(0);
        } catch (IOException e) {
            return RestfulResult.error(e.getMessage());
        }
        String appKey = base32.encodeToString(out.toByteArray());//Base64.getEncoder().encodeToString(out.toByteArray());
        String appSecret = sha1(authentication.getName() + id + verificationCodeGenerator.generatorCode(128));
        boolean flag = clientMapper.insertClient(appKey, userDetails.getUid(), passwordEncoder.encode(appSecret), name, redirectUri,
                null, null, null, true, description);
        flag = flag & clientMapper.insertClientScopes(appKey, scopes);
        flag = flag & clientMapper.insertClientGrantTypes(appKey, grantTypes);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("appKey", appKey);
        data.put("appSecret", appSecret);
        return flag ? RestfulResult.success(data) : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult deleteClient(String appKey, Authentication authentication) {
        boolean flag = AuthorityUtils.authorityListToSet(authentication.getAuthorities()).contains("DELETE_CLIENT_ANY") ?
                clientMapper.deleteClient(appKey) :
                clientMapper.deleteClientWithUid(appKey, ((UimUser) authentication.getPrincipal()).getUid());
        return flag ? RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult<String> resetClientSecret(String appKey, Authentication authentication) {
        Long id = snowflake.generate();
        String appSecret = sha1(authentication.getName() + id + verificationCodeGenerator.generatorCode(128));
        boolean flag = AuthorityUtils.authorityListToSet(authentication.getAuthorities()).contains("DELETE_CLIENT_ANY") ?
                clientMapper.updateClientSecret(appKey, passwordEncoder.encode(appSecret)) :
                clientMapper.updateClientSecretWithUid(appKey, passwordEncoder.encode(appSecret), ((UimUser) authentication.getPrincipal()).getUid());
        return flag ? RestfulResult.success(appSecret) : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult updateClientName(String appKey, String name, Authentication authentication) {
        boolean flag = AuthorityUtils.authorityListToSet(authentication.getAuthorities()).contains("UPDATE_CLIENT_ANY") ?
                clientMapper.updateClientName(appKey, name) :
                clientMapper.updateClientNameWithUid(appKey, name, ((UimUser) authentication.getPrincipal()).getUid());
        return flag ? RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult updateClientDescription(String appKey, String description, Authentication authentication) {
        boolean flag = AuthorityUtils.authorityListToSet(authentication.getAuthorities()).contains("UPDATE_CLIENT_ANY") ?
                clientMapper.updateClientDescription(appKey, description) :
                clientMapper.updateClientDescriptionWithUid(appKey, description, ((UimUser) authentication.getPrincipal()).getUid());
        return flag ? RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult updateClientRedirectUri(String appKey, String redirectUri, Authentication authentication) {
        boolean flag = AuthorityUtils.authorityListToSet(authentication.getAuthorities()).contains("UPDATE_CLIENT_ANY") ?
                clientMapper.updateClientRedirectUri(appKey, redirectUri) :
                clientMapper.updateClientRedirectUriWithUid(appKey, redirectUri, ((UimUser) authentication.getPrincipal()).getUid());
        return flag ? RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult uploadClientImage(String appKey, Authentication authentication) throws IOException {
        boolean flag = AuthorityUtils.authorityListToSet(authentication.getAuthorities()).contains("UPDATE_CLIENT_ANY")
                || clientMapper.isClientOwner(appKey, ((UimUser) authentication.getPrincipal()).getUid());
        if (!flag)
            throw new AccessDeniedException("access denied.");
        String url = storage.generatePutUrl(properties.getStorage().getStoragePath() + "app/img/logo/" + appKey,
                Permission.READABLE, properties.getStorage().getDefaultExpiration());
        return RestfulResult.success(url);
    }

    @Override
    public void getClientImage(String appKey, Integer size, Long t, HttpServletResponse response, HttpServletRequest request) throws IOException {
        String key = properties.getStorage().getStoragePath() + "app/img/logo/" + appKey;
        if (!storage.isExist(key)) {
            response.sendError(404); // 头像不存在
            return;
        }
        String urlString = properties.getStorage().getStorageBaseUrl() == null ?
                storage.generateGetUrl(key, 1000L * 60L * 60 * 24L) + "&" :
                properties.getStorage().getStorageBaseUrl() + key + "?";
        if (size != null)
            urlString += "imageMogr2/thumbnail/" + size + "x" + size;
        if (t != null)
            urlString += "&t=" + System.currentTimeMillis();
        response.sendRedirect(urlString);
    }

    @Override
    public RestfulResult addClientScopes(String appKey, List<Long> scopes, Authentication authentication) {
        boolean flag = AuthorityUtils.authorityListToSet(authentication.getAuthorities()).contains("UPDATE_CLIENT_ANY");
        if (!flag) {
            ClientDetails client = clientMapper.loadClientByClientId(appKey);
            if (client == null || client.getUid() != ((UimUser) authentication.getPrincipal()).getUid())
                return RestfulConstants.ERROR_UNKNOWN;
        }
        flag = clientMapper.insertClientScopes(appKey, scopes);
        return flag ? RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult removeClientScopes(String appKey, List<Long> scopes, Authentication authentication) {
        boolean flag = AuthorityUtils.authorityListToSet(authentication.getAuthorities()).contains("UPDATE_CLIENT_ANY");
        if (!flag) {
            ClientDetails client = clientMapper.loadClientByClientId(appKey);
            if (client == null || client.getUid() != ((UimUser) authentication.getPrincipal()).getUid())
                return RestfulConstants.ERROR_UNKNOWN;
        }
        flag = clientMapper.deleteClientScopes(appKey, scopes);
        return flag ? RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult addClientGrantTypes(String appKey, List<Long> types, Authentication authentication) {
        boolean flag = AuthorityUtils.authorityListToSet(authentication.getAuthorities()).contains("UPDATE_CLIENT_ANY");
        if (!flag) {
            ClientDetails client = clientMapper.loadClientByClientId(appKey);
            if (client == null || client.getUid() != ((UimUser) authentication.getPrincipal()).getUid())
                return RestfulConstants.ERROR_UNKNOWN;
        }
        flag = clientMapper.insertClientGrantTypes(appKey, types);
        return flag ? RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult removeClientGrantTypes(String appKey, List<Long> types, Authentication authentication) {
        boolean flag = AuthorityUtils.authorityListToSet(authentication.getAuthorities()).contains("UPDATE_CLIENT_ANY");
        if (!flag) {
            ClientDetails client = clientMapper.loadClientByClientId(appKey);
            if (client == null || client.getUid() != ((UimUser) authentication.getPrincipal()).getUid())
                return RestfulConstants.ERROR_UNKNOWN;
        }
        flag = clientMapper.deleteClientGrantTypes(appKey, types);
        return flag ? RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult insertScopeDetail(String name, String description) {
        return scopeDetailsMapper.insertScope(snowflake.generate(), name, description) ?
                RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult deleteScopeDetail(Long id) {
        return scopeDetailsMapper.removeScope(id) ?
                RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult updateScopeDetail(Long id, String name, String description) {
        return scopeDetailsMapper.updateScope(id, name, description) ?
                RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult removeScopeAuthority(Long scopeId, Long authorityId) {
        return authorityDetailsMapper.removeScopeAuthority(scopeId, authorityId) ?
                RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult insertScopeAuthority(Long scopeId, Long authorityId) {
        return authorityDetailsMapper.insertScopeAuthority(scopeId, authorityId) ?
                RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult insertRole(String name, String description) {
        return roleDetailsMapper.insertRoleDetails(snowflake.generate(), name, description) ? RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult updateRole(Long id, String name, String description) {
        return roleDetailsMapper.updateRoleDetails(id, name, description) ? RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult deleteRole(Long id) {
        return roleDetailsMapper.deleteRoleDetails(id) ? RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult removeRoleAuthority(Long roleId, Long authorityId) {
        return authorityDetailsMapper.removeRoleAuthority(roleId, authorityId) ?
                RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult insertRoleAuthority(Long roleId, Long authorityId) {
        return authorityDetailsMapper.insertRoleAuthority(roleId, authorityId) ?
                RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult<List<RoleDetails>> getRoles() {
        return RestfulResult.success(roleDetailsMapper.getAllRoleDetails());
    }

    @Override
    public RestfulResult updateAuthority(Long id, String name, String description) {
        return authorityDetailsMapper.updateAuthority(id, name, description) ? RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult deleteAuthority(Long id) {
        return authorityDetailsMapper.deleteAuthority(id) ? RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult insertRoleAuthority(String name, String description) {
        return authorityDetailsMapper.insertAuthority(snowflake.generate(), name, description) ? RestfulConstants.SUCCESS : RestfulConstants.ERROR_UNKNOWN;
    }

    @Override
    public RestfulResult<List<ScopeDetails>> getScopeDetails() {
        return RestfulResult.success(scopeDetailsMapper.getScopes());
    }

    @Override
    public RestfulResult<List<AuthorityDetails>> getAuthorityDetails() {
        return RestfulResult.success(authorityDetailsMapper.getAuthorities());
    }

    @Override
    public RestfulResult<List<GrantType>> getGrantTypes() {
        return RestfulResult.success(grantTypeMapper.getGrantTypes());
    }

    @Override
    public RestfulResult<List<ClientDetails>> getCurrentUserClientDetails(Authentication authentication) {
        UimUser userDetails = (UimUser) authentication.getPrincipal();
        return RestfulResult.success(clientMapper.loadClientsByUserId(userDetails.getUid()));
    }

    @Override
    public RestfulResult<List<ClientDetails>> getUserClientDetails(Long userId) {
        return RestfulResult.success(clientMapper.loadClientsByUserId(userId));
    }

    @Override
    public RestfulResult<List<AuthorityDetails>> getRoleAuthorities(Long roleId) {
        return RestfulResult.success(authorityDetailsMapper.getRoleAuthorities(roleId));
    }

    @Override
    public RestfulResult<List<AuthorityDetails>> getScopeAuthorities(Long scopeId) {
        return RestfulResult.success(authorityDetailsMapper.getScopeAuthorities(scopeId));
    }
}
