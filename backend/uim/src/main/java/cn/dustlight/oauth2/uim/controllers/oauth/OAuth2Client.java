package cn.dustlight.oauth2.uim.controllers.oauth;

import cn.dustlight.oauth2.uim.entities.v1.clients.DefaultClient;
import cn.dustlight.oauth2.uim.entities.v1.scopes.ClientScope;
import cn.dustlight.oauth2.uim.entities.v1.users.PublicUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class OAuth2Client extends DefaultClient {

    private String redirect;
    private boolean approved;
    private PublicUser user;

    @JsonIgnore
    @Override
    public Long getUid() {
        return super.getUid();
    }

    @JsonIgnore
    @Override
    public Set<String> getAuthorizedGrantTypes() {
        return super.getAuthorizedGrantTypes();
    }

    @JsonIgnore
    @Override
    public Set<String> getResourceIds() {
        return super.getResourceIds();
    }

    @JsonIgnore
    @Override
    public Set<String> getRegisteredRedirectUri() {
        return super.getRegisteredRedirectUri();
    }

    @JsonIgnore
    @Override
    public String getClientSecret() {
        return super.getClientSecret();
    }

    @JsonIgnore
    @Override
    public Integer getAccessTokenValiditySeconds() {
        return super.getAccessTokenValiditySeconds();
    }

    @JsonIgnore
    @Override
    public Integer getRefreshTokenValiditySeconds() {
        return super.getRefreshTokenValiditySeconds();
    }

    @JsonIgnore
    @Override
    public Map<String, Object> getAdditionalInformation() {
        return super.getAdditionalInformation();
    }

    @JsonIgnore
    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return super.getAuthorities();
    }

    public PublicUser getUser() {
        return user;
    }

    public void setUser(PublicUser user) {
        this.user = user;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public String getRedirect() {
        return redirect;
    }

    @ArraySchema(schema = @Schema(implementation = OAuth2ClientScope.class))
    @Override
    public Collection<? extends ClientScope> getScopes() {
        return super.getScopes();
    }
}
