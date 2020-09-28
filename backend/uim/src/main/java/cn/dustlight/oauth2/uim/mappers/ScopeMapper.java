package cn.dustlight.oauth2.uim.mappers;

import cn.dustlight.oauth2.uim.entities.v1.scopes.DefaultClientScope;
import cn.dustlight.oauth2.uim.entities.v1.types.DefaultGrantType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Mapper
public interface ScopeMapper {

    @Select("SELECT scopes.* FROM scopes,client_scope WHERE client_scope.cid=#{cid} AND client_scope.sid=scopes.sid")
    Collection<DefaultClientScope> getClientScopes(String cid);
}
