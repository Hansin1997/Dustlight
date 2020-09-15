package cn.dustlight.oauth2.uim.services;

import cn.dustlight.oauth2.uim.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class OAuthUserDetailService implements UserDetailsService {

    @Autowired
    private UserDetailsMapper userDetailsMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userDetailsMapper.loadUserOAuth(username);
        if(u == null)
            throw new UsernameNotFoundException("Username or email: '" + username + "' not found!");
        return u;
    }
}
